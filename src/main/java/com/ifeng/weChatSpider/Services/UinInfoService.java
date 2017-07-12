package com.ifeng.weChatSpider.Services;

import com.ifeng.weChatSpider.Bean.UinInfo;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import java.util.List;
import java.util.Map;

/**
 * UinInfoService.java
 * Created by zhusy on 2017/4/13 0013 11:13
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public interface UinInfoService {
    UinInfo selectOne(MongoSelect mongoSelect) throws Exception;

    List<UinInfo> selectList(MongoSelect mongoSelect) throws Exception;

    UpdateResult update(Map<String, Object> map, Where where) throws Exception;

    DeleteResult delete(Where where) throws Exception;

    int count(MongoSelect mongoSelect) throws Exception;

    void insertOne(UinInfo uinInfo) throws Exception;

    void insertList(List<UinInfo> uinInfoList) throws Exception;

    List<UinInfo> selectAvailableList() throws Exception;
}
