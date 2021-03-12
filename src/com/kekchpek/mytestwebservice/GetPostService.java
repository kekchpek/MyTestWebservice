package com.kekchpek.mytestwebservice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetPostService {

    private static final String GET_COMMAND = "get";
    private static final String POST_COMMAND = "post";
    private static final String SUCCESS_RESULT = "POST SUCCESS";

    private static final String ERROR_POST_NO_ARG = "POST FAILED. Value required";
    private static final String ERROR_EMPTY_MSG = "COMMAND FAILED. Empty message";
    private static final String ERROR_READ_MSG = "COMMAND FAILED. Fail to read message";

    private String storedValue = "--";
    private final ByteBuffer channelBuffer = ByteBuffer.allocate(256);;

    public static GetPostService createNew() {
        return new GetPostService();
    }

    private GetPostService() { }

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public void startup(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(this::mainLoop);
    }

    private void mainLoop() {
        while (true) {
            Set<SelectionKey> selectionKeys = selectKeys();
            for (SelectionKey key :
                    selectionKeys) {
                if (key.isAcceptable()) {
                    assert serverSocketChannel != null;
                    try {
                        serverSocketChannel.register(selector, SelectionKey.OP_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                if (key.isReadable()) {
                    String message = readFromChannel(key.channel());
                    if (message == null) {
                        writeToChannel(key.channel(), ERROR_READ_MSG);
                        return;
                    }
                    String[] messageParts = message.split(" ");
                    if (messageParts.length == 0) {
                        if (!writeToChannel(key.channel(), ERROR_EMPTY_MSG))
                            return;
                    }
                    if (messageParts[0].equals(GET_COMMAND)) {
                        if (!writeToChannel(key.channel(), storedValue))
                            return;
                    }

                    if (messageParts[0].equals(POST_COMMAND)) {
                        if (messageParts.length == 1) {
                            if (!writeToChannel(key.channel(), ERROR_POST_NO_ARG))
                                return;
                        }
                        storedValue = messageParts[1];
                        if (!writeToChannel(key.channel(), SUCCESS_RESULT))
                            return;
                    }
                }
            }
        }
    }

    private Set<SelectionKey> selectKeys() {
        if (selector == null)
            return new HashSet<>();

        try {
            selector.select();
            return selector.selectedKeys();
        } catch (IOException e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    private boolean writeToChannel(Channel c, String message) {
        if (c instanceof WritableByteChannel) {
            try {
                WritableByteChannel channel = (WritableByteChannel) c;
                channelBuffer.put(storedValue.getBytes());
                channel.write(channelBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                channelBuffer.flip();
                channelBuffer.clear();
            }
        }
        return true;
    }

    private String readFromChannel(Channel c) {
        if (c instanceof ReadableByteChannel) {
            String message = null;
            try {
                ReadableByteChannel channel = (ReadableByteChannel) c;
                channel.read(channelBuffer);
                return new String(channelBuffer.array()).trim();
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                channelBuffer.flip();
                channelBuffer.clear();
            }
        }
        return null;
    }

}
