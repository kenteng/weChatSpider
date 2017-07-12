package com.ifeng.weChatSpider.Thread;

import com.ifeng.weChatSpider.Bean.PictureListEntites;
import com.ifeng.weChatSpider.Dao.AbsDao;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.OrderByDirection;
import com.ifeng.weChatSpider.Util.DateUtil;
import com.ifeng.weChatSpider.Util.Log;

import java.util.concurrent.BlockingQueue;

/**
 * PictureDownloadProducer.java
 * Created by zhusy on 2017/6/6 0006 19:37
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class PictureDownloadProducer implements Runnable {
    private AbsDao<PictureListEntites> pictureListDaoImpl;
    private BlockingQueue<PictureListEntites> queue;

    public PictureDownloadProducer(AbsDao<PictureListEntites> pictureListDaoImpl, BlockingQueue<PictureListEntites> queue) {
        this.pictureListDaoImpl = pictureListDaoImpl;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (queue.size() <= 0) {
                    MongoSelect mongoSelect = new MongoSelect();
                    mongoSelect.where("status",0)
                            .orderBy("createTime", OrderByDirection.ASC)
                            .page(0,1000);
                    queue.addAll(pictureListDaoImpl.selectList(mongoSelect));
                    Log.info(DateUtil.now() + "生产抓取数据：1000");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
