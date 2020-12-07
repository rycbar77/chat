package com.company;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer extends ServerSocket {

    private static final int SERVER_PORT = 8899;

    public FileTransferServer() throws Exception {
        super(SERVER_PORT);
    }


    public void load() throws Exception {
        while (true) {
            Socket socket = this.accept();
            new Thread(new Task(socket)).start();
        }
    }


    static class Task implements Runnable {

        private final Socket socket;

        private DataInputStream dis;

        private FileOutputStream fos;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                dis = new DataInputStream(socket.getInputStream());

                // 文件名和长度
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();
                File directory = new File(".\\files");
                if(!directory.exists()) {
                    directory.mkdir();
                }
                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                fos = new FileOutputStream(file);

                byte[] bytes = new byte[1024];
                int length = 0;
                while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                }
                System.out.println("======== 文件接收成功 [File Name：" + fileName + "] ========");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if(fos != null)
                        fos.close();
                    if(dis != null)
                        dis.close();
                    socket.close();
                } catch (Exception ignored) {}
            }
        }
    }

    public static void main(String[] args) {
        try {
            FileTransferServer server = new FileTransferServer();
            server.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}