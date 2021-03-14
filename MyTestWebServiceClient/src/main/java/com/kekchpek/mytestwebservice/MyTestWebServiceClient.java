package com.kekchpek.mytestwebservice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;

public class MyTestWebServiceClient {

    private final SelectableChannel channel;
    private final ByteBuffer channelBuffer;

    public static MyTestWebServiceClient createNew(SelectableChannel channel) {
        return new MyTestWebServiceClient(channel);
    }

    private MyTestWebServiceClient(SelectableChannel channel) {
        this.channel = channel;
        channelBuffer = ByteBuffer.allocate(256);

    }

    public void post(String value) {

    }

    public String get(String value) {

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
