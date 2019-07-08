package com.liang.shadow.socks.service;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by lianglingtao on 2019/2/25.
 */
public class AccountManageService {

    private String userName;
    private String password;

    private static final SecureRandom sr = new SecureRandom();

    public AccountManageService(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public AccountManageService setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AccountManageService setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * 获取一个加解密实现，注意，此实现必须是基于8bit的，不然会在socks的传输中，被加解密组件遗留下一部分（不足一个加密/解密block）；
     * 其直接的表现是，客户端写完等响应，服务端等数据写完（还有一部分在加密未满的block中）；
     * 而无论是ss的客户端还是服务端，都不能肯定发送/接收的数据有没有结束，即无法准时调用doFinal方法，因此加解密必须是基于8bit的算法；
     * 大多数分享的'自定义混淆算法'的初衷都是为了绕开这个问题。
     *
     * @param mode 加解密的mode
     * @return java标准的Cipher，无需做doFinal即可做加解密的实现
     * @throws Exception
     */
    public Cipher cipher(int mode) throws Exception {
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        byte[] cipherKey = DigestUtils.sha1((userName + password).getBytes());
        cipherKey = Arrays.copyOf(cipherKey, 16); // use only first 128 bit
        SecretKeySpec secretKeySpec = new SecretKeySpec(cipherKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(mode, secretKeySpec, ivspec);
        return cipher;
    }

}
