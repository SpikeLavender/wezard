package com.natsumes.wezard.service.impl;

import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.OrderQueryRequest;
import com.lly835.bestpay.model.OrderQueryResponse;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.enums.PayPlatformEnum;
import com.natsumes.wezard.pojo.PayInfo;
import com.natsumes.wezard.service.DubboPayInfoService;
import com.natsumes.wezard.service.MessageProducer;
import com.natsumes.wezard.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PayServiceImpl implements PayService {

    private static final String QUEUE_PAY_NOTIFY = "payNotify";

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private MessageProducer messageProducer;

    @Reference
    private DubboPayInfoService dubboPayInfoService;

    @Override
    public Response<PayResponse> create(Integer userId, String orderId, String openId, BigDecimal amount, BestPayTypeEnum payTypeEnum) {
        //已存在, 查询
        PayInfo payInfo = dubboPayInfoService.selectByByOrderNo(orderId);
        if (payInfo == null) {

            //1. 写入数据库
            payInfo = new PayInfo(orderId,
                    PayPlatformEnum.getByBestPayTypeEnum(payTypeEnum).getCode(),
                    OrderStatusEnum.NOTPAY.name(),
                    amount);
            payInfo.setUserId(userId);

            dubboPayInfoService.insertSelective(payInfo);
        }

        if (payTypeEnum != BestPayTypeEnum.WXPAY_NATIVE
                && payTypeEnum != BestPayTypeEnum.WXPAY_MINI
                && payTypeEnum != BestPayTypeEnum.ALIPAY_PC) {
            throw new RuntimeException("暂不支持的支付类型");
        }

        PayRequest request = new PayRequest();
        request.setOrderName("2361478-最好的支付SDK");
        request.setOrderId(orderId);
        request.setOrderAmount(amount.doubleValue());
        request.setPayTypeEnum(payTypeEnum);
        request.setOpenid(openId);
        PayResponse response = bestPayService.pay(request);

        response.setOrderId(orderId);
        response.setOrderAmount(amount.doubleValue());
        response.setPayPlatformEnum(payTypeEnum.getPlatform());
        //存储信息
        log.info("response: " + response);
        return Response.success(response);
    }

    @Override
    public String asyncNotify(String notifyData) {
        //1.签名校验
        PayResponse response = bestPayService.asyncNotify(notifyData);
        log.info("response={}", response);

        //2.金额校验（从数据库查订单）
        //比较严重（正常情况下是不会发生的）发出告警：钉钉、短信
        PayInfo payInfo = dubboPayInfoService.selectByByOrderNo(response.getOrderId());
        if (payInfo == null) {
            //todo:
            throw new RuntimeException("通过orderNo查询到的结果是null");
        }

        //如果订单支付状态不是"已支付"
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
            //Double类型比较大小，精度。1.00  1.0
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(response.getOrderAmount())) != 0) {
                //todo:告警
                throw new RuntimeException("异步通知中的金额和数据库里的不一致，orderNo=" + response.getOrderId());
            }

            //3. 修改订单支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(response.getOutTradeNo());
            dubboPayInfoService.updateByPrimaryKeySelective(payInfo);
        }

        //TODO totoro发送MQ消息，natsume接受MQ消息, payInfoVo

        messageProducer.sendPayInfo(payInfo);

        if (response.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            //4.告诉微信不要在通知了
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        } else if (response.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }

        throw new RuntimeException("异步通知中错误的支付平台");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return dubboPayInfoService.selectByByOrderNo(orderId);
    }

    //查询订单接口
    private String query(String orderId, BestPayTypeEnum payTypeEnum) {
        PayInfo payInfo = dubboPayInfoService.selectByByOrderNo(orderId);
        if (payInfo != null) {
            OrderQueryRequest queryRequest = new OrderQueryRequest();
            queryRequest.setOrderId(orderId);
            queryRequest.setPlatformEnum(payTypeEnum.getPlatform());
            //更新状态
            OrderQueryResponse queryResponse = bestPayService.query(queryRequest);
            payInfo.setPlatformStatus(queryResponse.getOrderStatusEnum().name());
            //payInfoMapper.updateByPrimaryKeySelective(payInfo);
            if (queryResponse.getOrderStatusEnum().equals(OrderStatusEnum.CLOSED)
                    || queryResponse.getOrderStatusEnum().equals(OrderStatusEnum.SUCCESS)) {

            }

        }
        return "";
    }
}
