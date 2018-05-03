package com.liujie.gmall.passportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liujie.gmall.LoginRequire;
import com.liujie.gmall.UserInfo;
import com.liujie.gmall.UserService;
import com.liujie.gmall.passportweb.util.JwtUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @Value("${token.key}")
    String TOKEN_KEY;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping(value = "login",method = RequestMethod.POST)
    @ResponseBody

    public String login(HttpServletRequest request){

        String remoteAddr = request.getHeader("x-forwarded-for");

        String loginName = request.getParameter("loginName");
        String passwd = request.getParameter("passwd");
        if(loginName!=null&&passwd!=null){
            UserInfo userInfo = new UserInfo();
            userInfo.setLoginName(loginName);
            userInfo.setPasswd(passwd);
            UserInfo login = userService.login(userInfo);

            if(login==null){
                return "fail";
            }

            Map map = new HashMap();
            map.put("userId",login.getId());
            map.put("nickName",login.getNickName());
            String token = JwtUtil.encode(TOKEN_KEY, map, remoteAddr);
            return token;
        }
        return "fail";
    }
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        Map map = JwtUtil.decode(token,TOKEN_KEY, currentIp);
        if(map!=null){
            String userId = (String) map.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if(userInfo!=null){
                return "success";
            }
        }
        return "fail";

    }
    @Test
    public void test1(){
        String key = "liujie";
        String ip = "192.168.111.129";
        Map map = new HashMap();
        map.put("loginId","liujie");
        map.put("nickName","liuxiansheng");

        String encode = JwtUtil.encode(key, map, ip);
        System.out.println("encode = " + encode);

        Map<String, Object> decode = JwtUtil.decode(encode, key, ip);
        System.out.println("decode = " + decode);

    }

}
