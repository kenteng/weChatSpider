package com.ifeng.weChatSpider.WeChatTest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.ifeng.weChatSpider.Bean.Article;
import com.ifeng.weChatSpider.Bean.ArticleList;
import com.ifeng.weChatSpider.Bean.WeChat;
import com.ifeng.weChatSpider.Mongo.*;
import com.ifeng.weChatSpider.Util.DateUtil;
import com.ifeng.weChatSpider.Util.Log;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.collections.map.HashedMap;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * main.java
 * Created by zhusy on 2017/7/20 0020 15:15
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class main {
    private static final long FIVEDAYMIL = 5 * 24 * 3600 * 1000;
    public static List<String> status = Arrays.asList(new String[]{"-1", "9"});
    public static String url = "https://mp.weixin.qq.com/cgi-bin/appmsg?token=1147500981&lang=zh_CN&f=json&ajax=1&random=0.0932841953962531&action=list_ex&begin=0&count=5&query=&fakeid=%s&type=9";

    public static void main(String[] args) throws Exception {
        getWechat();
    }

    public static void getWechat() throws Exception {
        int id = 0;
        int totalCount = 0;
        MongoCli mongoCli = getWeChatMongoCli();
        MongoCli weChatMongoCli = getWeChatMongoCli();
        MongoCli articleCli = getWeChatMongoCli();
        articleCli.changeDb("spider");
        articleCli.getCollection("Article");
        weChatMongoCli.changeDb("spider");
        weChatMongoCli.getCollection("ArticleListNew");
        mongoCli.changeDb("spider");
        mongoCli.getCollection("Weixin");
        System.out.println("--------任务启动---------" + DateUtil.now());
        MongoSelect select = new MongoSelect();
        select.where("createTime", WhereType.GreaterAndEqual, "2017-01-01 00:00:00")
                .where("biz", WhereType.Like, "\\w+")
                .where("runStatus", "1")
                .where("status", WhereType.NotIn, status)
                .orderBy("spider_date", OrderByDirection.ASC);
        while (true) {
            List<WeChat> weixinInfos = mongoCli.selectList(select, WeChat.class);
            for (WeChat weixinInfo : weixinInfos) {
                String reslut = downloader(String.format(url, URLEncoder.encode(weixinInfo.getBiz(), "UTF-8")));
                JSONObject jsonObject = JSONObject.parseObject(reslut);
                try {
                    JSONArray list = jsonObject.getJSONArray("app_msg_list");
                    List<ArticleList> articleLists = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        JSONObject ar = list.getJSONObject(i);
                        ArticleList article = new ArticleList();
                        article.setAccount(weixinInfo.getAccount());
                        article.setUrl(ar.getString("link"));
                        article.setCreateTime(DateUtil.now());
                        article.setCreate_Time(DateUtil.formatDateTime(new Date(ar.getLong("update_time") * 1000)));
                        article.setCateid(weixinInfo.getCateId());
                        article.setCatename(weixinInfo.getCatename());
                        article.setCover(ar.getString("cover"));
                        article.setTitle(ar.getString("title").replaceAll("&quot;", "\"")
                                .replaceAll("&amp;", "&")
                                .replaceAll("&yen;", "¥")
                                .replaceAll("&nbsp;", " ")
                                .replaceAll("\\u00A0", " ").trim());
                        article.setStatus(0);
                        article.setFrom("official");
                        article.setWechat_name(weixinInfo.getWeChatName());
                        if (!"".equals(article.getTitle()) && !"".equals(article.getUrl()) && System.currentTimeMillis() - (ar.getLong("update_time") * 1000) <= FIVEDAYMIL) {
                            articleLists.add(article);
                        }
                    }
                    int count = 0;
                    for (ArticleList article : articleLists) {
                        MongoSelect selectATemp = new MongoSelect();
                        selectATemp.where("title", article.getTitle())
                                .where("cateid", article.getCateid());
                        Article temp = articleCli.selectOne(selectATemp, Article.class);
                        if (temp == null) {
                            count++;
                            Log.info("入库:" + new Gson().toJson(article));
                            weChatMongoCli.insert(article);
                        }
                    }
                    Map<String, Object> map = new HashedMap();
                    map.put("spider_date", DateUtil.now());//更新最新抓取时间
                    map.put("lastNum", count);//更新本次抓取是否有入库文章
                    Where where = new Where("biz", weixinInfo.getBiz());
                    mongoCli.update(map, where);
                    System.out.println((++id) + "--------账号：" + weixinInfo.getAccount() + " 入库" + count + "篇文章，共抓取" + articleLists.size() + "篇---------" + DateUtil.now());
//                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println((++id) + "--------账号：" + weixinInfo.getAccount() + " 失败" + DateUtil.now());
                }
            }
            id = 0;
            System.out.println("--------第" + (++totalCount) + "轮结束---------" + DateUtil.now());
        }
    }

    public static String downloader(String url) {
        Log.info("downloading..." + url);
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setStaleConnectionCheckEnabled(true)
                .build();
        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
//        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
//        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
            httpGet.addHeader("accept-encoding", "gzip, deflate, sdch, br");
            httpGet.addHeader("accept-language", "zh-CN,zh;q=0.8");
            httpGet.addHeader("cache-control", "no-cache");
            httpGet.addHeader("connection", "keep-alive");
            httpGet.addHeader("cookie", "noticeLoginFlag=1; remember_acct=Lemon1_bear2; RK=qfFDKVaaYD; cuid=2698805126; pgv_pvi=8006946816; pgv_si=s3259430912; cert=NmMcPPXvIXtLoP0NFFycNUKeALIEnl6J; pgv_info=ssid=s9627254497; pgv_pvid=9048190112; noticeLoginFlag=1; remember_acct=Lemon1_bear2; ptisp=cnc; ptcz=aafdf806228ea41ca6bef22902db79768b69194ad02ac8d49d2785c71691de5c; pt2gguin=o0624791977; uin=o0624791977; skey=@iIqk1387c; uuid=cb481fba70bb76c50aa4d408a569f086; ticket=19727fb5d81f477cf1bf16c671c20c439df3cfbe; ticket_id=gh_fab4a86ccb47; data_bizuin=3011401038; data_ticket=1YOb/8/sZbdBGGjLEjzHx+jwzzoUOOZ69POE7H6vbmAyxXk7tH4KMQaLF2QbU//o; ua_id=tuV4ynNzaptEWB7CAAAAAHcdmdTK1_rSlqw9aenryhc=; xid=a0051147471d48489df7d7859fad89fb; openid2ticket_oTlV-s6ivfTOfZ32jfYi0r53m0zA=PJsjA8KYzsgEAWN3gD8UpQE4ai27R/1/JatwUVmq38U=; slave_user=gh_fab4a86ccb47; slave_sid=Y3NJbGdfbjVaS2Z1V3hhQzBwU0tudFFXakx4NkxFbk1XSXdxTFRKMG0wblo1Q2xrdk5JNXBCM2xqQlNyQ2l4MkkwSlF2Z05fMFNsY25uRTkyel9wZkF3WGpLSEN1R1I5R2JQZzkxY3IzS09TRFVkMTZrSER2eXJjUjBKU2xUYnFkZE00Yno3dTZsVHdHd0xl; bizuin=3092580982");
            httpGet.addHeader("host", "mp.weixin.qq.com");
            httpGet.addHeader("pragma", "no-cache");
            httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.3176.400 QQBrowser/9.6.11520.400");
            httpGet.addHeader("x-requested-with", "XMLHttpRequest");
            CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                Log.info("downloaded..." + url);
                return EntityUtils.toString(httpEntity, "UTF-8");
            } else {
            }
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeableHttpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static MongoCli getWeChatMongoCli() {
        List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
        ServerAddress s = new ServerAddress("10.50.16.15", 27017);
        serverAddresses.add(s);
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        MongoCredential mongoCredential = MongoCredential.createCredential("spider", "spider", "aT4QTEThwkfDZWAEJb4B".toCharArray());
        credentials.add(mongoCredential);
        return new MongoCli(serverAddresses,credentials);
    }
}
