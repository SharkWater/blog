package com.shark.react.multithread.processor;

import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

public abstract class BaseProcessor {
    protected SelectableChannel channel;
    protected Selector selector;

    public BaseProcessor(SelectableChannel channel, Selector selector) {
        this.channel = channel;
        this.selector = selector;
    }

    public abstract void doProcess();
}
