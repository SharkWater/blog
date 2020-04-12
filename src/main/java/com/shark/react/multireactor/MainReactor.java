package com.shark.react.multireactor;

import com.shark.react.Client;
import com.shark.react.multireactor.processor.Acceptor;
import com.shark.react.multireactor.processor.WorkerThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 多Reactor多Process线程
 * MainReactor->Accept->|SubReactor->Read|->|Process|->|SubReactor->Send|
 *                      |SubReactor->Read|->|Process|->|SubReactor->Send|
 *                      |SubReactor->Read|->|Process|->|SubReactor->Send|
 * 多SubReactor运行在独立线程中，处理Read和Send（同一Reactor线程执行）
 * Process运行在独立的线程池中
 *
 * singlethread, multithread,multireactor三种模式代码结构差不多，改动很小，可见React模式的扩展性比较强。
 * 可以整合成一种融合模式的框架，自己偷懒放了三个包，进行了代码拷贝。
 */
public class MainReactor extends BaseReactor {

    private static Logger logger = LoggerFactory.getLogger(MainReactor.class);

    //Reactor运行线程，包括Main和Sub
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    private int port;
    private ServerSocketChannel serverSocket;

    //轮转选择分配Read事件注册的SubReactor
    private int subIndex;
    //SubReactor总数
    private int subReactorCount;
    private ArrayList<SubReactor> subReactors;
    //Accept事件处理器，因为Accept是单线程运行在MainReactor中，所以只需要一个处理对象即可
    private Acceptor acceptor;

    MainReactor(int port, int subReactorCount) throws IOException {
        this.port = port;
        this.needStop = false;
        this.subReactorCount = subReactorCount;
        this.subIndex = 0;
    }

    public void init() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);

        selector = Selector.open();

        subReactors = new ArrayList<SubReactor>();
        for(int i = 0; i < subReactorCount; i++) {
            SubReactor sub = new SubReactor();
            sub.init();
            subReactors.add(sub);
        }

        //注册Accept事件并设置事件响应Handler
        SelectionKey selectionKey = serverSocket.register(selector,
                SelectionKey.OP_ACCEPT);

        acceptor = new Acceptor(serverSocket, getNextSubSelector());
        selectionKey.attach(acceptor);
    }

    /**
     * 获取下一个subReactor的Selector对象，采用轮转发分配事件。因为Accept是串行操作，所以无需同步
     * @return
     */
    private Selector getNextSubSelector() {
        return subReactors.get(subIndex++ % subReactorCount).getSelector();
    }

    /**
     * 分发事件
     * @param selectionKey
     */
    protected void dispatch(SelectionKey selectionKey) {
        super.dispatch(selectionKey);
        //替换掉下一个Accept事件的注册Selector，即分配给下一个Reactor处理
        this.acceptor.setSelector(getNextSubSelector());
    }

    /**
     * 启动所有Reactor线程
     */
    public void start() {
        for(SubReactor subReactor : subReactors) {
            executor.submit(subReactor);
        }
        executor.submit(this);
    }

    public void stop() {
        this.needStop = true;
        for (SubReactor subReactor : subReactors) {
            subReactor.stop();
        }
        //关闭所有Reactor
        executor.shutdown();

        //等待所有工作线程结束停止
        WorkerThreadPool.shutdown();
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
            MainReactor mainReactor = new MainReactor(10096, 2);
            mainReactor.init();
            mainReactor.start();
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
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mainReactor.stop();
            threadPoolExecutor.shutdown();
            logger.info("Reactor exit success.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}