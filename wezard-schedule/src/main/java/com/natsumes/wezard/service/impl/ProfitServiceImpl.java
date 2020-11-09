package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.entity.IDs;
import com.natsumes.wezard.enums.LevelEnum;
import com.natsumes.wezard.enums.OrderStatusEnum;
import com.natsumes.wezard.pojo.Achievement;
import com.natsumes.wezard.pojo.Order;
import com.natsumes.wezard.pojo.Users;
import com.natsumes.wezard.service.DubboAchievementService;
import com.natsumes.wezard.service.DubboOrderService;
import com.natsumes.wezard.service.DubboUsersService;
import com.natsumes.wezard.service.ProfitService;
import com.natsumes.wezard.utils.DateUtils;
import com.natsumes.wezard.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.natsumes.wezard.consts.StefanieConst.ROOT_USER_PARENT_ID;


@Slf4j
@Service
public class ProfitServiceImpl implements ProfitService {

    @Reference
    private DubboUsersService dubboUsersService;

    @Reference
    private DubboOrderService dubboOrderService;

    @Reference
    private DubboAchievementService dubboAchievementService;

    @Override
    public void weekAchievement() {
        List<Achievement> achievements = buildWeekAchievement();
        updateAchievementInDB(achievements);
    }

    @Override
    public void curWeekAchievement() {
        List<Achievement> achievements = buildCurWeekAchievement();
        updateAchievementInDB(achievements);
    }

    @Override
    public void monthAchievement() {
        List<Achievement> achievements = buildMonthAchievement();
        updateAchievementInDB(achievements);
    }

    @Override
    public void curMonthAchievement() {
        List<Achievement> achievements = buildCurMonthAchievement();
        updateAchievementInDB(achievements);
    }

    /**
     * 创建全部周业绩条目
     * 每 fixedRate 执行一次，刷新本周业绩
     *
     * @return List<Achievement>
     */
    private List<Achievement> buildWeekAchievement() {
        List<Users> users = getUsers();
        Predicate<Order> predicate = order -> order.getStatus() >= OrderStatusEnum.PAID.getCode();
        List<Order> orders = getOrders(users, predicate);
        Map<Date, List<Order>> ordersMap = splitOrdersByWeek(orders);
        return createAchievement(ordersMap);
    }

    /**
     * 创建本周业绩
     * 每 fixedRate 执行一次，刷新本周业绩，30min
     *
     * @return
     */
    private List<Achievement> buildCurWeekAchievement() {
        List<Users> users = getUsers();
        Predicate<Order> predicate = order -> order.getStatus() >= OrderStatusEnum.PAID.getCode()
                && DateUtils.isThisWeek(order.getPaymentTime());
        List<Order> orders = getOrders(users, predicate);
        Map<Date, List<Order>> ordersMap = splitOrdersByWeek(orders);

        return createAchievement(ordersMap);
    }

    /**
     * 按周拆分 {"startTime": [order1, order2]}
     *
     * @param orders
     * @return
     */
    private Map<Date, List<Order>> splitOrdersByWeek(List<Order> orders) {
        Map<Date, List<Order>> ordersWeekMap = new HashMap<>();
        orders.forEach(order -> {
            Date paymentTime = order.getPaymentTime();
            Date startDayOfWeek = DateUtils.getStartDayOfWeek(paymentTime);
            List<Order> weekOrders = new ArrayList<>();
            if (ordersWeekMap.containsKey(startDayOfWeek)) {
                weekOrders = ordersWeekMap.get(startDayOfWeek);
            }
            weekOrders.add(order);
            ordersWeekMap.put(startDayOfWeek, weekOrders);
        });

        return ordersWeekMap;
    }

    /**
     * 创建全部周业绩条目
     * 每 fixedRate 执行一次，刷新本周业绩
     *
     * @return List<Achievement>
     */
    private List<Achievement> buildMonthAchievement() {
        List<Users> users = getUsers();
        Predicate<Order> predicate = order -> order.getStatus() >= OrderStatusEnum.PAID.getCode();
        List<Order> orders = getOrders(users, predicate);
        Map<Date, List<Order>> ordersMap = splitOrdersByMonth(orders);
        return createAchievement(ordersMap);
    }

    /**
     * 创建本周业绩
     * 每 fixedRate 执行一次，刷新本周业绩，30min
     *
     * @return
     */
    private List<Achievement> buildCurMonthAchievement() {
        List<Users> users = getUsers();
        Predicate<Order> predicate = order -> order.getStatus() >= OrderStatusEnum.PAID.getCode()
                && DateUtils.isThisMonth(order.getPaymentTime());
        List<Order> orders = getOrders(users, predicate);
        Map<Date, List<Order>> ordersMap = splitOrdersByMonth(orders);

        return createAchievement(ordersMap);
    }

    /**
     * 按周拆分 {"startTime": [order1, order2]}
     *
     * @param orders
     * @return
     */
    private Map<Date, List<Order>> splitOrdersByMonth(List<Order> orders) {
        Map<Date, List<Order>> ordersMonthMap = new HashMap<>();
        orders.forEach(order -> {
            Date paymentTime = order.getPaymentTime();
            Date startDayOfMonth = DateUtils.getStartDayOfMonth(paymentTime);
            List<Order> monthOrders = new ArrayList<>();
            if (ordersMonthMap.containsKey(startDayOfMonth)) {
                monthOrders = ordersMonthMap.get(startDayOfMonth);
            }
            monthOrders.add(order);
            ordersMonthMap.put(startDayOfMonth, monthOrders);
        });

        return ordersMonthMap;
    }

