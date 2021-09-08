package com.naixue.spear;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DesEncryption {

    public static final String ENCRYPTION_ALGORITHM = "DES";

    public static void main(String[] args) throws Exception {
        Key key =
                SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM)
                        .generateSecret(new DESKeySpec("12345678".getBytes()));
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] result = cipher.doFinal();
        System.out.println(result);
    }
}
