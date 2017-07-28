package com.ifeng.weChatSpider.Util;

import org.apache.log4j.Logger;

public abstract class Log {

    private static Logger logger = Logger.getLogger("");


    /**
     * 记录info信息
     *
     * @param msg 需要记录的字符串
     */
    public static void info(String msg) {
        logger.info(msg);
    }

    /**
     * 记录错误并发送邮件
     */

    public static void bug(Object msg) {
        logger.info(msg);
    }

    public static void error(String title, String msg) {
        logger.error("title:" + title + " | msg:" + msg);
        sendMail("", title, msg);
    }

    /**
     * @param maillist     收件人（example@example.com,...,example@example.com）
     * @param title        邮件标题
     * @param writeContent 邮件正文
     */
    public static String sendMail(String maillist, String title, String writeContent) {
        if ("".equals(maillist) || maillist == null) {
            maillist = "zhusy@ifeng.com";
        }
        String sysname = "spider";
        String copyto = "";//"mayz@ifeng.com";
        String postData = "maillist=" + maillist + "&content=" + writeContent + "&title=" + title + "&systemname=" + sysname + "&copyto=" + copyto;
        HttpAttr attr = HttpAttr.getDefaultInstance();
        String response = "-1";
        try {
                response = HttpHelper.postData("http://pmop.staff.ifeng.com/Cmpp/runtime/interface_54.jhtml", attr, postData, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}

