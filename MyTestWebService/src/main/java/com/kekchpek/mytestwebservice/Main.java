package com.kekchpek.mytestwebservice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class Main {

    private static final int SERVICE_PORT = 12345;

    public static void main(String[] args) {
        ServerSocketChannel channel;
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(SERVICE_PORT));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        GetPostService service = GetPostService.createNew(channel);
        try {
            service.startup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
