package com.liujie.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liujie.gmall.*;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SpuManageController {
    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return  "success";
    }


    @RequestMapping("spuListPage")
    public String getSpuListPage(){
        return "spuListPage";
    }


    @RequestMapping(value ="spuImageList" ,method = RequestMethod.GET)
    @ResponseBody
    public  List<SpuImage> getSpuImageList(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return spuImageList;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(HttpServletRequest httpServletRequest){
        String spuId = httpServletRequest.getParameter("spuId");
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map=new HashMap();
            map.put("total",spuSaleAttrValueList.size());
            map.put("rows",spuSaleAttrValueList);
            // String spuSaleAttrValueJson = JSON.toJSONString(map);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }


        return spuSaleAttrList;

    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuInfoList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        SpuInfo spuInfo =new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return spuInfoList;

    }
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr>  getBaseSaleAttr(){
        List<BaseSaleAttr> baseSaleAttr = manageService.getBaseSaleAttrList();
        return baseSaleAttr;

    }

    @RequestMapping(value = "onSale",method =RequestMethod.GET)
    @ResponseBody
    public void onSale(String skuId){

        SkuInfo skuInfo= manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
    }
}
