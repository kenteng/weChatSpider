/**
 * ArticleService.java
 * Created by zhusy on 2016/11/11 0011 10:26
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Services;

import com.ifeng.weChatSpider.Bean.Article;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import java.util.List;
import java.util.Map;

public interface ArticleService {

    Article selectOne(MongoSelect mongoSelect) throws Exception;

    List<Article> selectList(MongoSelect mongoSelect) throws Exception;

    UpdateResult update(Map<String, Object> map, Where where) throws Exception;

    DeleteResult delete(Where where) throws Exception;

    int count(MongoSelect mongoSelect) throws Exception;

    void insertOne(Article article) throws Exception;

    void insertList(List<Article> articleList) throws Exception;

}
