package com.shark.react.multireactor;

import com.shark.react.multireactor.processor.Acceptor;
import com.shark.react.multireactor.processor.BaseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Reactor基类，实现侦听和分发逻辑
 */
public abstract class BaseReactor implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(BaseReactor.class);

    protected Selector selector;
    protected boolean needStop;

    public abstract void init() throws IOException;

    public abstract void stop();

    /**
     * Reactor主逻辑，监听分发任务
     */
    public void run() {
        try {
            while (!needStop) {
                //MainReactor(Accept)，SubReactor(Read,Write)和Process线程和Ractor线程之间是异步的，所以采用非阻塞侦听避免register和select相互阻塞
                if (selector.select(100) > 0) {
                    Iterator it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        dispatch((SelectionKey) it.next());
                        it.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分发事件
     *
     * @param selectionKey
     */
    protected void dispatch(SelectionKey selectionKey) {
        BaseProcessor processor = (BaseProcessor) (selectionKey.attachment());
        if (processor != null)
            processor.doProcess();
    }

    public Selector getSelector() {
        return this.selector;
    }
}
