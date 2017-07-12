package com.ifeng.weChatSpider.Bean;

import com.ifeng.weChatSpider.Mongo.DataLoader;
import com.ifeng.weChatSpider.Mongo.EntyCodec;

/**
 * OtherArticle.java
 * Created by zhusy on 2017/4/28 0028 9:26
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class OtherArticle extends EntyCodec {
    private String title;
    private String pageUrl;
    private String preUrl;
    private String cateName;
    private String site;
    private int isFenghuang;

    @Override
    public void decode(DataLoader loader) {
        this.title = loader.getString("title");
        this.pageUrl = loader.getString("pageUrl");
        this.preUrl = loader.getString("preUrl");
        this.cateName = loader.getString("cateName");
        this.site = loader.getString("site");
        this.isFenghuang = loader.getInt("isFenghuang");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getPreUrl() {
        return preUrl;
    }

    public void setPreUrl(String preUrl) {
        this.preUrl = preUrl;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getIsFenghuang() {
        return isFenghuang;
    }

    public void setIsFenghuang(int isFenghuang) {
        this.isFenghuang = isFenghuang;
    }
}
