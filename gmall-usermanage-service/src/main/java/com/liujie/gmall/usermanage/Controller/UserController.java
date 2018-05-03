package com.liujie.gmall.usermanage.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liujie.gmall.UserInfo;
import com.liujie.gmall.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Reference
    UserService userService;

    @RequestMapping("addUser")
    public ResponseEntity addUser(UserInfo userInfo){
        userService.addUser(userInfo);
        return ResponseEntity.ok().build();

    }
}
