package com.shark.socket.multi;

import com.shark.socket.single.SocketClient;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultiThreadMain {
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
            10, 1,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(16));

    public static void main(String[] args) {
        MultiThreadServer server = new MultiThreadServer();

        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                server.run();
            }
        });

        threadPoolExecutor.submit(()-> {
            SocketClient client = new SocketClient();
            client.run();
        });

        threadPoolExecutor.submit(()-> {
            SocketClient client = new SocketClient();
            client.run();
        });

        threadPoolExecutor.shutdown();
    }
}
