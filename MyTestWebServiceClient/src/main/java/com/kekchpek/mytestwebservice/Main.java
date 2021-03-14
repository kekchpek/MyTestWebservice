package com.kekchpek.mytestwebservice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Main {

    public static void main(String[] args) {
        if (args == null || args.length == 1) {
            System.out.println("Server address should be specified");
            return;
        }
        String[] addressParts = args[0].split(":");
        if (addressParts.length != 2) {
            System.out.println("Server address should be specified in format \"host:port\"");
            return;
        }
        SocketChannel channel;
        InetSocketAddress address = new InetSocketAddress(addressParts[0], Integer.parseInt(addressParts[1]));
        try {
            channel = SocketChannel.open();
        } catch (Exception e) {
            System.out.printf("Fail to open channel%n");
            e.printStackTrace();
            return;
        }
        try {
            channel.connect(address);
        } catch (IOException e) {
            System.out.printf("Fail to connect to the server on %s%n", args[0]);
            e.printStackTrace();
        }
        MyTestWebServiceClient client = MyTestWebServiceClient.createNew(channel);
    }

}
