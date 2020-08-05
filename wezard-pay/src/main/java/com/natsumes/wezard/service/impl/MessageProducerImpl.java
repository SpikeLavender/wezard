package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.pojo.PayInfo;
import com.natsumes.wezard.processor.RocketProcessor;
import com.natsumes.wezard.service.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;


@EnableBinding(RocketProcessor.class)
public class MessageProducerImpl implements MessageProducer {


    @Autowired
    private RocketProcessor rocketProcessor;

    @Override
    public void sendPayInfo(PayInfo payInfo) {
        //向MQ中发送消息  并不直接操作mq    应该操作的是spring cloud stream
        //使用通道向外发出消息
        rocketProcessor.payInfoOutput().send(MessageBuilder.withPayload(payInfo).build());

    }
}
