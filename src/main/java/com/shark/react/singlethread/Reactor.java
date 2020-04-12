package com.shark.react.singlethread;

import com.shark.react.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 单线程单Reactor模型
 * Server->Accept->Read->Process->Write运行在同一个线程，串行执行
 */
public class Reactor implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Reactor.class);

    private int port;
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private boolean needStop;

    Reactor(int port) throws IOException {
        this.port = port;
        this.needStop = false;
    }

    public void init() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);

        selector = Selector.open();

        //注册Accept事件并设置事件响应Handler
        SelectionKey selectionKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor(serverSocket, selector));
    }

    /**
     * Rector主逻辑负责事件侦听与分发
     */
    public void run() {
        try {
            while (!needStop) {
                selector.select();
                Iterator it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    dispatch((SelectionKey) (it.next()));
                    it.remove();
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
    void dispatch(SelectionKey selectionKey) {
        BaseProcessor processor = (BaseProcessor) (selectionKey.attachment());
        if (processor != null)
            processor.doProcess();
    }

    private void stop() {
        this.needStop = true;
        try {
            this.serverSocket.close();
            this.selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        try {
            Reactor reactor = new Reactor(10096);
            reactor.init();
            threadPoolExecutor.submit(reactor);
            for (int i = 0; i < 10; i++) {
                int index = i;
                threadPoolExecutor.submit(() -> {
                    Client client = new Client("127.0.0.1", 10096);
                    try {
                        client.init();
                    } catch (IOException e) {
                        logger.error("Init client fail, error[{}].", e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                    client.send(String.format("Hello server. I am client[%d].", index));
                    logger.info("Client[{}] recv data[{}].", index, client.read());
                    client.close();
                });
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reactor.stop();
            threadPoolExecutor.shutdown();

            logger.info("Reactor exit success.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}