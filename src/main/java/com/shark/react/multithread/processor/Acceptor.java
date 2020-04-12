package com.shark.react.multithread.processor;

import java.io.IOException;
import java.nio.channels.*;

/**
 * 连接处理器
 */
public class Acceptor extends BaseProcessor {
    public Acceptor(SelectableChannel channel, Selector selector) {
        super(channel, selector);
    }

    public void doProcess() {
        try {
            SocketChannel socketChannel = ((ServerSocketChannel)this.channel).accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
                BaseProcessor processor = new ReadProcessor(socketChannel, selector);
                key.attach(processor);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

