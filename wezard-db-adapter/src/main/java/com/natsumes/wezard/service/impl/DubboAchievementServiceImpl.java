package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.mapper.AchievementMapper;
import com.natsumes.wezard.pojo.Achievement;
import com.natsumes.wezard.service.DubboAchievementService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hetengjiao
 */
@Service
public class DubboAchievementServiceImpl implements DubboAchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return achievementMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Achievement achievement) {
        return achievementMapper.insert(achievement);
    }

    @Override
    public int insertSelective(Achievement achievement) {
        return achievementMapper.insertSelective(achievement);
    }

    @Override
    public Achievement selectByPrimaryKey(Integer id) {
        return achievementMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Achievement achievement) {
        return achievementMapper.updateByPrimaryKeySelective(achievement);
    }

    @Override
    public int updateByPrimaryKey(Achievement achievement) {
        return achievementMapper.updateByPrimaryKey(achievement);
    }

    @Override
    public int updateBatchSelective(List<Achievement> achievements) {
        return achievementMapper.updateBatchSelective(achievements);
    }

    @Override
    public int insertBatch(List<Achievement> achievements) {
        return achievementMapper.insertBatch(achievements);
    }

    @Override
    public int replaceBatch(List<Achievement> achievements) {
        return achievementMapper.replaceBatch(achievements);
    }

    @Override
    public List<Achievement> selectByUid(Integer id) {
        return achievementMapper.selectByUid(id);
    }
}