    /**
     * 创建订单条目
     *
     * @param ordersMap
     * @return
     */
    private List<Achievement> createAchievement(Map<Date, List<Order>> ordersMap) {
        List<Users> users = getUsers();
        List<IDs> idVos = createIDVos(users);
        log.debug("map={}", JSONUtils.printFormat(idVos));

        List<Achievement> profits = new ArrayList<>();
        ordersMap.forEach((weekDate, orders) -> {
            Map<Integer, Achievement> profitMap = calcAchievement(orders, users);
            calcAchievementByUser(idVos, profitMap, ROOT_USER_PARENT_ID);
            List<Achievement> weekProfits = profitMap.values().stream().peek(e -> {
                e.setEndTime(DateUtils.getEndDayOfMonth(weekDate));
                e.setStartTime(weekDate);
            }).collect(Collectors.toList());
            profits.addAll(weekProfits);
        });

        log.debug("profitMap=\n{}", JSONUtils.printFormat(profits));
        return profits;
    }


    /**
     * 计算总业绩
     *
     * @param orders
     * @return
     */
    private Map<Integer, Achievement> calcAchievement(List<Order> orders, List<Users> users) {
        //计算总业绩 {"userId": []}

        Map<Integer, Integer> idMap = users.stream()
                .collect(Collectors.toMap(Users::getId, Users::getParentId, (a, b) -> b));

        Map<Integer, List<Order>> ordersMap = orders.stream()
                .collect(Collectors.groupingBy(Order::getUserId));


        Map<Integer, Achievement> profitMap = new HashMap<>();
        ordersMap.forEach((id, orderVos) -> {
            Integer parentId = idMap.getOrDefault(id, 0);
            if (parentId != 0) {
                BigDecimal payment = orderVos.stream()
                        .map(Order::getPayment)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Achievement achievement = profitMap.get(parentId);
                if (achievement == null) {
                    achievement = new Achievement();
                    achievement.setUserId(parentId);
                    achievement.setSubAchievement(BigDecimal.ZERO);
                }

                payment = payment.add(achievement.getSelfAchievement());
                achievement.setSelfAchievement(payment);

                profitMap.put(parentId, achievement);
            }
        });

        return profitMap;
    }

    private void calcAchievementByUser(List<IDs> idVos, Map<Integer, Achievement> achievementMap,
                                       Integer parentId) {
        //Map<Integer, Order> collect = orders.stream().collect(Collectors.toMap(Order::getUserId, Order::getStatus));

        for (IDs idVo : idVos) {
            if (idVo.getParentId().equals(parentId)) {
                calcAchievementByUser(idVo.getSubIdVos(), achievementMap, idVo.getId());
            }

            Achievement achievement = achievementMap.get(idVo.getId());
            if (achievement == null) {
                achievement = new Achievement();
            }

            List<IDs> subIDVos = idVo.getSubIdVos();
            BigDecimal achievementValue = BigDecimal.ZERO;
            BigDecimal subProfit = BigDecimal.ZERO;
            for (IDs subIDVo : subIDVos) {
                BigDecimal subAchievementValue =
                        subIDVo.getAchievement() == null ? BigDecimal.ZERO : subIDVo.getAchievement().getAchievement();
                achievementValue = achievementValue.add(subAchievementValue);
                BigDecimal ratio = subIDVo.getAchievement() == null ? BigDecimal.ZERO : LevelEnum.getRatio(subAchievementValue);
                subProfit = subProfit.add(subAchievementValue.multiply(ratio));
            }
            achievement.setSubAchievement(achievementValue);
            achievement.setUserId(idVo.getId());
            achievement.setParentId(idVo.getParentId());
            BigDecimal totalAchievementValue = achievementValue.add(achievement.getSelfAchievement());
            achievement.setAchievement(totalAchievementValue);
            achievement.setLevel(LevelEnum.getLevel(totalAchievementValue));
            BigDecimal profit = totalAchievementValue.multiply(LevelEnum.getRatio(totalAchievementValue)).subtract(subProfit);
            achievement.setProfit(profit);
            idVo.setAchievement(achievement);
            achievementMap.put(idVo.getId(), achievement);
        }
    }

    private List<Users> getUsers() {
        return dubboUsersService.selectAll();
    }

    private List<Order> getOrders(List<Users> users, Predicate<Order> predicate) {
        //uid -> parentId
        //List<IDVo> idVos = new ArrayList<>();
        Set<Integer> ids = users.stream().map(Users::getId).collect(Collectors.toSet());

        List<Order> orders = dubboOrderService.selectByUidSet(ids);
        return orders.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 更新业绩条目
     */
    private void updateAchievementInDB(List<Achievement> achievements) {
        log.debug("achievements=\n{}", JSONUtils.printFormat(achievements));
        if (achievements != null && achievements.size() > 0) {
            int i = dubboAchievementService.insertBatch(achievements);
            log.info("update number is {}", i);
        }
    }

    private List<IDs> createIDVos(List<Users> users) {
        List<IDs> resIdVo = users.stream()
                .filter(e -> e.getParentId().equals(ROOT_USER_PARENT_ID))
                .map(this::user2IDVo)
                .collect(Collectors.toList());

        findSubId(resIdVo, users);
        return resIdVo;
    }

    private void findSubId(List<IDs> resIdVos, List<Users> users) {
        for (IDs resIdVo : resIdVos) {
            List<IDs> subResIDs = new ArrayList<>();
            for (Users user : users) {
                IDs idVo = new IDs();
                if (resIdVo.getId().equals(user.getParentId())) {
                    BeanUtils.copyProperties(user, idVo);
                    subResIDs.add(idVo);
                }
                resIdVo.setSubIdVos(subResIDs);
                findSubId(subResIDs, users);
            }
        }

    }

    private IDs user2IDVo(Users user) {
        IDs idVo = new IDs();
        BeanUtils.copyProperties(user, idVo);
        return idVo;
    }
}
