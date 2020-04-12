package com.shark.react.multithread.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class BussinessProcessor extends BaseProcessor {

    private static Logger logger = LoggerFactory.getLogger(BussinessProcessor.class);

    private String request;

    public BussinessProcessor(SelectableChannel channel, Selector selector) {
        super(channel, selector);
    }

    @Override
    public void doProcess() {
        WorkerThreadPool.submitTask(() -> {
            logger.info("Request[{}] has received, start to process.", request);
            String response = "Request process success.";
            logger.info("Response[{}] will send to client.", response);
            try {
                SendProcessor sendProcessor = new SendProcessor(channel, this.selector);;
                sendProcessor.setResponse(response);
                SelectionKey key = channel.register(this.selector, SelectionKey.OP_WRITE);
                key.attach(sendProcessor);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        });
    }

    public BussinessProcessor setRequest(String request) {
        this.request = request;
        return this;
    }
}
