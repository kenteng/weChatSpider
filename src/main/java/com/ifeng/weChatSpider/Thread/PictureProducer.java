package com.ifeng.weChatSpider.Thread;

import com.ifeng.weChatSpider.Util.DateUtil;
import com.ifeng.weChatSpider.Util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * PictureProducer.java
 * Created by zhusy on 2017/6/6 0006 16:13
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class PictureProducer implements Runnable {
    private BlockingQueue<String> queue;
    private String starturl = "http://www.dfic.cn/showTopicDetail.ic?id=%s&reqid=049068ac8f284db1&viewtype=0&perPage=60&ppnumber=&isgo=false&tenPage=1&page=%s&perPage=60";
    private int preId = 1;

    public PictureProducer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (queue.size() <= 0) {
                    List<String> urlList = new ArrayList<>();
                    for (int i = preId; i < preId + 500; i++) {
                        urlList.add("http://www.dfic.cn/showTopicDetail.ic?id=" + i + "&reqid=049068ac8f284db1&viewtype=0&perPage=60&ppnumber&isgo=false&tenPage=1&page=%s&perPage=60");
                    }
                    preId += 500;
                    queue.addAll(urlList);
                    Log.info(DateUtil.now() + "生产抓取数据：1000");
                    if (preId >= 30000) {
                        Log.info(DateUtil.now() + "所有数据生产完毕");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }
}
