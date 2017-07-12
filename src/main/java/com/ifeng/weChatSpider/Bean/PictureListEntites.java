package com.ifeng.weChatSpider.Bean;

import com.ifeng.weChatSpider.Mongo.DataLoader;
import com.ifeng.weChatSpider.Mongo.EntyCodec;

/**
 * PictureListEntites.java
 * Created by zhusy on 2017/6/6 0006 17:05
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class PictureListEntites extends EntyCodec{
    private int id;
    private String url;
    private String title;
    private int status;
    private String createTime;
    private String processTime;

    @Override
    public void decode(DataLoader loader) {
        this.id = loader.getInt("id");
        this.url = loader.getString("url");
        this.title = loader.getString("title");
        this.status = loader.getInt("status");
        this.createTime = loader.getString("createTime");
        this.processTime = loader.getString("processTime");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getProcessTime() {
        return processTime;
    }

    public void setProcessTime(String processTime) {
        this.processTime = processTime;
    }
}
