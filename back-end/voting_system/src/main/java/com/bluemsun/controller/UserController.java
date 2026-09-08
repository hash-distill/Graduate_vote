
package com.bluemsun.controller;

import com.bluemsun.entity.User;
import com.bluemsun.entity.dto.ResultDto;
import com.bluemsun.service.UserService;
import com.bluemsun.utils.CustomUserComparator;
import com.bluemsun.utils.CustomUserComparatorInsti;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/hello")
    public ResultDto<String> hello(){
        return new ResultDto<>(true, "hello", "hello");
    }

    /**
     * 获取本轮待投票名单（按设备限一票，无口令/链接/编号）。
     *
     * 前端首次访问时自动生成设备标识 deviceId 并保存在 localStorage（见 auth.js getDeviceId），
     * 每次请求经请求头 X-Device-Id 带给后端。
     * - 名单：重投轮返回重投名单（revote），否则返回全部候选人；
     * - 返回该设备本轮是否已投（alreadyVoted），供前端禁用表单；
     * - 不再要求任何预发放凭证。
     */
    @GetMapping("/users")
    public ResultDto<Object> getAll(@RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                    HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>();
        ServletContext application = request.getServletContext();

        if(application.getAttribute("limit") == null){
            rt.setResult(false);
            rt.setMsg("请管理员先设置投票限制和老师数量");
            return rt;
        }

        List<User> users = (List<User>) application.getAttribute("revote");
        if (users == null) {
            users = userService.getAllUsers();  // 获取数据库中所有的学生名单
        }
        users.sort(new CustomUserComparatorInsti());    // 按学院排序

        Map<String, Object> map = new HashMap<>();
        map.put("limit", application.getAttribute("limit"));
        map.put("teachersNum", application.getAttribute("teachers"));
        map.put("students", users);
        map.put("deviceId", deviceId);
        map.put("alreadyVoted", userService.hasVotedCurrentRound(application, deviceId));

        rt.setMsg("success");
        rt.setResult(true);
        rt.setData(map);

        return rt;
    }

    /**
     * 投票（按设备限一票）。
     *
     * 设备标识由请求头 X-Device-Id 携带；同设备同轮重复提交会被 recordVote 拒绝。
     * 记账/幂等/名额推导统一收敛到 UserService.recordVote 的同一把状态锁内执行。
     */
    @PostMapping("/vote")
    public ResultDto<Object> vote(@RequestBody List<Integer> students,
                                  @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                  HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>();
        ServletContext application = request.getServletContext();
        Map<String, Object> map = new HashMap<>();

        StringBuilder msg = new StringBuilder();
        boolean success = userService.recordVote(application, deviceId, students, msg);
        rt.setResult(success);
        rt.setMsg(msg.toString());
        synchronized (com.bluemsun.service.impl.UserServiceImpl.STATE_LOCK) {
            map.put("teachersNum", application.getAttribute("teachers"));
        }
        map.put("deviceId", deviceId);
        rt.setData(map);
        return rt;
    }

    /**
     * 投票状态/结果只读接口（无副作用），供评委 waiting、结果展示等页面轮询。
     * 未初始化时返回 result=false + 提示；正常返回与历史 /admin/getVoteResult 的 data 结构一致。
     */
    @PostMapping("/vote/status")
    public ResultDto<Object> status(HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>();
        ServletContext application = request.getServletContext();
        Map<String, Object> data = userService.assembleVoteStatus(application);
        if (data == null) {
            rt.setResult(false);
            rt.setMsg("请管理员先设置投票限制和老师数量");
            return rt;
        }
        rt.setResult(true);
        rt.setMsg("success");
        rt.setData(data);
        return rt;
    }
}
