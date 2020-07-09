package com.natsumes.wezard.service;

import com.natsumes.wezard.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DubboOrderItemService {

    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem orderItem);

    int insertSelective(OrderItem orderItem);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem orderItem);

    int updateByPrimaryKey(OrderItem orderItem);

    int batchInsert(@Param("orderItems") List<OrderItem> orderItems);

    List<OrderItem> selectByOrderNoSet(@Param("orderNoSet") Set<String> orderNoSet);

}
