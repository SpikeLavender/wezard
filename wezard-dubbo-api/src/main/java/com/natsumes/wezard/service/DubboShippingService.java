package com.natsumes.wezard.service;

import com.natsumes.wezard.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DubboShippingService {

    int deleteByPrimaryKey(Integer id);

    int insert(Shipping shipping);

    Integer insertSelective(Shipping shipping);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping shipping);

    int updateByPrimaryKey(Shipping shipping);

    int deleteByIdAndUid(@Param("uId") Integer uId,
                         @Param("shippingId") Integer shippingId);

    List<Shipping> selectByUid(Integer uId);

    Shipping selectByUidAndShippingId(@Param("uId") Integer uId,
                                      @Param("shippingId") Integer shippingId);

    List<Shipping> selectByIdSet(@Param("idSet") Set<Integer> idSet);

    int updateBatch(List<Shipping> shippings);

}
