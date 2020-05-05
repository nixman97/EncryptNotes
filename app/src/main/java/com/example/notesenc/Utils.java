package com.example.notesenc;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Utils {


    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;

    }
    public boolean checkPasswordSecurity(String passwd) {
        int u=0;
        int n=0;
        int a=0;
        if (passwd.length()<8)
            return false;
        for (int i=0;i<passwd.length();i++) {
            if (Character.isUpperCase(passwd.charAt(i))) {
                u++;
            }
            if (!Character.isAlphabetic(passwd.charAt(i))) {
                n++;
            }
            if (!Character.isAlphabetic(passwd.charAt(i))) {
                a++;
            }
        }
        return u > 2 && n > 3 && a > 2;
    }
    public String encrypt(IvParameterSpec iv, String text, String passwd) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");

            byte[] bytes = passwd.getBytes();
            digest.update(bytes, 0, bytes.length);
            byte[] key = digest.digest();
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            c.init(Cipher.ENCRYPT_MODE, spec, iv);
            return Base64.encodeToString(c.doFinal(text.getBytes()),Base64.DEFAULT);

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean isEnc() {
        return enc;
    }

    public String hashBasedCheck(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(data.getBytes());
        return bytesToHex(md.digest()).substring(64);
    }

    public String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes)
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }


    private String passwd=null;

    public void setEnc(boolean enc) {
        this.enc = enc;
    }

    private boolean enc;
    private static final Utils ourInstance = new Utils();

    static Utils getInstance() {
        return ourInstance;
    }

    private Utils() {

    }
    public String decrypt(IvParameterSpec iv, String data, String passwd) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = passwd.getBytes();
            digest.update(bytes, 0, bytes.length);
            byte[] key = digest.digest();
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            c.init(Cipher.DECRYPT_MODE, spec, iv);
            return new String(c.doFinal(Base64.decode(data, Base64.DEFAULT)));
        }
        catch (NoSuchAlgorithmException e) {

        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

}
