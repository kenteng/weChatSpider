package com.ifeng.weChatSpider.Dao.impl;

import com.ifeng.weChatSpider.Bean.OtherArticle;
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
 * OtherArticleDaoImpl.java
 * Created by zhusy on 2017/4/28 0028 9:52
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
@Repository
public class OtherArticleDaoImpl implements AbsDao {
    private static final String DATEBASE_NAME = "spider";
    private static final String COLLECTION_NAME = "OtherAriticle";
    @Resource(name = "WeChatMongoClient")
    private MongoCli mongoCli;
    @Override
    public OtherArticle selectOne(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATEBASE_NAME);
        mongoCli.getCollection(COLLECTION_NAME);
        return mongoCli.selectOne(mongoSelect,OtherArticle.class);
    }

    @Override
    public List<OtherArticle> selectList(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATEBASE_NAME);
        mongoCli.getCollection(COLLECTION_NAME);
        return mongoCli.selectList(mongoSelect,OtherArticle.class);
    }

    @Override
    public DeleteResult delete(Where where) throws Exception {
        mongoCli.changeDb(DATEBASE_NAME);
        mongoCli.getCollection(COLLECTION_NAME);
        return mongoCli.remove(where);
    }

    @Override
    public int count(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATEBASE_NAME);
        mongoCli.getCollection(COLLECTION_NAME);
        return mongoCli.count(mongoSelect);
    }

    @Override
    public void insertOne(EntyCodec en) throws Exception {
        mongoCli.changeDb(DATEBASE_NAME);
        mongoCli.getCollection(COLLECTION_NAME);
        mongoCli.insert(en);
    }

    @Override
    public void insertList(List list) throws Exception {
        mongoCli.changeDb(DATEBASE_NAME);
        mongoCli.getCollection(COLLECTION_NAME);
        mongoCli.insert(list);
    }

    @Override
    public UpdateResult update(Map map, Where where) throws Exception {
        mongoCli.changeDb(DATEBASE_NAME);
        mongoCli.getCollection(COLLECTION_NAME);
        return mongoCli.update(map,where);
    }
}
