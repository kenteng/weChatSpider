package com.ifeng.weChatSpider.Dao;

import com.ifeng.weChatSpider.Mongo.EntyCodec;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import java.util.List;
import java.util.Map;

/**
 * AbsDao.java
 * Created by zhusy on 2017/4/28 0028 9:47
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public interface AbsDao<T extends EntyCodec>{

    T selectOne(MongoSelect mongoSelect) throws Exception;

    List<T> selectList(MongoSelect mongoSelect) throws Exception;

    UpdateResult update(Map<String, Object> map, Where where) throws Exception;

    DeleteResult delete(Where where) throws Exception;

    int count(MongoSelect mongoSelect) throws Exception;

    void insertOne(T en) throws Exception;

    void insertList(List<T> list) throws Exception;

}
