package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.mapper.ShippingMapper;
import com.natsumes.wezard.pojo.Shipping;
import com.natsumes.wezard.service.DubboShippingService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Service
public class DubboShippingServiceImpl implements DubboShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return shippingMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Shipping shipping) {
        return shippingMapper.insert(shipping);
    }

    @Override
    public int insertSelective(Shipping shipping) {
        return shippingMapper.insertSelective(shipping);
    }

    @Override
    public Shipping selectByPrimaryKey(Integer id) {
        return shippingMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Shipping shipping) {
        return shippingMapper.updateByPrimaryKeySelective(shipping);
    }

    @Override
    public int updateByPrimaryKey(Shipping shipping) {
        return shippingMapper.updateByPrimaryKey(shipping);
    }

    @Override
    public int deleteByIdAndUid(Integer uId, Integer shippingId) {
        return shippingMapper.deleteByIdAndUid(uId, shippingId);
    }

    @Override
    public List<Shipping> selectByUid(Integer uId) {
        return shippingMapper.selectByUid(uId);
    }

    @Override
    public Shipping selectByUidAndShippingId(Integer uId, Integer shippingId) {
        return shippingMapper.selectByUidAndShippingId(uId, shippingId);
    }

    @Override
    public List<Shipping> selectByIdSet(Set<Integer> idSet) {
        return shippingMapper.selectByIdSet(idSet);
    }

    @Override
    public int updateBatch(List<Shipping> shippings) {
        return shippingMapper.updateBatch(shippings);
    }
}
