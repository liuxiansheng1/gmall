package com.liujie.gmall.gmallservice.task;

import com.liujie.gmall.OrderInfo;
import com.liujie.gmall.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class OrderTask {

    @Autowired
    OrderService orderService;

    /*@Scheduled(cron = "0/5 * * * * ?")
    public void work() throws InterruptedException {
        System.out.println("thread = ===============" + Thread.currentThread());
    }*/

    @Scheduled(cron="0/15 * * * * ?")
    public void checkOrderExpiredList(){
        System.out.println("开始扫描过期订单");
        List<OrderInfo> expiredOrderList = orderService.getExpiredOrderList();
        System.out.println("开始处理过期订单");
        long starttime = System.currentTimeMillis();
        for (OrderInfo orderInfo : expiredOrderList) {
            orderService.execExpiredOrder(orderInfo);
        }
        long costtime = System.currentTimeMillis() - starttime;
        System.out.println("一共处理"+expiredOrderList.size()+"个订单 共消耗"+costtime+"毫秒");
        System.out.println("扫描完成 " );

    }
}