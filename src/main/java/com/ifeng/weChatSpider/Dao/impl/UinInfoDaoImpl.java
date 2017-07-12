package com.ifeng.weChatSpider.Dao.impl;

import com.ifeng.weChatSpider.Bean.UinInfo;
import com.ifeng.weChatSpider.Dao.UinInfoDao;
import com.ifeng.weChatSpider.Mongo.MongoCli;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * UinInfoDaoImpl.java
 * Created by zhusy on 2017/4/13 0013 11:07
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
@Repository
public class UinInfoDaoImpl implements UinInfoDao {
    public static final String COLLECTION = "UinInfo";
    public static final String DATABASE =  "spider";
    @Resource(name = "WeChatMongoClient")
    private MongoCli mongoCli;

    @Override
    public UinInfo selectOne(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectOne(mongoSelect,UinInfo.class);
    }

    @Override
    public List<UinInfo> selectList(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectList(mongoSelect,UinInfo.class);
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
    public void insertOne(UinInfo uinInfo) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(uinInfo);
    }

    @Override
    public void insertList(List<UinInfo> uinInfoList) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(uinInfoList);
    }
}
