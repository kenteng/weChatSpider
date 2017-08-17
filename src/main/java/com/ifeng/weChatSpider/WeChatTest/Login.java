package com.ifeng.weChatSpider.WeChatTest;

import com.alibaba.fastjson.JSONArray;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.ifeng.weChatSpider.Bean.Article;
import com.ifeng.weChatSpider.Bean.ArticleList;
import com.ifeng.weChatSpider.Bean.WeChat;
import com.ifeng.weChatSpider.Mongo.*;
import com.ifeng.weChatSpider.Util.DateUtil;
import com.ifeng.weChatSpider.Util.HttpAttr;
import com.ifeng.weChatSpider.Util.HttpHelper;
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
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.junit.Test;
import org.openqa.selenium.remote.Augmenter;
import org.springframework.scheduling.annotation.Scheduled;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xufh on 2017/7/25.
 */
public class Login {

    private static final String HOST = "https://mp.weixin.qq.com/";
    private static String ACCOUNT = "suwenxing258@163.com";
    private static String PWD = "869345286?";
    private static HashMap<String, String> user = new HashMap<String, String>(){
        {
            put("sun-ideas", "suwenxing123");
            put("suwenxing258@163.com", "869345286?");
            put("869345286@qq.com", "suwenxing123");
        }
    };
    private static List<String> userName = new ArrayList<String>(){
        {
            add("suwenxing258@163.com");
            add("869345286@qq.com");
            add("sun-ideas");
        }
    };
    //change记录账户变化次数
    int change = 0;

    private static String LOGIN_URL = "https://mp.weixin.qq.com/cgi-bin/bizlogin?action=validate&lang=zh_CN&account=" + ACCOUNT;
    private static long TIMETAP = System.currentTimeMillis();
    private static final String HOME_URL= "https://mp.weixin.qq.com/cgi-bin/home?t=home/index&lang=zh_CN&token=";
    private static final long FIVEDAYMIL = 5 * 24 * 3600 * 1000;
    public static List<String> status = Arrays.asList(new String[]{"-1", "9"});

    static {
        // 设置 chrome 的路径（如果你安装chrome的时候用的默认安装路径，则可省略这步）
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        System.getProperties().setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
    }

    WebDriver webDriver = new ChromeDriver();
    WebDriver newDriver = null;
    String token = "";

