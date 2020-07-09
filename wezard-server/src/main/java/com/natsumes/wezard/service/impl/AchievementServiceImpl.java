package com.natsumes.wezard.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.vo.AchievementVo;
import com.natsumes.wezard.entity.vo.ProfitDetailVo;
import com.natsumes.wezard.entity.vo.ProfitVo;
import com.natsumes.wezard.enums.ProfitStatusEnum;
import com.natsumes.wezard.pojo.Achievement;
import com.natsumes.wezard.service.AchievementService;
import com.natsumes.wezard.service.DubboAchievementService;
import com.natsumes.wezard.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    @Reference
    private DubboAchievementService dubboAchievementService;

    @Override
    public Response<AchievementVo> list(Integer uid) {
        AchievementVo achievementVo = new AchievementVo();
        List<Achievement> achievements = dubboAchievementService.selectByUid(uid);
        achievements.forEach(achievement -> {
            if (DateUtils.isThisMonth(achievement.getStartTime())) {
                achievementVo.updateProfit(achievement.getProfit(), achievement.getAchievement(),
                        achievement.getSelfAchievement(), achievement.getSubAchievement());
            }
            if (DateUtils.isPointMonth(achievement.getStartTime(), -1)) {
                achievementVo.updateLastProfit(achievement.getProfit(), achievement.getAchievement(),
                        achievement.getSelfAchievement(), achievement.getSubAchievement());
            }
        });
        achievementVo.setUserId(uid);

        return Response.success(achievementVo);
    }

    @Override
    public Response<ProfitVo> profit(Integer uId) {
        ProfitVo profitVo = new ProfitVo();
        List<Achievement> achievements = dubboAchievementService.selectByUid(uId);
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal validProfit = BigDecimal.ZERO;
        /**
         * 每个月7日之后可以领取上个月的利润
         */
        for (Achievement achievement : achievements) {
            if (DateUtils.isPointMonth(achievement.getStartTime(), -1)) {
                profitVo.setLastProfit(achievement.getProfit());
                if (!DateUtils.isBeforeDay(7)
                        && ProfitStatusEnum.UNPAID.getCode().equals(achievement.getStatus())) {
                    validProfit = validProfit.add(achievement.getProfit());
                }
            }
            if (DateUtils.isBeforeMonth(achievement.getStartTime(), -1)
                    && ProfitStatusEnum.UNPAID.getCode().equals(achievement.getStatus())) {
                validProfit = validProfit.add(achievement.getProfit());
            }
            profit = profit.add(achievement.getProfit());
        }
        profitVo.setTotalProfit(profit);
        profitVo.setValidProfit(validProfit);
        profitVo.setUserId(uId);
        return Response.success(profitVo);
    }

    /**
     * status: 0-未领取，5-可领取，10-领取中，15-已领取
     *
     * @param uId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response<PageInfo> detail(Integer uId, Integer pageNum, Integer pageSize) {
        //ProductDetailVo productDetailVo = new ProductDetailVo();
        PageHelper.startPage(pageNum, pageSize);
        List<Achievement> achievements = dubboAchievementService.selectByUid(uId);
        PageInfo pageInfo = new PageInfo<>(achievements);

        List<ProfitDetailVo> profitDetailVos = achievements.stream()
                .map(this::achievement2ProfitDetailVo)
                .collect(Collectors.toList());

        pageInfo.setList(profitDetailVos);

        return Response.success(pageInfo);
    }

    private ProfitDetailVo achievement2ProfitDetailVo(Achievement achievement) {
        ProfitDetailVo profitDetailVo = new ProfitDetailVo();
        BeanUtils.copyProperties(achievement, profitDetailVo);

        /**
         * 0 - 不可领取，5 - 可领取， 10 - 领取中， 15 - 已领取
         */
        if (ProfitStatusEnum.UNPAID.getCode().equals(achievement.getStatus())
                && DateUtils.isBeforeMonth(achievement.getStartTime(), -1) // 本月之前
                && DateUtils.isAfterDay(7)) {   // 当月7号之后
            profitDetailVo.setStatus(ProfitStatusEnum.VALID_PAID.getCode());
        }

        return profitDetailVo;
    }

}
