package com.ifeng.weChatSpider.Thread;

import com.ifeng.weChatSpider.Bean.UinInfo;
import com.ifeng.weChatSpider.Services.UinInfoService;
import com.ifeng.weChatSpider.Util.DateUtil;
import com.ifeng.weChatSpider.Util.Log;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * UinInfoProducer.java
 * Created by zhusy on 2017/4/13 0013 13:28
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class UinInfoProducer implements Runnable {

    private UinInfoService uinInfoService;

    BlockingQueue<UinInfo> queue;

    public UinInfoProducer(BlockingQueue<UinInfo> queue, UinInfoService uinInfoService) {
        this.uinInfoService = uinInfoService;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (queue.size() <= 0) {
                    List<UinInfo> weChatList = uinInfoService.selectAvailableList();
                    queue.addAll(weChatList);
                    Log.info(DateUtil.now() + "生产UIN数据");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
