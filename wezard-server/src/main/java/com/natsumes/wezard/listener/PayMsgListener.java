package com.natsumes.wezard.listener;

import com.alibaba.fastjson.JSON;
import com.natsumes.wezard.pojo.PayInfo;
import com.natsumes.wezard.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(queues = "payNotify")
public class PayMsgListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void process(String msg) {
        log.info("接收到消息 => {}", msg);
        PayInfo payInfo = JSON.parseObject(msg, PayInfo.class);

        if (payInfo.getPlatformStatus().equals("SUCCESS")) {
            //修改订单里的状态
            orderService.paid(payInfo.getOrderNo());
        }

    }
}
