package com.ifeng.weChatSpider.Controller;

import com.alibaba.fastjson.JSONObject;
import com.ifeng.weChatSpider.Bean.PictureListEntites;
import com.ifeng.weChatSpider.Dao.AbsDao;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.ifeng.weChatSpider.Thread.PictureDownloadProducer;
import com.ifeng.weChatSpider.Thread.PictureProducer;
import com.ifeng.weChatSpider.Util.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * PictureController.java
 * Created by zhusy on 2017/6/6 0006 16:09
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
@Controller
@RequestMapping(value = "/picture")
public class PictureController {

    private static String savePath = "/data/picture/";
    private static boolean isCookieDown = false;
    private static boolean isProxyDown = false;
    private static PictureProducer producer;
    private static PictureDownloadProducer downloadProducer;
    private static int totalCount;
    private static int sleepTime;
    private static int downloadSleepTime;
    private static int spiderThreadNum = 10;
    private static int downloadThreadNum = 20;
    private static int totalDownloadCount;
    private static String proxyIp = "123.57.20.169";
    private static int proxyPort = 80;
    private static String currentId;
    private static int currentDownloadId;
    private static boolean swich = true;
    private static boolean downloadSwitch = true;
    private Thread pictureProducerThread;
    private Thread pictureDownloadProducerThread;
    private Thread pictureSpiderThread;
    private Thread pictureDownloadThread;
    private static ExecutorService fixedSpiderThreadPool = null;
    private static ExecutorService fixedThreadPool = null;

    private static BlockingQueue<String> staticQueue = new ArrayBlockingQueue<>(500);
    private static BlockingQueue<PictureListEntites> pictureQueue = new ArrayBlockingQueue<>(1000);
    private static String cookie = "SizeperPage=24; _gat=1; _ga=GA1.2.1602527791.1496731244; _gid=GA1.2.1215278276.1496731244; NumperPage=96; ASPSESSIONIDQABRRBAB=JCOPOCNDCLPBNBOLPLBCAMCK; JSESSIONID=7DDB38E8E50D5BD470B63213B5C973CF.tomcat6669; uid=; ps=; _ga=GA1.2.1602527791.1496731244; _gid=GA1.2.1215278276.1496731244";


    @Resource(name = "pictureListDaoImpl")
    private AbsDao<PictureListEntites> pictureListDaoImpl;

