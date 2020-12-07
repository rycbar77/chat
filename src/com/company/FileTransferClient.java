package com.company;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


public class FileTransferClient {
    private static final int SERVER_PORT = 8899; // 服务端端口


    private FileInputStream fis;

    private DataOutputStream dos;


    public void sendFile(String filename, String ip) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, SERVER_PORT));
            File file = new File(filename);
            if (file.exists()) {
                fis = new FileInputStream(file);
                dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();

                byte[] bytes = new byte[1024];
                int length = 0;
                while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);
                    dos.flush();
                }
                System.out.println("========File Sent Done========");
                socket.shutdownInput();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                fis.close();
            if (dos != null)
                dos.close();

        }
    }

    public static void main(String[] args) {
        try {
            FileTransferClient client = new FileTransferClient(); // 启动客户端连接
            client.sendFile("C:\\Users\\schro\\Desktop\\pwn_printf", "127.0.0.1"); // 传输文件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}