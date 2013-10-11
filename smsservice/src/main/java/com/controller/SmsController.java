package com.controller;

import com.domainVO.SmsBody;
import com.service.DataAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created with IntelliJ IDEA.
 * User: wula
 * Date: 13-10-9
 * Time: 下午11:02
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/sms")
public class SmsController{
    @Autowired
    private DataAccessService dataAccessService;

    @RequestMapping("smsPage")
     public String smsPage(){
        return "smsPage";
     }

    /*发送短信*/
    @RequestMapping(value = "sendSMS")
    @ResponseBody
    public Object sendSMS(HttpSession session,SmsBody smsBody){
        /*smsBody.setMsg("".equals(smsBody.getMsg())?null:smsBody.getMsg());
        smsBody.setPhoneNo("".equals(smsBody.getPhoneNo())?null:smsBody.getPhoneNo());
        smsBody.setServiceId("".equals(smsBody.getServiceId())?null:smsBody.getServiceId());*/
        Assert.notNull(smsBody.getServiceId()!=null&&!"".equals(smsBody.getServiceId()),"服务号为空!");
        Assert.notNull(smsBody.getPhoneNo()!=null&&!"".equals(smsBody.getPhoneNo()),"号码为空!");
        Assert.notNull(smsBody.getMsg()!=null&&!"".equals(smsBody.getMsg()),"内容为空!");
        dataAccessService.insertSend(smsBody);
        return "ok" ;
    }
}
