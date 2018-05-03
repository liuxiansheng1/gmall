package com.liujie.gmall.gmallmanageweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserManagerController {

    @RequestMapping("index")
    public String index(){
        return "index";
    }


     @RequestMapping("attrListPage")
     public String getAttrListPage(){
            return "attrListPage";
     }

}
