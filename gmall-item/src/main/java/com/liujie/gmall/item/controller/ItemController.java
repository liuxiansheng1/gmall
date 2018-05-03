package com.liujie.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liujie.gmall.*;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class ItemController {

    @Reference
    ManageService manageService;

   // @Reference
    //ListService listService;

   /* @RequestMapping("/{skuId}.html")
    public String item(@PathVariable String skuId, Model model){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);

        List<SpuSaleAttr> saleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);

        model.addAttribute("saleAttrList",saleAttrList);

        List<SkuSaleAttrValue> saleAttrValueList = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        String valueIdKey = "";
        Map valueIdSkuMap = new HashMap();
        for (int i = 0; i <saleAttrValueList.size() ; i++) {
            SkuSaleAttrValue skuSaleAttrValue =  saleAttrValueList.get(i);
            if(valueIdKey.length()!=0){
               valueIdKey = valueIdKey+"|";
            }
            valueIdKey=valueIdKey+skuSaleAttrValue.getSaleAttrValueId();
            if(i+1==saleAttrValueList.size()||skuSaleAttrValue.getSkuId().equals(saleAttrValueList.get(i+1).getSkuId())){
                valueIdSkuMap.put(valueIdKey,skuSaleAttrValue.getSkuId());
                valueIdKey="";
            }
        }
        String valuesSkuJson  =  JSON.toJSONString(valueIdSkuMap);
        model.addAttribute("valuesSkuJson",valuesSkuJson);
        return "item";
    }*/
    @RequestMapping("/{skuId}.html")
    @LoginRequire
    public String item(@PathVariable String skuId, Model model){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);

        List<SpuSaleAttr> saleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);

        model.addAttribute("saleAttrList",saleAttrList);

        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());


        //把列表变换成 valueid1|valueid2|valueid3 ：skuId  的 哈希表 用于在页面中定位查询
        String valueIdsKey="";

        Map<String,String> valuesSkuMap=new HashMap<>();

        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            if(valueIdsKey.length()!=0){
                valueIdsKey= valueIdsKey+"|";
            }
            valueIdsKey=valueIdsKey+skuSaleAttrValue.getSaleAttrValueId();

            if((i+1)== skuSaleAttrValueListBySpu.size()||!skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())  ){

                valuesSkuMap.put(valueIdsKey,skuSaleAttrValue.getSkuId());
                valueIdsKey="";
            }

        }

        //把map变成json串
        String valuesSkuJson = JSON.toJSONString(valuesSkuMap);

        model.addAttribute("valuesSkuJson",valuesSkuJson);


       // listService.incrHotScore( skuId);
        return "item";
    }

}