    @RequestMapping(value = "/index")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/startSpider")
    @ResponseBody
    public boolean startSpider() {
        swich = true;
        if (producer == null) {
            producer = new PictureProducer(staticQueue);
        }
        if (pictureProducerThread == null) {
            pictureProducerThread = new Thread(producer);
            pictureProducerThread.start();
        }
        Thread thread = new Thread(() -> {
            while (swich) {
                try {
                    int pageSize = 10;
                    String urlTemp = staticQueue.take();
                    String id = urlTemp.substring(urlTemp.indexOf("id=") + 3, urlTemp.indexOf("&reqid"));
                    currentId = id;
                    BasicHeader[] headers = new BasicHeader[9];
                    headers[0] = new BasicHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
                    headers[1] = new BasicHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    headers[2] = new BasicHeader("accept-encoding", "gzip, deflate, sdch");
                    headers[3] = new BasicHeader("accept-language", "zh-CN,zh;q=0.8");
                    headers[4] = new BasicHeader("connection", "keep-alive");
                    headers[5] = new BasicHeader("cookie", cookie);
                    headers[6] = new BasicHeader("host", "www.dfic.cn");
                    headers[7] = new BasicHeader("upgrade-insecure-requests", "1");
                    headers[8] = new BasicHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
                    for (int i = 1; i <= pageSize; i++) {
                        String targerUrl = String.format(urlTemp, i);
                        String documentStr = downloader(targerUrl, proxyIp, headers, proxyPort);
                        if (documentStr == null || "".equals(documentStr)) {
                            isProxyDown = true;
                            Log.bug(DateUtil.now() + "NO Response:" + targerUrl);
                            continue;
                        } else if (documentStr.contains("502 Bad Gateway")) {
                            isProxyDown = true;
                            Log.bug(DateUtil.now() + "Proxy Outtime:" + targerUrl);
                            return;
                        } else if (documentStr.contains("请您先登录")) {
                            Log.bug(DateUtil.now() + "Cookie Outtime:" + targerUrl);
                            isCookieDown = true;
                            return;
                        }
                        isProxyDown = false;
                        isCookieDown = false;
                        Document document = Jsoup.parse(documentStr);
                        Elements pages = document.select(".pagediv1");
                        pageSize = pages.size();
                        Elements tables = document.select(".ShowStoryListTable3");
                        if (tables.size() == 0) {
                            Log.bug(DateUtil.now() + "NO EffectResult:" + targerUrl);
                            continue;
                        }
                        for (Element element : tables) {
                            String url = element.select("tr>td>a").get(0).attr("href");
                            if (!url.startsWith("http")) {
                                url = "http://www.dfic.cn/" + url;
                            }
                            String title = element.select("tr>td>a>img").get(0).attr("title");
                            PictureListEntites entites = new PictureListEntites();
                            entites.setId(Integer.parseInt(id));
                            entites.setUrl(url);
                            entites.setTitle(title);
                            entites.setCreateTime(DateUtil.now());
                            entites.setStatus(0);
                            if (pictureListDaoImpl.selectOne(new MongoSelect(new Where("url", url))) == null) {
                                pictureListDaoImpl.insertOne(entites);
                            }
                        }
                        Log.bug(DateUtil.now() + "Success:" + targerUrl);
                        totalCount++;
                        Thread.currentThread().sleep(sleepTime);
                    }
                } catch (InterruptedException er) {
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (fixedSpiderThreadPool == null || fixedSpiderThreadPool.isShutdown()) {
            fixedSpiderThreadPool = Executors.newFixedThreadPool(spiderThreadNum);
        }
        for (int i = 0; i < spiderThreadNum; i++) {
            fixedSpiderThreadPool.execute(thread);
        }
//        if (pictureSpiderThread.getState().compareTo(Thread.State.WAITING) == 0) {
//            pictureSpiderThread.notify();
//        } else {
//            pictureSpiderThread.start();
//        }
        return true;
    }

    @RequestMapping(value = "/stopSpider")
    @ResponseBody
    public boolean stopSpider() {
        swich = false;
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        boolean b = pictureSpiderThread.getState().compareTo(Thread.State.RUNNABLE) == 0;
//        pictureSpiderThread = null;
        fixedSpiderThreadPool.shutdown();
        return fixedSpiderThreadPool.isShutdown();
    }

    @RequestMapping(value = "/stopDownload")
    @ResponseBody
    public boolean stopDownload() {
        downloadSwitch = false;
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        boolean b = pictureDownloadThread.getState().compareTo(Thread.State.RUNNABLE) == 0;
//        pictureDownloadThread = null;
        fixedThreadPool.shutdown();
        return fixedThreadPool.isShutdown();
    }

    @RequestMapping(value = "/setStartId")
    @ResponseBody
    public boolean setpreId(@RequestParam(value = "startId") int startId) {
        if (producer == null) {
            producer = new PictureProducer(staticQueue);
        }
        producer.setPreId(startId);
        staticQueue.clear();
        return true;
    }

    @RequestMapping(value = "/setDownloadNum")
    @ResponseBody
    public boolean setDownloadNum(@RequestParam(value = "downloadedPicNum") int downloadedPicNum) {
        totalDownloadCount = downloadedPicNum;
        return true;
    }

    @RequestMapping(value = "/setCoookie")
    @ResponseBody
    public boolean setCoookie(@RequestParam(value = "cookie") String cookies) {
        cookie = cookies;
        return true;
    }

    @RequestMapping(value = "/setProxy")
    @ResponseBody
    public boolean setProxy(@RequestParam(value = "proxyIp") String proxyIpT, @RequestParam(value = "proxyPort") int proxyPortT) {
        proxyIp = proxyIpT;
        proxyPort = proxyPortT;
        return true;
    }
    @RequestMapping(value = "/setSleepTime")
    @ResponseBody
    public boolean setSleepTime(@RequestParam(value = "sleepTime") int sleepTime) {
        this.sleepTime = sleepTime;
        return true;
    }
    @RequestMapping(value = "/setDownloadSleepTime")
    @ResponseBody
    public boolean setDownloadSleepTime(@RequestParam(value = "downloadSleepTime") int downloadSleepTime) {
        this.downloadSleepTime = downloadSleepTime;
        return true;
    }
    @RequestMapping(value = "/setDownloadThreadNum")
    @ResponseBody
    public boolean setDownloadThreadNum(@RequestParam(value = "downloadThreadNum") int downloadThreadNum) {
        this.downloadThreadNum = downloadThreadNum;
        return true;
    }
    @RequestMapping(value = "/setSpiderThreadNum")
    @ResponseBody
    public boolean setSpiderThreadNum(@RequestParam(value = "spiderThreadNum") int spiderThreadNum) {
        this.spiderThreadNum = spiderThreadNum;
        return true;
    }

    @RequestMapping(value = "/getStatus")
    @ResponseBody
    public JSONObject getStatus() {
        MongoSelect select = new MongoSelect();
        MongoSelect select0 = new MongoSelect().where("status", 0);
        MongoSelect select1 = new MongoSelect().where("status", 1);
        MongoSelect select2 = new MongoSelect().where("status", 2);
        int total = 0;
        int unDownload = 0;
        int downloaded = 0;
        int error = 0;
        try {
            total = pictureListDaoImpl.count(select);
            unDownload = pictureListDaoImpl.count(select0);
            downloaded = pictureListDaoImpl.count(select1);
            error = pictureListDaoImpl.count(select2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("totalCount", total);
        jsonObject.put("sleepTime", sleepTime);
        jsonObject.put("spiderStackNum", staticQueue.size());
        jsonObject.put("downloadStackNum", pictureQueue.size());
        jsonObject.put("downloadSleepTime", downloadSleepTime);
        jsonObject.put("preId", producer == null ? 1 : producer.getPreId());
        jsonObject.put("proxyIp", proxyIp);
        jsonObject.put("proxyPort", proxyPort);
        jsonObject.put("cookie", cookie);
        jsonObject.put("currentId", currentId);
        jsonObject.put("currentDownloadId", currentDownloadId);
        jsonObject.put("total", unDownload);
        jsonObject.put("downloaded", downloaded);
        jsonObject.put("error", error);
        jsonObject.put("downloadedPicNum", totalDownloadCount);
        jsonObject.put("spiderThreadNum", spiderThreadNum);
        jsonObject.put("downloadThreadNum", downloadThreadNum);
        jsonObject.put("isProxyDown", isProxyDown?"已失效":"正常");
        jsonObject.put("isCookieDown", isCookieDown?"已失效":"正常");
        jsonObject.put("spiderState", fixedSpiderThreadPool == null ? "已停止" : (fixedSpiderThreadPool.isShutdown()?"已停止":"运行中"));
        jsonObject.put("downloadState", fixedThreadPool == null ? "已停止" : (fixedThreadPool.isShutdown()?"已停止":"运行中"));
        return jsonObject;
    }

    @RequestMapping(value = "/startDownload")
    @ResponseBody
    public boolean startDownload() {
        downloadSwitch = true;
        if (downloadProducer == null) {
            downloadProducer = new PictureDownloadProducer(pictureListDaoImpl, pictureQueue);
        }
        if (pictureDownloadProducerThread == null) {
            pictureDownloadProducerThread = new Thread(downloadProducer);
            pictureDownloadProducerThread.start();
        }
        Thread thread = new Thread(() -> {
            while (downloadSwitch) {
                try {
                    Map<String, Object> map = new HashedMap();
                    Where where = new Where();
                    PictureListEntites picture = pictureQueue.take();
                    currentDownloadId = picture.getId();
                    BasicHeader[] headers = new BasicHeader[9];
                    headers[0] = new BasicHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
                    headers[1] = new BasicHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    headers[2] = new BasicHeader("accept-encoding", "gzip, deflate, sdch");
                    headers[3] = new BasicHeader("accept-language", "zh-CN,zh;q=0.8");
                    headers[4] = new BasicHeader("connection", "keep-alive");
                    headers[5] = new BasicHeader("cookie", cookie);
                    headers[6] = new BasicHeader("host", "www.dfic.cn");
                    headers[7] = new BasicHeader("upgrade-insecure-requests", "1");
                    headers[8] = new BasicHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
                    String url = picture.getUrl();
                    if(url.startsWith("http://www.dfic.cn/http")){
                        url = url.replace("http://www.dfic.cn/","");
                    }
                    String documentStr = downloader(url, proxyIp, headers, proxyPort);
                    if (documentStr == null || "".equals(documentStr)) {
                        Log.bug(DateUtil.now() + "NO Response:" + url);
                        isProxyDown = true;
                        map.put("status", 4);
                        where.and("url", picture.getUrl());
                        pictureListDaoImpl.update(map, where);
                        continue;
                    } else if (documentStr.contains("502 Bad Gateway")) {
                        Log.bug(DateUtil.now() + "Proxy Outtime:" + url);
                        isProxyDown = true;
                        return;
                    } else if (documentStr.contains("请您先登录")) {
                        Log.bug(DateUtil.now() + "Cookie Outtime:" + url);
                        isCookieDown = true;
                        return;
                    }
                    isProxyDown = false;
                    isCookieDown = false;
                    Document document = Jsoup.parse(documentStr);
                    Elements tables = document.select("td[id~=^imagebox]");
                    Elements pages = document.select(".pagediv1");
                    if (tables.size() == 0) {
                        Log.bug(DateUtil.now() + "NO EffectResult:" + url);
                        map.put("status", 2);
                        where.and("url", picture.getUrl());
                        pictureListDaoImpl.update(map, where);
                        continue;
                    }
                    String imgPath = savePath + "/" + DateUtil.today() + "/" + url.substring(url.indexOf("id=") + 3, url.indexOf("&reqid")) + "/";
                    java.io.File file = new java.io.File(imgPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    for (Element element : tables) {
                        String picId = element.attr("id");
                        String smallPicUrl = element.select("td>a>img").get(0).attr("src");
                        String bigPic = element.select("td>a").get(0).attr("onmouseover");
                        String bigPicUrl = bigPic.substring(bigPic.indexOf("('") + 2, bigPic.indexOf("'", 15));
                        bigPicUrl = bigPicUrl.replace("s/jpg", "/jpg");
                        HttpResult smallRes = HttpUtils.download(smallPicUrl, imgPath + picId + "_small");
                        HttpResult bigRes = HttpUtils.download(bigPicUrl, imgPath + picId + "_big");
                        Log.info(DateUtil.now() + "小图：" + smallPicUrl + "路径：" + smallRes.getBody().toString());
                        Log.info(DateUtil.now() + "大图：" + bigPicUrl + "路径：" + bigRes.getBody().toString());
                        totalDownloadCount++;
                        totalDownloadCount++;
                    }
                    if (pages.size() > 0) {
                        map.put("status", 3);
                    } else {
                        map.put("status", 1);
                    }
                    where.and("url", picture.getUrl());
                    pictureListDaoImpl.update(map, where);
                    Thread.currentThread().sleep(downloadSleepTime);
                } catch (InterruptedException er) {
                    er.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (fixedThreadPool == null || fixedThreadPool.isShutdown()) {
            fixedThreadPool = Executors.newFixedThreadPool(downloadThreadNum);
        }
        for (int i = 0; i < downloadThreadNum; i++) {
            fixedThreadPool.execute(thread);
        }
//        if (pictureDownloadThread.getState().compareTo(Thread.State.WAITING) == 0) {
//            pictureDownloadThread.notify();
//        } else {
//            pictureDownloadThread.start();
//        }
        return true;
    }


    /**
     * 代理下载
     *
     * @param url       目标URL
     * @param proxyIp   代理IP
     * @param proxyPort 代理端口
     * @return 页面html
     */
    private String downloader(String url, String proxyIp, Header[] headers, int proxyPort) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        try {
            HttpGet httpGet = new HttpGet(url);
            if (!StringUtil.isEmpty(proxyIp) && proxyPort != 0) {
                HttpHost proxy = new HttpHost(proxyIp, proxyPort);
                RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
                httpGet.setConfig(config);
            }
            httpGet.setHeaders(headers);
            CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
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
}
