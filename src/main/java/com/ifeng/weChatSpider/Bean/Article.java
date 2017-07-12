/*
* WeChat.java
* Created on  202016/11/9 0009 15:27
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved
*/
package com.ifeng.weChatSpider.Bean;

import com.ifeng.weChatSpider.Mongo.DataLoader;
import com.ifeng.weChatSpider.Mongo.EntyCodec;

/**
 * Created by zhusy on 2016/11/9 0009.
 */
public class Article extends EntyCodec {
    private Object _id;
    /*** 栏目ID */
    private int cateId;
    /*** 文章标题 */
    private String title;
    /*** 类型（weixin/rss） */
    private String type;
    /*** 栏目名称 */
    private String sourceName;
    /*** 文章链接 */
    private String sourceLink;
    /*** 文章缩略图 */
    private String thumbnail;
    /*** 文章正文 */
    private String content;
    /*** 文章创建时间 */
    private String createTime;
    /*** 文章抓取时间 */
    private String crawlerDate;
    /*** 文章状态 0未同步 1已同步 */
    private int status;
    private String delayedTime;
    private String synResult;
    private int synStatus;
    private String account;
    private String from;
    private String headImgUrl;
    private int haveVideo = 1;


    @Override
    public void decode(DataLoader loader) {
        this._id = loader.getObject("_id");
        this.cateId = loader.getInt("cateid");
        this.title = loader.getString("title");
        this.type = loader.getString("type");
        this.sourceName = loader.getString("source_name");
        this.sourceLink = loader.getString("source_link");
        this.thumbnail = loader.getString("thumbnail");
        this.content = loader.getString("content");
        this.createTime = loader.getString("create_time");
        this.crawlerDate = loader.getString("crawler_date");
        this.synResult = loader.getString("synResult");
        this.from = loader.getString("from");
        this.status = loader.getInt("status");
        this.synStatus = loader.getInt("synStatus");
        this.headImgUrl=loader.getString("headImgUrl");
        this.haveVideo=loader.getInt("haveVideo");
        this.account=loader.getString("account");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"cateid\":\"").append(this.cateId).append("\",")
                .append("\"title\":\"").append(this.title).append("\",")
                .append("\"type\":\"").append(this.type).append("\",")
                .append("\"sourceName\":\"").append(this.sourceName).append("\",")
                .append("\"sourceLink\":\"").append(this.sourceLink).append("\",")
                .append("\"thumbnail\":\"").append(this.thumbnail).append("\",")
                .append("\"content\":\"").append(this.content).append("\",")
                .append("\"createTime\":\"").append(this.createTime).append("\",")
                .append("\"crawlerDate\":\"").append(this.crawlerDate).append("\",")
                .append("\"status\":\"").append(this.status).append("\"}")
                .append("\"from\":\"").append(this.from).append("\"}");
        return sb.toString();
    }
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"cateid\":\"").append(this.cateId).append("\",")
                .append("\"title\":\"").append(this.title).append("\",")
                .append("\"type\":\"").append(this.type).append("\",")
                .append("\"sourceName\":\"").append(this.sourceName).append("\",")
                .append("\"sourceLink\":\"").append(this.sourceLink).append("\",")
                .append("\"thumbnail\":\"").append(this.thumbnail).append("\",")
                .append("\"content\":\"").append(this.content).append("\",")
                .append("\"from\":\"").append(this.from).append("\",")
                .append("\"createTime\":\"").append(this.createTime).append("\"}");
        return sb.toString();
    }

    public String getSynResult() {
        return synResult;
    }

    public void setSynResult(String synResult) {
        this.synResult = synResult;
    }

    public int getSynStatus() {
        return synStatus;
    }

    public void setSynStatus(int synStatus) {
        this.synStatus = synStatus;
    }

    public String getDelayedTime() {
        return delayedTime;
    }

    public void setDelayedTime(String delayedTime) {
        this.delayedTime = delayedTime;
    }

    public int getCateId() {
        return cateId;
    }

    public void setCateId(int cateId) {
        this.cateId = cateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }

    public String getCrawlerDate() {
        return crawlerDate;
    }

    public void setCrawlerDate(String crawlerDate) {
        this.crawlerDate = crawlerDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public int getHaveVideo() {
        return haveVideo;
    }

    public void setHaveVideo(int haveVideo) {
        this.haveVideo = haveVideo;
    }
}
