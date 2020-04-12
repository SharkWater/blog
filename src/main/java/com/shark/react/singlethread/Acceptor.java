package com.shark.react.singlethread;

import java.io.IOException;
import java.nio.channels.*;

/**
 * 连接处理器
 */
class Acceptor extends BaseProcessor {
    public Acceptor(SelectableChannel channel, Selector selector) {
        super(channel, selector);
    }

    public void doProcess() {
        try {
            SocketChannel socketChannel = ((ServerSocketChannel)this.channel).accept();
            if (socketChannel != null) {
                BaseProcessor processor = new ReadProcessor(socketChannel, selector);

                socketChannel.configureBlocking(false);
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
                key.attach(processor);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

