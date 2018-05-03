package com.liujie.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liujie.gmall.CartInfo;
import com.liujie.gmall.CookieUtil;
import com.liujie.gmall.ManageService;
import com.liujie.gmall.SkuInfo;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
@Component
public class CartCookHandler {
    public static final String COOKIE_CART_NAME="cart";
    public static final int COOKIE_MAX_NAME=7*24*3600;

    @Reference
    ManageService manageService;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, String userId, String skuId, Integer skuNum){
        String cartJson = CookieUtil.getCookieValue(request, COOKIE_CART_NAME, true);
       List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist = false;
        if(cartJson!=null){

             cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if(skuId.equals(cartInfo.getId())){

                    cartInfo.setSkuNum(cartInfo.getSkuNum());
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist=true;
                    break;
                }

            }

        }
         if(!ifExist){
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);

            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setSkuNum(skuNum);
            cartInfo.setUserId(userId);

            cartInfoList.add(cartInfo);

        }
        String jsonString = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request,response,COOKIE_CART_NAME,jsonString,COOKIE_MAX_NAME,true);

    }

    public List<CartInfo> getCartList(HttpServletRequest request){
        String cartJson = CookieUtil.getCookieValue(request, COOKIE_CART_NAME, true);

        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);


        return cartInfoList;
    }

    public void deleteCartList(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,COOKIE_CART_NAME);
    }

    public void checkCart(HttpServletRequest request,HttpServletResponse response,String skuId,String isChecked){
        List<CartInfo> cartList = getCartList(request);
        for (CartInfo cartInfo : cartList) {
            if(cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }
        String jsonString = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,COOKIE_CART_NAME,jsonString,COOKIE_MAX_NAME,true);


    }
}
