/**
 * MainController.java
 * Created by zhusy on 2017/3/21 0021 16:13
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class MainController {

    @RequestMapping(value = "helloWorld")
    public String helloWorld(HttpSession session){
        session.setAttribute("msg","Hello World!");
        return "hello_world";
    }
}
