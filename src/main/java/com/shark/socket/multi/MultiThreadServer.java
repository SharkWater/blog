package com.shark.socket.multi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadServer {
    private static Logger logger = LoggerFactory.getLogger(MultiThreadServer.class);

    private int port = 8081;// 默认服务器端口

    /**
     * 主线程侦听客户端请求
     */
    public void run() {
        int count = 0;
        try {
            ServerSocket server = new ServerSocket(port, 3);
            while (true) {
                Socket socket = server.accept();
                count++;
                logger.info("第" + count + "个客户连接成功！");
                new Thread(new ServerThread(socket, count)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


/**
 * 子线程处理请求
 */
class ServerThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(ServerThread.class);

    private int index;
    private Socket socket;

    public ServerThread(Socket socket, int index) {
        this.socket = socket;
        this.index = index;
    }

    @Override
    public void run() {
        try {
            try {
                logger.info("开始运行服务端处理线程.");
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String recvBuffer  =  null;
                StringBuffer recvData = new StringBuffer();
                while((recvBuffer = bufferedReader.readLine()) != null){
                    recvData.append(recvBuffer);
                }
                logger.info("客户端请求接受完毕[{}].", recvData);

                logger.info("发送应答消息.");
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter printWriter = new PrintWriter(outputStream);
                printWriter.print("Request Accepted!");
                printWriter.flush();
                socket.shutdownOutput();
                printWriter.close();
                outputStream.close();
                bufferedReader.close();
                inputStream.close();
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
