package com.natsumes.wezard.controller;

import com.github.pagehelper.PageInfo;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.vo.AchievementVo;
import com.natsumes.wezard.entity.vo.ProfitVo;
import com.natsumes.wezard.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/{userId}")
    public Response<AchievementVo> list(@PathVariable Integer userId) {
        return achievementService.list(userId);
    }

    @GetMapping("/{userId}/profit")
    public Response<ProfitVo> profit(@PathVariable Integer userId) {
        return achievementService.profit(userId);
    }

    @GetMapping("/{userId}/detail")
    public Response<PageInfo> detail(@PathVariable Integer userId,
                                     @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return achievementService.detail(userId, pageNum, pageSize);
    }

}
