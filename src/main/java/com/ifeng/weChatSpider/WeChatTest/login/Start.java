package com.ifeng.weChatSpider.WeChatTest.login;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhanggq on 2017/8/22.
 */
public class Start {
    public static void main(String[] args){
        int num = Users.userName.size() - 1;
        ExecutorService pool = Executors.newFixedThreadPool(num);
        for (int i = 0; i < num; i++){
            String account = Users.userName.get(i);
            String pwd = Users.user.get(account);
            Login login = new Login();
            login.setACCOUNT(account);
            login.setPWD(pwd);
            pool.execute(login);
        }
    }
}
