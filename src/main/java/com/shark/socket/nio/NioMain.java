package com.shark.socket.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NioMain {
    private static Logger logger = LoggerFactory.getLogger(NioMain.class);

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            5,
            10,
            1,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(16));

    public static void main(String[] args) {
        //server thread
        threadPoolExecutor.submit(()->{
            NioServer server = new NioServer();
            server.init(8080);
            server.run();
        });

        for (int i=0; i<2; i++) {
            int finalI = i;
            threadPoolExecutor.submit(()->{
                NioClient client = new NioClient();
                client.init("127.0.0.1", 8080);
                try {
                    logger.info("Client start to sleep.");
                    Thread.sleep(5000);
                    logger.info("Client end sleep.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                client.send(String.format("Hello server. I am client[%d].", finalI));
                logger.info("服务端响应消息接受成功[{}].", client.recv());
                client.close();
            });
        }

        threadPoolExecutor.shutdown();
    }
}
