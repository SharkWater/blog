package com.shark.react.multireactor.processor;

import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

/**
 * 事件响应基类
 */
public abstract class BaseProcessor {
    protected SelectableChannel channel;
    protected Selector selector;

    public BaseProcessor(SelectableChannel channel, Selector selector) {
        this.channel = channel;
        this.selector = selector;
    }

    public BaseProcessor setSelector(Selector selector) {
        this.selector = selector;
        return this;
    }

    public abstract void doProcess();
}

