package com.shark.socket.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NioServer {
    private static Logger logger = LoggerFactory.getLogger(NioServer.class);

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5,
            10,
            1,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(16));
    private Selector selector;

    public void run(){
        while(true){
            try {
                selector.select();
                Iterator it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();
                    if (!key.isValid()) {
                        continue;
                    } else if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        process(key);
                    }
                }
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化
     */
    public void init(int port){
        try {
            selector = SelectorProvider.provider().openSelector();
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(port));
            channel.socket().setReuseAddress(true);
            channel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 接受TCP连接请求
     * @param key
     * @throws Exception
     */
    public void accept(SelectionKey key) throws Exception{
        logger.info("New client accepted.");
        ServerSocketChannel channel = (ServerSocketChannel)key.channel();
        SocketChannel sc = channel.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
    }

    /**
     * 服务端相应函数
     * @param key
     */
    public void process(SelectionKey key){
        SocketChannel channel = (SocketChannel)key.channel();
        if(channel.isOpen() && channel.isConnected()){
            executor.submit(new ProcessThread(key));
        }
    }
}

class ProcessThread extends Thread{
    private static Logger logger = LoggerFactory.getLogger(ProcessThread.class);
    private SocketChannel sc;
    private SelectionKey key;
    public ProcessThread(SelectionKey key){
        this.key = key;
    }

    public void run() {
        logger.info("开始运行服务端处理线程.");
        String message = recv();
        logger.info("客户端请求接受完毕[{}].", message);
        if (message != null && !message.isEmpty()) {
            write("Hello client.");
        }
    }

    public String recv(){
        int cnt = 0;
        ByteBuffer bb = ByteBuffer.allocate(4000);
        sc = (SocketChannel)key.channel();
        StringBuffer buffer = new StringBuffer();
        try{
            while(sc.isConnected()&&sc.isOpen()&&(cnt = sc.read(bb))>0){
                String t = new String(bb.array(),"utf-8").trim();
                buffer.append(t);
                bb.clear();
            }
            return buffer.toString();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            key.cancel();
        }
        return null;
    }

    public void write(String str){
        try {
            if(sc.isConnected()&&sc.isOpen())
                sc.write(ByteBuffer.wrap(str.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                sc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

