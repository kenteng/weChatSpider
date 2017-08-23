package com.ifeng.weChatSpider.WeChatTest.login;

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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by zhanggq on 2017/8/22.
 * 操作流程为：
 *      1.登录HOST：https://mp.weixin.qq.com/
 *      2.填写用户名、密码，点击登录，跳转至二维码页面，LOGIN_URL：https://mp.weixin.qq.com/cgi-bin/bizlogin?action=validate&lang=zh_CN&account=
 *        需拼接公众号账户名
 *      3.手机扫码登录，跳转至公众号首页，HOME_URL：https://mp.weixin.qq.com/cgi-bin/home?t=home/index&lang=zh_CN&token=
 *        需拼接token，比较是否包含即可
 */
public class Login implements Runnable{
    //初始化weixinInfos，得到公众号阻塞队列，由多个线程共同完成
    protected static BlockingQueue<WeChat> weixinInfos = new LinkedBlockingDeque<>();
    protected static List<String> status = Arrays.asList(new String[]{"-1", "9"});
    static {
        MongoCli Cli = getWeChatMongoCli();
        MongoSelect select = new MongoSelect();
        select.where("createTime", WhereType.GreaterAndEqual, "2017-01-01 00:00:00")
                .where("biz", WhereType.Like, "\\w+")
                .where("runStatus", "1")
                .where("status", WhereType.NotIn, status)
                .orderBy("spider_date", OrderByDirection.ASC);
        Cli.changeDb("spider");
        Cli.getCollection("Weixin");
        try {
            List<WeChat> list = Cli.selectList(select, WeChat.class);
            for (int i = 0; i < list.size(); i++){
                weixinInfos.put(list.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        // 设置 chrome 的路径（如果你安装chrome的时候用的默认安装路径，则可省略这步）
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        System.getProperties().setProperty("webdriver.chrome.driver", "C:\\Users\\zhanggq\\chromedriver.exe");
    }

    private String ACCOUNT = "";
    private String PWD = "";
    private final long FIVEDAYMIL = 5 * 24 * 3600 * 1000;
    private String token = "";
    private long TIMETAP = System.currentTimeMillis();
    //登录url，需拼接个人账户名
    private String LOGIN_URL = "https://mp.weixin.qq.com/cgi-bin/bizlogin?action=validate&lang=zh_CN&account=";
    //公众号首页url
    private final String HOST = "https://mp.weixin.qq.com/";
    //个人公众号主页url，需拼接token
    private final String HOME_URL= "https://mp.weixin.qq.com/cgi-bin/home?t=home/index&lang=zh_CN&token=";
    private WebDriver webDriver = new ChromeDriver();
    private WebDriver newDriver = null;

    @Override
    public void run() {
        try {
            getWeChat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getWeChat() throws Exception {
        //拼接账户，得到登录地址
        LOGIN_URL = LOGIN_URL + ACCOUNT;
        int id = 0;
//        int totalCount = 0;
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

        //模拟登陆，获取web中的token和cookie
        while(cookie == null || "".equals(cookie) || token==null || "".equals(token)){
            cookie = analogLogin();
        }

        url = "https://mp.weixin.qq.com/cgi-bin/appmsg?token=" + token + "&lang=zh_CN&f=json&ajax=1&random=0.0932841953962531&action=list_ex&begin=0&count=5&query=&fakeid=%s&type=9";
        while (true) {
            if (weixinInfos.isEmpty()) {
                System.out.println("遍历结束");
                break;
            } else {
                WeChat weixinInfo = weixinInfos.take();
                Log.info("当前公众号：" + weixinInfo.getAccount());
                String reslut = null;
                try {
                    reslut = downloader(String.format(url, URLEncoder.encode(weixinInfo.getBiz(), "UTF-8")), cookie);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error("", "----Download Exception:" + e + "-------");
                    Log.sendMail("zhanggq@ifeng.com", "Error", "Error Occurred" + e);
                }
                if (reslut == null) {
//                    continue;
                }
                JSONObject jsonObject = JSONObject.parseObject(reslut);
                //判断是否有效
                String ret = jsonObject.getJSONObject("base_resp").getString("ret");
                String err_msg = jsonObject.getJSONObject("base_resp").getString("err_msg");
                if ("200003".equals(ret) || "invalid session".equals(err_msg)) {
                    System.out.println("200003失效======================");
                    Log.info("200003失效===============");
                    //失效，重新登录
                    //continue;
                } else {
                    JSONArray list = jsonObject.getJSONArray("app_msg_list");
                    if (list == null) {
                        System.out.println(Thread.currentThread().getName() + "  " + (++id) + "--------账号：" + weixinInfo.getAccount() + " 失败" + DateUtil.now());
                    } else {
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
                        System.out.println(Thread.currentThread().getName() + "  " + (++id) + "--------账号：" + weixinInfo.getAccount() + " 入库" + count + "篇文章，共抓取" + articleLists.size() + "篇---------" + DateUtil.now());
                    }
                }
                //每个公众号抓取后休息3秒
                Thread.sleep(3000);
            }
        }
        System.out.println(Thread.currentThread().getName() + " 遍历结束 ------ " + DateUtil.now());
    }

    /**
     * 获取列表
     * @param url
     * @param cookie
     * @return
     */
    private String downloader(String url, String cookie) {
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

    public static MongoCli getWeChatMongoCli() {
        List<ServerAddress> serverAddresses = new ArrayList<>();
        ServerAddress s = new ServerAddress("10.50.16.15", 27017);
        serverAddresses.add(s);
        List<MongoCredential> credentials = new ArrayList<>();
        MongoCredential mongoCredential = MongoCredential.createCredential("spider", "spider", "aT4QTEThwkfDZWAEJb4B".toCharArray());
        credentials.add(mongoCredential);
        return new MongoCli(serverAddresses, credentials);
    }

    /**
     * 模拟登陆
     * @return 返回cookie和token
     */
    public String analogLogin() throws Exception {
        getErCode(0);
        long time_begin = System.currentTimeMillis();
        while (true){
            long time_end = System.currentTimeMillis();
            if (time_end - time_begin <= 10 * 60 * 1000){
                String currentUrl = webDriver.getCurrentUrl();
                currentUrl = currentUrl.replaceAll("%40","@");
                System.out.println("当前页面的地址为：" + currentUrl);
                if (!currentUrl.equals(LOGIN_URL) && !currentUrl.equals(HOST)){
                    System.out.println("登录成功~");
                    Log.info("登录成功");
                    break;
                }else {
                    System.out.println("登录失败！");
                    Log.info("登录失败");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                //超过时间，重新获取二维码
                System.out.println("二维码过期重新扫描");
                getErCode(1);
                time_begin = System.currentTimeMillis();
            }
        }
        //获取当前主页链接
        String home_url = webDriver.getCurrentUrl();
        if (!home_url.contains(HOME_URL)){
            System.out.println("非法操作");
            Log.info("不合法的网址");
            return "";
        }else {
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
            webDriver.get(HOST);
            WebElement account = webDriver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[1]/div/span/input"));
            account.sendKeys(ACCOUNT);
            WebElement pwd = webDriver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[2]/div/span/input"));
            pwd.sendKeys(PWD);
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
            //获取二维码截图
            BufferedImage image=getShot(erCode);
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
            System.out.println("图片本地化失败");
            Log.sendMail("zhanggq@ifeng.com", "getErCode",e.toString());
        }
    }

    private String getFormatUrl(String imgUrl) {
        imgUrl = imgUrl.replace("\\", "/");
        HttpAttr attr = HttpAttr.getDefaultInstance();
        return HttpHelper.getData("http://pmop.staff.ifeng.com/Cmpp/runtime/interface_405.jhtml?locationUrl=http://pmopsource.staff.ifeng.com/source2/pmop/storage_main/"+ imgUrl,attr,"utf-8");
    }

    /**
     * 获取二维码截图
     * @param erCode
     * @return
     */
    private BufferedImage getShot(WebElement erCode) {
        Point location = erCode.getLocation();
        Dimension size = erCode.getSize();
        BufferedImage croppedImage = null;
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(takeScreenshot()));
            //截取webElement所在位置的子图
            croppedImage = originalImage.getSubimage(
                    location.getX(),
                    location.getY(),
                    size.getWidth(),
                    size.getHeight()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return croppedImage;
        }
    }

    public  byte[] takeScreenshot() throws IOException {
        WebDriver augmentedDriver = new Augmenter().augment(webDriver);
        return ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.BYTES);
    }

    /**
     *定时滚动滚动条保持在线状态
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    private void keepLive(){
        System.out.println("*****定时触发浏览器*******");
        ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(100,100)");
        ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(-100,-100)");
    }

    /**
     * 菜单操作
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

    private String getCookieAndToken(){
        String currentURL = webDriver.getCurrentUrl();
        int i = currentURL.indexOf("token");
        if (i < 0){
            Log.info("未找到token，请检查网址");
            return null;
        }else {
            token = currentURL.substring(i + 6);
            System.out.println(currentURL);
            //转换为json格式
            JSONArray jsonArray = (JSONArray) JSONObject.toJSON(webDriver.manage().getCookies());
            return parseCookie(jsonArray);
        }
    }

    private String parseCookie(JSONArray jsonArray) {
        String cookie = "";
        Iterator<Object> iterator = jsonArray.iterator();
        while (iterator.hasNext()){
            JSONObject object = (JSONObject) iterator.next();
            cookie += object.getString("name") + "=" + object.getString("value") + "; ";
        }
        cookie = cookie.substring(0, cookie.length() - 2);
        System.out.println(cookie);
        return cookie;
    }

    public void setACCOUNT(String ACCOUNT){
        this.ACCOUNT = ACCOUNT;
    }

    public void setPWD(String PWD){
        this.PWD = PWD;
    }
}
