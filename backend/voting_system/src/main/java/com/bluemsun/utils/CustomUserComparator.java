package com.bluemsun.utils;

import com.bluemsun.entity.User;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CustomUserComparator implements Comparator<User> {
    private List<String> customOrder = Arrays.asList(
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



    public CustomUserComparator(List<String> customOrder) {
        this.customOrder = customOrder;
    }

    public CustomUserComparator() {
    }

    /**
     * 二级排序，一级按票数排，票数相同按上述学院顺序排
     * @param user1
     * @param user2
     * @return
     */
    @Override
    public int compare(User user1, User user2) {
        // 首先按照票数属性进行比较
        int voteComparison = Integer.compare(user2.getVotePoll(), user1.getVotePoll());

        if (voteComparison != 0) {
            // 如果vote不同，直接返回voteComparison结果
            return voteComparison;
        } else {
            // 如果vote相同，按照学院属性进行比较
            int index1 = customOrder.indexOf(user1.getVoteInsti());
            int index2 = customOrder.indexOf(user2.getVoteInsti());

            if (index1 == -1 || index2 == -1) {
                // 处理学院属性不在自定义顺序列表中的情况
                return user1.getVoteInsti().compareTo(user2.getVoteInsti());
            }

            return Integer.compare(index1, index2);
        }
    }
}
