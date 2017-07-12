package com.ifeng.weChatSpider.Bean;

import com.ifeng.weChatSpider.Mongo.DataLoader;
import com.ifeng.weChatSpider.Mongo.EntyCodec;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Created by duanyb on 2017/2/16.
 */
public class ArticleList extends EntyCodec {
    private Object _id;
    private int cateid;
    private String wechat_name;
    private String catename;
    private String account;
    private String url;
    private String createTime;
    private String create_Time; //文章得时间
    private int status;  //0 正常
    private String latestTime;
    private String title;
    private String cover;
    private String from;

    public ArticleList() {
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(String latestTime) {
        this.latestTime = latestTime;
    }

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public int getCateid() {
        return cateid;
    }

    public void setCateid(int cateid) {
        this.cateid = cateid;
    }

    public String getWechat_name() {
        return wechat_name;
    }

    public void setWechat_name(String wechat_name) {
        this.wechat_name = wechat_name;
    }

    public String getCatename() {
        return catename;
    }

    public void setCatename(String catename) {
        this.catename = catename;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void decode(DataLoader loader) {
        this.cateid = loader.getInt("cateid");
        this.wechat_name = loader.getString("wechat_name");
        this.catename = loader.getString("catename");
        this.account = loader.getString("account");
        this.url = loader.getString("url");
        this.status = loader.getInt("status");
        this.createTime = loader.getString("createTime");
        this.latestTime = loader.getString("latestTime");
        this.title = loader.getString("title");
        this.create_Time = loader.getString("create_Time");
        this.cover = loader.getString("cover");
        this.from = loader.getString("from");
        this._id = (ObjectId) loader.getObject("_id");
    }

    @Override
    public <T> Document encode() {
        Document en = new Document();
        en.put("cateid", this.cateid);
        en.put("wechat_name", this.wechat_name);
        en.put("catename", this.catename);
        en.put("account", this.account);
        en.put("url", this.url);
        en.put("status", this.status);
        en.put("createTime", this.createTime);
        en.put("latestTime", this.latestTime);
        en.put("create_Time", this.create_Time);
        en.put("cover", this.cover);
        en.put("title", this.title);
        en.put("from", this.from);
        return en;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreate_Time() {
        return create_Time;
    }

    public void setCreate_Time(String create_Time) {
        this.create_Time = create_Time;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}