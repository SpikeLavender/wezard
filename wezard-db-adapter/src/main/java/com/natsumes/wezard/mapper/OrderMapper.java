package com.natsumes.wezard.mapper;

import com.natsumes.wezard.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order order);

    int insertSelective(Order order);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order order);

    int updateByPrimaryKey(Order order);

    List<Order> selectByUid(Integer id);

    Order selectByOrderNo(String orderNo);

    List<Order> selectByUidSet(@Param("uIdSet") Set<Integer> uIdSet);
}
