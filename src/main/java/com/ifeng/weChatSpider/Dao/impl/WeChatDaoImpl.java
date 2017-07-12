/**
 * WeChatDaoImpl.java
 * Created by zhusy on 2016/11/11 0011 10:00
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Dao.impl;

import com.ifeng.weChatSpider.Bean.WeChat;
import com.ifeng.weChatSpider.Dao.WeChatDao;
import com.ifeng.weChatSpider.Mongo.MongoCli;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class WeChatDaoImpl implements WeChatDao {

    public static final String COLLECTION = "Weixin";
    public static final String DATABASE =  "spider";
    @Resource(name = "WeChatMongoClient")
    private MongoCli mongoCli;

    @Override
    public WeChat selectOne(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectOne(mongoSelect,WeChat.class);
    }

    @Override
    public List<WeChat> selectList(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectList(mongoSelect,WeChat.class);
    }

    @Override
    public UpdateResult update(Map<String, Object> map, Where where) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.update(map,where);
    }

    @Override
    public DeleteResult delete(Where where) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.remove(where);
    }

    @Override
    public int count(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.count(mongoSelect);
    }

    @Override
    public void insertOne(WeChat weChat) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(weChat);
    }

    @Override
    public void insertList(List<WeChat> weChatList) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(weChatList);
    }
}
