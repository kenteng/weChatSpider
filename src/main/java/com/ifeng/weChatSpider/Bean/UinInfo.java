package com.ifeng.weChatSpider.Bean;

import com.ifeng.weChatSpider.Mongo.DataLoader;
import com.ifeng.weChatSpider.Mongo.EntyCodec;
import org.bson.Document;

/**
 * UinInfo.java
 * Created by zhusy on 2017/4/13 0013 10:52
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class UinInfo extends EntyCodec{
    private Object _id;
    private String uin;
    private String key;
    private String url;
    private String cookie;
    private String ua2;
    private String guid;
    private String auth;
    private String version;
    private String passTicket;
    private int status = 1;
    private int count;
    private String startTime;
    private String endTime;
    private String createTime;

    @Override
    public void decode(DataLoader loader) {
        this._id = loader.getObject("_id");
        this.uin = loader.getString("uin");
        this.key = loader.getString("key");
        this.url = loader.getString("url");
        this.cookie = loader.getString("cookie");
        this.ua2 = loader.getString("ua2");
        this.guid = loader.getString("guid");
        this.auth = loader.getString("auth");
        this.version = loader.getString("version");
        this.passTicket = loader.getString("passTicket");
        this.status = loader.getInt("status");
        this.count = loader.getInt("count");
        this.startTime = loader.getString("startTime");
        this.endTime = loader.getString("endTime");
        this.createTime = loader.getString("createTime");
    }

    @Override
    public <T> Document encode() {
        Document en = new Document();
        en.put("uin",this.uin);
        en.put("key",this.key);
        en.put("url",this.url);
        en.put("cookie",this.cookie);
        en.put("ua2",this.ua2);
        en.put("guid",this.guid);
        en.put("auth",this.auth);
        en.put("version",this.version);
        en.put("passTicket",this.passTicket);
        en.put("status",this.status);
        en.put("count",this.count);
        en.put("startTime",this.startTime);
        en.put("endTime",this.endTime);
        en.put("createTime",this.createTime);
        return en;
    }

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getUa2() {
        return ua2;
    }

    public void setUa2(String ua2) {
        this.ua2 = ua2;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPassTicket() {
        return passTicket;
    }

    public void setPassTicket(String passTicket) {
        this.passTicket = passTicket;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }


}
