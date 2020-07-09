package com.natsumes.wezard.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.form.ShippingForm;
import com.natsumes.wezard.enums.ResponseEnum;
import com.natsumes.wezard.pojo.Shipping;
import com.natsumes.wezard.service.DubboShippingService;
import com.natsumes.wezard.service.ShippingService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ShippingServiceImpl implements ShippingService {

    @Reference
    private DubboShippingService dubboShippingService;

    @Override
    public Response<Map<String, Integer>> add(Integer uId, ShippingForm form) {

        Shipping shipping = new Shipping();
        BeanUtils.copyProperties(form, shipping);
        shipping.setUserId(uId);
        shipping.setIsDefault(Boolean.TRUE);

        int row = dubboShippingService.insertSelective(shipping);

        if (row <= 0) {
            return Response.error(ResponseEnum.SYSTEM_ERROR);
        }

        List<Shipping> shippings = dubboShippingService.selectByUid(uId).stream()
                .filter(e -> e.getIsDefault() && !e.getId().equals(shipping.getId()))
                .peek(e -> e.setIsDefault(Boolean.FALSE)).collect(Collectors.toList());

        if (!shippings.isEmpty() && dubboShippingService.updateBatch(shippings) != shippings.size() * 2) {
            return Response.error(ResponseEnum.SYSTEM_ERROR);
        }
        //判断是否为默认，是的话更新其他的为false
        Map<String, Integer> map = new HashMap<>();
        map.put("shippingId", shipping.getId());
        return Response.success(map);
    }


    //todo:软删除
    @Override
    public Response delete(Integer uId, Integer shippingId) {
        int row = dubboShippingService.deleteByIdAndUid(uId, shippingId);
        if (row == 0) {
            return Response.error(ResponseEnum.DELETE_SHIPPING_FAIL);
        }
        return Response.success();
    }

    @Override
    public Response update(Integer uId, Integer shippingId, ShippingForm form) {
        Shipping shipping = new Shipping();
        BeanUtils.copyProperties(form, shipping);
        shipping.setUserId(uId);
        shipping.setId(shippingId);

        List<Shipping> shippings = new ArrayList<>();

        if (form.getIsDefault()) {
            shippings = dubboShippingService.selectByUid(uId).stream()
                    .filter(e -> e.getIsDefault() && !e.getId().equals(shippingId))
                    .peek(e -> e.setIsDefault(Boolean.FALSE))
                    .collect(Collectors.toList());
        }

        shippings.add(shipping);

        int row = dubboShippingService.updateBatch(shippings);
        if (row <= shippings.size()) {
            return Response.error(ResponseEnum.SYSTEM_ERROR);
        }
        return Response.success();
    }

    @Override
    public Response<PageInfo> list(Integer uId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippings = dubboShippingService.selectByUid(uId);
        PageInfo<Shipping> pageInfo = new PageInfo<>(shippings);
        return Response.success(pageInfo);
    }
}
