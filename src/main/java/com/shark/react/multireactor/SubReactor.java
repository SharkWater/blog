package com.shark.react.multireactor;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * SubReactor主要逻辑实现在基类中
 */
public class SubReactor extends BaseReactor {
    @Override
    public void init() throws IOException {
        selector = Selector.open();
    }

    @Override
    public void stop() {
        this.needStop = true;
    }
}
