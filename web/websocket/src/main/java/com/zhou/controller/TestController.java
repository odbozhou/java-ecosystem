package com.zhou.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : zhoubo
 * @Project: java-framework
 * @Package controller
 * @Description: TODO
 * @date Date : 2018-11-22 下午10:37
 */
@RequestMapping("test")
@RestController
public class TestController {

    @RequestMapping("test")
    public Object test() {
        return "test";
    }
}
