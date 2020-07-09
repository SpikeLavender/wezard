package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.mapper.OrderItemMapper;
import com.natsumes.wezard.pojo.OrderItem;
import com.natsumes.wezard.service.DubboOrderItemService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Service
public class DubboOrderItemServiceImpl implements DubboOrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderItemMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(OrderItem orderItem) {
        return orderItemMapper.insert(orderItem);
    }

    @Override
    public int insertSelective(OrderItem orderItem) {
        return orderItemMapper.insertSelective(orderItem);
    }

    @Override
    public OrderItem selectByPrimaryKey(Integer id) {
        return orderItemMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(OrderItem orderItem) {
        return orderItemMapper.updateByPrimaryKeySelective(orderItem);
    }

    @Override
    public int updateByPrimaryKey(OrderItem orderItem) {
        return orderItemMapper.updateByPrimaryKey(orderItem);
    }

    @Override
    public int batchInsert(List<OrderItem> orderItems) {
        return orderItemMapper.batchInsert(orderItems);
    }

    @Override
    public List<OrderItem> selectByOrderNoSet(Set<String> orderNoSet) {
        return orderItemMapper.selectByOrderNoSet(orderNoSet);
    }
}
