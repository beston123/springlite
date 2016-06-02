package org.springlite.bean;

import org.springlite.beans.factory.FactoryBean;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *

 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class EncryptPasswordFactory implements FactoryBean<String> {

    private String password;

    public EncryptPasswordFactory() {
    }

    public void init(){
        System.out.println("Start to decode password '"+this.password+"'");
    }

    private static String encode(String secret) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] kbytes = "jaas is the way".getBytes();
        SecretKeySpec key = new SecretKeySpec(kbytes, "Blowfish");

        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encoding = cipher.doFinal(secret.getBytes());
        BigInteger n = new BigInteger(encoding);
        return n.toString(16);
    }

    private static char[] decode(String secret) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] kbytes = "jaas is the way".getBytes();
        SecretKeySpec key = new SecretKeySpec(kbytes, "Blowfish");

        BigInteger n = new BigInteger(secret, 16);
        byte[] encoding = n.toByteArray();

        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decode = cipher.doFinal(encoding);
        return new String(decode).toCharArray();
    }


    @Override
    public String getObject() throws Exception {
        return this.password != null? String.valueOf(decode(this.password)) : null;
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public static void main(String[] args) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        EncryptPasswordFactory encrypt = new EncryptPasswordFactory();
        String pwd = "123456";
        System.out.println(encrypt.encode(pwd)); //64c5fd2979a86168
    }
}
