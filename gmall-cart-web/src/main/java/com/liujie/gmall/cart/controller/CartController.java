package com.liujie.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liujie.gmall.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.rmi.server.InactiveGroupException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Autowired
    CartCookHandler cartCookHandler;

    @Reference
    ManageService manageService;



    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        String userId = (String)request.getAttribute("userId");



        if(userId!=null){



            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else{
            cartCookHandler.addToCart(request,response,userId,skuId,Integer.parseInt(skuNum));
        }



        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String getCartList(HttpServletRequest request,HttpServletResponse response){
        String userId = (String)request.getAttribute("userId");
        if(userId!=null){
            List<CartInfo> cartListFromCookie = cartCookHandler.getCartList(request);
            List<CartInfo> cartList =null;
            if(cartListFromCookie!=null&&cartListFromCookie.size()>0){
                 cartList = cartService.mergeToCartList(cartListFromCookie, userId);


                cartCookHandler.deleteCartList(request,response);
            }else{

                cartList = cartService.getCartList(userId);
            }

            request.setAttribute("cartList",cartList);
        }else{
            List<CartInfo> cartList = cartCookHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }
        return "cartList";

    }

    @RequestMapping(value = "checkCart",method =RequestMethod.POST)
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String  userId = (String)request.getAttribute("userId");

        if(userId!=null){
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            cartCookHandler.checkCart(request,response,skuId,isChecked);
        }

    return ;

    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");
        List<CartInfo> cartListFromCookie = cartCookHandler.getCartList(request);
        if(cartListFromCookie!=null&&cartListFromCookie.size()>0){
            //1 合并到后台
            List<CartInfo> cartList = cartService.mergeToCartList(cartListFromCookie, userId);
            //2 cookie中的删除掉
            cartCookHandler.deleteCartList(request,response);
        }
        return "redirect://order.gmall.com/trade";
    }
}
