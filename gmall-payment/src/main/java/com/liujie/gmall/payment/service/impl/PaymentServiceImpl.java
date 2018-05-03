package com.liujie.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.liujie.gmall.PaymentInfo;
import com.liujie.gmall.PaymentStatus;
import com.liujie.gmall.config.ActiveMQUtil;
import com.liujie.gmall.payment.mapper.PaymentInfoMapper;
import com.liujie.gmall.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;


    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQr){
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQr);
        return paymentInfo;
    }

    public void UpdatePaymentInfo(String outTradeNo,PaymentInfo paymentInfoQr){
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);

        paymentInfoMapper.updateByExampleSelective(paymentInfoQr,example);

    }

    public void sendPaymentResult(String orderId,String result){

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection=null;
        try {
             connection = connectionFactory.createConnection();
             connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",orderId);
            mapMessage.setString("result",result);

            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();


        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    /*public boolean checkPayment(String outTradeNo){
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo  = getPaymentInfo(paymentInfoQuery);
        if(paymentInfo.getPaymentStatus()== PaymentStatus.PAID||
                paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
            return true;
        }
        System.out.println("初始化支付参数:"+paymentInfo.getOutTradeNo());
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+paymentInfo.getOutTradeNo()+"\" "+
                "  }");
        AlipayTradeQueryResponse response = null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){

            if("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())) {
                System.out.println("支付成功！！ = "+paymentInfo.getOutTradeNo()   );
                PaymentInfo paymentInfo4Upt = new PaymentInfo();
                paymentInfo4Upt.setPaymentStatus(PaymentStatus.PAID);
                UpdatePaymentInfo(paymentInfo.getOutTradeNo(), paymentInfo4Upt);

                sendPaymentResult(paymentInfo.getOutTradeNo(),"success");

                return true;
            }else{
                System.out.println("未支付！！ = "+paymentInfo.getOutTradeNo()   );
                return false;
            }
        } else {
            System.out.println("未支付！！ = "+paymentInfo.getOutTradeNo()   );
            return false;
        }


    }*/
    public boolean  checkPayment(PaymentInfo paymentInfoQuery){

        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery);
        if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID||paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
            return true;
        }
        System.out.println("初始化支付参数 = "+paymentInfo.getOutTradeNo()   );
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+paymentInfo.getOutTradeNo()+"\" "+
                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute( request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){

            if("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())) {
                System.out.println("支付成功！！ = "+paymentInfo.getOutTradeNo()   );
                PaymentInfo paymentInfo4Upt = new PaymentInfo();
                paymentInfo4Upt.setPaymentStatus(PaymentStatus.PAID);
                UpdatePaymentInfo(paymentInfo.getOutTradeNo(), paymentInfo4Upt);

                sendPaymentResult(paymentInfo.getOrderId(),"success");

                return true;
            }else{
                System.out.println("未支付！！ = "+paymentInfo.getOutTradeNo()   );
                return false;
            }
        } else {
            System.out.println("未支付！！ = "+paymentInfo.getOutTradeNo()   );
            return false;
        }
    }
    public void sendDelayPaymentResult(String outTradeNo,int delaySec,int checkCount){
        //发送支付结果
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MapMessage mapMessage= new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            producer.send(mapMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
    public void closePayment(String orderId){
        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }
}
