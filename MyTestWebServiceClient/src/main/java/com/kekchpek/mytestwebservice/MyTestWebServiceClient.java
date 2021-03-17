package com.kekchpek.mytestwebservice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class MyTestWebServiceClient {

    public interface AnswerListener {
        void run(String answer);
    }

    private static final String GET_COMMAND = "get";
    private static final String POST_COMMAND = "post";

    private final SelectableChannel channel;
    private final ByteBuffer channelBuffer;
    private Selector selector;

    private final Object answerListenerLock = new Object();
    private AnswerListener answerListener;

    public static MyTestWebServiceClient createNew(SelectableChannel channel) {
        return new MyTestWebServiceClient(channel);
    }

    private MyTestWebServiceClient(SelectableChannel channel) {
        this.channel = channel;
        channelBuffer = ByteBuffer.allocate(256);
    }

    public void startup() throws IOException {
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(this::mainLoop);
    }

    public void post(String value, AnswerListener listener) {
        if (!setAnswerListener(listener)) {
            listener.run("Client does not support several commands executing");
            return;
        }
        writeToChannel(channel, String.format("%s %s", POST_COMMAND, value));
    }

    public void get(AnswerListener listener) {
        if (!setAnswerListener(listener)) {
            listener.run("Client does not support several commands executing");
            return;
        }
        writeToChannel(channel, "get");
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

    private boolean setAnswerListener(AnswerListener listener) {
        synchronized (answerListenerLock) {
            if (answerListener != null)
                return false;
            answerListener = listener;
            return true;
        }
    }

    private void gotMessage(String message) {
        synchronized (answerListenerLock) {
            if (answerListener != null)
                answerListener.run(message);
            answerListener = null;
        }
    }

    private void mainLoop() {
        while (true) {
            Set<SelectionKey> selectionKeys = selectKeys();
            for (SelectionKey key :
                    selectionKeys) {
                if (key.isReadable()) {
                    String message = readFromChannel(key.channel());
                    gotMessage(message);
                }
                if (key.isConnectable()) {
                    try {
                        channel.register(selector, SelectionKey.OP_READ);
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean writeToChannel(Channel c, String message) {
        if (c instanceof WritableByteChannel) {
            try {
                WritableByteChannel channel = (WritableByteChannel) c;
                channelBuffer.put(message.getBytes());
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
