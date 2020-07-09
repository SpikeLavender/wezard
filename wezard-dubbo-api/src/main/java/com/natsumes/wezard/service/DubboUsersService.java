package com.natsumes.wezard.service;

import com.natsumes.wezard.pojo.Users;

import java.util.List;

public interface DubboUsersService {

    int deleteByPrimaryKey(Integer id);

    int insert(Users users);

    int insertSelective(Users users);

    Users selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Users users);

    int updateByPrimaryKey(Users users);

    int countById(Integer id);

    int countByUsername(String username);

    int countByOpenid(String openid);

    int countByEmail(String email);

    Users selectByUsername(String username);

    Users selectByOpenid(String openid);

    List<Users> selectByParentId(Integer parentId);

    List<Users> selectAll();

}
