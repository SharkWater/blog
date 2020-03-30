package com.shark.socket.single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {

    private static Logger logger = LoggerFactory.getLogger(SocketClient.class);

    public void run() {
        try {
            logger.info("客户端启动.");
            Thread.sleep(5000);
            logger.info("开始连接服务端.");
            Socket socket = new Socket("localhost",8081);
            logger.info(String.format("连接状态[%s]", socket.isConnected()));

            logger.info("Client begin to sleep.");
            Thread.sleep(5000);
            logger.info("Client end sleep.");

            logger.info("开始向服务端发送数据.");
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.print("Hello Server.");
            printWriter.flush();
            socket.shutdownOutput();

            logger.info("等待服务端应答数据.");
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer recvData = new StringBuffer();
            String temp = null;
            while((temp = bufferedReader.readLine())!=null){
                recvData.append(temp);
            }
            logger.info(String.format("客户端接收到服务端发送信息[%s]", recvData));

            bufferedReader.close();
            inputStream.close();
            printWriter.close();
            outputStream.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}