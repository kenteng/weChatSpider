/**
 * TEst.java
 * Created by zhusy on 2017/3/21 0021 16:11
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider;

import com.ifeng.weChatSpider.Bean.OtherArticle;
import com.ifeng.weChatSpider.Mongo.MongoCli;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.List;

public class Test {
    /**
     * @param option 1:视频:36.110.204.91  2:微信:10.50.16.15   3:vdnstat:10.50.16.18
     * @return
     */
    public static MongoCli getMongoCli(int option) {
        List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
        ServerAddress _15 = new ServerAddress("10.50.16.15", 27017);
        ServerAddress _91 = new ServerAddress("36.110.204.91", 27017);
        ServerAddress _18 = new ServerAddress("10.50.16.18", 27021);
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        MongoCredential _15mongoCredential = MongoCredential.createCredential("spider", "spider", "123456".toCharArray());
        MongoCredential _91mongoCredential = MongoCredential.createCredential("spider", "spider", "aT4QTEThwkfDZWAEJb4B".toCharArray());
        switch (option) {
            case 1:
                credentials.add(_91mongoCredential);
                serverAddresses.add(_91);
                break;
            case 2:
                credentials.add(_15mongoCredential);
                serverAddresses.add(_15);
                break;
            case 3:
                serverAddresses.add(_18);
                return new MongoCli(serverAddresses);
            default:
                credentials.add(_91mongoCredential);
                serverAddresses.add(_91);
                break;
        }
        return new MongoCli(serverAddresses, credentials);
    }
    public static void main(String[] args) throws Exception {
        MongoCli mongoCli = getMongoCli(2);
        mongoCli.changeDb("spider");
        mongoCli.getCollection("OtherAriticle");
        MongoSelect select = new MongoSelect();
        select.where("site","iqiyi")
                .where("isFenghuang",1);
        List<OtherArticle> otherArticleList = mongoCli.selectList(select,OtherArticle.class);
        for (OtherArticle article : otherArticleList){
            System.out.println(article.getCateName() + "\t" + article.getPreUrl() + "\t" + article.getTitle() + "\t" + article.getPageUrl());
        }
    }
}
