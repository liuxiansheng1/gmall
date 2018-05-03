package com.liujie.gmall.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liujie.gmall.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;
    @Reference
    ManageService manageService;

    @RequestMapping("list.html")
    public String list(SkuLsParams skuLsParams, HttpServletRequest request){
        SkuLsResult skuLsResult = listService.searchSkuinfoList(skuLsParams);

        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();

        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);

        List <BaseAttrValue> selectedValueList =  new ArrayList<>();

        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo =  iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

            for (BaseAttrValue baseAttrValue : attrValueList) {
                String urlParam = makeUrlParam(skuLsParams);

                baseAttrValue.setUrlParam(urlParam);
                if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0) {
                    for (String valueId : skuLsParams.getValueId()) {
                        if (valueId.equals(baseAttrValue.getId())) {
                            iterator.remove();


                            BaseAttrValue attrValueSelected = new BaseAttrValue();
                            attrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            attrValueSelected.setId(valueId);
                            attrValueSelected.setUrlParam(makeUrlParam(skuLsParams,valueId));
                            selectedValueList.add(attrValueSelected);
                        }
                    }
                }
            }



        }

        request.setAttribute("keyword",skuLsParams.getKeyword());

        request.setAttribute("selectedValueList",selectedValueList);


        request.setAttribute("attrList",attrList);



        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        request.setAttribute("skuLsInfoList",skuLsInfoList);



        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams,String...excludeValueIds){
        String urlParam = "";
        if(skuLsParams.getKeyword()!=null){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        if(skuLsParams.getCatalog3Id()!=null){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){

            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if(excludeValueIds!=null&&excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if(excludeValueId.equals(valueId)){
                        continue;
                    }
                }
                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam += "valueId="+valueId.toString();
            }
        }
        return urlParam;
    }
}
