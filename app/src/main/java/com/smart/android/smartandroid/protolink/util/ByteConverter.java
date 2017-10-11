package com.smart.android.smartandroid.protolink.util;

/**
 * Created by liujia on 17/1/20.
 *
 * 用于将int long short等转为网络字节序(Big Endian), 和从网络包二进制流中解析出int long short等
 * 哥真是没找到java里的htonl() ntohl()等函数，也懒得找了，自己撸吧
 *
 * short int long每个分别提供四个函数，两个数字到byte[](返回新分配的数组，写入传入的数组)，两个byte[]到数字(直接返回，给定数组的offset从那里解析)
 *
 */

public class ByteConverter {
    /*
    * long  8字节
    */
    public static byte[] longToBytes(long n) {
        byte[] b = new byte[8];
        b[7] = (byte) (n & 0xff);
        b[6] = (byte) (n >> 8 & 0xff);
        b[5] = (byte) (n >> 16 & 0xff);
        b[4] = (byte) (n >> 24 & 0xff);
        b[3] = (byte) (n >> 32 & 0xff);
        b[2] = (byte) (n >> 40 & 0xff);
        b[1] = (byte) (n >> 48 & 0xff);
        b[0] = (byte) (n >> 56 & 0xff);
        return b;
    }

    public static void longToBytes(long n, byte[] array, int offset) {
        array[7 + offset] = (byte) (n & 0xff);
        array[6 + offset] = (byte) (n >> 8 & 0xff);
        array[5 + offset] = (byte) (n >> 16 & 0xff);
        array[4 + offset] = (byte) (n >> 24 & 0xff);
        array[3 + offset] = (byte) (n >> 32 & 0xff);
        array[2 + offset] = (byte) (n >> 40 & 0xff);
        array[1 + offset] = (byte) (n >> 48 & 0xff);
        array[0 + offset] = (byte) (n >> 56 & 0xff);
    }

    public static long bytesToLong(byte[] array) {
        return (  (((long) array[0] & 0xff) << 56)
                | (((long) array[1] & 0xff) << 48)
                | (((long) array[2] & 0xff) << 40)
                | (((long) array[3] & 0xff) << 32)
                | (((long) array[4] & 0xff) << 24)
                | (((long) array[5] & 0xff) << 16)
                | (((long) array[6] & 0xff) << 8)
                | (((long) array[7] & 0xff) << 0)
            );
    }

    public static long bytesToLong(byte[] array, int offset) {
        return ((((long) array[offset + 0] & 0xff) << 56) | (((long) array[offset + 1] & 0xff) << 48)
                | (((long) array[offset + 2] & 0xff) << 40) | (((long) array[offset + 3] & 0xff) << 32)
                | (((long) array[offset + 4] & 0xff) << 24) | (((long) array[offset + 5] & 0xff) << 16)
                | (((long) array[offset + 6] & 0xff) << 8) | (((long) array[offset + 7] & 0xff) << 0));
    }

    /*
    * int  4字节
    */
    public static byte[] intToBytes(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static void intToBytes(int n, byte[] array, int offset) {
        array[3 + offset] = (byte) (n & 0xff);
        array[2 + offset] = (byte) (n >> 8 & 0xff);
        array[1 + offset] = (byte) (n >> 16 & 0xff);
        array[offset] = (byte) (n >> 24 & 0xff);
    }

    public static int bytesToInt(byte b[]) {
        return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
    }

    public static int bytesToInt(byte b[], int offset) {
        return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16 | (b[offset] & 0xff) << 24;
    }

    /*
    * uint  4字节, 用long的低32位来处理
    */
    public static byte[] uintToBytes(long n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static void uintToBytes(long n, byte[] array, int offset) {
        array[3 + offset] = (byte) (n);
        array[2 + offset] = (byte) (n >> 8 & 0xff);
        array[1 + offset] = (byte) (n >> 16 & 0xff);
        array[offset] = (byte) (n >> 24 & 0xff);
    }

    public static long bytesToUint(byte[] array) {
        return ((long) (array[3] & 0xff)) | ((long) (array[2] & 0xff)) << 8 | ((long) (array[1] & 0xff)) << 16 | ((long) (array[0] & 0xff)) << 24;
    }

    public static long bytesToUint(byte[] array, int offset) {
        return ((long) (array[offset + 3] & 0xff)) | ((long) (array[offset + 2] & 0xff)) << 8 | ((long) (array[offset + 1] & 0xff)) << 16 | ((long) (array[offset] & 0xff)) << 24;
    }

    /*
    * short  2字节
    */
    public static byte[] shortToBytes(short n) {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) ((n >> 8) & 0xff);
        return b;
    }

    public static void shortToBytes(short n, byte[] array, int offset) {
        array[offset + 1] = (byte) (n & 0xff);
        array[offset] = (byte) ((n >> 8) & 0xff);
    }

    public static short bytesToShort(byte[] b) {
        return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
    }

    public static short bytesToShort(byte[] b, int offset) {
        return (short) (b[offset + 1] & 0xff | (b[offset] & 0xff) << 8);
    }

    /*
    * ushort  2字节, 用int的低16位来处理
    */
    public static byte[] ushortToBytes(int n) {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) ((n >> 8) & 0xff);
        return b;
    }

    public static void ushortToBytes(int n, byte[] array, int offset) {
        array[offset + 1] = (byte) (n & 0xff);
        array[offset] = (byte) ((n >> 8) & 0xff);
    }

    public static int bytesToUshort(byte b[]) {
        return b[1] & 0xff | (b[0] & 0xff) << 8;
    }

    public static int bytesToUshort(byte b[], int offset) {
        return b[offset + 1] & 0xff | (b[offset] & 0xff) << 8;
    }

    /*
    * ubyte  1字节, 用int的低8位来处理
    */
    public static byte[] ubyteToBytes(int n) {
        byte[] b = new byte[1];
        b[0] = (byte) (n & 0xff);
        return b;
    }

    public static void ubyteToBytes(int n, byte[] array, int offset) {
        array[0] = (byte) (n & 0xff);
    }

    public static int bytesToUbyte(byte[] array) {
        return array[0] & 0xff;
    }

    public static int bytesToUbyte(byte[] array, int offset) {
        return array[offset] & 0xff;
    }
}