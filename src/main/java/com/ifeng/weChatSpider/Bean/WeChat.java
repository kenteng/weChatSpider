/*
* WeChat.java
* Created on  202016/11/9 0009 15:38
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved
*/
package com.ifeng.weChatSpider.Bean;

import com.ifeng.weChatSpider.Mongo.DataLoader;
import com.ifeng.weChatSpider.Mongo.EntyCodec;
import org.bson.Document;

import java.util.List;

/**
 * Created by zhusy on 2016/11/9 0009.
 */
public class WeChat extends EntyCodec {
    private Object _id;
    /*** 栏目ID */
    private int cateId;
    /*** 栏目ID */
    private String openid;
    /*** 微信名 */
    private String weChatName;
    /*** 栏目名称 */
    private String catename;
    /*** 微信号 */
    private String account;
    /*** 个人页 */
    private String profileUrl;
    /*** 抓取时间 */
    private String spiderDate;
    /*** 是否有效 */
    private int isExpired;
    /*** 优先级 */
    private int priority;
    /*** 原因 */
    private String reason;
    /*** 状态 */
    private String status;
    /*** 是否热点 */
    private String isHot;
    /*** 是否热点 */
    private String creater;
    private int count;
    private int delayedCount;
    private int update_flag;
    private String update;
    private String runStatus;
    private String resourceId;
    private String createTime;
    private int yesterdayCount;
    private int todayCount;
    private String biz;
    private int lastNum;
    private String lastSucTime;
    private String nextPubTime;
    private List<String> pubTimes;
    private int pubTimesIndex;

    @Override
    public Document encode() {
        Document result = new Document();
        result.put("cateid",this.cateId);
        result.put("openid",this.openid);
        result.put("wechat_name",this.weChatName);
        result.put("catename",this.catename);
        result.put("account",this.account);
        result.put("profile_url",this.profileUrl);
//        if(this.spiderDate == null || "".equals(spiderDate)){
//            result.put("spider_date",null);
//        } else {
//            result.put("spider_date",DateUtil.parseDateTime(spiderDate).getTime());
//        }
        result.put("spider_date",this.spiderDate);
        result.put("is_expired",this.isExpired);
        result.put("priority",this.priority);
        result.put("reason",this.reason);
        result.put("status",this.status);
        result.put("is_hot",this.isHot);
        result.put("creater",this.creater);
        result.put("runStatus",this.runStatus);
        result.put("resourceId",this.resourceId);
        result.put("update_flag",this.update_flag);
        result.put("createTime",this.createTime);
        result.put("lastNum",this.lastNum);
        result.put("lastSucTime",this.lastSucTime);
        result.put("nextPubTime",this.nextPubTime);
        result.put("pubTimes",this.pubTimes);
        result.put("pubTimesIndex",this.pubTimesIndex);
        return result;
    }

