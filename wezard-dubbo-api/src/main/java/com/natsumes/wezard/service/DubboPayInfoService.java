package com.natsumes.wezard.service;

import com.natsumes.wezard.pojo.PayInfo;

public interface DubboPayInfoService {

    int deleteByPrimaryKey(Integer id);

    int insert(PayInfo payInfo);

    int insertSelective(PayInfo payInfo);

    PayInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PayInfo payInfo);

    int updateByPrimaryKey(PayInfo payInfo);

    PayInfo selectByByOrderNo(String orderNo);

}
