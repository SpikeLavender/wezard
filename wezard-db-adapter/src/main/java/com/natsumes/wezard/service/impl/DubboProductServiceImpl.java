package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.entity.form.SearchForm;
import com.natsumes.wezard.mapper.ProductMapper;
import com.natsumes.wezard.pojo.Product;
import com.natsumes.wezard.service.DubboProductService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * @author  hetengjiao
 * @date    2020-10-30
 */
@Service
public class DubboProductServiceImpl implements DubboProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return productMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Product product) {
        return productMapper.insert(product);
    }

    @Override
    public int insertSelective(Product product) {
        return productMapper.insertSelective(product);
    }

    @Override
    public Product selectByPrimaryKey(Integer id) {
        return productMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Product product) {
        return productMapper.updateByPrimaryKeySelective(product);
    }

    @Override
    public int updateByPrimaryKey(Product product) {
        return productMapper.updateByPrimaryKey(product);
    }

    @Override
    public List<Product> selectByCategoryIdSet(Set<Integer> categoryIdSet) {
        return productMapper.selectByCategoryIdSet(categoryIdSet);
    }

    @Override
    public List<Product> selectByProductIdSet(Set<Integer> productIdSet) {
        return productMapper.selectByProductIdSet(productIdSet);
    }

    @Override
    public List<Product> selectSelective(Set<Integer> categoryIdSet, SearchForm searchForm) {
        return productMapper.selectSelective(categoryIdSet, searchForm);
    }
}
