/**
 * ArticleListService.java
 * Created by zhusy on 2016/11/11 0011 10:26
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Services;

import com.ifeng.weChatSpider.Bean.ArticleList;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import java.util.List;
import java.util.Map;

public interface ArticleListService {

    ArticleList selectOne(MongoSelect mongoSelect) throws Exception;

    List<ArticleList> selectList(MongoSelect mongoSelect) throws Exception;

    UpdateResult update(Map<String, Object> map, Where where) throws Exception;

    DeleteResult delete(Where where) throws Exception;

    int count(MongoSelect mongoSelect) throws Exception;

    void insertOne(ArticleList article) throws Exception;

    void insertList(List<ArticleList> articleList) throws Exception;

    int flush(List<ArticleList> articleList) throws Exception;
}
