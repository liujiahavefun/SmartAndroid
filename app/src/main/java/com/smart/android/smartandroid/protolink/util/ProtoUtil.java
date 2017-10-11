package com.smart.android.smartandroid.protolink.util;

import java.util.Arrays;

/**
 * Created by liujia on 17/1/24.
 */

public class ProtoUtil {
    /*
    * 拼接两个数组，通常是拼接两个byte[]
    * String[] both = concat(first, second);
    */
    public static byte[] concat(byte[] first, byte[] second) {
        if(first == null) {
            return second;
        }
        if(second == null) {
            return first;
        }
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /*
    * 拼接多个数组，通常是拼接多个byte[]
    * String[] more = concat(first, second, third, fourth);
    */
    public static byte[] concatAll(byte[] first, byte[]... rest) {
        if(first == null) {
            throw new IllegalArgumentException("first array should not be null");
        }
        int totalLength = first.length;
        for (byte[] array : rest) {
            if(array != null) {
                totalLength += array.length;
            }
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            if(array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }
}
