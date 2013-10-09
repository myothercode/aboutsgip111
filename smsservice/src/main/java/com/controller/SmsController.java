package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * User: wula
 * Date: 13-10-9
 * Time: 下午11:02
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/sms")
public class SmsController {
    @RequestMapping("smsPage")
     public String smsPage(){
        return "smsPage";
     }
}
