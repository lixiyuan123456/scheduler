package com.naixue.dp.common.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Base64;

// import org.springframework.util.Assert;
// import org.springframework.util.Base64Utils;

public class SecurityUtils {

    public static String simpleEncrypt(String text) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(text), "请求参数text不允许为空");
        // Assert.hasText(text, "请求参数text不允许为空");
        char[] in = text.toCharArray();
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (byte) (in[i] ^ 'a');
        }
        // return Base64Utils.encodeToString(out);
        return Base64.getEncoder().encodeToString(out);
    }

    public static String simpleDecrypt(String text) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(text), "请求参数text不允许为空");
        // Assert.hasText(text, "请求参数text不允许为空");
        // byte[] in = Base64Utils.decodeFromString(text);
        byte[] in = Base64.getDecoder().decode(text);
        char[] out = new char[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (char) (in[i] ^ 'a');
        }
        return new String(out);
    }

    public static void main(String[] args) {
        String s1 = SecurityUtils.simpleEncrypt("xbc123@!");
        System.out.println(s1);
        String s2 = SecurityUtils.simpleDecrypt(s1);
        System.out.println(s2);
    }
}
