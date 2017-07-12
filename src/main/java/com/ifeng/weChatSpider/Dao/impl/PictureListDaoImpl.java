package com.ifeng.weChatSpider.Dao.impl;

import com.ifeng.weChatSpider.Bean.PictureListEntites;
import com.ifeng.weChatSpider.Dao.AbsDao;
import com.ifeng.weChatSpider.Mongo.EntyCodec;
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
 * PictureListDaoImpl.java
 * Created by zhusy on 2017/6/6 0006 17:08
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
@Repository
public class PictureListDaoImpl implements AbsDao {
    public static final String COLLECTION = "PictureList";
    public static final String DATABASE =  "spider";
    @Resource(name = "mongoClient")
    private MongoCli mongoCli;

    @Override
    public PictureListEntites selectOne(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectOne(mongoSelect,PictureListEntites.class);
    }

    @Override
    public List<PictureListEntites> selectList(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectList(mongoSelect,PictureListEntites.class);
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
    public void insertOne(EntyCodec en) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(en);
    }

    @Override
    public void insertList(List list) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(list);
    }

    @Override
    public UpdateResult update(Map map, Where where) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.update(map,where);
    }
}
