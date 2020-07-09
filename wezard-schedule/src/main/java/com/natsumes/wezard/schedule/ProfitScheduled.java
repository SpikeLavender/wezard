package com.natsumes.wezard.schedule;

import com.natsumes.wezard.service.ProfitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(value = 1)
public class ProfitScheduled implements ApplicationRunner {

    @Autowired
    private ProfitService profitService;

    /**
     * 创建全部周业绩条目
     * 每 30分钟执行一次，刷新全部业绩
     */
    @Override
    public void run(ApplicationArguments args) {
        // weekAchievement();
        //手动调用
        monthAchievement();
    }

    /**
     * 创建全部周业绩条目
     * 每 30分钟执行一次，刷新全部业绩
     */
    //@Scheduled(cron = "* */${scheduled.week.interval} * * * *")
    @Scheduled(cron = "${scheduled.month.cron}") //每天的13，18，21点执行一次 "0 0 0,13,18,21 * * ?"
    public void monthAchievement() {
        profitService.monthAchievement();
    }

    /**
     * 创建本周业绩
     * 每 fixedRate 执行一次，刷新本周业绩，30min
     */
    @Scheduled(fixedRateString = "${scheduled.month.fixedRate}")
    public void curMonthAchievement() {
        profitService.curMonthAchievement();
    }
}
