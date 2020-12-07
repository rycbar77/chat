package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Vector;

public class Main extends Thread {
    private static DatagramSocket socket;
    private static final JTextArea textArea = new JTextArea(15, 25);
    private static final JList<String> listArea = new JList<>();
    private static final Vector<String> listcontent = new Vector<>();
    private static final Vector<String> ips = new Vector<>();
    private static final JFrame jf = new JFrame("Chat");
    private static final Dialog d = new Dialog(jf, "传输文件确认", true);
    private static final Label lab = new Label();
    private static String file = "";
    private static FileTransferClient client;
    private static final Button cancel = new Button("Cancel");
    private static final Button okBut = new Button("OK");
    private static String fileSendIp = "";


    static {
        try {
            client = new FileTransferClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Main() throws Exception {
    }

    private static String getLocalMac(InetAddress ia) throws SocketException {
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            int tmp = mac[i] & 0xff;
            String str = Integer.toHexString(tmp);
            if (str.length() == 1) {
                sb.append("0").append(str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }

    private static int[] getLocalPrefix(InetAddress ia) throws SocketException {
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ia);
//        System.out.println(ia.getHostAddress());
        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
            if (address.getAddress().toString().substring(1).equals(ia.getHostAddress())) {
//                StringBuilder maskStr = new StringBuilder();
                int mask = address.getNetworkPrefixLength();
                int[] maskIp = new int[4];
                for (int i = 0; i < maskIp.length; i++) {
                    maskIp[i] = (mask >= 8) ? 255 : (mask > 0 ? (mask & 0xff) : 0);
                    mask -= 8;
                }
                return maskIp;
            }
        }
        return new int[]{255, 255, 255, 0};
    }

    private static void sendDataWithUDPSocket(String str, String ip) {
        try {
            socket = new DatagramSocket();
            InetAddress local = InetAddress.getLocalHost();
            InetAddress serverAddress = InetAddress.getByName(ip);
            String[] addr = local.toString().split("/");
            Msg msg = new Msg(addr[1], getLocalMac(local), addr[0], str);
//            byte[] data = str.getBytes();
            byte[] data = msg.packMsg();
//            System.out.println(Arrays.toString(data));
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 10025);

            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFileRequestWithUDPSocket(String str, String ip, boolean send) {
        try {
            socket = new DatagramSocket();
            InetAddress local = InetAddress.getLocalHost();
            InetAddress serverAddress = InetAddress.getByName(ip);
            String[] addr = local.toString().split("/");
            Msg msg = new Msg(addr[1], getLocalMac(local), addr[0], str);
//            byte[] data = str.getBytes();
            if (send) {
                msg.setFileRequest(1);
            } else {
                msg.setFileRequest(2);
            }

            byte[] data = msg.packMsg();
//            System.out.println(Arrays.toString(data));
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 10025);

            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendDataWithUDPSocket(String str) {
        try {
            socket = new DatagramSocket();
            InetAddress local = InetAddress.getLocalHost();
            InetAddress serverAddress = InetAddress.getLocalHost();
            String[] addr = local.toString().split("/");
            Msg msg = new Msg(addr[1], getLocalMac(local), addr[0], str);
//            byte[] data = str.getBytes();
            byte[] data = msg.packMsg();
//            System.out.println(Arrays.toString(data));
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 10025);
            textArea.append(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC() + " : " + str + "\n");
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendDataWithUDPSocketBroadcast(boolean online, String ip) throws InterruptedException {
        for (int i = 0; i < 4; i++) {
            try {
                socket = new DatagramSocket();
                InetAddress local = InetAddress.getLocalHost();
                InetAddress serverAddress = InetAddress.getByName(ip);
                String[] addr = local.toString().split("/");
                Msg msg = new Msg(addr[1], getLocalMac(local), addr[0], "");
//            byte[] data = str.getBytes();
                if (online) {
                    msg.setOnline(1);
                } else {
                    msg.setOnline(4);
                }
                byte[] data = msg.packMsg();
//            System.out.println(Arrays.toString(data));
                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 10025);
//            textArea.append(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC() + " : " + str + "\n");
                socket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }
            sleep(500);
        }

    }

    public static class Server extends Thread {
//        private static String fileSendIp;

        //        private static String fileSendIp;
        private static void ServerReceivedByUdp() {
            DatagramSocket socket;
            try {
                socket = new DatagramSocket(10025);
                while (true) {
                    byte[] data = new byte[4 * 1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    socket.receive(packet);
                    Msg msg = new Msg();
                    msg.unpackMsg(packet.getData());
                    InetAddress local = InetAddress.getLocalHost();
                    String mac = getLocalMac(local);
                    int Online = msg.getOnline();
                    int FileRequest = msg.getFileRequest();
                    if (FileRequest == 0) {
                        if (Online == 1) {
                            if (!listcontent.contains(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC())) {
                                listcontent.addElement(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC());
                                listArea.setListData(listcontent);
                            }

                            InetAddress serverAddress = InetAddress.getByName(msg.getIp());
                            Msg onlineRes = new Msg(local.getHostAddress(), mac, local.getHostName(), "");
                            onlineRes.setOnline(2);
                            byte[] dataRes = onlineRes.packMsg();
                            DatagramPacket packetRes = new DatagramPacket(dataRes, dataRes.length, serverAddress, 10025);
                            socket.send(packetRes);
//                        disconnect();
                        } else if (Online == 3) {
                            String result = msg.getMsg();
                            textArea.append(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC() + " : " + result + "\n");
//                    System.out.println(packet.getAddress() + " : " + result);
                        } else if (Online == 2) {
                            if (!listcontent.contains(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC())) {
                                listcontent.addElement(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC());
                                listArea.setListData(listcontent);
                            }

                        } else {
                            if (listcontent.contains(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC())) {
                                listcontent.removeElement(msg.getName() + "/" + msg.getIp() + "/" + msg.getMAC());
                                listArea.setListData(listcontent);
                            }

                        }
                    } else if (FileRequest == 1) {
                        fileSendIp = msg.getIp();
                        okBut.addActionListener(e -> {
                            System.out.println("file send request");
//                            System.out.println(fileSendIp);
                            sendFileRequestWithUDPSocket("", fileSendIp, false);
                            d.setVisible(false);
                        });

                        cancel.addActionListener(e -> {
                            d.setVisible(false);
                        });
                        lab.setText("Want to receive file from " + fileSendIp + " ?");
                        d.setVisible(true);

                    } else {
                        System.out.println("Start sending file");
                        client.sendFile(Main.file, msg.getIp());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            ServerReceivedByUdp();
        }
    }

    public static class fileServer extends Thread {
        private static final int SERVER_PORT = 8899;
        Socket socket = null;
        ServerSocket ss = new ServerSocket(SERVER_PORT);


        private DataInputStream dis;

        private FileOutputStream fos;

        public fileServer() throws IOException {
        }


        @Override
        public void run() {

            while (true) {
                try {
                    socket = ss.accept();
                    dis = new DataInputStream(socket.getInputStream());

                    String fileName = dis.readUTF();
                    long fileLength = dis.readLong();
                    File directory = new File(".\\files");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                    fos = new FileOutputStream(file);

                    byte[] bytes = new byte[1024];
                    int length = 0;
                    while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                        fos.write(bytes, 0, length);
                        fos.flush();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                        if (dis != null)
                            dis.close();
                        socket.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }


    private static void disconnect() {
        socket.close();
        socket.disconnect();
    }

    private static class Exit {

        Exit() {
            Thread t = new Thread(() -> {

                try {
                    sendDataWithUDPSocketBroadcast(false, getNetworkAddr());
                    Thread.sleep(500);
                } catch (UnknownHostException | SocketException | InterruptedException unknownHostException) {
                    unknownHostException.printStackTrace();
                }

            });
            Runtime.getRuntime().addShutdownHook(t);
        }

    }

    private static String getNetworkAddr() throws UnknownHostException, SocketException {
        StringBuilder ip = new StringBuilder();
        InetAddress local = InetAddress.getLocalHost();
        int[] mask = getLocalPrefix(local);
        String[] localip = local.getHostAddress().split("\\.");
        for (int i = 0; i < 4; i++) {
            int tmp = 0;
            if (mask[i] == 0) {
                tmp = 255;
            } else {
                tmp = Integer.parseInt(localip[i]) & mask[i];
            }
            ip.append(tmp);
            if (i != 3) {
                ip.append(".");
            }
        }
        return ip.toString();
    }

    public static void main(String[] args) throws Exception {
//        okBut.addActionListener(e -> {
//            System.out.println("file send request");
//            sendFileRequestWithUDPSocket("", fileSendIp, false);
//            d.setVisible(false);
//        });
//
//        cancel.addActionListener(e -> {
//            d.setVisible(false);
//        });
        d.setBounds(100, 200, 100, 100);
        d.setLayout(new FlowLayout());
        d.add(lab);        d.add(okBut);
        d.add(cancel);

        Thread server = new Server();
        server.start();
        new Exit();
        Thread fileserver = new fileServer();
        fileserver.start();


        jf.setSize(600, 400);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setFont(new Font(null, Font.PLAIN, 15));

        JScrollPane scrollPane = new JScrollPane(
                textArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
//        panel.add(textArea);

        final JTextField textField = new JTextField(10);
        textField.setFont(new Font(null, Font.PLAIN, 15));
        panel.add(textField);


        JButton btn = new JButton("Send");
        btn.setFont(new Font(null, Font.PLAIN, 15));
        btn.addActionListener(e -> {
            for (String ip : ips) {
                sendDataWithUDPSocket(textField.getText(), ip);
            }
            try {
                InetAddress local = InetAddress.getLocalHost();
                String mac = getLocalMac(local);
                textArea.append(local.getHostName() + "/" + local.getHostAddress() + "/" + mac + " : " + textField.getText() + "\n");
            } catch (UnknownHostException | SocketException unknownHostException) {
                unknownHostException.printStackTrace();
            }

//            disconnect();
            textField.setText("");
//                textArea.append("Me : " + textField.getText() + "\n");
        });
        panel.add(btn);
        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(10));
        vBox.add(scrollPane);
        vBox.add(Box.createVerticalStrut(10));
        vBox.add(panel);

        JButton btn2 = new JButton("Scan");

//        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btn2.setFont(new Font(null, Font.PLAIN, 15));
        btn2.addActionListener(e -> {

            try {
                sendDataWithUDPSocketBroadcast(true, getNetworkAddr());
            } catch (UnknownHostException | SocketException | InterruptedException unknownHostException) {
                unknownHostException.printStackTrace();
            }
//                textArea.append("Me : " + textField.getText() + "\n");
        });
//        panel2.add(btn2);
        InetAddress local = InetAddress.getLocalHost();
        String mac = getLocalMac(local);
        listcontent.addElement(local.getHostName() + "/" + local.getHostAddress() + "/" + mac);
        listArea.setListData(listcontent);
        listArea.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane2 = new JScrollPane(
                listArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        listArea.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ips.removeAllElements();
                int[] indices = listArea.getSelectedIndices();

                ListModel<String> listModel = listArea.getModel();

                for (int index : indices) {
                    ips.addElement(listModel.getElementAt(index).split("/")[1]);
                }
                System.out.println();
            }
        });

        JButton open;


        open = new JButton("File");
        open.setFont(new Font(null, Font.PLAIN, 15));
        open.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            jfc.showDialog(new JLabel(), "选择");
            File files = jfc.getSelectedFile();
            file = files.getAbsolutePath();
            for (String ip : ips) {
                sendFileRequestWithUDPSocket("", ip, true);
            }
        });

//        Box hBox0 = Box.createHorizontalBox();
//        hBox0.add(open);
        JPanel empty_panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        empty_panel.setMaximumSize(new Dimension(200, 100));
//        empty_panel.setSize(50, 50);
        empty_panel.add(btn2);
        empty_panel.add(open);
//        hBox0.add(empty_panel);
////        hBox0.add(Box.createHorizontalStrut(30));
//        hBox0.add(btn2);


        Box vBox2 = Box.createVerticalBox();
        vBox2.add(Box.createVerticalStrut(10));
        vBox2.add(empty_panel);
        vBox2.add(Box.createVerticalStrut(10));
        vBox2.add(scrollPane2);

        vBox2.add(Box.createVerticalStrut(10));
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(vBox);
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(vBox2);
        hBox.add(Box.createHorizontalStrut(10));


        jf.setContentPane(hBox);

        jf.setVisible(true);
    }

}
