package com.liujie.gmall.orderweb.controller;



import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liujie.gmall.*;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
public class OrderController {

   @Reference
   private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    @Reference
    private OrderService orderService;

    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){

        String userId = (String)request.getAttribute("userId");

        List<UserAddress> userAddressList = userService.getUserAddressList(userId);



        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);

        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);


       // String s = JSON.toJSONString(userAddressList);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("userAddressList",userAddressList);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        String tradeCode = orderService.genTradeCode(userId);

        request.setAttribute("tradeCode",tradeCode);

        return "trade";

    }
    @RequestMapping("submitOrder")
    @LoginRequire
    public  String submitOrder(OrderInfo orderInfo, HttpServletRequest request){
        //0 检查tradeCode
        String userId =(String) request.getAttribute("userId");
        String tradeCode = request.getParameter("tradeCode");
        boolean existsTradeCode = orderService.checkTradeCode(userId, tradeCode);
        if(!existsTradeCode){
            request.setAttribute("errMsg","该页面已失效，请重新结算！");
            return "tradeFail";
        }

        //1 初始化参数

        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        //2 校验  验价
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            if(!skuInfo.getPrice().equals( orderDetail.getOrderPrice())){
                request.setAttribute("errMsg","您选择的商品可能存在价格变动，请重新下单。");
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
            boolean flag = checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if(!flag){
                request.setAttribute("errMsg","库存不足"+orderDetail.getSkuName()+"，请重新下单。");
                return "tradeFail";
            }
        }


        //3  保存
        orderService.saveOrder(orderInfo);
        orderService.delTradeCode(userId);
        //4 重定向
        return "redirect://payment.gmall.com/index";

    }

    public boolean checkStock(String skuId,Integer skuNum){
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if("1".equals(result)){
            return true;
        }
        return false;


    }

    @RequestMapping(value = "orderSplit",method = RequestMethod.POST)
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMapJson = request.getParameter("wareSkuMap");
        List<OrderInfo> subOrderInfoList =  orderService.orderSplit(orderId,wareSkuMapJson);
        List wareSkuMapList = new ArrayList();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            wareSkuMapList.add(map);
        }

        return JSON.toJSONString(wareSkuMapList);
    }
}
