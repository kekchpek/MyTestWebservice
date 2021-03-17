package com.kekchpek.mytestwebservice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Server address should be specified as argument");
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
            channel.configureBlocking(false);
        } catch (IOException e) {
            System.out.printf("Fail to connect to the server on %s%n", args[0]);
            e.printStackTrace();
            return;
        }
        MyTestWebServiceClient client = MyTestWebServiceClient.createNew(channel);
        try {
            client.startup();
        } catch (IOException e) {
            System.out.println("Fail to start client");
            e.printStackTrace();
            return;
        }
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit"))
                return;
            String[] commandParts = command.split(" ");
            if (commandParts.length > 0) {
                if (commandParts[0].equals("get")) {
                    client.get(System.out::println);
                }
                if (commandParts[0].equals("post")) {
                    if (commandParts.length < 2) {
                        System.out.println("post command should has an argument");
                        return;
                    }
                    client.post(commandParts[1], System.out::println);
                }
            } else {
                System.out.println("Fail to execute command");
            }
        }
    }

}
