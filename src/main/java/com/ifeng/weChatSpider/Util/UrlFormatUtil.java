package com.ifeng.weChatSpider.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhusy on 2016/9/6.
 */
public class UrlFormatUtil {
    public static String getFormatURL(String url) {
        String formatUrl;
        Pattern pattern_i = Pattern.compile(".*(/i.youku.com/i).*");
        // Pattern pattern_u = Pattern.compile(".*(/i.youku.com/u).*(?<!(videos))");
        Pattern pattern_u = Pattern.compile(".*(/i.youku.com/u).*");
        Pattern pattern_souhu = Pattern.compile(".*(tv.sohu.com).*");
        Pattern pattern_t = Pattern.compile(".*(toutiao.com).*");
        Pattern pattern_q = Pattern.compile(".*(v.qq.com).*(?<!(/videos))");
        Pattern pattern_iqiyi = Pattern.compile(".*(www.iqiyi.com).*");
        Pattern pattern_letv = Pattern.compile(".*(http://chuang.le.com/).*");
        Matcher matcher_i = pattern_i.matcher(url);
        Matcher matcher_u = pattern_u.matcher(url);
        Matcher matcher_t = pattern_t.matcher(url);
        Matcher matcher_q = pattern_q.matcher(url);
        Matcher matcher_souhu = pattern_souhu.matcher(url);
        Matcher matcher_iqiyi = pattern_iqiyi.matcher(url);
        Matcher matcher_letv = pattern_letv.matcher(url);
        if (matcher_i.matches()) {
            if (url.contains("videos")) {
                if (!url.contains("?order=1&page=")) {
                    formatUrl = url + "?order=1&page=1";
                } else {
                    formatUrl = url;
                }
            } else {
                formatUrl = url + "/videos?order=1&page=1";
            }
        } else if (matcher_u.matches()) {
            if (url.contains("videos")) {
                formatUrl = url + "/fun_ajaxload/?__rt=1&__ro=&v_page=1&page_num=%s&page_order=1&q=";
            } else {
                formatUrl = url + "/videos/fun_ajaxload/?__rt=1&__ro=&v_page=1&page_num=%s&page_order=1&q=";
            }
        } else if (matcher_t.matches()) {
            if (url.contains("?page_type=0")) {
                formatUrl = url;
            } else {
                formatUrl = url + "?page_type=0";
            }
        } else if (matcher_souhu.matches()) {
            if (url.contains("uid=")) {
                if (url.contains("&pg=%s&size=50")) {
                    url.replace("&pg=%s&size=50", "");
                }
                String uid = url.substring(url.indexOf("uid="), url.length());
                formatUrl = "http://my.tv.sohu.com/user/wm/ta/v.do?uid=" + uid + "&page=%s&size=50";
            } else {
                formatUrl = url;
            }
        } else if (matcher_q.matches()) {
            formatUrl = url;
        } else if (matcher_iqiyi.matches()) {
            if (url.contains("http://www.iqiyi.com/u/")) {
                String temp = url.replace("http://www.iqiyi.com/u/", "");
                int i = temp.indexOf("/");
                formatUrl = "http://www.iqiyi.com/u/api/V/video/get_paged_video?page_size=42&uid=" + temp.substring(0, i != -1 ? i : temp.length()) + "&page=%s";
            } else {
                formatUrl = url;
            }
        } else if (matcher_letv.matches()) {
            formatUrl = url + "queryvideolist?callback=&currentPage=%s";
        } else {
            formatUrl = url;
        }
        return formatUrl;
    }
}
