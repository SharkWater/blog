package com.shark.react.singlethread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SendProcessor extends BaseProcessor {

    private static int MAXOUT = 10000;
    private ByteBuffer outputBuffer = ByteBuffer.allocate(MAXOUT);

    private String response;

    public SendProcessor(SelectableChannel channel, Selector selector) {
        super(channel, selector);
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public void doProcess() {
        SocketChannel channel = (SocketChannel)this.channel;
        outputBuffer.clear();
        outputBuffer.put(ByteBuffer.wrap(response.getBytes()));
        outputBuffer.flip();
        try {
            while(outputBuffer.hasRemaining()) {
                channel.write(outputBuffer);
            }
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
