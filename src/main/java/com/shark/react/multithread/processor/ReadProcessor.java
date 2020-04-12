package com.shark.react.multithread.processor;

import com.shark.react.multithread.processor.BaseProcessor;
import com.shark.react.multithread.processor.BussinessProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 处理逻辑
 */
public class ReadProcessor extends BaseProcessor {

    private static Charset charset = Charset.defaultCharset();
    private static int MAXIN = 10000;

    private ByteBuffer readBuffer = ByteBuffer.allocate(MAXIN);

    public ReadProcessor(SocketChannel channel, Selector selector) throws IOException {
        super(channel, selector);
    }

    public void doProcess() {
        StringBuffer stringBuffer = new StringBuffer();
        SocketChannel channel = (SocketChannel) this.channel;
        try {
            while (channel.read(readBuffer) > 0) {
                readBuffer.flip();
                stringBuffer.append(charset.decode(readBuffer).toString());
                readBuffer.clear();
            }
            new BussinessProcessor(this.channel, this.selector)
                    .setRequest(stringBuffer.toString())
                    .doProcess();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
