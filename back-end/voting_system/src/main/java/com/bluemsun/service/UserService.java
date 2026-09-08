package com.bluemsun.service;

import com.bluemsun.entity.User;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletContext;
import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> getAllUsers();

    boolean insertOne(User user);

    boolean updateByIds(List<Integer> students);

    Map<String, Object> getRevote(List<User> list, int students, List<User> last);

    Map<String, Object> getPreRevote(List<User> list, int students, List<User> pre);

    boolean insertAll(List<User> users);

    boolean setPollZero();

    boolean updatePollToFirst(List<User> list);


    Map<String, Object> vote(ServletContext application, Map<String, Object> map, Integer prenum);

    /**
     * 记录一台设备的投票（按设备限一票）。
     * 在统一状态锁内执行“加票/记录设备/名额推导”，并当本轮已投设备数归零名额时自动触发计票。
     *
     * @param application servlet 上下文（现状态存放处）
     * @param deviceId    投票设备标识（前端生成并保存，见 auth.js getDeviceId）
     * @param students    本轮所选候选人 id
     * @param outMsg      操作结果消息
     * @return 是否受理成功
     */
    boolean recordVote(ServletContext application, String deviceId, List<Integer> students, StringBuilder outMsg);

    /**
     * 该设备是否已投过“当前轮次”（同设备防重复）。
     */
    boolean hasVotedCurrentRound(ServletContext application, String deviceId);

    /**
     * 场次初始化：清空“本轮已投设备集合”并把轮次复位为 1（无预生成链接，评委在各自设备直接投）。
     */
    void resetVotingState(ServletContext application);

    /**
     * 组装投票结果/状态视图（只读，不触发计票副作用）。
     * 供评委 waiting/userShow/show 等页面轮询，替代原先轮询带副作用的管理接口。
     */
    Map<String, Object> assembleVoteStatus(ServletContext application);

    /**
     * 触发（一次）计票处理：当本轮已投设备数达到应投数量且结果未处理时执行平票判定/名单确认，
     * 并同步回 application。管理员接口兜底/手动触发使用。
     */
    Map<String, Object> processVoteResultIfReady(ServletContext application);
}
