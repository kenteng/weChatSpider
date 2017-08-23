package com.ifeng.weChatSpider.WeChatTest.login;

import com.ifeng.weChatSpider.Bean.WeChat;
import com.ifeng.weChatSpider.Mongo.MongoCli;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.OrderByDirection;
import com.ifeng.weChatSpider.Mongo.WhereType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by zhanggq on 2017/8/23.
 */
public class GetWeChatInfos {
    protected static BlockingQueue<WeChat> weixinInfos = new LinkedBlockingDeque<>();
    protected static List<String> status = Arrays.asList(new String[]{"-1", "9"});

    static {
        Login login = new Login();
        MongoCli mongoCli = login.getWeChatMongoCli();
        MongoSelect select = new MongoSelect();
        select.where("createTime", WhereType.GreaterAndEqual, "2017-01-01 00:00:00")
                .where("biz", WhereType.Like, "\\w+")
                .where("runStatus", "1")
                .where("status", WhereType.NotIn, status)
                .orderBy("spider_date", OrderByDirection.ASC);
        try {
            List<WeChat> list = mongoCli.selectList(select, WeChat.class);
            for (int i = 0; i < list.size(); i++){
                weixinInfos.put(list.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reGet(){
        Login login = new Login();
        MongoCli mongoCli = login.getWeChatMongoCli();
        MongoSelect select = new MongoSelect();
        select.where("createTime", WhereType.GreaterAndEqual, "2017-01-01 00:00:00")
                .where("biz", WhereType.Like, "\\w+")
                .where("runStatus", "1")
                .where("status", WhereType.NotIn, status)
                .orderBy("spider_date", OrderByDirection.ASC);
        try {
            List<WeChat> list = mongoCli.selectList(select, WeChat.class);
            for (int i = 0; i < list.size(); i++){
                weixinInfos.put(list.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
