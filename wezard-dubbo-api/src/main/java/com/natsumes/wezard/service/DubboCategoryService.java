package com.natsumes.wezard.service;

import com.natsumes.wezard.pojo.Category;

import java.util.List;

public interface DubboCategoryService {
    int deleteByPrimaryKey(Integer id);

    int insert(Category category);

    int insertSelective(Category category);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category category);

    int updateByPrimaryKey(Category category);

    List<Category> selectAll();
}
