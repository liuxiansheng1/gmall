package com.liujie.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.liujie.gmall.*;
import com.liujie.gmall.payment.config.AlipayConfig;
import com.liujie.gmall.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("index")
    @LoginRequire
    public  String index(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        orderInfo.sumTotalAmount();

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("orderId",orderId);
        return "index";


    }

    @RequestMapping(value = "/alipay/submit",method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        paymentService.savePaymentInfo(paymentInfo);

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);

        Map<String,Object> bizContnetMap=new HashMap<>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        String bizContent = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(bizContent);
        String form=null;
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8" );

        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);

        return form;


    }
    @RequestMapping("/alipay/callback/return")
    public String PaymentReturn(){
        return "redirect://"+AlipayConfig.return_order_url;
    }


    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    public String paymentNotify(@RequestParam Map<String,String> paramMap , HttpServletRequest request){
        String sign = request.getParameter("sign");
        boolean isChecked = false;
        try {
            isChecked = AlipaySignature.rsaCheckV2(paramMap, AlipayConfig.alipay_public_key, "UTF-8");
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(!isChecked){
            return "fail";
        }

        String trade_status = paramMap.get("trade_status");
        if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            String out_trade_no = paramMap.get("out_trade_no");

            PaymentInfo paymentInfoQr = new PaymentInfo();
            paymentInfoQr.setOutTradeNo(out_trade_no);

            PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQr);

            if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID||paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else{

                //
                PaymentInfo paymentInfoUpdate = new PaymentInfo();
                paymentInfoUpdate.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUpdate.setCallbackTime(new Date());
                paymentInfoUpdate.setCallbackContent(paramMap.toString());

                paymentService.UpdatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoUpdate) ;

                return "success";
            }
        }
        return "fail";

    }

    @RequestMapping("sendResult")
    @ResponseBody
    public String sendPaymentResult(String orderId,String result){

        paymentService.sendPaymentResult(orderId,"success");
        return "has been sent";
    }

    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
            boolean result = paymentService.checkPayment(paymentInfo);
        return ""+result;
    }
}
