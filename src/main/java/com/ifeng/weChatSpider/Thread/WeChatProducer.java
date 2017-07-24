/**
 * WeChatProducer.java
 * Created by zhusy on 2017/3/8 0008 11:21
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Thread;

import com.ifeng.weChatSpider.Bean.WeChat;
import com.ifeng.weChatSpider.Services.WeChatService;
import com.ifeng.weChatSpider.Util.DateUtil;
import com.ifeng.weChatSpider.Util.Log;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WeChatProducer implements Runnable {

    private WeChatService weChatService;

    private static int totalSize = 200;

    BlockingQueue<WeChat> queue;

    public WeChatProducer(BlockingQueue<WeChat> queue, WeChatService weChatService) {
        this.queue = queue;
        this.weChatService = weChatService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (queue.size() <= 0) {
                    List<WeChat> weChatList = weChatService.selectUpdateList();
                    int size = weChatList.size();
                    if (size < totalSize) {
                        weChatList.addAll(weChatService.selectPubpiderList(totalSize - size));
                        size = weChatList.size();
                        if(size < totalSize) {
                            weChatList.addAll(weChatService.selectSpiderList(totalSize - size));
                        }
                    }
                    queue.addAll(weChatList);
                    Log.info(DateUtil.now() + "生产WeChat数据:规律：" + size + "总数：" + weChatList.size());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
