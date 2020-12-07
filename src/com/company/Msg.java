package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Msg {
    private static int FileRequest = 0;
    private static int Online = 0;
    private static String Ip = "";
    private static String MAC = "";
    private static String Name = "";
    private static String Msg = "";


    public static int getWordCountCode(String str, String code) throws UnsupportedEncodingException {
        return str.getBytes(code).length;
    }

    public void setOnline(int online) {
        Online = online;
    }

    public void setFileRequest(int fileRequest) {
        FileRequest = fileRequest;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public void setMAC(String mac) {
        MAC = mac;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public int getFileRequest() {
        return FileRequest;
    }

    public int getOnline() {
        return Online;
    }

    public String getIp() {
        return Ip;
    }

    public String getMAC() {
        return MAC;
    }

    public String getName() {
        return Name;
    }

    public String getMsg() {
        return Msg;
    }

    public byte[] packMsg() throws UnsupportedEncodingException {
        byte[] result = new byte[4 * 1024];
        result[0] = (byte) ((FileRequest >> 24) & 0xFF);
        result[1] = (byte) ((FileRequest >> 16) & 0xFF);
        result[2] = (byte) ((FileRequest >> 8) & 0xFF);
        result[3] = (byte) (FileRequest & 0xFF);
        result[4] = 0x17;
        result[5] = (byte) ((Online >> 24) & 0xFF);
        result[6] = (byte) ((Online >> 16) & 0xFF);
        result[7] = (byte) ((Online >> 8) & 0xFF);
        result[8] = (byte) (Online & 0xFF);
        result[9] = 0x17;
        if (Ip.length() > 0)
            System.arraycopy(Ip.getBytes(), 0, result, 10, Ip.length());
        result[10 + Ip.length()] = 0x17;
        if (MAC.length() > 0)
            System.arraycopy(MAC.getBytes(), 0, result, 10 + Ip.length() + 1, MAC.length());
        result[10 + Ip.length() + 1 + MAC.length()] = 0x17;
        if (Name.length() > 0)
            System.arraycopy(Name.getBytes(), 0, result, 10 + Ip.length() + 1 + MAC.length() + 1, Name.getBytes(StandardCharsets.UTF_8).length);
        result[10 + Ip.length() + 1 + MAC.length() + 1 + Name.length()] = 0x17;
//        System.out.println(Msg);
//        System.out.println(Arrays.toString(Msg.getBytes("GBK")));
        if (Msg.length() > 0)
            System.arraycopy(Msg.getBytes("GBK"), 0, result, 10 + Ip.length() + 1 + MAC.length() + 1 + Name.length() + 1, Msg.getBytes("GBK").length);
        result[10 + Ip.length() + 1 + MAC.length() + 1 + Name.length() + 1 + Msg.getBytes("GBK").length] = 0x17;
        return result;
    }

    public void unpackMsg(byte[] data) throws UnsupportedEncodingException {
        int fileRequest = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            fileRequest += (data[i] & 0xFF) << shift;
        }
        FileRequest = fileRequest;

        int online = 0;
        for (int i = 5; i < 9; i++) {
            int shift = (8 - i) * 8;
            online += (data[i] & 0xFF) << shift;
        }
        Online = online;

        int tmp = 10;
        int index = 10;
        for (int i = 10; i < data.length; i++) {
            if (data[i] == 0x17) {
                index = i;
                break;
            }
        }
        byte[] ip = new byte[16];
        int len = 0;
        len = Math.min(index - tmp, 15);
        if (len > 0)
            System.arraycopy(data, tmp, ip, 0, len);
        Ip = new String(ip, 0, len);

        tmp = index + 1;
        for (int i = index + 1; i < data.length; i++) {
            if (data[i] == 0x17) {
                index = i;
                break;
            }
        }

        byte[] mac = new byte[18];
        len = Math.min(index - tmp, 17);
        if (len > 0)
            System.arraycopy(data, tmp, mac, 0, len);
        MAC = new String(mac, 0, len);


        tmp = index + 1;
        for (int i = index + 1; i < data.length; i++) {
            if (data[i] == 0x17) {
                index = i;
                break;
            }
        }
        byte[] name = new byte[30];
        len = Math.min(index - tmp, 30);
        if (len > 0)
            System.arraycopy(data, tmp, name, 0, len);
        Name = new String(name, 0, len);

        tmp = index + 1;
        for (int i = index + 1; i < data.length; i++) {
            if (data[i] == 0x17 && data[i + 1] == 0x00) {
                index = i;
                break;
            }
        }
        byte[] msg = new byte[4 * 1024];
        len = Math.min(index - tmp, 4 * 1024);
        if (len > 0)
            System.arraycopy(data, tmp, msg, 0, len);

//        System.out.println(Arrays.toString(msg));
        Msg = new String(msg, "GBK");

    }

    Msg() {
        Online = 0;
        Ip = "";
        MAC = "";
        Name = "";
        Msg = "";
        FileRequest = 0;
    }

    Msg(String ip, String mac, String name, String msg) {
        Online = 3;
        Ip = ip;
        MAC = mac;
        Name = name;
        Msg = msg;
        FileRequest = 0;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Online = 3;
        Ip = "192.168.1.1";
        Msg = "hhhhhhh";
//        unpackMsg(packMsg());
//        System.out.println(getOnline());

        Runtime run = Runtime.getRuntime();
        Process p = run.exec("ipconfig /all");

        InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
//        p.waitFor();
        String s;
        while ((s = reader.readLine()) != null) {
            System.out.println(s);
        }

    }

}
