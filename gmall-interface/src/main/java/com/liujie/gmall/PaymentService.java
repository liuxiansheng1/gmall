package com.liujie.gmall;

import com.liujie.gmall.PaymentInfo;

public interface PaymentService {
    public void savePaymentInfo(PaymentInfo paymentInfo);

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQr);

    public void UpdatePaymentInfo(String outTrade,PaymentInfo paymentInfoQr);

    public void sendPaymentResult(String orderId,String result);

    public boolean  checkPayment(PaymentInfo paymentInfoQuery);

    public void sendDelayPaymentResult(String outTradeNo,int delaySec,int checkCount);

    public void closePayment(String orderId);
}
