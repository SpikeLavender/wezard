package com.natsumes.wezard.service;

import com.natsumes.wezard.pojo.Comment;

import java.util.List;

public interface DubboCommentService {
    int deleteByPrimaryKey(Integer id);

    int insert(Comment comment);

    int insertSelective(Comment comment);

    Comment selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Comment comment);

    int updateByPrimaryKey(Comment comment);

    List<Comment> selectSelective(Comment comment);
}
