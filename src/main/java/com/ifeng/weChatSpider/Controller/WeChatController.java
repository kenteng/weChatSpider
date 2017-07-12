/**
 * WeChatController.java
 * Created by zhusy on 2017/2/24 0024 16:56
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Controller;

import com.ifeng.weChatSpider.Bean.*;
import com.ifeng.weChatSpider.Dao.AbsDao;
import com.ifeng.weChatSpider.Mongo.MongoSelect;
import com.ifeng.weChatSpider.Mongo.Where;
import com.ifeng.weChatSpider.Mongo.WhereType;
import com.ifeng.weChatSpider.Services.ArticleListService;
import com.ifeng.weChatSpider.Services.ArticleService;
import com.ifeng.weChatSpider.Services.UinInfoService;
import com.ifeng.weChatSpider.Services.WeChatService;
import com.ifeng.weChatSpider.Thread.UinInfoProducer;
import com.ifeng.weChatSpider.Thread.WeChatProducer;
import com.ifeng.weChatSpider.Util.*;
import com.mongodb.util.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class WeChatController {

    private static final String filePath = "E:/IfengProgram/OtherArticle/%s.txt";

    private static int totalCount = 0;

    private static boolean swich = true;

    private static final String RESULT = "redirect:https://mp.weixin.qq.com/mp/getmasssendmsg?__biz=%s$wechat_webview_type=1&wechat_redirect";

    private static final long FIVEDAYMIL = 5 * 24 * 3600 * 1000;

    private Thread weChatProducerThread;
    private Thread uinInfoProducerThread;
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    private static long TIMETAP = System.currentTimeMillis();

    private static BlockingQueue<WeChat> staticWeChatList = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<UinInfo> staticUinInfoList = new ArrayBlockingQueue<>(100);
    private static Map<String, WeChat> weChatMap = new HashMap<>();
    private static Map<String, Integer> uinCountMap = new HashMap<>();
    private WeChatProducer weChatProducer;
    private UinInfoProducer uinInfoProducer;

    private static String script = "";

    public WeChatController() {
    }

    @Resource(name = "otherArticleDaoImpl")
    private AbsDao<OtherArticle> otherArticleDao;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private UinInfoService uinInfoService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleListService articleListService;

    @RequestMapping(value = "saveUin")
    @ResponseBody
    public boolean saveUin(String uin, String key, String cookie, String ua2, String guid, String auth, String url) throws UnsupportedEncodingException {
        if (StringUtil.isEmpty(uin) || StringUtil.isEmpty(key)) {
            return false;
        }
        uin = URLDecoder.decode(uin, "UTF-8");
        key = URLDecoder.decode(key, "UTF-8");
        cookie = URLDecoder.decode(cookie, "UTF-8");
        ua2 = URLDecoder.decode(ua2, "UTF-8");
        guid = URLDecoder.decode(guid, "UTF-8");
        auth = URLDecoder.decode(auth, "UTF-8");
        url = URLDecoder.decode(url, "UTF-8");

        UinInfo uinInfo = new UinInfo();
        uinInfo.setUin(uin);
        uinInfo.setKey(key);
        uinInfo.setUrl(url);
        uinInfo.setUa2(ua2);
        uinInfo.setGuid(guid);
        uinInfo.setAuth(auth);
        String[] urls = url.split("&");
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < urls.length; i++) {
            String item[] = urls[i].split("=");
            params.put(item[0], item[1]);
        }
        uinInfo.setCookie(cookie);
        uinInfo.setPassTicket(params.get("pass_ticket"));
        uinInfo.setVersion(params.get("version"));
        uinInfo.setCreateTime(DateUtil.now());
        uinInfo.setStatus(1);
        try {
            Log.info(DateUtil.now() + "入库：uin：" + uin);
            uinInfoService.insertOne(uinInfo);
        } catch (Exception e) {
            Log.bug(JSONObject.fromObject(uinInfo).toString());
            e.printStackTrace();
        }
        return true;
    }

    private void parseAritcle(String data, String url, WeChat lastWeChat) throws Exception {
        List<Map<String, String>> arlist = new ArrayList<>();
        List<ArticleList> articleList = new ArrayList<>();
        data = data.replaceAll("&quot;", "\"");
        data = data.replaceAll("'", "");
        data = data.replaceAll("\\\\/", "/");
        if (data.contains("if(!!window.__initCatch)")) {
            data = data.substring(data.indexOf("var msgList ="), data.indexOf("if(!!window.__initCatch)"));
        } else if (data.contains("window.__from =")) {
            data = "var " + data.substring(data.indexOf("msgList ="), data.indexOf("window.__from ="));
        }
        Map<String, Object> context = new HashMap<>();
        context.put("arlist", arlist);
        context.put("d", data);
        ScriptEngin.getInstance().run(script, context);
        TIMETAP = System.currentTimeMillis();
        arlist.forEach(m -> {
            ArticleList article = new ArticleList();
            String title = m.get("title").replaceAll("&amp;", "&").replaceAll("&nbsp;", " ").replaceAll("&quot;", "\"").replaceAll("\\u00A0", " ").trim();
            String datetime = m.get("datetime");
            String cover = m.get("cover").replaceAll("&amp;amp;", "&").replaceAll("\\\\/", "/");
            String sourceLink = m.get("url").replaceAll("&amp;amp;", "&").replaceAll("\\\\", "");
            article.setAccount(lastWeChat.getAccount());
            article.setUrl(sourceLink);
            article.setCreateTime(DateUtil.now());
            article.setCreate_Time(DateUtil.formatDateTime(datetime == null ? 0L : Long.valueOf(datetime) * 1000));
            article.setCateid(lastWeChat.getCateId());
            article.setCatename(lastWeChat.getCatename());
            article.setCover(cover);
            article.setTitle(title);
            article.setStatus(0);
            article.setWechat_name(lastWeChat.getWeChatName());
            if (!"".equals(title) && !"".equals(url) && TIMETAP - (datetime == null ? 0L : Long.valueOf(datetime) * 1000) <= FIVEDAYMIL) {
                articleList.add(article);
            }
        });
        int count = articleListService.flush(articleList);
        List<String> pubTimes = lastWeChat.getPubTimes();
        int index = lastWeChat.getPubTimesIndex();
        Map<String, Object> map = new HashedMap();
        map.put("spider_date", DateUtil.now());
        map.put("lastNum", count);
        if (count > 0) {
            pubTimes.set(index, articleList.get(0).getCreate_Time().substring(11));
            if (index == pubTimes.size() - 1) {
                index = 0;
                map.put("nextPubTime", DateUtil.tomorrow() + " " + pubTimes.get(index));
            } else {
                index++;
                map.put("nextPubTime", DateUtil.today() + " " + pubTimes.get(index));
            }
            map.put("pubTimesIndex", index);
            map.put("pubTimes", pubTimes);
        } else {
            String nextPubTime = lastWeChat.getNextPubTime();
            nextPubTime = DateUtil.formatDateTime(DateUtil.parseDateTime(nextPubTime).getTime() + 2 * 60 * 60 * 1000);
            map.put("nextPubTime", nextPubTime);
        }

        Where where = new Where("biz", lastWeChat.getBiz());
        weChatService.update(map, where);
        Log.info(DateUtil.now() + " 账号：" + lastWeChat.getAccount() + "\t入库：" + count + "\t抓取：" + articleList.size() + "\t下次抓取时间：" + map.get("nextPubTime"));
    }

    @RequestMapping(value = "getWeChat")
    public String getWeChat(String data, String url, HttpSession session) throws UnsupportedEncodingException {
        if (weChatProducer == null) {
            weChatProducer = new WeChatProducer(staticWeChatList, weChatService);
        }
        if (weChatProducerThread == null) {
            weChatProducerThread = new Thread(weChatProducer);
            weChatProducerThread.start();
        }
        if ("".equals(script)) {
            Log.info(session.getServletContext().getRealPath("/") + "files" + File.separator + "profile.js");
            System.out.println(session.getServletContext().getRealPath("/") + "files" + File.separator + "profile.js");
            script = TextFile.read(session.getServletContext().getRealPath("/") + "files" + File.separator + "profile.js");
        }
        WeChat lastWeChat = (WeChat) session.getAttribute("wechat");
        if (lastWeChat != null && url.contains(lastWeChat.getBiz())) {
            try {
                if (data.startsWith("faild")) {
                    Log.info(DateUtil.now() + Thread.currentThread().getName() + " 账号：" + lastWeChat.getAccount() + "被封号" + "\t列表剩余：" + staticWeChatList.size());
                    Map<String, Object> map = new HashedMap();
                    map.put("status", "-1");
                    Where where = new Where("biz", lastWeChat.getBiz());
                    weChatService.update(map, where);
                } else if (!data.startsWith("error")) {
                    parseAritcle(data, url, lastWeChat);
                    session.removeAttribute(lastWeChat.getAccount());
                } else {
                    Object count = session.getAttribute(lastWeChat.getAccount());
                    int time;
                    if (count != null) {
                        int num = (int) count;
                        if (num > 1) {
                            num -= 1;
                            session.setAttribute(lastWeChat.getAccount(), num);
                            staticWeChatList.offer(lastWeChat);
                            time = 3 - num;
                        } else {
                            session.removeAttribute(lastWeChat.getAccount());
                            Map<String, Object> map = new HashedMap();
                            map.put("status", "9");
                            Where where = new Where("biz", lastWeChat.getBiz());
                            weChatService.update(map, where);
                            time = 3;
                        }
                    } else {
                        time = 1;
                        session.setAttribute(lastWeChat.getAccount(), 2);
                        staticWeChatList.offer(lastWeChat);
                    }
                    Log.info(DateUtil.now() + Thread.currentThread().getName() + " 账号：" + lastWeChat.getAccount() + "第" + time + " 次抓取失败:" + data + "\t列表剩余：" + staticWeChatList.size());
                }
                Map<String, Object> map = new HashedMap();
                map.put("spider_date", DateUtil.now());
                Where where = new Where("biz", lastWeChat.getBiz());
                weChatService.update(map, where);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        WeChat weChat = null;
        try {
            weChat = staticWeChatList.take();
            String biz = weChat.getBiz();
            while (biz == null || "".equals(biz)) {
                weChat = staticWeChatList.take();
                biz = weChat.getBiz();
            }
            session.setAttribute("wechat", weChat);
            Thread.sleep(1000);
            String result = "redirect:";
            if (url.contains("&uin")) {
                result += url.substring(0, url.indexOf("__biz")) + "__biz=" + biz + url.substring(url.indexOf("&uin"));
            } else {
                result += "https://mp.weixin.qq.com/mp/getmasssendmsg?" + "__biz=" + biz + url.substring(url.indexOf("&scene"));
            }
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "redirect:" + url;
    }

    @RequestMapping(value = "startSpider")
    @ResponseBody
    public boolean startSpider(HttpSession session) {
        swich = true;
        if (weChatProducer == null) {
            weChatProducer = new WeChatProducer(staticWeChatList, weChatService);
        }
        if (uinInfoProducer == null) {
            uinInfoProducer = new UinInfoProducer(staticUinInfoList, uinInfoService);
        }
        if (weChatProducerThread == null) {
            weChatProducerThread = new Thread(weChatProducer);
            weChatProducerThread.start();
        }
        if (uinInfoProducerThread == null) {
            uinInfoProducerThread = new Thread(uinInfoProducer);
            uinInfoProducerThread.start();
        }
        if ("".equals(script)) {
            Log.info(session.getServletContext().getRealPath("/") + "files" + File.separator + "profile.js");
            System.out.println(session.getServletContext().getRealPath("/") + "files" + File.separator + "profile.js");
            script = TextFile.read(session.getServletContext().getRealPath("/") + "files" + File.separator + "profile.js");
        }
        Thread thread = new Thread(() -> {
            while (swich) {
                WeChat weChat = null;
                try {
                    weChat = staticWeChatList.take();
                    String biz = weChat.getBiz();
                    while (biz == null || "".equals(biz)) {
                        weChat = staticWeChatList.take();
                        biz = weChat.getBiz();
                    }
                    UinInfo uinInfo = staticUinInfoList.take();
                    if (StringUtil.isEmpty(uinInfo.getStartTime())) {
                        Map<String, Object> map = new HashedMap();
                        map.put("startTime", DateUtil.now());
                        Where where = new Where("_id", uinInfo.get_id());
                        uinInfoService.update(map, where);
                    }
                    int count = 0;
                    while (swich && uinInfo.getStatus() == 1) {
                        String url = "https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=" + biz +
                                "&scene=124&devicetype=android-22&" +
                                "version=" + uinInfo.getVersion() +
                                "&lang=zh_CN&nettype=WIFI&a8scene=3&" +
                                "pass_ticket=" + uinInfo.getPassTicket() +
                                "&wx_header=1 ";
                        Document document = Jsoup.connect(url)
                                .header("host", "mp.weixin.qq.com")
                                .header("connection", "keep-alive")
                                .header("upgrade-insecure-requests", "1")
                                .header("user-agent", "Mozilla/5.0 (Linux; Android 5.1; OPPO A59m Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043124 Safari/537.36 MicroMessenger/6.5.7.1041 NetType/WIFI Language/zh_CN")
                                .header("x-wechat-uin", uinInfo.getUin())
                                .header("x-wechat-key", uinInfo.getKey())
                                .header("q-ua2", uinInfo.getUa2())
                                .header("q-guid", uinInfo.getGuid())
                                .header("q-auth", uinInfo.getAuth())
                                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/wxpic,image/sharpp,*/*;q=0.8")
                                .header("accept-language", "zh-CN,en-US;q=0.8")
                                .header("content-length", "0")
                                .header("cookie", uinInfo.getCookie())
                                .header("cache-control", "no-cache")
                                .get();
                        count++;
                        uinCountMap.put(uinInfo.getUin(), uinCountMap.get(uinInfo.getUin()) == null ? 1 : (uinCountMap.get(uinInfo.getUin()) + 1));
                        if (document.html().contains("var msgList =")) {
                            String jsonList = document.html().substring(document.html().indexOf("var msgList ="), document.html().indexOf("if(!!window.__initCatch)"));
                            parseAritcle(jsonList, url, weChat);
                        } else if (document.html().contains("已停止访问该网页")) {
                            Log.info(DateUtil.now() + " 账号：" + weChat.getAccount() + "被封号" + "\t列表剩余：" + staticWeChatList.size());
                            Map<String, Object> map1 = new HashedMap();
                            map1.put("status", "-1");
                            map1.put("spider_date", DateUtil.now());
                            Where where1 = new Where("biz", weChat.getBiz());
                            weChatService.update(map1, where1);
                        } else if (document.html().contains("此帐号已申请帐号迁移")) {
                            Log.info(DateUtil.now() + " 账号：" + weChat.getAccount() + "已迁移" + "\t列表剩余：" + staticWeChatList.size());
                            Map<String, Object> map2 = new HashedMap();
                            map2.put("status", "-2");
                            map2.put("spider_date", DateUtil.now());
                            Where where2 = new Where("biz", weChat.getBiz());
                            weChatService.update(map2, where2);
                        } else if (document.html().contains("页面无法打开")) {
                            Log.info(DateUtil.now() + " UIN：" + uinInfo.getUin() + "次数用完" + "\t列表剩余：" + staticWeChatList.size());
                            uinInfo.setStatus(0);
                            Map<String, Object> map3 = new HashedMap();
                            map3.put("status", -1);
                            map3.put("count", count);
                            map3.put("endTime", DateUtil.now());
                            Where where3 = new Where("_id", uinInfo.get_id());
                            uinInfoService.update(map3, where3);
                        } else {
                            Log.info(DateUtil.now() + " UIN：" + uinInfo.getUin() + "已过期" + "\t列表剩余：" + staticWeChatList.size());
                            uinInfo.setStatus(0);
                            Map<String, Object> map4 = new HashedMap();
                            map4.put("status", 0);
                            map4.put("count", count);
                            map4.put("endTime", DateUtil.now());
                            Where where4 = new Where("_id", uinInfo.get_id());
                            uinInfoService.update(map4, where4);
                        }
                        weChat = staticWeChatList.take();
                        biz = weChat.getBiz();
                        while (biz == null || "".equals(biz)) {
                            weChat = staticWeChatList.take();
                            biz = weChat.getBiz();
                        }
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (Map.Entry entry : uinCountMap.entrySet()) {
                Map<String, Object> map = new HashedMap();
                map.put("uinTotalCount", entry.getValue());
                Where where = new Where("uin", entry.getKey());
                try {
                    uinInfoService.update(map, where);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        fixedThreadPool.execute(thread);
        fixedThreadPool.execute(thread);
        fixedThreadPool.execute(thread);
        fixedThreadPool.execute(thread);
        fixedThreadPool.execute(thread);
        return true;
    }

    @RequestMapping(value = "stopSpider")
    @ResponseBody
    public boolean stopSpider() throws InterruptedException {
        swich = !swich;
        Log.info("-------------------------------------------------------------------");
        for (Map.Entry entry : uinCountMap.entrySet()) {
            Log.info("--------------uin:" + entry.getKey() + "--------------count:" + entry.getValue() + "--------------");
        }
        Log.info("-------------------------------------------------------------------");
        return swich;
    }

    @RequestMapping(value = "getNextWeChat")
    @ResponseBody
    public String getNextWeChat(String data, String uin, String url) throws UnsupportedEncodingException {
        uinCountMap.put(uin, uinCountMap.get(uin) == null ? 1 : (uinCountMap.get(uin) + 1));
        data = URLDecoder.decode(data, "UTF-8");
        url = URLDecoder.decode(url, "UTF-8");
        data = data.replace("\\\\/", "/");
        if (weChatProducer == null) {
            weChatProducer = new WeChatProducer(staticWeChatList, weChatService);
        }
        if (weChatProducerThread == null) {
            weChatProducerThread = new Thread(weChatProducer);
            weChatProducerThread.start();
        }
        String lastBiz = url.substring(url.indexOf("__biz=") + 6, url.indexOf("&scene"));
        WeChat lastWeChat = weChatMap.get(lastBiz);
        List<ArticleList> articleList = new ArrayList<>();
        if (lastWeChat != null) {
            weChatMap.remove(lastBiz);
            Map<String, Integer> publishTimeMap = new TreeMap<>();
            try {
                if (data.startsWith("faild")) {
                    Log.info(DateUtil.now() + Thread.currentThread().getName() + " 账号：" + lastWeChat.getAccount() + "被封号" + "\t列表剩余：" + staticWeChatList.size());
                    Map<String, Object> map = new HashedMap();
                    map.put("status", "-1");
                    map.put("spider_date", DateUtil.now());
                    Where where = new Where("biz", lastWeChat.getBiz());
                    weChatService.update(map, where);
                } else {
                    TIMETAP = System.currentTimeMillis();
                    JSONArray jsonArray = JSONArray.fromObject(JSON.parse(data));
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        ArticleList article = new ArticleList();
                        article.setAccount(lastWeChat.getAccount());
                        article.setUrl(object.getString("url"));
                        article.setCreateTime(DateUtil.now());
                        article.setCreate_Time(DateUtil.formatDateTime(object.getLong("datetime") * 1000));
                        article.setCateid(lastWeChat.getCateId());
                        article.setCatename(lastWeChat.getCatename());
                        article.setCover(object.getString("cover"));
                        article.setTitle(object.getString("title").replaceAll("&quot;", "\"")
                                .replaceAll("&amp;", "&")
                                .replaceAll("&yen;", "¥")
                                .replaceAll("&nbsp;", " ")
                                .replaceAll("\\u00A0", " ").trim());
                        article.setStatus(0);
                        article.setFrom("client");
                        article.setWechat_name(lastWeChat.getWeChatName());
                        if (!"".equals(article.getTitle()) && !"".equals(url) && TIMETAP - (object.getLong("datetime") * 1000) <= FIVEDAYMIL) {
                            articleList.add(article);
                            if (DateUtil.yesterday().getTime() < (object.getLong("datetime") * 1000)) {
                                publishTimeMap.put(article.getCreate_Time().substring(11), 1);//昨天以后的文章发布时间，按照时间从早到晚排列
                            }
                        }
                    }
                    int count = articleListService.flush(articleList);//返回入库数量
                    List<String> pubTimes = lastWeChat.getPubTimes();//微信原始文章发布时间列表
                    int index = lastWeChat.getPubTimesIndex();//微信原始文章发布时间列表脚标
                    Map<String, Object> map = new HashedMap();
                    map.put("spider_date", DateUtil.now());//更新最新抓取时间
                    map.put("lastNum", count);//更新本次抓取是否有入库文章
                    if (publishTimeMap.size() > 0) {//如果抓取到两天之内的文章，说明次次抓取有效，可以更新抓取规律
                        if (pubTimes == null) {//如果原始微信的发布时间列表不存在，则将此次抓取到的发布时间列表存进去
                            pubTimes = new ArrayList<>();
                            for (Map.Entry entryTemp : publishTimeMap.entrySet()) {
                                pubTimes.add(((String) entryTemp.getKey()));
                            }
                            index = pubTimes.size() - 1;//脚标为最后一个文章的发布时间脚标
                        }
                        pubTimes.set(index, articleList.get(0).getCreate_Time().substring(11));//将最后一次发布时间更新到列表对应脚标上
                        if (index >= pubTimes.size() - 1) {//如果脚标越界了，说明一天的时间都抓取了，下一次抓取时间为明天的第一个时间点
                            index = 0;
                            map.put("nextPubTime", DateUtil.tomorrow() + " " + pubTimes.get(index));
                        } else {//否则抓取时间列表的下一个时间点
                            index++;
                            map.put("nextPubTime", DateUtil.today() + " " + pubTimes.get(index));
                        }
                        map.put("lastSucTime", DateUtil.now());
                        map.put("pubTimesIndex", index);
                        map.put("pubTimes", pubTimes);
                    } else {//如果没有抓到近两天的文章，下一次抓取时间为明天的lastSucTime时间点
                        if (count > 0) {//如果有入库文章，则下次抓取时间为明天的此次入库最后一篇文章的发布时间点
                            map.put("lastSucTime", DateUtil.now());
                            map.put("nextPubTime", DateUtil.tomorrow() + " " + articleList.get(0).getCreate_Time().substring(11));
                        } else {//如果没有入库文章，则下次抓取时间为明天 + 上一次有效入库时间点
                            String nextPubTime = StringUtil.isEmpty(lastWeChat.getLastSucTime()) ? DateUtil.formatTime(new Date()) : lastWeChat.getLastSucTime().substring(11);
                            nextPubTime = DateUtil.tomorrow() + " " + nextPubTime;
                            map.put("pubTimes", null);
                            map.put("nextPubTime", nextPubTime);
                        }
                    }
                    Where where = new Where("biz", lastWeChat.getBiz());
                    weChatService.update(map, where);
                    Log.info(DateUtil.now() + " 账号：" + lastWeChat.getAccount() + "\t入库：" + count + "\t有效发布时间数：" + publishTimeMap.size() + "\t抓取：" + articleList.size() + "\t下次抓取时间：" + map.get("nextPubTime"));
                }
            } catch (Exception e) {
                Log.info(DateUtil.now() + " 账号：" + lastWeChat.getAccount() + "\t出错" + e.toString() + "\n" + data);
                e.printStackTrace();
            }
        }
        WeChat weChat;
        try {
            weChat = staticWeChatList.take();
            String biz = weChat.getBiz();
            while (biz == null || "".equals(biz)) {
                weChat = staticWeChatList.take();
                biz = weChat.getBiz();
            }
            weChatMap.put(biz, weChat);
            int random = (int) (Math.random() * 4 + 8) * 10000;
            if ("MzA3OTk4NzczMQ==".equals(lastBiz)) {
                random = 1000;
            }
            if (!shouldSpider()) {
                random = 6 * 60 * 60 * 1000 + 10000;
            }
            String result = "<script>setTimeout(function(){window.location.href='https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=" + biz + "&scene=124#wechat_redirect" /*url.substring(url.indexOf("&scene"))*/ + "';}," + random + ");</script>";
//            String result = "<script>setTimeout(function(){window.location.href='https://mp.weixin.qq.com/mp/getmasssendmsg?__biz=" + biz + "#wechat_webview_type=1&wechat_redirect" /*url.substring(url.indexOf("&scene"))*/  + "';},"+ random + ");</script>";
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "<script>setTimeout(function(){window.location.href='https://mp.weixin.qq.com" + url + "';},10000<);/script>";
        return null;
    }

    @RequestMapping(value = "getNextSougou")
    @ResponseBody
    public String getNextSougou(String data, String account) throws UnsupportedEncodingException {
        data = URLDecoder.decode(data, "UTF-8");
        account = URLDecoder.decode(account, "UTF-8");
        data = data.replace("\\\\/", "/");
        if (data.startsWith("verify")) {
            Log.sendMail("zhusy@ifeng.com", "验证码", "搜狗客户端抓取需要验证码！");
            return "null";
        }
        if (weChatProducer == null) {
            weChatProducer = new WeChatProducer(staticWeChatList, weChatService);
        }
        if (weChatProducerThread == null) {
            weChatProducerThread = new Thread(weChatProducer);
            weChatProducerThread.start();
        }
        WeChat lastWeChat = weChatMap.get(account);
        List<ArticleList> articleList = new ArrayList<>();
        if (lastWeChat != null) {
            weChatMap.remove(account);
            Map<String, Integer> publishTimeMap = new TreeMap<>();
            try {
                if (data.startsWith("faild")) {
                    Log.info(DateUtil.now() + Thread.currentThread().getName() + " 账号：" + lastWeChat.getAccount() + "被封号" + "\t列表剩余：" + staticWeChatList.size());
                    Map<String, Object> map = new HashedMap();
                    map.put("status", "-1");
                    map.put("spider_date", DateUtil.now());
                    Where where = new Where("biz", lastWeChat.getBiz());
                    weChatService.update(map, where);
                } else if (data.startsWith("verify")) {
                    Log.sendMail("zhusy@ifeng.com", "验证码", "搜狗客户端抓取需要验证码！");
                    return null;
                } else {
                    String[] replace = new String[]{"&#39;", "'", "&quot;", "\\\"", "&nbsp;", " ", "&gt;", ">", "&lt;", "<", "&amp;", "&", "&yen;", "¥", "\\\\/", "/"};
                    for (int i = 0; i < replace.length; i += 2) {
                        data = data.replace(replace[i], replace[i + 1]);
                    }
                    TIMETAP = System.currentTimeMillis();
                    JSONArray jsonArray = JSONArray.fromObject(JSON.parse(data));
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        ArticleList article = new ArticleList();
                        article.setAccount(lastWeChat.getAccount());
                        article.setUrl(object.getString("url"));
                        article.setCreateTime(DateUtil.now());
                        article.setCreate_Time(DateUtil.formatDateTime(object.getLong("datetime") * 1000));
                        article.setCateid(lastWeChat.getCateId());
                        article.setCatename(lastWeChat.getCatename());
                        article.setCover(object.getString("cover"));
                        article.setTitle(object.getString("title").replaceAll("\\u00A0", " ").trim());
                        article.setStatus(0);
                        article.setFrom("client");
                        article.setWechat_name(lastWeChat.getWeChatName());
                        if (!"".equals(article.getTitle()) && !"".equals(account) && TIMETAP - (object.getLong("datetime") * 1000) <= FIVEDAYMIL) {
                            articleList.add(article);
                            if (DateUtil.yesterday().getTime() < (object.getLong("datetime") * 1000)) {
                                publishTimeMap.put(article.getCreate_Time().substring(11), 1);
                            }
                        }
                    }
                    int count = articleListService.flush(articleList);
                    List<String> pubTimes = lastWeChat.getPubTimes();
                    int index = lastWeChat.getPubTimesIndex();
                    Map<String, Object> map = new HashedMap();
                    map.put("spider_date", DateUtil.now());
                    map.put("lastNum", count);
                    if (publishTimeMap.size() > 0) {
                        if (pubTimes == null) {
                            pubTimes = new ArrayList<>();
                            for (Map.Entry entryTemp : publishTimeMap.entrySet()) {
                                pubTimes.add(((String) entryTemp.getKey()));
                            }
                            index = pubTimes.size() - 1;
                        }
                        pubTimes.set(index, articleList.get(0).getCreate_Time().substring(11));
                        if (index >= pubTimes.size() - 1) {
                            index = 0;
                            map.put("nextPubTime", DateUtil.tomorrow() + " " + pubTimes.get(index));
                        } else {
                            index++;
                            map.put("nextPubTime", DateUtil.today() + " " + pubTimes.get(index));
                        }
                        map.put("lastSucTime", DateUtil.now());
                        map.put("pubTimesIndex", index);
                        map.put("pubTimes", pubTimes);
                    } else {
                        if (count > 0) {
                            map.put("lastSucTime", DateUtil.now());
                            map.put("nextPubTime", DateUtil.tomorrow() + " " + articleList.get(0).getCreate_Time().substring(11));
                        } else {
                            String nextPubTime = StringUtil.isEmpty(lastWeChat.getLastSucTime()) ? DateUtil.formatTime(new Date()) : lastWeChat.getLastSucTime().substring(11);
                            nextPubTime = DateUtil.tomorrow() + " " + nextPubTime;
                            map.put("pubTimes", null);
                            map.put("nextPubTime", nextPubTime);
                        }
                    }
                    Where where = new Where("biz", lastWeChat.getBiz());
                    weChatService.update(map, where);
                    Log.info(DateUtil.now() + " 账号：" + lastWeChat.getAccount() + "\t入库：" + count + "\t有效发布时间数：" + publishTimeMap.size() + "\t抓取：" + articleList.size() + "\t下次抓取时间：" + map.get("nextPubTime"));
                }
            } catch (Exception e) {
                Log.info(DateUtil.now() + " 账号：" + lastWeChat.getAccount() + "\t出错" + e.toString() + "\n" + data);
                e.printStackTrace();
            }
        }
        WeChat weChat;
        try {
            weChat = staticWeChatList.take();
            String accountNew = weChat.getAccount();
            weChatMap.put(accountNew, weChat);
            int random = (int) (Math.random() * 4 + 10) * 10000;
            if ("luobobaogao".equals(account)) {
                random = 1000;
            }
            if (!shouldSpider()) {
                random = 6 * 60 * 60 * 1000 + 10000;
            }
            return accountNew.toLowerCase();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = account;
        return null;
    }

    @RequestMapping(value = "getOtherArticle")
    @ResponseBody
    public boolean getOtherArticle(@RequestParam(value = "otherArticles") String data, @RequestParam(value = "originalStr")String originalStr) throws UnsupportedEncodingException {
        List<OtherArticle> articles = com.alibaba.fastjson.JSONObject.parseArray(data, OtherArticle.class);
        String site = articles.get(0).getSite();
        for (OtherArticle article : articles) {
            if (article.getCateName().contains("凤凰")) {
                article.setIsFenghuang(1);
            } else {
                article.setIsFenghuang(0);
            }
            if (article.getIsFenghuang()==1) {//只入库包含 "凤凰"的
                try {
                    if (otherArticleDao.selectOne(new MongoSelect().where("site",article.getSite()).where("title", article.getTitle())) == null) {
                        otherArticleDao.insertOne(article);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        TextFile.write(String.format(filePath, site + "_" + System.currentTimeMillis()), originalStr);
        return true;
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    private void alert() {
        long time = System.currentTimeMillis();
        if (time - TIMETAP >= 10 * 60 * 1000 && swich && shouldSpider()) {
            Log.sendMail("zhusy@ifeng.com", "微信客户端抓取停止", "最后抓取时间:" + DateUtil.formatDateTime(TIMETAP));
        }
    }

//    @Scheduled(cron = "0 59 23 * * ?")
    private void resetStatus() {
//        uinCountMap.clear();
        try {
            weChatService.resetStatus();
            Log.info(DateUtil.now() + "状态重置成功");
        } catch (Exception e) {
            e.printStackTrace();
            Log.info(DateUtil.now() + "状态重置失败");
        }
    }

    public static boolean shouldSpider() {
        Date start = DateUtil.parseTime("01:00:00");
        Date end = DateUtil.parseTime("07:00:00");
        String nowTime = DateUtil.formatTime(System.currentTimeMillis());
        Date now = DateUtil.parseTime(nowTime);
        return !(now.after(start) && now.before(end));
    }

    @Scheduled(cron = "0 59 23 * * ?")
    private void calcLastDayPubTime() {
        MongoSelect select = new MongoSelect();
        Map<Integer, Map<String, Integer>> resultMap = new TreeMap<>();
        String today = DateUtil.today() + " 00:00:00";
        String tomorrow = DateUtil.tomorrow() + " 00:00:00";
        select.where("type", "weixin")
                .where("create_time", WhereType.GreaterAndEqual, today)
                .where("create_time", WhereType.LessAndEqual, tomorrow);
        try {
            List<Article> articleList = articleService.selectList(select);
            for (Article article : articleList) {
                if (resultMap.containsKey(article.getCateId())) {
                    Map<String, Integer> temp = resultMap.get(article.getCateId());
                    if (temp.containsKey(article.getCreateTime())) {
                        temp.put(article.getCreateTime(), temp.get(article.getCreateTime()) + 1);
                    } else {
                        temp.put(article.getCreateTime(), 1);
                    }
                } else {
                    Map<String, Integer> temp = new TreeMap<>();
                    temp.put(article.getCreateTime(), 1);
                    resultMap.put(article.getCateId(), temp);
                }
            }
            for (Map.Entry entry : resultMap.entrySet()) {
                Map<String, Integer> temp = (Map<String, Integer>) entry.getValue();
                List<String> pubTimes = new ArrayList<>();
                for (Map.Entry entryTemp : temp.entrySet()) {
                    pubTimes.add(((String) entryTemp.getKey()).substring(11));
                }
                Map<String, Object> map = new HashedMap();
                map.put("pubTimes", pubTimes);
                map.put("pubTimesIndex", 0);
                map.put("nextPubTime", DateUtil.tomorrow() + " " + pubTimes.get(0));
                Where where = new Where("cateid", entry.getKey());
                weChatService.update(map, where);
//                System.out.println(weChatMongoCli.update(map, where));
            }
            Log.info(DateUtil.now() + "发布时间更新成功");
        } catch (Exception e) {
            Log.info(DateUtil.now() + "发布时间更新失败");
            e.printStackTrace();
        }
    }

    private Article parseArticle(String url) {
        Article article = new Article();
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = document.html();
        String headImg = null;
        try {
            headImg = content.substring(content.indexOf("msg_cdn_url = \""), content.indexOf("msg_cdn_url = \"") + 200);
            headImg = headImg.substring(headImg.indexOf("http"), headImg.indexOf("\";"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        document.select("noscript").remove();
        String title = document.select("title").text();
        String account = document.select("#js_profile_qrcode > div > p:nth-child(3) > span").text();
        Elements select = document.select("[id=js_content]");
        String outerImg = "";
        if (document.select("#media > script").size() != 0) {
            outerImg = document.select("#media > script").html();
            if (outerImg.contains("http://")) {
                outerImg = "<img src=\"" + outerImg.substring(outerImg.indexOf("http://"), outerImg.indexOf(";") - 1) + "\"/>";
            } else if (outerImg.contains("https://")) {
                outerImg = "<img src=\"" + outerImg.substring(outerImg.indexOf("https://"), outerImg.indexOf(";") - 1) + "\"/>";
            }
        }
        Elements imgs = document.select("img");
        for (Element img : imgs) {
            String img_url = img.attr("data-src");
            img.attr("src", img_url);
        }
        Elements videos = document.select("[class=video_iframe]");
        for (Element video : videos) {
            String video_url = video.attr("data-src");
            if (video_url == null || "".equals(video_url)) {
                video_url = video.attr("src");
            }
            if (!"".equals(video_url) && video_url.length() > 2) {
                if (video.attr("src") != null && !"".equals(video.attr("src"))) {
                    video.removeAttr("src");
                }
                video.attr("src", video_url);
            }
        }
        if (videos.size() > 0) {
            content = outerImg + Jsoup.clean(select.html(), Whitelist.basicWithImages().addAttributes("iframe", new String[]{"height", "src", "width"})).replaceAll("&amp;", "&").replaceAll("iframe>", "video>");
            article.setHaveVideo(2);
        } else {
            content = outerImg + Jsoup.clean(select.html(), Whitelist.basicWithImages()).replaceAll("&amp;", "&");
            article.setHaveVideo(1);
        }
//        logger.info("Content: " + content);
        article.setSourceLink(url);
        article.setHeadImgUrl(headImg);
        article.setContent(content);
        article.setType("weixin");
        article.setTitle(title);
        article.setAccount(account);
        return article;
    }
}
