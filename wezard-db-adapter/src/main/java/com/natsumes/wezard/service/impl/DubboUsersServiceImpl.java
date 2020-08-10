package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.mapper.UsersMapper;
import com.natsumes.wezard.pojo.Users;
import com.natsumes.wezard.service.DubboUsersService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class DubboUsersServiceImpl implements DubboUsersService {

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return usersMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Users users) {
        return usersMapper.insert(users);
    }

    @Override
    public int insertSelective(Users users) {
        int i = usersMapper.insertSelective(users);
        if (i <= 0) {
            return -1;
        }
        return users.getId();
    }

    @Override
    public Users selectByPrimaryKey(Integer id) {
        return usersMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Users users) {
        return usersMapper.updateByPrimaryKeySelective(users);
    }

    @Override
    public int updateByPrimaryKey(Users users) {
        return usersMapper.updateByPrimaryKey(users);
    }

    @Override
    public int countById(Integer id) {
        return usersMapper.countById(id);
    }

    @Override
    public int countByUsername(String username) {
        return usersMapper.countByUsername(username);
    }

    @Override
    public int countByOpenid(String openid) {
        return usersMapper.countByOpenid(openid);
    }

    @Override
    public int countByEmail(String email) {
        return usersMapper.countByEmail(email);
    }

    @Override
    public Users selectByUsername(String username) {
        return usersMapper.selectByUsername(username);
    }

    @Override
    public Users selectByOpenid(String openid) {
        return usersMapper.selectByOpenid(openid);
    }

    @Override
    public List<Users> selectByParentId(Integer parentId) {
        return usersMapper.selectByParentId(parentId);
    }

    @Override
    public List<Users> selectAll() {
        return usersMapper.selectAll();
    }
}
