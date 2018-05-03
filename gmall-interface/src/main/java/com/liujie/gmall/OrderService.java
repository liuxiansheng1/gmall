package com.liujie.gmall;

import java.util.List;
import java.util.Map;

public interface OrderService {

    public  void saveOrder(OrderInfo orderInfo);

    public String genTradeCode(String userId);

    public boolean checkTradeCode(String userId,String tradeCodePage);

    public void delTradeCode(String userId);

    public OrderInfo getOrderInfo(String orderId);

    public void updateOrderStatus(String orderId, ProcessStatus processStatus);

    public void sendOrderStatus(String orderId);

    public List<OrderInfo> getExpiredOrderList();

    public void execExpiredOrder(OrderInfo orderInfo);

    public String initWareOrder(String orderId);

    public Map initWareOrder(OrderInfo orderInfo);

    public List<OrderInfo> orderSplit(String orderId,String wareSkuMapJson);


}
