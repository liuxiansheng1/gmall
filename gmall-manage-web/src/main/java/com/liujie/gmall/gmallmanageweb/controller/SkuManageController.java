package com.liujie.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liujie.gmall.BaseAttrInfo;
import com.liujie.gmall.ManageService;
import com.liujie.gmall.SkuInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class SkuManageController {

    @Reference
    ManageService manageService;

    @RequestMapping("attrInfoList1")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(@RequestParam Map<String,String> map){
        String catalog3Id =   map.get("catalog3Id") ;
        List<BaseAttrInfo> attrList = manageService.getAttrList1(catalog3Id);
        return attrList;
    }

    @RequestMapping(value = "saveSku",method = RequestMethod.POST)
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }

    @RequestMapping(value="skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo>  getSkuInfoListBySpu(HttpServletRequest httpServletRequest){
        String spuId = httpServletRequest.getParameter("spuId");
        List<SkuInfo> skuInfoList = manageService.getSkuInfoListBySpu(spuId);
        return skuInfoList;
    }

}
