/**
 * ArticleDaoImpl.java
 * Created by zhusy on 2016/11/11 0011 9:50
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Dao.impl;

import com.ifeng.weChatSpider.Bean.Article;
import com.ifeng.weChatSpider.Dao.ArticleDao;
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
public class ArticleDaoImpl implements ArticleDao {
    public static final String COLLECTION = "Article";
    public static final String DATABASE =  "spider";
    @Resource(name = "WeChatMongoClient")
    private MongoCli mongoCli;

    @Override
    public Article selectOne(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectOne(mongoSelect,Article.class);
    }

    @Override
    public List<Article> selectList(MongoSelect mongoSelect) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.selectList(mongoSelect,Article.class);
    }

    @Override
    public UpdateResult update(Map<String, Object> map, Where where) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        return mongoCli.update(map, where);
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
    public void insertOne(Article article) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(article);
    }

    @Override
    public void insertList(List<Article> articleList) throws Exception {
        mongoCli.changeDb(DATABASE);
        mongoCli.getCollection(COLLECTION);
        mongoCli.insert(articleList);
    }
}
