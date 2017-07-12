/**
 * ArticleServiecImpl.java
 * Created by zhusy on 2016/11/11 0011 10:26
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Services.impl;

import com.ifeng.weChatSpider.Bean.Article;
import com.ifeng.weChatSpider.Dao.ArticleDao;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.ifeng.weChatSpider.Services.ArticleService;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ArticleServiceImpl implements ArticleService{

    @Autowired
    private ArticleDao articleDao;

    @Override
    public Article selectOne(MongoSelect mongoSelect) throws Exception {
        return articleDao.selectOne(mongoSelect);
    }

    @Override
    public List<Article> selectList(MongoSelect mongoSelect) throws Exception {
        return articleDao.selectList(mongoSelect);
    }

    @Override
    public UpdateResult update(Map<String, Object> map, Where where) throws Exception {
        return articleDao.update(map, where);
    }

    @Override
    public DeleteResult delete(Where where) throws Exception {
        return articleDao.delete(where);
    }

    @Override
    public int count(MongoSelect mongoSelect) throws Exception {
        return articleDao.count(mongoSelect);
    }

    @Override
    public void insertOne(Article article) throws Exception {
        articleDao.insertOne(article);
    }

    @Override
    public void insertList(List<Article> articleList) throws Exception {
        articleDao.insertList(articleList);
    }
}
