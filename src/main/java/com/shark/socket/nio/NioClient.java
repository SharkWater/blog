package com.shark.socket.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioClient {
    private static Logger logger = LoggerFactory.getLogger(NioClient.class);

    private Selector selector;
    private SocketChannel socketChannel ;

    /**
     * 初始化
     * @param serverIp
     * @param serverPort
     */
    public void init(String serverIp, int serverPort){
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息
     * @param str
     */
    public void send(String str){
        try {
            if(socketChannel.isConnected()){
                socketChannel.write(ByteBuffer.wrap(str.getBytes()));
            } else {
                logger.error("Connect to server fail.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接受返回值
     * @return
     */
    public String recv(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(4000);
        try {
            while(true) {
                if(selector.select(1000) == 1){
                    Iterator it = selector.selectedKeys().iterator();
                    StringBuffer buffer = new StringBuffer();
                    while(it.hasNext()){
                        SelectionKey key = (SelectionKey)it.next();
                        if(key.isReadable()){
                            SocketChannel channel = (SocketChannel)key.channel();
                            while(channel.isOpen() && channel.read(byteBuffer)>0){
                                buffer.append(new String(byteBuffer.array()).trim());
                                byteBuffer.clear();
                            }
                            channel.close();
                        }
                        key.cancel();
                        it.remove();
                    }
                    return buffer.toString();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭客户端
     */
    public void close(){
        try {
            socketChannel.close();
            selector.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

