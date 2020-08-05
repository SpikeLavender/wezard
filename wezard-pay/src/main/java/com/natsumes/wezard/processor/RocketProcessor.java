package com.natsumes.wezard.processor;


import com.natsumes.wezard.consts.StefanieConst;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface RocketProcessor {

    @Output(StefanieConst.PAY_INFO_MESSAGE_OUTPUT)
    MessageChannel payInfoOutput();
}
