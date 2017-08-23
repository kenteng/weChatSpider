package com.ifeng.weChatSpider.WeChatTest.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhanggq on 2017/8/22.
 */
public class Users {
    protected static HashMap<String, String> user = new HashMap<String, String>(){
        {
            put("869345286@qq.com", "suwenxing123");
            put("sun-ideas", "suwenxing123");
            put("suwenxing258@163.com", "869345286?");
            put("zhanggq@ifeng.com", "ifeng1234");
        }
    };

    protected static List<String> userName = new ArrayList<String>(){
        {
            add("869345286@qq.com");
            add("sun-ideas");
            add("zhanggq@ifeng.com");
            add("suwenxing258@163.com");
        }
    };

    public boolean addUser(String account, String pwd){
        Boolean result;
        if (user.keySet().contains(account)){
            System.out.println("当前用户已存在！");
            result = false;
        }else {
            userName.add(account);
            user.put(account, pwd);
            System.out.println("添加成功！当前用户总数为：" + userName.size());
            result = true;
        }
        return result;
    }
}