    @Test
    public void getWechat() throws Exception {
        int id = 0;
        int totalCount = 0;
        String cookie = "";
        String url = "";
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
        //模拟登陆，获取web中的token和cookie

        while(cookie == null || "".equals(cookie) || token==null || "".equals(token)){
            cookie = analogLogin();   //获取cookie 和 token
        }

        url = "https://mp.weixin.qq.com/cgi-bin/appmsg?token=" + token + "&lang=zh_CN&f=json&ajax=1&random=0.0932841953962531&action=list_ex&begin=0&count=5&query=&fakeid=%s&type=9";

        while (true) {
            List<WeChat> weixinInfos = mongoCli.selectList(select, WeChat.class);
            for (WeChat weixinInfo : weixinInfos) {
                //System.out.println("公众号名" + weixinInfo.getAccount());
                Log.info("当前公众号：" + weixinInfo.getAccount());
                String reslut = null;
                try {
                    reslut = downloader(String.format(url, URLEncoder.encode(weixinInfo.getBiz(), "UTF-8")), cookie);
                } catch (Exception e){
                    e.printStackTrace();
                    Log.error("" ,"----Download Exception:" + e +"-------");
                    Log.sendMail("zhanggq@ifeng.com","Error","Error Occurred"+e);
                }
                if(reslut == null){
                    continue;
                }
                JSONObject jsonObject = JSONObject.parseObject(reslut);
                //判断是否有效
                String ret = jsonObject.getJSONObject("base_resp").getString("ret");
                String err_msg = jsonObject.getJSONObject("base_resp").getString("err_msg");
                if("200003".equals(ret) || "invalid session".equals(err_msg)){
                    System.out.println("200003失效======================");
                    Log.info("200003失效===============");
                    //失效，重新登录
                    change++;
                    try {
                        ACCOUNT = userName.get(change%userName.size());
                        PWD = user.get(ACCOUNT);
                        int loc = LOGIN_URL.lastIndexOf("=");
                        LOGIN_URL = LOGIN_URL.substring(0, loc+1) + ACCOUNT;
                        webDriver.quit();
                        newDriver.quit();
                        webDriver = new ChromeDriver();
                        cookie = analogLogin();   //获取cookie 和 token
                        url = "https://mp.weixin.qq.com/cgi-bin/appmsg?token=" + token + "&lang=zh_CN&f=json&ajax=1&random=0.0932841953962531&action=list_ex&begin=0&count=5&query=&fakeid=%s&type=9";
                        Thread.sleep(1000* 10);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //continue;
                }else{
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
                            long time = System.currentTimeMillis() - (ar.getLong("update_time") * 1000);
                            if (!"".equals(article.getTitle()) && !"".equals(article.getUrl()) && time <= FIVEDAYMIL) {
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
                                weChatMongoCli.insert(article);
                                Log.info("入库:" + new Gson().toJson(article));
                            }
                        }
                        Map<String, Object> map = new HashedMap();
                        map.put("spider_date", DateUtil.now());//更新最新抓取时间
                        map.put("lastNum", count);//更新本次抓取是否有入库文章
                        Where where = new Where("biz", weixinInfo.getBiz());
                        mongoCli.update(map, where);
                        System.out.println((++id) + "--------账号：" + weixinInfo.getAccount() + " 入库" + count + "篇文章，共抓取" + articleLists.size() + "篇---------" + DateUtil.now());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println((++id) + "--------账号：" + weixinInfo.getAccount() + " 失败" + DateUtil.now());
                    }
                }
                //每个公众号抓取后休息3秒
                Thread.sleep(5000);
            } //for循环结束
            id = 0;
            System.out.println("--------第" + (++totalCount) + "轮结束---------" + DateUtil.now());
            //每次轮询完所有公众号后休息8秒
            Thread.sleep(1000*8);

            //每10个轮询切换账号
            if (totalCount >= change * 10){
                change++;
                try {
                    ACCOUNT = userName.get(change%userName.size());
                    PWD = user.get(ACCOUNT);
                    int loc = LOGIN_URL.lastIndexOf("=");
                    LOGIN_URL = LOGIN_URL.substring(0, loc+1) + ACCOUNT;
                    webDriver.quit();
                    newDriver.quit();
                    webDriver = new ChromeDriver();
                    cookie = analogLogin();   //获取cookie 和 token
                    url = "https://mp.weixin.qq.com/cgi-bin/appmsg?token=" + token + "&lang=zh_CN&f=json&ajax=1&random=0.0932841953962531&action=list_ex&begin=0&count=5&query=&fakeid=%s&type=9";
                    Thread.sleep(1000* 20);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }//while结束
    }
    /**
     * 登录失败获取cookie和token
     * @return 模拟登录返回token和cookie值
     * */
    public String analogLogin() throws Exception{
        getErCode(0);
        //是否登录
        long time_begin = System.currentTimeMillis();
        while(true){
            long time_end = System.currentTimeMillis();
            //10分钟
            if(time_end - time_begin <= 10 * 60 * 1000){
                String currUrl = webDriver.getCurrentUrl();
                currUrl = currUrl.replaceAll("%40","@");
                System.out.println(currUrl);
                if(!Login.LOGIN_URL.equals(currUrl) && !Login.HOST.equals(currUrl)){
                    System.out.println("登陆成功");
                    //获取到cookie和token
                    //getCookieAndToken();
                    Log.info("登录成功");
                    break;
                }else{
                    System.out.println("登陆失败");
                    Log.info("登录失败");
                    Thread.sleep(2000);
                }
            }else{
                //大于60秒二维码失效，重新获取
                System.out.println("二维码过期重新扫描");
                getErCode(1);
                time_begin = System.currentTimeMillis();
            }
        }
        //获取主页当前链接
        String homeUrl = webDriver.getCurrentUrl();
        if(!homeUrl.contains(Login.HOME_URL)){
            System.out.println("非法操作");
            Log.info("不合法的网址");
            return "";
        }else{
            //合法则进行自动操作
            menu();
            //获取当前页面的cookie
            return getCookieAndToken();
        }
    }
    /**
     * 获取登录二维码并发送邮件
     *return 二维码地址
     */
    private void getErCode(int flag){
        try {
            webDriver.get(Login.HOST);
            WebElement account = webDriver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[1]/div/span/input"));
            account.sendKeys(Login.ACCOUNT);
            WebElement pwd = webDriver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[2]/div/span/input"));
            pwd.sendKeys(Login.PWD);
            WebElement sub_btn = webDriver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[4]/a"));
            sub_btn.click();
            //获取界面的二维码地址
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                System.exit(0);//退出程序
            }

            WebElement erCode = webDriver.findElement(By.xpath("//*[@id=\'body\']/div/div[2]/div[6]/div/div[1]/img"));
            while(true){
                if(erCode != null){
                    break;
                }else{
                    erCode = webDriver.findElement(By.xpath("//*[@id=\'body\']/div/div[2]/div[6]/div/div[1]/img"));
                }
            }
            //
            BufferedImage image=getShot(webDriver, erCode);    //获取二维码截图
            Date date=new Date();
            String year=new SimpleDateFormat("yyyy").format(date);
            String imgUrl=year+"\\ercode\\"+UUID.randomUUID()+".jpg";
            ImageIO.write(image,"jpg",new File("X:\\pmop\\storage_main\\"+imgUrl)); //本地化

            String sendUrl=getFormatUrl(imgUrl);
            System.out.println(sendUrl);
            if(flag == 0){
                Log.sendMail("zhanggq@ifeng.com", "微信公众号二维码扫描" + DateUtil.formatDateTime(TIMETAP), sendUrl);  //发送邮件
            }else{
                Log.sendMail("zhanggq@ifeng.com", "微信公众号二维码过期扫描" + DateUtil.formatDateTime(TIMETAP), sendUrl);  //发送邮件
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.sendMail("zhanggq@ifeng.com", "getErCode",e.toString());
        }
    }
    /**
     *获取二维码截图
     *
     */
    public BufferedImage getShot(WebDriver webDriver,WebElement webElement) throws IOException {
        Point location=webElement.getLocation();
        Dimension size=webElement.getSize();
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(takeScreenshot(webDriver)));
        // 截取webElement所在位置的子图。
        BufferedImage croppedImage = originalImage.getSubimage(
                location.getX(),
                location.getY(),
                size.getWidth(),
                size.getHeight());
        return croppedImage;
    }

