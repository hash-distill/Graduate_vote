package com.bluemsun.utils;

import com.bluemsun.entity.User;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CustomUserComparatorInsti implements Comparator<User> {
    private List<String> order = Arrays.asList(
            "教育学部",
            "心理学院",
            "政法学院",
            "经济与管理学院",
            "文学院",
            "历史文化学院",
            "外国语学院",
            "音乐学院",
            "美术学院",
            "马克思主义学部",
            "数学与统计学院",
            "信息科学与技术学院",
            "物理学院",
            "化学学院",
            "生命科学学院",
            "地理科学学院",
            "环境学院",
            "体育学院（冰雪学院）",
            "传媒科学学院（新闻学院）",
            "思想政治教育研究中心",
            "国际汉学院",
            "教室教育研究院",
            "附属中学（高中）",
            "附属中学（初中）",
            "附属中学净月实验学校",
            "附属小学",
            "第二附属小学",
            "第三附属小学（中信实验校)"
    );

    /**
     * 按上述学院顺序排序
     * @param u1
     * @param u2
     * @return
     */
    @Override
    public int compare(User u1, User u2) {
        return order.indexOf(u1.getVoteInsti()) - order.indexOf(u2.getVoteInsti());
    }
}
