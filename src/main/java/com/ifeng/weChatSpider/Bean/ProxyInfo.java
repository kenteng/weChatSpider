package com.ifeng.weChatSpider.Bean;

/**
 * ProxyInfo.java
 * Created by zhusy on 2017/6/9 0009 9:58
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class ProxyInfo {
    private String ip;
    private int port;
    private String cookie;
    private String account;
    private String password;
    private boolean status;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
