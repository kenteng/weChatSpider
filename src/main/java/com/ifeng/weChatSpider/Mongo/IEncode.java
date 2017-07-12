package com.ifeng.weChatSpider.Mongo;

import org.bson.Document;

/**
 * Created by zhanglr on 2016/2/24.
 */
public interface IEncode {
    public <T> Document encode();
}
