package com.bluemsun.service.impl;

import com.bluemsun.dao.UserDao;
import com.bluemsun.dao.VoteLogDao;
import com.bluemsun.entity.User;
import com.bluemsun.service.UserService;
import com.bluemsun.utils.CustomUserComparator;
import com.bluemsun.utils.CustomUserComparatorInsti;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.ServletContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserDao userDao;

    @Autowired(required = false)
    VoteLogDao voteLogDao;

    /** 候补人数默认值（可在 /set 中覆盖，存 application attr "preNum"）。 */
    public static final int DEFAULT_PRE_NUM = 4;

    /** 从 application 读取本轮候补人数（未设置时回退默认）。 */
    public static int currentPreNum(ServletContext application) {
        Object v = application == null ? null : application.getAttribute("preNum");
        if (v instanceof Integer && (Integer) v > 0) {
            return (Integer) v;
        }
        return DEFAULT_PRE_NUM;
    }

    /** 统一投票状态锁：覆盖“投票记账/名额扣减/计票触发/名单确认”，替代原先控制器中的两把锁。 */
    public static final Object STATE_LOCK = new Object();

    @Override
    public List<User> getAllUsers() {
        return userDao.selectAll();
    }

    @Override
    public boolean insertOne(User user) {
        int result = userDao.insertOne(user);
        return result == 1;
    }

    @Override
    @Transactional
    public boolean updateByIds(List<Integer> students) {
        for (int id : students) {
            int result = userDao.updateById(id);
            if (result != 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取正选需要重投的名单
     * 
     * @param list     本次参与投票的学生
     * @param students 正选人数
     * @param last     正选名单
     * @return 需要重投的名单，和需要重选的人数
     */
    @Override
    public Map<String, Object> getRevote(List<User> list, int students, List<User> last) {
        List<User> users = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        list.sort(new CustomUserComparator());
        int limit = 0;
        if (Objects.equals(list.get(students - 1).getVotePoll(), list.get(students).getVotePoll())) {
            // 选中的最后一个人的票数和没选中的第一个人的票数相等，说明这俩人需要重新投票
            // 统计需要重新投票的学生
            boolean isFirst = false;
            int targetVotes = list.get(students).getVotePoll();
            for (int i = 0; i < list.size(); i++) {
                // 如果后面还有人与上面两人票数相同就添加
                if (list.get(i).getVotePoll() == targetVotes) {
                    list.get(i).setVotePoll(0);
                    users.add(list.get(i));
                    if (i <= students - 1) {
                        limit++;
                    }
                    if (!isFirst) {
                        isFirst = true;
                    }
                }
                if (!isFirst) {
                    last.add(list.get(i));
                }
            }
        } else {
            for (int i = 0; i < students; i++) { // 正选确定
                last.add(list.get(i));
            }
        }
        map.put("revoteList", users);
        map.put("limit", limit);
        return map;
    }

    /**
     * 获取候补需要重投的名单（泛化：支持任意剩余候补名额）。
     *
     * 算法与 getRevote（正选）同构：在按票数+学院排序后的名单中，
     * - 若剩余人数不足以凑满 students 名 → 全部直接列为候补，无需重投；
     * - 否则比较“第 students 名”与“第 students+1 名”的票数：
     *   　不平票 → 前 students 名全部确定为候补；
     *   　平票（边界平票）→ 票数高于平票线者确定为候补，
     *   　票数等于平票线的整段进入重投名单（票数清零），limit = 该段中位于前
     *   　students 名以内的人数（即下一轮还需从平票段中选出的人数）。
     *
     * @param list     本次参与投票/竞争候补的学生（按序比较，内部会排序）
     * @param students 还需确定的候补人数
     * @param pre      候补名单（已确认者；本方法会把本轮可直接确认者追加进去）
     * @return revoteList=本轮需重投的平票段；limit=本轮还需选出的人数
     */
    @Override
    public Map<String, Object> getPreRevote(List<User> list, int students, List<User> pre) {
        List<User> users = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        list.sort(new CustomUserComparator());
        int limit = 0;
        if (students <= 0) {
            map.put("revoteList", users);
            map.put("limit", limit);
            return map;
        }
        if (list.size() <= students) {
            // 剩余人数不足以凑满：全部列为候补，无需重投
            pre.addAll(list);
            map.put("revoteList", users);
            map.put("limit", limit);
            return map;
        }
        // 边界平票判定：第 students-1 名（0-based）与第 students 名同票 → 平票段重投
        if (Objects.equals(list.get(students - 1).getVotePoll(), list.get(students).getVotePoll())) {
            int targetVotes = list.get(students).getVotePoll();
            boolean firstStable = false; // 是否已越过平票段（之前的是稳定高票）
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getVotePoll() == targetVotes) {
                    list.get(i).setVotePoll(0);
                    users.add(list.get(i));
                    if (i <= students - 1) {
                        limit++;
                    }
                    firstStable = true;
                } else if (!firstStable) {
                    // 稳定高于平票线 → 直接确定为候补
                    pre.add(list.get(i));
                }
                // 低于平票线的：不进入本轮名单，落入待定（若有后续轮次再参与）
            }
        } else {
            // 无边界平票：前 students 名直接确定
            for (int i = 0; i < students; i++) {
                pre.add(list.get(i));
            }
        }
        map.put("revoteList", users);
        map.put("limit", limit);
        return map;
    }

    @Override
    public boolean insertAll(List<User> users) {
        for (User user : users) {
            int i = userDao.insertOne(user);
            if (i != 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean setPollZero() {
        return userDao.setAllPollZero() >= 0;
    }

    @Override
    public boolean updatePollToFirst(List<User> list) {
        for (User user : list) {
            User u = userDao.updatePollToFirst(user.getVoteId());
            user.setVotePoll(u.getVotePoll());
        }
        return true;
    }

    /**
     * 处理投票结果
     * 
     * @param application application域
     * @param map         各种标志信息
     * @param prenum      默认的候补人数
     * @return
     */
    @Override
    public Map<String, Object> vote(ServletContext application, Map<String, Object> map, Integer prenum) {
        List<User> users = (List<User>) map.get("revote"); // 默认在重投，获取revote中的信息
        map.put("first", 1); // 已经在第一次处理
        if (users == null) {
            // 没有重投，是第一次投票，获取数据库中的信息
            users = getAllUsers();
        }

        // 正选名单是否确定
        List<User> last = (List<User>) map.get("last"); // 正选名单
        List<User> pre = (List<User>) map.get("pre"); // 候补名单
        if (last.size() == (int) application.getAttribute("students")) {
            // 正选名单已经确认
            System.out.println("正选名单已经确认");
            // 候补名单是否确认
            if (pre.size() == prenum) {
                // 候补名单已经确认，说明投票已经结束了，直接返回
                return map;

            } else {
                // 候补名单没有确认,说明此次是候补重投
                System.out.println("候补名单没有确认");
                // 先保存此次候补的投票结果
                savePreRevote(map, users);

                Map<String, Object> preRevote = getPreRevote(users, prenum - pre.size(), pre); // 获取重投名单
                List<User> revoteList = (List<User>) preRevote.get("revoteList");
                int limit = (int) preRevote.get("limit");
                if (revoteList.size() == 0) {
                    // 没有需要重投的，说明投票结束
                    System.out.println("第一，第二候补确定");
                    last.sort(new CustomUserComparatorInsti());
                    map.put("pre", pre);
                    map.put("students", last);
                    map.put("limit", 0);
                    map.put("isRevote", 0);
                    map.put("revote", null);
                    map.put("determineNum", pre.size() + last.size());
                    return map;
                } else {
                    // 有需要重投的，重投
                    System.out.println("候补需要重投");
                    map.put("revote", revoteList);
                    map.put("limit", limit);
                    map.put("isRevote", 1);
                    map.put("isPreRevote", true);
                    map.put("determineNum", pre.size() + last.size());
                    return map;
                }

            }
        }

        else {
            // 正式名单没有确认
            System.out.println("正式名单没有确认");
            // 保留此次正选候补投票结果
            saveRevote(map, users);

            int removeLastNum = users.size() - (int) map.get("limit"); // 出去正选还需要选出的人，还剩多少学生
            if (removeLastNum > 1) {
                // 剩余的大于1，说明候补就在这些学生中选
                System.out.println("剩余大于1");
                map.put("lastTimes", map.get("revoteTimes")); // 将lasttimes指向此次投票结果

                Map<String, Object> map1 = getRevote(users, (Integer) map.get("limit"), last); // 获取重投名单

                List<User> revoteList = (List<User>) map1.get("revoteList");
                int limit = (int) map1.get("limit");
                if (revoteList.size() == 0) {
                    // 不需要重投
                    System.out.println("正选名单确定");
                    /*
                     * 查看是否能确认候补
                     * 1、先将此次的投票结果中，已经确认为正选的同学剔除
                     * 2、然后查看是否能确认候补
                     */
                    Set<Integer> lastIds = last.stream().map(User::getVoteId).collect(Collectors.toSet());
                    boolean success = users.removeIf(user -> lastIds.contains(user.getVoteId()));

                    Map<String, Object> preRevote = getPreRevote(users, prenum - pre.size(), pre);
                    List<User> preRevoteList = (List<User>) preRevote.get("revoteList");
                    int preLimit = (int) preRevote.get("limit");
                    if (preRevoteList.size() == 0) {
                        // 不需要重投的，说明投票结束
                        System.out.println("候补确定");
                        last.sort(new CustomUserComparatorInsti());
                        map.put("pre", pre);
                        map.put("students", last);
                        map.put("limit", 0);
                        map.put("isRevote", 0);
                        map.put("revote", null);
                        map.put("determineNum", pre.size() + last.size());
                        return map;
                    } else {
                        // 有需要重投的，重投
                        System.out.println("候补重投");
                        map.put("revote", preRevoteList);
                        map.put("limit", preLimit);
                        map.put("isRevote", 1);
                        map.put("isPreRevote", true);
                        map.put("determineNum", pre.size() + last.size());
                        return map;
                    }
                } else {
                    // 需要重投
                    System.out.println("正选需要重投");
                    map.put("revote", revoteList);
                    map.put("limit", limit);
                    map.put("isRevote", 1);
                    map.put("isPreRevote", false);
                    map.put("determineNum", pre.size() + last.size());
                    return map;
                }
            } else {
                // 剩余的等于1，说明第一候补可以在正选之后直接确认
                System.out.println("剩余等于1");
                Map<String, Object> map1 = getRevote(users, (Integer) map.get("limit"), last); // 获取重投名单
                List<User> revoteList = (List<User>) map1.get("revoteList");
                int limit = (int) map1.get("limit");
                if (revoteList.size() == 0) {
                    // 不需要重投，正选确定，第一候补确定
                    System.out.println("正选确定");
                    // 查看是否能确认第二候补

                    Set<Integer> lastIds = last.stream().map(User::getVoteId).collect(Collectors.toSet());
                    boolean success = users.removeIf(user -> lastIds.contains(user.getVoteId()));
                    if (users.size() == 1) {
                        // 正常情况，剩下的这个就为第1候补
                        pre.add(0, users.get(0));
                        System.out.println("第一候补确定");
                    } else {
                        // 不正常
                        System.out.println(last.size());
                        System.out.println(
                                "===========================================error===================================================");
                        return null;
                    }

                    // 从lasttimes指定的那个投票结果中获取第二候选的名单
                    Map<Integer, Map<String, Object>> revoteResult = (Map<Integer, Map<String, Object>>) map
                            .get("revoteResult");
                    int lastTimes = (int) map.get("lastTimes");
                    Map<String, Object> stringObjectMap = revoteResult.get(lastTimes);
                    users = (List<User>) stringObjectMap.get("revoteList");
                    List<User> temp = new ArrayList<>();
                    temp.addAll(users);
                    // users = (List<User>) revoteResult.get(map.get("lastTimes"));

                    // 去除users中已经确定的正选名单和候选名单
                    // 获取集合pre中所有用户的voteId
                    pre = (List<User>) map.get("pre");
                    last = (List<User>) map.get("last");

                    Set<Integer> lastIds2 = last.stream().map(User::getVoteId).collect(Collectors.toSet());
                    Set<Integer> preIds = pre.stream().map(User::getVoteId).collect(Collectors.toSet());
                    // last中所有用户从list中删除
                    temp.removeIf(user -> lastIds2.contains(user.getVoteId()));
                    temp.removeIf(user -> preIds.contains(user.getVoteId()));

                    if (temp.size() == 1) {
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
                        map.put("determineNum", pre.size() + last.size());
                        return map;
                    } else {
                        // 剩余大于1，送入getPreRevote函数
                        Map<String, Object> preRevote = getPreRevote(temp, prenum - pre.size(), pre);
                        List<User> preRevoteList = (List<User>) preRevote.get("revoteList");
                        int preLimit = (int) preRevote.get("limit");
                        if (preRevoteList.size() == 0) {
                            // 没有需要重投的，说明投票结束
                            System.out.println("第二候补确定");
                            last.sort(new CustomUserComparatorInsti());
                            map.put("pre", pre);
                            map.put("students", last);
                            map.put("limit", 0);
                            map.put("isRevote", 0);
                            map.put("revote", null);
                            map.put("determineNum", pre.size() + last.size());
                            return map;
                        } else {
                            // 有需要重投的，重投
                            System.out.println("第二候补重投");
                            map.put("revote", preRevoteList);
                            map.put("limit", preLimit);
                            map.put("isRevote", 1);
                            map.put("isPreRevote", true);
                            map.put("determineNum", pre.size() + last.size());
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
                    map.put("determineNum", pre.size() + last.size());
                    return map;
                }
            }
        }

    }

    /**
     * 保留本次候补重投的投票结果
     * 
     * @param map
     * @param users
     */
    public void savePreRevote(Map<String, Object> map, List<User> users) {
        map.put("preRevoteTimes", (int) map.get("preRevoteTimes") + 1);
        Map<Integer, Map<String, Object>> preRevoteResult = (Map<Integer, Map<String, Object>>) map
                .get("preRevoteResult");
        List<User> listTemp = new ArrayList<>();
        for (User user : users) {
            listTemp.add(User.getUser(user));
        }
        listTemp.sort(new CustomUserComparator()); // 排序
        Map<String, Object> map_temp = new HashMap<>();
        map_temp.put("limit", map.get("limit"));
        map_temp.put("revoteList", listTemp);
        preRevoteResult.put((Integer) map.get("preRevoteTimes"), map_temp);
        map.put("preRevoteResult", preRevoteResult);

    }

    /**
     * 保留本次正选重投的结果
     * 
     * @param map
     * @param users
     */
    public void saveRevote(Map<String, Object> map, List<User> users) {
        map.put("revoteTimes", (int) map.get("revoteTimes") + 1);
        Map<Integer, Map<String, Object>> revoteResult = (Map<Integer, Map<String, Object>>) map.get("revoteResult");
        List<User> listTemp = new ArrayList<>();
        for (User user : users) {
            listTemp.add(User.getUser(user));
        }
        listTemp.sort(new CustomUserComparator()); // 排序
        Map<String, Object> map_temp = new HashMap<>();
        map_temp.put("limit", map.get("limit"));
        map_temp.put("revoteList", listTemp);
        revoteResult.put((Integer) map.get("revoteTimes"), map_temp);
        map.put("revoteResult", revoteResult);
    }

    /**
     * 记录一位评委提交的一轮票（统一状态锁内执行）。
     *
     * Step2（评委身份/幂等）：
     * 1) voterNo 由一次性 ticket 解析（resolveVoterNo），同一评委一轮只能投一次；
     * 2) “剩余名额”改为推导值：remaining = teachers_all - 本轮已投评委数，
     *    不再手工递减，杜绝负数与重复扣减；
     * 3) 全部评委投完后立即触发一次计票处理（processVoteResultIfReady）。
     *
     * @param application servlet 上下文（现投票状态存放处）
     * @param voterNo     评委编号（1..teachers_all；null 表示未提供有效 ticket）
     * @param students    本轮所选候选人 voteId 列表
     * @param outMsg      操作结果消息（成功/失败原因）
     * @return 是否受理成功
     */
    @Override
    @Transactional
    public boolean recordVote(ServletContext application, String deviceId, List<Integer> students, StringBuilder outMsg) {
        if (students == null || students.isEmpty()) {
            outMsg.append("请至少选择一位候选人");
            return false;
        }
        if (deviceId == null || deviceId.isEmpty()) {
            outMsg.append("缺少设备标识，请刷新页面后重试");
            return false;
        }
        synchronized (STATE_LOCK) {
            if (application.getAttribute("limit") == null) {
                outMsg.append("请管理员先设置投票限制和老师数量");
                return false;
            }
            if (hasVotedCurrentRound(application, deviceId)) {
                outMsg.append("本设备已投过，请勿重复提交");
                return false;
            }
            List<User> revote = (List<User>) application.getAttribute("revote");
            boolean ok = true;
            if (revote != null) {
                // 重投轮：修改 application 域名单中的内存票数（历史行为保持一致）
                for (User user : revote) {
                    for (Integer id : students) {
                        if (Objects.equals(user.getVoteId(), id)) {
                            user.setVotePoll(user.getVotePoll() + 1);
                        }
                    }
                }
            } else {
                // 首轮投票：写数据库票数
                for (Integer id : students) {
                    if (userDao.updateById(id) != 1) {
                        ok = false;
                        break;
                    }
                }
            }
            if (!ok) {
                outMsg.append("投票失败：候选人不存在或已失效");
                return false;
            }
            // 本票受理成功：标记该设备本轮已投，并重新推导剩余名额
            markVoted(application, deviceId);
            // Step4：记录投票流水到 DB（审计 + 幂等兜底；失败不阻断，已 try/catch）
            persistVoteLog(application, deviceId, students);
            int remaining = remainingVoters(application);
            application.setAttribute("teachers", remaining);
            application.setAttribute("first", 0);
            if (remaining == 0) {
                // 全部设备已投完 → 立即尝试处理本轮结果（平票判定/名单确认）
                processVoteResultIfReady(application);
            }
            outMsg.append("投票成功");
            return true;
        }
    }

    // ==================== 按设备限一票（无预分发链接/口令/编号） ====================

    private static final String KEY_VOTED = "votedDeviceLastRound";  // Map<deviceId, roundNo>
    private static final String KEY_ROUND = "roundNo";               // 当前投票轮次

    /** 设备是否已投过当前轮次。 */
    @Override
    public boolean hasVotedCurrentRound(ServletContext application, String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return false;
        }
        synchronized (STATE_LOCK) {
            int round = currentRound(application);
            Object attr = application.getAttribute(KEY_VOTED);
            if (attr instanceof Map) {
                Object voted = ((Map<?, ?>) attr).get(deviceId);
                return voted instanceof Integer && (Integer) voted == round;
            }
            return false;
        }
    }

    /** 场次初始化：清空已投设备集合与轮次（无预生成链接）。 */
    @Override
    public void resetVotingState(ServletContext application) {
        synchronized (STATE_LOCK) {
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            application.setAttribute(KEY_SESSION, sessionId);
            application.setAttribute(KEY_VOTED, new HashMap<String, Integer>());
            application.setAttribute(KEY_ROUND, 1);
            persistSession(application, sessionId);
        }
    }

    // ==================== Step4：记录型落库（审计/幂等兜底，不改变内存状态机） ====================

    private static final String KEY_SESSION = "sessionId";

    /** 落库场次（任何失败只告警）。 */
    private void persistSession(ServletContext application, String sessionId) {
        if (voteLogDao == null) {
            return;
        }
        try {
            int limit = (Integer) application.getAttribute("limit");
            int students = (Integer) application.getAttribute("students");
            int teachersAll = (Integer) application.getAttribute("teachers_all");
            voteLogDao.insertSession(sessionId, limit, students, teachersAll);
        } catch (Exception ex) {
            log.warn("落库场次失败（不影响内存流程）: {}", ex.getMessage());
        }
    }

    /** 投票受理成功后落一条轮次流水（DB 唯一键兜底幂等），失败仅告警。 */
    private void persistVoteLog(ServletContext application, String deviceId, List<Integer> students) {
        if (voteLogDao == null) {
            return;
        }
        Object sid = application.getAttribute(KEY_SESSION);
        if (!(sid instanceof String)) {
            return;
        }
        String sessionId = (String) sid;
        int round = currentRound(application);
        try {
            for (Integer candidateId : students) {
                voteLogDao.insertVote(sessionId, round, deviceId, candidateId);
            }
        } catch (Exception ex) {
            log.warn("落库投票流水失败（不影响本次投票）: {}", ex.getMessage());
        }
    }

    /** 场次轮次推进时同步 DB 状态（重投/轮次），失败仅告警。 */
    private void persistSessionRound(ServletContext application, String status, int roundNo) {
        if (voteLogDao == null) {
            return;
        }
        Object sid = application.getAttribute(KEY_SESSION);
        if (!(sid instanceof String)) {
            return;
        }
        try {
            voteLogDao.updateSession((String) sid, status, roundNo);
        } catch (Exception ex) {
            log.warn("落库场次轮次失败（不影响内存流程）: {}", ex.getMessage());
        }
    }

    /** 当前轮次号；未初始化按 1。 */
    private int currentRound(ServletContext application) {
        Object r = application.getAttribute(KEY_ROUND);
        return r instanceof Integer ? (Integer) r : 1;
    }

    /** 记录某设备本轮已投。 */
    private void markVoted(ServletContext application, String deviceId) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> voted = (Map<String, Integer>) application.getAttribute(KEY_VOTED);
        if (voted == null) {
            voted = new HashMap<>();
            application.setAttribute(KEY_VOTED, voted);
        }
        voted.put(deviceId, currentRound(application));
    }

    /** 推导本轮剩余名额：teachers_all - 本轮已投设备数。 */
    private int remainingVoters(ServletContext application) {
        Object all = application.getAttribute("teachers_all");
        int teachersAll = all instanceof Integer ? (Integer) all : 0;
        int round = currentRound(application);
        int votedCount = 0;
        Object attr = application.getAttribute(KEY_VOTED);
        if (attr instanceof Map) {
            for (Object v : ((Map<?, ?>) attr).values()) {
                if (v instanceof Integer && (Integer) v == round) {
                    votedCount++;
                }
            }
        }
        return Math.max(0, teachersAll - votedCount);
    }

    /** 计票完成且判定需要重投时推进轮次（重投“续牌”），仅由 processVoteResultIfReady 调用。 */
    private void advanceRound(ServletContext application) {
        synchronized (STATE_LOCK) {
            application.setAttribute(KEY_ROUND, currentRound(application) + 1);
            // 进入新一轮：剩余名额重置为“全部设备尚未投”（已投记录按轮次隔离，见 hasVotedCurrentRound）
            Object all = application.getAttribute("teachers_all");
            int teachersAll = all instanceof Integer ? (Integer) all : 0;
            application.setAttribute("teachers", teachersAll);
            persistSessionRound(application, "REVOTE", currentRound(application));
        }
    }

    /**
     * 组装投票状态/结果视图（只读，无副作用）。
     *
     * 供评委 waiting / 结果展示等页面轮询，替代原先轮询带计票副作用的管理接口。
     * 返回结构与原 /admin/getVoteResult 的 data 保持一致（students/pre/isRevote/teachersNum…），
     * 未初始化（limit 未设置）时返回 null，由调用方给出“请先设置参数”的响应。
     */
    @Override
    public Map<String, Object> assembleVoteStatus(ServletContext application) {
        synchronized (STATE_LOCK) {
            return buildStatusView(application);
        }
    }

    /**
     * 触发（一次）计票处理：当全部评委已投（teachers==0）且本轮结果尚未处理（first==0）时，
     * 执行平票判定/名单确认并同步回 application；随后返回与 assembleVoteStatus 相同的视图。
     * 管理员接口兜底/手动触发使用；recordVote 在名额归零时也会调用本方法。
     */
    @Override
    public Map<String, Object> processVoteResultIfReady(ServletContext application) {
        synchronized (STATE_LOCK) {
            if (application.getAttribute("limit") == null) {
                return null;
            }
            List<User> pre = (List<User>) application.getAttribute("pre");
            if (pre == null) {
                pre = new ArrayList<>();
                application.setAttribute("pre", pre);
            }
            int teachers = (int) application.getAttribute("teachers");
            int first = (int) application.getAttribute("first");
            int preNum = currentPreNum(application);
            if (teachers == 0 && pre.size() != preNum && first == 0) {
                Map<String, Object> map = buildProcessingMap(application);
                map = vote(application, map, preNum);
                if (map != null) {
                    syncResultToApplication(application, map);
                    // Step2 续牌：本轮处理完若判定仍需要重投（isRevote==1），推进轮次，
                    // 使各评委在本轮“已投”记录失效，可进入下一轮重投。
                    Object isRevoteObj = application.getAttribute("isRevote");
                    if (isRevoteObj instanceof Integer && (Integer) isRevoteObj == 1) {
                        advanceRound(application);
                    } else {
                        // 本轮处理完且无需重投 → 若候补已齐则整场结束
                        List<User> preAfter = (List<User>) application.getAttribute("pre");
                        if (preAfter != null && preAfter.size() == preNum) {
                            persistSessionRound(application, "FINISHED", currentRound(application));
                        }
                    }
                } else {
                    // vote() 内部异常分支（名单不可收敛），保持原语义：返回失败
                    return null;
                }
            }
            return buildStatusView(application);
        }
    }

    /**
     * 构建计票处理所需的完整状态 Map（从 application 读取全部标志）。
     */
    private Map<String, Object> buildProcessingMap(ServletContext application) {
        Map<String, Object> map = new HashMap<>();
        map.put("limit", application.getAttribute("limit"));
        map.put("teachersNum", application.getAttribute("teachers"));
        map.put("revote", application.getAttribute("revote"));
        map.put("teachers_all", application.getAttribute("teachers_all"));
        map.put("isRevote", application.getAttribute("isRevote"));
        map.put("last", application.getAttribute("last"));
        map.put("pre", application.getAttribute("pre"));
        map.put("isPreRevote", application.getAttribute("isPreRevote"));
        map.put("revoteTimes", application.getAttribute("revoteTimes"));
        map.put("revoteResult", application.getAttribute("revoteResult"));
        map.put("preRevoteTimes", application.getAttribute("preRevoteTimes"));
        map.put("preRevoteResult", application.getAttribute("preRevoteResult"));
        map.put("lastTimes", application.getAttribute("lastTimes"));
        map.put("determineNum", application.getAttribute("determineNum"));
        map.put("first", application.getAttribute("first"));
        return map;
    }

    /**
     * 把 vote() 处理后的结果同步回 application（替代原 AdminController 中逐条 setAttribute）。
     */
    private void syncResultToApplication(ServletContext application, Map<String, Object> map) {
        application.setAttribute("limit", map.get("limit"));
        application.setAttribute("teachers", map.get("teachersNum"));
        application.setAttribute("isRevote", map.get("isRevote"));
        application.setAttribute("revote", map.get("revote"));
        application.setAttribute("last", map.get("last"));
        application.setAttribute("pre", map.get("pre"));
        application.setAttribute("isPreRevote", map.get("isPreRevote"));
        application.setAttribute("determineNum", map.get("determineNum"));
        application.setAttribute("revoteTimes", map.get("revoteTimes"));
        application.setAttribute("revoteResult", map.get("revoteResult"));
        application.setAttribute("preRevoteTimes", map.get("preRevoteTimes"));
        application.setAttribute("lastTimes", map.get("lastTimes"));
        application.setAttribute("first", map.get("first"));
        application.setAttribute("preRevoteResult", map.get("preRevoteResult"));
        application.setAttribute("all", map.get("all"));
    }

    /**
     * 组装对外返回的只读状态视图。
     * 与原 /admin/getVoteResult 的响应组装保持结构一致（students/pre/isRevote/teachersNum…）。
     */
    private Map<String, Object> buildStatusView(ServletContext application) {
        if (application.getAttribute("limit") == null) {
            return null;
        }
        int teachers = (int) application.getAttribute("teachers");
        int teachersAll = (int) application.getAttribute("teachers_all");
        int isRevote = (int) application.getAttribute("isRevote");
        List<User> last = (List<User>) application.getAttribute("last");
        List<User> pre = (List<User>) application.getAttribute("pre");
        if (last == null) {
            last = new ArrayList<>();
        }
        if (pre == null) {
            pre = new ArrayList<>();
        }

        // 重投开始后，首次组装时把 DB 全量名单作为“首轮结果快照”缓存到 all（与原逻辑一致）
        if (isRevote != 0 && application.getAttribute("all") == null) {
            List<User> allUsers = userDao.selectAll();
            allUsers.sort(new CustomUserComparator());
            application.setAttribute("all", allUsers);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("limit", application.getAttribute("limit"));
        map.put("teachersNum", teachers);
        map.put("revote", application.getAttribute("revote"));
        map.put("teachers_all", teachersAll);
        map.put("isRevote", isRevote);
        map.put("last", last);
        map.put("pre", pre);
        map.put("isPreRevote", application.getAttribute("isPreRevote"));
        map.put("revoteTimes", application.getAttribute("revoteTimes"));
        map.put("revoteResult", application.getAttribute("revoteResult"));
        map.put("preRevoteTimes", application.getAttribute("preRevoteTimes"));
        map.put("preRevoteResult", application.getAttribute("preRevoteResult"));
        map.put("lastTimes", application.getAttribute("lastTimes"));
        map.put("determineNum", last.size() + pre.size());
        map.put("all", application.getAttribute("all"));

        int preNum = currentPreNum(application);
        // students 三态（与原逻辑一致）：
        // 1) 有 revote → 展示重投名单；2) 候补已齐 → 展示正选名单；3) 否则 → 全部候选人
        map.put("students", map.get("revote"));
        if (map.get("students") == null) {
            if (pre.size() == preNum) {
                map.put("students", last);
            } else {
                List<User> allUsers = userDao.selectAll();
                allUsers.sort(new CustomUserComparator());
                map.put("students", allUsers);
            }
        }
        // 候补未全部确认前，pre 不对外展示（与历史接口约定一致）
        if (pre.size() != preNum) {
            map.put("pre", null);
        }
        map.put("preNum", preNum);
        return map;
    }
}
