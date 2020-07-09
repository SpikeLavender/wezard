package com.natsumes.wezard.mapper;

import com.natsumes.wezard.pojo.Achievement;

import java.util.List;

public interface AchievementMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Achievement achievement);

    int insertSelective(Achievement achievement);

    Achievement selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Achievement achievement);

    int updateByPrimaryKey(Achievement achievement);

    int updateBatchSelective(List<Achievement> achievements);

    int insertBatch(List<Achievement> achievements);

    int replaceBatch(List<Achievement> achievements);

    List<Achievement> selectByUid(Integer id);
}