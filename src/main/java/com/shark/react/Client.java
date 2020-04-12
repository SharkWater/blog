package com.shark.react;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 简单的客户端实现
 */
public class Client {
    private String ip;
    private int port;

    private Selector selector;
    private SocketChannel socketChannel;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void init() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(ip, port));
        socketChannel.configureBlocking(false);
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public void send(String str) {
        try {
            if (socketChannel.isConnected()) {
                socketChannel.write(ByteBuffer.wrap(str.getBytes("utf-8")));
            } else {
                System.out.println("Connect to server fail.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4000);
        try {
            selector.select();
            //客户端的逻辑较为简单，只注册了一个channel的read事件，所以用不着走事件分发的逻辑
            byteBuffer.clear();
            StringBuffer stringBuffer = new StringBuffer();
            while (socketChannel.isOpen() && socketChannel.read(byteBuffer) > 0) {
                String content = new String(byteBuffer.array(), "utf-8");
                stringBuffer.append(content);
                byteBuffer.clear();
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        try {
            socketChannel.close();
            selector.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
