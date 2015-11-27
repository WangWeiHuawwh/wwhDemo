package com.wwh;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class AESEncrypt {
    public static final int CMD = 10000;
    public static final int READ_ERROR = -1;
    public static final int READ_SUCCESS = -2;
    public static final byte[] PUBLIC_KEY = "wwhhaha".getBytes();
    public static final byte[] RANDOM_KEY = getRandomKey();
    public byte[] PRIVATE_KEY = PUBLIC_KEY;

    public static byte[] encrypt(byte[] data) {
        return encrypt(Commen.key, data);
    }

    public static byte[] encrypt(byte[] key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            int plaintextLength = data.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(data, 0, plaintext, 0, data.length);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(plaintext);
            return encrypted;
            //            return Base64.encode(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    static public String getMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static byte[] desEncrypt(byte[] key, byte[] data, int outBytes) {
        try {
            //            byte[] encrypted1 = Base64.decode(data, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            byte[] original = cipher.doFinal(data);
            byte[] originalBytes = new byte[outBytes];
            System.arraycopy(original, 0, originalBytes, 0, outBytes);
            return originalBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getRandomKey() {
        byte[] out = new byte[16];
        for (int i = 0; i < 16; i++) {
            out[i] = (byte) (Math.random() * 255);
        }

        return out;
    }

}