    @Override
    public void decode(DataLoader loader) {
        this._id = loader.getObject("_id");
        this.cateId = loader.getInt("cateid");
        this.openid = loader.getString("openid");
        this.weChatName = loader.getString("wechat_name");
        this.catename = loader.getString("catename");
        this.account = loader.getString("account");
        this.profileUrl = loader.getString("profile_url");
        this.update = loader.getString("update");
//        if (loader.getString("spider_date") == null) {
//            this.spiderDate = null;
//        }else {
//            try {
//                this.spiderDate = DateUtil.formatDateTime(Long.parseLong(loader.getString("spider_date")));
//            } catch (Exception e){
//                this.spiderDate = null;
//            }
//        }
        this.spiderDate = loader.getString("spider_date");
        this.isExpired = loader.getInt("is_expired");
        this.priority = loader.getInt("priority");
        this.reason = loader.getString("reason");
        this.status = loader.getString("status");
        this.isHot = loader.getString("is_hot");
        this.creater = loader.getString("creater");
        this.update_flag = loader.getInt("update_flag");
        this.runStatus = loader.getString("runStatus");
        this.resourceId = loader.getString("resourceId");
        this.createTime = loader.getString("createTime");
        this.biz = loader.getString("biz");
        this.lastNum = loader.getInt("lastNum");
        this.lastSucTime = loader.getString("lastSucTime");
        this.nextPubTime = loader.getString("nextPubTime");
        this.pubTimes = (List<String>) loader.getObject("pubTimes");
        this.pubTimesIndex = loader.getInt("pubTimesIndex");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"cateid\":\"").append(this.cateId).append("\",")
                .append("\"_id\":\"").append(this._id).append("\",")
                .append("\"openid\":\"").append(this.openid).append("\",")
                .append("\"wechat_name\":\"").append(this.weChatName).append("\",")
                .append("\"catename\":\"").append(this.catename).append("\",")
                .append("\"account\":\"").append(this.account).append("\",")
                .append("\"profile_url\":\"").append(this.profileUrl).append("\",")
                .append("\"spider_date\":\"").append(this.spiderDate).append("\",")
                .append("\"expired\":\"").append(this.isExpired).append("\",")
                .append("\"priority\":\"").append(this.priority).append("\",")
                .append("\"reason\":\"").append(this.reason).append("\",")
                .append("\"is_hot\":\"").append(this.isHot).append("\",")
                .append("\"count\":\"").append(this.count).append("\",")
                .append("\"update_flag\":\"").append(this.update_flag).append("\",")
                .append("\"update\":\"").append(this.update).append("\",")
                .append("\"creater\":\"").append(this.creater).append("\",")
                .append("\"runStatus\":\"").append(this.runStatus).append("\",")
                .append("\"resourceId\":\"").append(this.resourceId).append("\",")
                .append("\"createTime\":\"").append(this.createTime).append("\",")
                .append("\"yesterdayCount\":\"").append(this.yesterdayCount).append("\",")
                .append("\"todayCount\":\"").append(this.todayCount).append("\",")
                .append("\"status\":\"").append(this.status).append("\"}");
        return sb.toString();
    }

    public String getCreater() {
        return creater;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }

    public int getUpdate_flag() {
        return update_flag;
    }

    public void setUpdate_flag(int update_flag) {
        this.update_flag = update_flag;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public int getDelayedCount() {
        return delayedCount;
    }

    public void setDelayedCount(int delayedCount) {
        this.delayedCount = delayedCount;
    }

    public String getIsHot() {
        return isHot;
    }

    public void setIsHot(String isHot) {
        this.isHot = isHot;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCateId() {
        return cateId;
    }

    public void setCateId(int cateId) {
        this.cateId = cateId;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getWeChatName() {
        return weChatName;
    }

    public void setWeChatName(String weChatName) {
        this.weChatName = weChatName;
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

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getSpiderDate() {
        return spiderDate;
    }

    public void setSpiderDate(String spiderDate) {
        this.spiderDate = spiderDate;
    }

    public int getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(int isExpired) {
        this.isExpired = isExpired;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getYesterdayCount() {
        return yesterdayCount;
    }

    public void setYesterdayCount(int yesterdayCount) {
        this.yesterdayCount = yesterdayCount;
    }

    public int getTodayCount() {
        return todayCount;
    }

    public void setTodayCount(int todayCount) {
        this.todayCount = todayCount;
    }

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public int getLastNum() {
        return lastNum;
    }

    public void setLastNum(int lastNum) {
        this.lastNum = lastNum;
    }

    public String getLastSucTime() {
        return lastSucTime;
    }

    public void setLastSucTime(String lastSucTime) {
        this.lastSucTime = lastSucTime;
    }

    public String getNextPubTime() {
        return nextPubTime;
    }

    public void setNextPubTime(String nextPubTime) {
        this.nextPubTime = nextPubTime;
    }

    public List<String> getPubTimes() {
        return pubTimes;
    }

    public void setPubTimes(List<String> pubTimes) {
        this.pubTimes = pubTimes;
    }

    public int getPubTimesIndex() {
        return pubTimesIndex;
    }

    public void setPubTimesIndex(int pubTimesIndex) {
        this.pubTimesIndex = pubTimesIndex;
    }
}
