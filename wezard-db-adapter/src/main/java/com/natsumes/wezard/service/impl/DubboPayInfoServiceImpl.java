package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.mapper.PayInfoMapper;
import com.natsumes.wezard.pojo.PayInfo;
import com.natsumes.wezard.service.DubboPayInfoService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class DubboPayInfoServiceImpl implements DubboPayInfoService {

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return payInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(PayInfo payInfo) {
        return payInfoMapper.insert(payInfo);
    }

    @Override
    public int insertSelective(PayInfo payInfo) {
        return payInfoMapper.insertSelective(payInfo);
    }

    @Override
    public PayInfo selectByPrimaryKey(Integer id) {
        return payInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(PayInfo payInfo) {
        return payInfoMapper.updateByPrimaryKeySelective(payInfo);
    }

    @Override
    public int updateByPrimaryKey(PayInfo payInfo) {
        return payInfoMapper.updateByPrimaryKey(payInfo);
    }

    @Override
    public PayInfo selectByByOrderNo(String orderNo) {
        return payInfoMapper.selectByByOrderNo(orderNo);
    }
}
