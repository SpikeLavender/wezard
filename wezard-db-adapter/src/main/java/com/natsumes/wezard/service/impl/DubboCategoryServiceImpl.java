package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.mapper.CategoryMapper;
import com.natsumes.wezard.pojo.Category;
import com.natsumes.wezard.service.DubboCategoryService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author  hetengjiao
 */
@Service
public class DubboCategoryServiceImpl implements DubboCategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return categoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Category category) {
        return categoryMapper.insert(category);
    }

    @Override
    public int insertSelective(Category category) {
        return categoryMapper.insertSelective(category);
    }

    @Override
    public Category selectByPrimaryKey(Integer id) {
        return categoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Category category) {
        return categoryMapper.updateByPrimaryKeySelective(category);
    }

    @Override
    public int updateByPrimaryKey(Category category) {
        return categoryMapper.updateByPrimaryKey(category);
    }

    @Override
    public List<Category> selectAll() {
        return categoryMapper.selectAll();
    }
}
