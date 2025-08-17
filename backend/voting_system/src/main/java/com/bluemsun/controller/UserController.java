
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
//@RequestMapping("/user")
public class UserController {

    private static final Object lock = new Object();

    @Autowired
    UserService userService;
    @RequestMapping("/hello")
    public ResultDto<String> hello(){
        return new ResultDto<>(true, "hello", "hello");
    }

    /**
     * 获取全部待选人
     * @param request
     * @return
     */
    @GetMapping("/users")
    public ResultDto<Object> getAll(HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>();
        ServletContext application = request.getServletContext();
        List<User> users = (List<User>) application.getAttribute("revote");
        if(users != null) { // 已经开始重投
            if((int)application.getAttribute("isRevote")==1) {  // 开始重投后，第一次访问这个请求
                // 将老师数量重置为最初设置的数量
                application.setAttribute("teachers", application.getAttribute("teachers_all"));
                // isRevote+1,防止下一位老师投票时，teachers再次被重置
                application.setAttribute("isRevote", (int) application.getAttribute("isRevote") + 1);
            }
        } else{ // 还未重投

            users = userService.getAllUsers();  // 获取数据库中所有的学生名单
            users.sort(new CustomUserComparator());
        }
        Map<String, Object> map = new HashMap<>();

        if(application.getAttribute("limit") == null){
            rt.setResult(false);
            rt.setMsg("请管理员先设置投票限制和老师数量");
            return rt;
        }
        users.sort(new CustomUserComparatorInsti());    // 排序
        // 封装响应数据
        map.put("limit", application.getAttribute("limit"));
        map.put("teachersNum", application.getAttribute("teachers"));
        map.put("students", users);

        rt.setMsg("success");
        rt.setResult(true);
        rt.setData(map);

        return rt;
    }

    /**
     * 投票，接收数组数据，里面存对应学生的id（数据库的主键）
     * @param students
     * @param request
     * @return
     */
    @PostMapping("/vote")
    public ResultDto<Object> vote(@RequestBody List<Integer> students, HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>();
        ServletContext application = request.getServletContext();
        Map<String, Object> map = new HashMap<>();
        if(application.getAttribute("limit") == null){
            rt.setResult(false);
            rt.setMsg("请管理员先设置投票限制和老师数量");
            return rt;
        }
        map.put("limit", application.getAttribute("limit"));

        boolean success = true;
        // 可能多个老师同时投票，保证线程安全
        synchronized(lock){
            List<User> revote = (List<User>) application.getAttribute("revote");
            if(revote != null){
                // 已经在重投，修改application域中学生的票数
                for(User user : revote){
                    for(Integer id: students){
                        if (Objects.equals(user.getVoteId(), id)){
                            user.setVotePoll(user.getVotePoll()+1);
                        }
                    }
                }
            } else {
                // 第一次投票，修改数据库里的学生票数
                success = userService.updateByIds(students);
            }
        }
        rt.setResult(success);
        if(success){
            rt.setMsg("投票成功");
            application.setAttribute("first", 0);
            synchronized(lock){
                int teachers = (int) application.getAttribute("teachers");
                teachers = teachers-1;
                application.setAttribute("teachers", teachers);
            }
            map.put("teachersNum", application.getAttribute("teachers"));
            rt.setData(map);

        } else {
            rt.setMsg("投票失败");
            map.put("teachersNum", application.getAttribute("teachers"));
            rt.setData(map);
        }
        return rt;
    }
}
