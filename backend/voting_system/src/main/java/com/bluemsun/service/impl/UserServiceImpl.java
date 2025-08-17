package com.bluemsun.service.impl;

import com.bluemsun.dao.UserDao;
import com.bluemsun.entity.User;
import com.bluemsun.service.UserService;
import com.bluemsun.utils.CustomUserComparator;
import com.bluemsun.utils.CustomUserComparatorInsti;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    @Override
    public List<User> getAllUsers() {
        return userDao.selectAll();
    }

    @Override
    public boolean insertOne(User user) {
        int result = userDao.insertOne(user);
        return result==1;
    }

    @Override
    public boolean updateByIds(List<Integer> students) {
        for (int id: students) {
             int result = userDao.updateById(id);
             if(result != 1){
                 return false;
             }
        }
        return true;
    }

    /**
     * 获取正选需要重投的名单
     * @param list  本次参与投票的学生
     * @param students  正选人数
     * @param last  正选名单
     * @return  需要重投的名单，和需要重选的人数
     */
    @Override
    public Map<String, Object> getRevote(List<User> list, int students, List<User> last) {
        List<User> users = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        list.sort(new CustomUserComparator());
        int limit = 0;
        if(Objects.equals(list.get(students - 1).getVotePoll(), list.get(students).getVotePoll())) {
            //选中的最后一个人的票数和没选中的第一个人的票数相等，说明这俩人需要重新投票
            //统计需要重新投票的学生
            boolean isFirst = false;
            int targetVotes = list.get(students).getVotePoll();
            for (int i = 0; i < list.size(); i++) {
                //如果后面还有人与上面两人票数相同就添加
                if (list.get(i).getVotePoll() == targetVotes) {
                    list.get(i).setVotePoll(0);
                    users.add(list.get(i));
                    if(i <= students-1){
                        limit++;
                    }
                    if(!isFirst){
                        isFirst = true;
                    }
                }
                if(!isFirst){
                    last.add(list.get(i));
                }
            }
        } else {
            for(int i = 0; i < students; i++){  // 正选确定
                last.add(list.get(i));
            }
        }
        map.put("revoteList", users);
        map.put("limit", limit);
        return map;
    }


    /**
     * 获取候补需要重投的名单
     * @param list  本次参与投票学生
     * @param students  候补人数还差多少
     * @param pre   候补名单
     * @return
     */
    @Override
    public Map<String, Object> getPreRevote(List<User> list, int students, List<User> pre) {
        List<User> users = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        list.sort(new CustomUserComparator());
        int limit = 0;
        if(students == 2){
            if(list.size() >= 3){
                if(Objects.equals(list.get(1).getVotePoll(), list.get(2).getVotePoll())){   // 第2个人和第三个人平票，需要重投，并计算重投人数
                    if(!Objects.equals(list.get(0).getVotePoll(), list.get(1).getVotePoll())){
                        pre.add(list.get(0));   // 第1个人和第二个人不同，那第一个人必定是第一候补
                    }

                    int targetVotes = list.get(1).getVotePoll();
                    for(int i = 0; i < list.size(); i++){
                        if(list.get(i).getVotePoll() == targetVotes){
                            list.get(i).setVotePoll(0);
                            users.add(list.get(i));
                            if(i <= students-1){
                                limit++;
                            }
                        }
                    }

                } else {
                    if(!Objects.equals(list.get(0).getVotePoll(), list.get(1).getVotePoll())){
                        pre.add(list.get(0));   // 前3个都不平票，第一候补和第二候补确定
                        pre.add(list.get(1));
                    } else { // 前两个平票
                        list.get(0).setVotePoll(0);
                        list.get(1).setVotePoll(0);
                        users.add(list.get(0)); // 只有第1和第2个平票，这俩重投，确定第一候补和第二候补
                        users.add(list.get(1));
                        limit = 1;
                    }
                }
            } else { // 只有2个人，直接判断这俩人是否平票
                if(Objects.equals(list.get(0).getVotePoll(), list.get(1).getVotePoll())){
                    list.get(0).setVotePoll(0);
                    list.get(1).setVotePoll(0);
                    users.add(list.get(0)); // 第1和第2个平票，这俩重投，确定第一候补和第二候补
                    users.add(list.get(1));
                    limit = 1;
                } else {
                    pre.add(list.get(0));   // 不平票，第一候补和第二候补确定
                    pre.add(list.get(1));
                }
            }
        } else if(students == 1) { // 第一候补已经确定，确定第二候补

            if(Objects.equals(list.get(0).getVotePoll(), list.get(1).getVotePoll())){
                // 前两个平票，重投
                int targetVotes = list.get(1).getVotePoll();
                for(int i = 0; i < list.size(); i++){
                    if(list.get(i).getVotePoll() == targetVotes){
                        list.get(i).setVotePoll(0);
                        users.add(list.get(i));
                        if(i <= students-1){
                            limit++;
                        }
                    }
                }
            } else {
                // 前两个不平票，确定第二候选
                pre.add(list.get(0));
            }
        }

        map.put("revoteList", users);
        map.put("limit", limit);
        return map;
    }

    @Override
    public boolean insertAll(List<User> users) {
        for(User user: users){
            int i = userDao.insertOne(user);
            if(i != 1){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean setPollZero() {
        return userDao.setAllPollZero()>=0;
    }

    @Override
    public boolean updatePollToFirst(List<User> list) {
        for(User user: list){
            User u = userDao.updatePollToFirst(user.getVoteId());
            user.setVotePoll(u.getVotePoll());
        }
        return true;
    }

    /**
     * 处理投票结果
     * @param application   application域
     * @param map   各种标志信息
     * @param prenum 默认的候补人数
     * @return
     */
    @Override
    public Map<String, Object> vote(ServletContext application, Map<String, Object> map, Integer prenum) {
        List<User> users = (List<User>) map.get("revote");  // 默认在重投，获取revote中的信息
        map.put("first", 1);    // 已经在第一次处理
        if(users == null){
            // 没有重投，是第一次投票，获取数据库中的信息
            users = getAllUsers();
        }

        // 正选名单是否确定
        List<User> last = (List<User>) map.get("last"); // 正选名单
        List<User> pre = (List<User>) map.get("pre");   // 候补名单
        if(last.size() == (int)application.getAttribute("students")){
            // 正选名单已经确认
            System.out.println("正选名单已经确认");
            // 候补名单是否确认
            if(pre.size() == prenum){
                // 候补名单已经确认，说明投票已经结束了，直接返回
                return map;

            }
            else {
                // 候补名单没有确认,说明此次是候补重投
                System.out.println("候补名单没有确认");
                // 先保存此次候补的投票结果
                savePreRevote(map, users);

                Map<String, Object> preRevote = getPreRevote(users, prenum - pre.size(), pre);  // 获取重投名单
                List<User> revoteList = (List<User>) preRevote.get("revoteList");
                int limit = (int) preRevote.get("limit");
                if(revoteList.size() == 0){
                    // 没有需要重投的，说明投票结束
                    System.out.println("第一，第二候补确定");
                    last.sort(new CustomUserComparatorInsti());
                    map.put("pre", pre);
                    map.put("students", last);
                    map.put("limit", 0);
                    map.put("isRevote", 0);
                    map.put("revote", null);
                    map.put("determineNum", pre.size()+last.size());
                    return map;
                } else {
                    // 有需要重投的，重投
                    System.out.println("候补需要重投");
                    map.put("revote", revoteList);
                    map.put("limit", limit);
                    map.put("isRevote", 1);
                    map.put("isPreRevote", true);
                    map.put("determineNum", pre.size()+last.size());
                    return map;
                }

            }
        }

        else {
            // 正式名单没有确认
            System.out.println("正式名单没有确认");
            // 保留此次正选候补投票结果
            saveRevote(map, users);

            int removeLastNum = users.size() - (int)map.get("limit");   // 出去正选还需要选出的人，还剩多少学生
            if(removeLastNum > 1){
                // 剩余的大于1，说明候补就在这些学生中选
                System.out.println("剩余大于1");
                map.put("lastTimes", map.get("revoteTimes")); // 将lasttimes指向此次投票结果

                Map<String, Object> map1 = getRevote(users, (Integer) map.get("limit"), last);  // 获取重投名单

                List<User> revoteList = (List<User>) map1.get("revoteList");
                int limit = (int) map1.get("limit");
                if(revoteList.size() == 0){
                    // 不需要重投
                    System.out.println("正选名单确定");
                    /* 查看是否能确认候补
                            1、先将此次的投票结果中，已经确认为正选的同学剔除
                            2、然后查看是否能确认候补
                     */
                    Set<Integer> lastIds = last.stream().map(User::getVoteId).collect(Collectors.toSet());
                    boolean success = users.removeIf(user -> lastIds.contains(user.getVoteId()));

                    Map<String, Object> preRevote = getPreRevote(users, prenum-pre.size(), pre);
                    List<User> preRevoteList = (List<User>) preRevote.get("revoteList");
                    int preLimit = (int) preRevote.get("limit");
                    if(preRevoteList.size() == 0){
                        // 不需要重投的，说明投票结束
                        System.out.println("候补确定");
                        last.sort(new CustomUserComparatorInsti());
                        map.put("pre", pre);
                        map.put("students", last);
                        map.put("limit", 0);
                        map.put("isRevote", 0);
                        map.put("revote", null);
                        map.put("determineNum", pre.size()+last.size());
                        return map;
                    } else {
                        // 有需要重投的，重投
                        System.out.println("候补重投");
                        map.put("revote", preRevoteList);
                        map.put("limit", preLimit);
                        map.put("isRevote", 1);
                        map.put("isPreRevote", true);
                        map.put("determineNum", pre.size()+last.size());
                        return map;
                    }
                } else {
                    // 需要重投
                    System.out.println("正选需要重投");
                    map.put("revote", revoteList);
                    map.put("limit", limit);
                    map.put("isRevote", 1);
                    map.put("isPreRevote", false);
                    map.put("determineNum", pre.size()+last.size());
                    return map;
                }
            }
            else {
                // 剩余的等于1，说明第一候补可以在正选之后直接确认
                System.out.println("剩余等于1");
                Map<String, Object> map1 = getRevote(users, (Integer) map.get("limit"), last);  // 获取重投名单
                List<User> revoteList = (List<User>) map1.get("revoteList");
                int limit = (int) map1.get("limit");
                if(revoteList.size() == 0){
                    // 不需要重投，正选确定，第一候补确定
                    System.out.println("正选确定");
                    // 查看是否能确认第二候补

                    Set<Integer> lastIds = last.stream().map(User::getVoteId).collect(Collectors.toSet());
                    boolean success = users.removeIf(user -> lastIds.contains(user.getVoteId()));
                    if(users.size() == 1){
                        // 正常情况，剩下的这个就为第1候补
                        pre.add(0, users.get(0));
                        System.out.println("第一候补确定");
                    }
                    else {
                        // 不正常
                        System.out.println(last.size());
                        System.out.println("===========================================error===================================================");
                        return null;
                    }

                    // 从lasttimes指定的那个投票结果中获取第二候选的名单
                    Map<Integer, Map<String, Object>> revoteResult = (Map<Integer, Map<String, Object>>)map.get("revoteResult");
                    int lastTimes = (int) map.get("lastTimes");
                    Map<String, Object> stringObjectMap = revoteResult.get(lastTimes);
                    users = (List<User>)stringObjectMap.get("revoteList");
                    List<User> temp = new ArrayList<>();
                    temp.addAll(users);
//                    users = (List<User>) revoteResult.get(map.get("lastTimes"));

                    // 去除users中已经确定的正选名单和候选名单
                    // 获取集合pre中所有用户的voteId
                    pre = (List<User>) map.get("pre");
                    last = (List<User>) map.get("last");

                    Set<Integer> lastIds2 = last.stream().map(User::getVoteId).collect(Collectors.toSet());
                    Set<Integer> preIds = pre.stream().map(User::getVoteId).collect(Collectors.toSet());
                    // last中所有用户从list中删除
                    temp.removeIf(user -> lastIds2.contains(user.getVoteId()));
                    temp.removeIf(user -> preIds.contains(user.getVoteId()));

                    if(temp.size() == 1){
                        // 只剩1个，直接加入pre
                        System.out.println("第二候补直接确定");
                        pre.add(temp.get(0));
                        // 结束
                        // 没有需要重投的，说明投票结束
                        last.sort(new CustomUserComparatorInsti());
                        map.put("pre", pre);
                        map.put("students", last);
                        map.put("limit", 0);
                        map.put("isRevote", 0);
                        map.put("revote", null);
                        map.put("determineNum", pre.size()+last.size());
                        return map;
                    } else {
                        // 剩余大于1，送入getPreRevote函数
                        Map<String, Object> preRevote = getPreRevote(temp, prenum-pre.size(), pre);
                        List<User> preRevoteList = (List<User>) preRevote.get("revoteList");
                        int preLimit = (int) preRevote.get("limit");
                        if(preRevoteList.size() == 0){
                            // 没有需要重投的，说明投票结束
                            System.out.println("第二候补确定");
                            last.sort(new CustomUserComparatorInsti());
                            map.put("pre", pre);
                            map.put("students", last);
                            map.put("limit", 0);
                            map.put("isRevote", 0);
                            map.put("revote", null);
                            map.put("determineNum", pre.size()+last.size());
                            return map;
                        } else {
                            // 有需要重投的，重投
                            System.out.println("第二候补重投");
                            map.put("revote", preRevoteList);
                            map.put("limit", preLimit);
                            map.put("isRevote", 1);
                            map.put("isPreRevote", true);
                            map.put("determineNum", pre.size()+last.size());
                            return map;
                        }
                    }

                } else {
                    // 需要重投
                    System.out.println("正选需要重投");
                    map.put("revote", revoteList);
                    map.put("limit", limit);
                    map.put("isRevote", 1);
                    map.put("isPreRevote", false);
                    map.put("determineNum", pre.size()+last.size());
                    return map;
                }
            }
        }

    }

    /**
     * 保留本次候补重投的投票结果
     * @param map
     * @param users
     */
    public void savePreRevote(Map<String, Object> map, List<User> users){
        map.put("preRevoteTimes", (int)map.get("preRevoteTimes") + 1);
        Map<Integer, Map<String, Object>> preRevoteResult = (Map<Integer, Map<String, Object>>) map.get("preRevoteResult");
        List<User> listTemp = new ArrayList<>();
        for(User user: users){
            listTemp.add(User.getUser(user));
        }
        listTemp.sort(new CustomUserComparator());  // 排序
        Map<String, Object> map_temp = new HashMap<>();
        map_temp.put("limit", map.get("limit"));
        map_temp.put("revoteList", listTemp);
        preRevoteResult.put((Integer) map.get("preRevoteTimes"), map_temp);
        map.put("preRevoteResult", preRevoteResult);

    }

    /**
     * 保留本次正选重投的结果
     * @param map
     * @param users
     */
    public void saveRevote(Map<String, Object> map, List<User> users){
        map.put("revoteTimes", (int)map.get("revoteTimes")+1);
        Map<Integer, Map<String, Object>> revoteResult = (Map<Integer, Map<String, Object>>)map.get("revoteResult");
        List<User> listTemp = new ArrayList<>();
        for(User user: users){
            listTemp.add(User.getUser(user));
        }
        listTemp.sort(new CustomUserComparator());  // 排序
        Map<String, Object> map_temp = new HashMap<>();
        map_temp.put("limit", map.get("limit"));
        map_temp.put("revoteList", listTemp);
        revoteResult.put((Integer) map.get("revoteTimes"), map_temp);
        map.put("revoteResult", revoteResult);
    }
}