    public  byte[] takeScreenshot(WebDriver driver) throws IOException {
        WebDriver augmentedDriver = new Augmenter().augment(driver);
        return ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.BYTES);
    }

    public String getFormatUrl(String url){
        url=url.replace("\\","/");
        HttpAttr attr=HttpAttr.getDefaultInstance();
        return  HttpHelper.getData("http://pmop.staff.ifeng.com/Cmpp/runtime/interface_405.jhtml?locationUrl=http://pmopsource.staff.ifeng.com/source2/pmop/storage_main/"+url,attr,"utf-8");
    }
    /**
     *定时滚动滚动条保持在线状态
     *
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    private void keepLive(){
        System.out.println("*****定时触发浏览器*******");
        ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(100,100)");
        ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(-100,-100)");
    }
    /**
     * 菜单操作
     *
     * */
    private void menu() throws Exception{
        //点击菜单栏素材管理
        WebElement element = webDriver.findElement(By.xpath("//*[@id='menuBar']/dl[4]/dd[3]/a"));
        element.click();
        Thread.sleep(1000);
        //点击新建图文素材
        element = webDriver.findElement(By.xpath("//*[@id=\'js_main\']/div[5]/div[2]/a[1]"));
        element.click();
        Thread.sleep(1000);
        //点击超链接进行插入文章
        //得到当前窗口的句柄
        String currentWindow = webDriver.getWindowHandle();
        //得到所有窗口的句柄
        Set<String> handles = webDriver.getWindowHandles();
        Iterator<String> it = handles.iterator();

        while(it.hasNext()){
            String handle = it.next();
            if(currentWindow.equals(handle)) continue;
            newDriver = webDriver.switchTo().window(handle);
            System.out.println("title,url = " + newDriver.getTitle() + "," +  newDriver.getCurrentUrl());
        }
        Thread.sleep(1000);
        element = newDriver.findElement(By.xpath("//*[@id=\'edui21_body\']/div"));
        element.click();
        Thread.sleep(1000);
        //选择查找文章
        ((JavascriptExecutor)webDriver).executeScript(
                "$('#checkbox3').click()"
        );
        Thread.sleep(1000);
        element = newDriver.findElement(By.xpath("//*[@id=\'myform\']/div[3]/div[3]/div[1]/div/span[1]/input"));
        element.sendKeys("来一只烤全羊");
        Thread.sleep(1000);
        element = newDriver.findElement(By.xpath("/html/body/div[11]/div/div[2]/form/div[3]/div[3]/div[1]/div/span[1]/a[2]"));
        element.click();
        Thread.sleep(1000);
        ((JavascriptExecutor)webDriver).executeScript(
                " $('.search_biz_info').eq(0).click();"
        );
        Thread.sleep(1000);
    }
    /**
     * 获取cookie和token
     * @return 返回格式化后的cookie
     * **/
    private String getCookieAndToken(){
        String currURL = webDriver.getCurrentUrl();
        int i = currURL.indexOf("token");
        if(i < 0){
            Log.info("没有找到token，请检查网址");
            return null;
        }else{
            token = currURL.substring(i + 6);
            System.out.println(currURL);
            //格式化json串
            JSONArray jsonArray = (JSONArray) JSONObject.toJSON(webDriver.manage().getCookies());

            return parseCookie(jsonArray);
        }
    }
    /**
     * 获取列表
     *
     * */
    public  String downloader(String url,String cookie) throws Exception{
        Log.info("downloading..." + url);
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(15000)
                .setConnectTimeout(15000)
                .setConnectionRequestTimeout(15000)
                .setStaleConnectionCheckEnabled(true)
                .build();
        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
            httpGet.addHeader("accept-encoding", "gzip, deflate, sdch, br");
            httpGet.addHeader("accept-language", "zh-CN,zh;q=0.8");
            httpGet.addHeader("cache-control", "no-cache");
            httpGet.addHeader("connection", "keep-alive");
            httpGet.addHeader("cookie", cookie);
            httpGet.addHeader("host", "mp.weixin.qq.com");
            httpGet.addHeader("pragma", "no-cache");
            httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.3176.400 QQBrowser/9.6.11520.400");
            httpGet.addHeader("x-requested-with", "XMLHttpRequest");
            CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                Log.info("downloaded..." + url);
                return EntityUtils.toString(httpEntity, "UTF-8");
            }
        }catch (Exception e) {
            e.printStackTrace();
            Log.info(DateUtil.today()+e.toString());
            //Log.sendMail("zhanggq@ifeng.com","downloader-1(Exception)",e.toString());
        } finally {
            try {
                closeableHttpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.info(DateUtil.today()+e.toString());
                Log.sendMail("zhanggq@ifeng.com","downloader-2(IOException)",e.toString());
            }
        }
        return null;
    }

    public  MongoCli getWeChatMongoCli() {
        List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
        ServerAddress s = new ServerAddress("10.50.16.15", 27017);
        serverAddresses.add(s);
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        MongoCredential mongoCredential = MongoCredential.createCredential("spider", "spider", "aT4QTEThwkfDZWAEJb4B".toCharArray());
        credentials.add(mongoCredential);
        return new MongoCli(serverAddresses, credentials);
    }
    /**
     * 格式化cookie
     *
     * **/
    private String parseCookie(JSONArray jsonArray){
        String cookie = "";

        Iterator<Object> it = jsonArray.iterator();
        while (it.hasNext()) {
            JSONObject ob = (JSONObject) it.next();
            cookie += ob.getString("name") + "=" + ob.getString("value") + "; ";
        }
        cookie = cookie.substring(0,cookie.length() - 2);
        System.out.println(cookie);
        return cookie;
    }
}