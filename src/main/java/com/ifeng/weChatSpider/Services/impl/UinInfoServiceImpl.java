package com.ifeng.weChatSpider.Services.impl;

import com.ifeng.weChatSpider.Bean.UinInfo;
import com.ifeng.weChatSpider.Dao.UinInfoDao;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.OrderByDirection;
import com.ifeng.weChatSpider.Mongo.Where;
import com.ifeng.weChatSpider.Services.UinInfoService;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * UinInfoServiceImpl.java
 * Created by zhusy on 2017/4/13 0013 11:14
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
@Repository
public class UinInfoServiceImpl implements UinInfoService {

    @Autowired
    private UinInfoDao uinInfoDao;

    @Override
    public UinInfo selectOne(MongoSelect mongoSelect) throws Exception {
        return uinInfoDao.selectOne(mongoSelect);
    }

    @Override
    public List<UinInfo> selectList(MongoSelect mongoSelect) throws Exception {
        return uinInfoDao.selectList(mongoSelect);
    }

    @Override
    public UpdateResult update(Map<String, Object> map, Where where) throws Exception {
        return uinInfoDao.update(map, where);
    }

    @Override
    public DeleteResult delete(Where where) throws Exception {
        return uinInfoDao.delete(where);
    }

    @Override
    public int count(MongoSelect mongoSelect) throws Exception {
        return uinInfoDao.count(mongoSelect);
    }

    @Override
    public void insertOne(UinInfo uinInfo) throws Exception {
        uinInfoDao.insertOne(uinInfo);
    }

    @Override
    public void insertList(List<UinInfo> uinInfoList) throws Exception {
        uinInfoDao.insertList(uinInfoList);
    }

    @Override
    public List<UinInfo> selectAvailableList() throws Exception {
        MongoSelect select = new MongoSelect();
        select.where("status",1);
        select.orderBy("createTime", OrderByDirection.ASC);
        select.page(1,100);
        return uinInfoDao.selectList(select);
    }
}
