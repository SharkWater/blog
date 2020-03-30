package com.shark.socket.single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    private static Logger logger = LoggerFactory.getLogger(SocketServer.class);

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8081);
            logger.info("Socket服务端已启动.");
            while (true) {
                logger.info("阻塞等待客户端连接.");
                Socket socket = serverSocket.accept();
                logger.info(String.format("客户端[%s]已连接，阻塞等待客户端发送数据.",
                        socket.getInetAddress().getHostAddress()));
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String recvBuffer  =  null;
                StringBuffer recvData = new StringBuffer();
                while((recvBuffer = bufferedReader.readLine()) != null){
                    recvData.append(recvBuffer);
                }
                logger.info(String.format("从客户端[%s]接受请求[%s]成功.",
                        socket.getInetAddress().getHostAddress(), recvData.toString()));

                Thread.sleep(5000);
                logger.info("向客户端发送应答数据.");
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter printWriter = new PrintWriter(outputStream);
                printWriter.print("Request Accepted!");
                printWriter.flush();
                socket.shutdownOutput();
                printWriter.close();
                outputStream.close();
                bufferedReader.close();
                inputStream.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}