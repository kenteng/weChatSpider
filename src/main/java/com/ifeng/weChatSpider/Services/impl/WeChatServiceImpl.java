/**
 * WeChatServiceImpl.java
 * Created by zhusy on 2016/11/11 0011 10:31
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Services.impl;

import com.ifeng.weChatSpider.Bean.WeChat;
import com.ifeng.weChatSpider.Dao.WeChatDao;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.OrderByDirection;
import com.ifeng.weChatSpider.Mongo.Where;
import com.ifeng.weChatSpider.Mongo.WhereType;
import com.ifeng.weChatSpider.Services.WeChatService;
import com.ifeng.weChatSpider.Util.DateUtil;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(value = "weChatService")
public class WeChatServiceImpl implements WeChatService {

    private List<String> status = Arrays.asList(new String[]{"-1", "9"});
    private List<String> nullList = Arrays.asList(new String[]{null, ""});

    @Autowired
    private WeChatDao weChatDao;

    @Override
    public WeChat selectOne(MongoSelect mongoSelect) throws Exception {
        return weChatDao.selectOne(mongoSelect);
    }

    @Override
    public List<WeChat> selectList(MongoSelect mongoSelect) throws Exception {
        return weChatDao.selectList(mongoSelect);
    }

    @Override
    public UpdateResult update(Map<String, Object> map, Where where) throws Exception {
        return weChatDao.update(map, where);
    }

    @Override
    public DeleteResult delete(Where where) throws Exception {
        return weChatDao.delete(where);
    }

    @Override
    public int count(MongoSelect mongoSelect) throws Exception {
        return weChatDao.count(mongoSelect);
    }

    @Override
    public void insertOne(WeChat weChat) throws Exception {
        weChatDao.insertOne(weChat);
    }

    @Override
    public void insertList(List<WeChat> weChatList) throws Exception {
        weChatDao.insertList(weChatList);
    }

    @Override
    public List<WeChat> selectSpiderList(int size) throws Exception {
//        if (WeChatController.shouldSpider()) {
            MongoSelect select = new MongoSelect();
            select.where("createTime", WhereType.GreaterAndEqual, "2017-01-01 00:00:00")
                    .where("biz", WhereType.Like, "\\w+")
                    .where("runStatus", "1")
//                    .where("nextPubTime", WhereType.In, nullList)
                    .where("status", WhereType.NotIn, status)
                    .orderBy("spider_date", OrderByDirection.ASC)
                    .page(1, size);
            List<WeChat> weChats = weChatDao.selectList(select);
//        for (WeChat weChat : weChats) {
//            if (weChat.getLastNum() > 0 && DateUtil.parseDateTime(weChat.getLastSucTime()).getTime() > (System.currentTimeMillis() - 3 * 60 * 60 * 1000)) {
//                weChats.remove(weChat);
//            }
//        }
            return weChats;
//        }
//        return new ArrayList<>();
    }

    @Override
    public void resetStatus() throws Exception {
        Map<String, Object> map = new HashedMap();
        map.put("status", "1");
        Where where = new Where("biz", WhereType.Like, "\\w+");
        where.and("status", WhereType.In, status);
        weChatDao.update(map, where);
    }

    @Override
    public List<WeChat> selectUpdateList() throws Exception{
        MongoSelect select = new MongoSelect();
        select.where("createTime", WhereType.GreaterAndEqual, "2017-01-01 00:00:00")
                .where("biz", WhereType.Like, "\\w+")
                .where("runStatus", "1")
                .where("status", WhereType.NotIn, status)
                .where("articleUpdateTime", WhereType.GreaterAndEqual, DateUtil.formatDateTime(System.currentTimeMillis() - 60 * 60 * 1000))
                .where("spider_date", WhereType.LessAndEqual, DateUtil.formatDateTime(System.currentTimeMillis() - 60 * 60 * 1000))
                .orderBy("articleUpdateTime", OrderByDirection.ASC)
                .orderBy("spider_date", OrderByDirection.ASC)
                .page(1, 100);
        List<WeChat> weChats = weChatDao.selectList(select);
        return weChats;
    }

    @Override
    public List<WeChat> selectPubpiderList(int size) throws Exception {
        MongoSelect select = new MongoSelect();
        select.where("createTime", WhereType.GreaterAndEqual, "2017-01-01 00:00:00")
                .where("biz", WhereType.Like, "\\w+")
                .where("runStatus", "1")
                .where("status", WhereType.NotIn, status)
                .where("nextPubTime", WhereType.LessAndEqual, DateUtil.formatDateTime(System.currentTimeMillis() - 60 * 60 * 1000))
                .orderBy("nextPubTime", OrderByDirection.ASC)
                .orderBy("spider_date", OrderByDirection.ASC)
                .page(1, size);
        List<WeChat> weChats = weChatDao.selectList(select);
        return weChats;
    }
}
