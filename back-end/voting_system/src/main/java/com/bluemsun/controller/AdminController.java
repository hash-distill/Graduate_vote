package com.bluemsun.controller;


import com.bluemsun.auth.AdminSessionManager;
import com.bluemsun.entity.User;
import com.bluemsun.entity.dto.ResultDto;
import com.bluemsun.service.UserService;
import com.bluemsun.service.impl.UserServiceImpl;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {
    @Autowired
    UserService userService;

    @Autowired
    AdminSessionManager adminSessionManager;

    /**
     * 管理员登录（内网轻量鉴权，见 docs/投票系统鉴权与身份投票设计.md §4）。
     * 登录成功后返回会话 token，前端以 X-Admin-Token 请求头携带访问 /admin/**。
     */
    @PostMapping("/login")
    public ResultDto<Object> login(@RequestBody Map<String, String> body) {
        String password = body == null ? null : body.get("password");
        if (!adminSessionManager.passwordMatches(password)) {
            return new ResultDto<>(false, "口令错误", null);
        }
        String token = adminSessionManager.createSession();
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expireMs", 4L * 60 * 60 * 1000);
        return new ResultDto<>(true, "登录成功", data);
    }

    /**
     * 管理员登出：销毁当前会话 token。
     */
    @PostMapping("/logout")
    public ResultDto<Object> logout(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        adminSessionManager.invalidate(token);
        return new ResultDto<>(true, "已退出登录", null);
    }

    /**
     * 批量录入待选人信息，上传待选人的信息，excel文件，文件name为file
     * @param file
     * @return
     */
    @RequestMapping("/uploadExcel")
    public ResultDto<Object> uploadExcel(@RequestParam("file") MultipartFile file){
        ResultDto<Object> rt = new ResultDto<>();
        // 处理上传的Excel文件
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                // 在这里可以调用相应的处理方法对Excel文件进行处理
                // 例如，可以使用Apache POI或其他Java库来解析Excel文件
                // 在文件上传处理方法中处理Excel文件
//                Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes)); // 根据Excel格式选择适当的Workbook
                Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
                Sheet sheet = workbook.getSheetAt(1); // 获取工作表
                int i = 0;
                List<User> list = new ArrayList<>();
                for (Row row : sheet) {

                    Cell nameCell = row.getCell(1);
                    Cell genderCell = row.getCell(2);
                    Cell politicsCell = row.getCell(3);
                    Cell collegeCell = row.getCell(4);

                    if (nameCell != null && genderCell != null && politicsCell != null && collegeCell != null) {
                        String name = nameCell.getStringCellValue();
                        String gender = genderCell.getStringCellValue();
                        String politics = politicsCell.getStringCellValue();
                        String college = collegeCell.getStringCellValue();

                        // 在这里可以对解析出的数据进行处理，例如打印或存储到数据结构中
                        System.out.println("姓名: " + name);
                        System.out.println("性别: " + gender);
                        System.out.println("政治面貌: " + politics);
                        System.out.println("学院: " + college);
                        System.out.println("----------------------");
                        if(!"".equals(name) && name!=null && !"姓名".equals(name)){
                            int gender_temp = 0;
                            if(gender.equals("男")){
                                gender_temp = 1;
                            }
                            User user = new User(name, gender_temp, politics, college, 0);
                            list.add(user);
                        }
                    }
                }
                workbook.close(); // 关闭Workbook
                boolean success = userService.insertAll(list);
                if(success){
                    rt.setMsg("上传成功");
                    rt.setResult(success);
                    rt.setData(list);
                } else {
                    rt.setMsg("存入数据库失败");
                    rt.setResult(success);
                }

                return rt;
            } catch (IOException e) {
                e.printStackTrace();
                rt.setResult(false);
                rt.setMsg("上传失败");
                return rt;
            }
        } else {
            rt.setResult(false);
            rt.setMsg("文件不能为空");

            return rt;
        }
    }

    /**
     * 单个录入待选人的信息
     *
     * @param user
     * @return
     */
    @RequestMapping("/uploadPeople")
    public ResultDto<Object> uploadPeople(@RequestBody User user){
        ResultDto<Object> rt = new ResultDto<>();
        boolean success = userService.insertOne(user);
        rt.setResult(success);
        if(success){
            rt.setMsg("学生信息录入成功");
            rt.setData(null);
        } else {
            rt.setMsg("学生信息录入失败");
            rt.setData(null);
        }
        return rt;
    }

    /**
     * 设置每人限投数、参与投票评委数、正选人数与候补人数
     * @param map
     * @param request
     * @return
     */
    @RequestMapping("/setMsg")
    public ResultDto<Object> setLimitAndTeachers(@RequestBody Map<String,Integer> map, HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>(false, "设置失败", null);

        // 获取前端传送的设置信息
        Integer limit = map.get("limit");   // 限投几票（=正选人数）
        Integer teachers = map.get("teachers"); // 参与投票的老师人数（=设备数）
        Integer students = map.get("students"); // 正选需要选出多少人
        Integer preNum = map.get("preNum"); // 候补人数（缺省取默认 4）
        if(limit == null || teachers == null || students == null){
            return rt;
        }
        if (preNum == null || preNum <= 0) {
            preNum = UserServiceImpl.DEFAULT_PRE_NUM;
        }
        // 参数一致性校验：limit 与 students 语义不同，但当前计票算法按 limit 作为“本轮应选名额”，
        // 为避免死锁/错误名单（见审查报告 H5），强制两者相等并提示；若业务确需分离需改算法。
        if (!Objects.equals(limit, students)) {
            rt.setMsg("设置失败：限投票数必须与正选人数一致");
            return rt;
        }
        Integer teachers_all = teachers;    // 使用teachers_all存储老师人数
        // 数据不为空，则设置成功
        rt = new ResultDto<>(true, "设置成功", null);
        ServletContext application = request.getServletContext();   // application域对象
        // 将用到的各种参数存入数据域
        application.setAttribute("limit", limit);
        application.setAttribute("teachers", teachers); // 当前投票的老师人数，可能会变
        application.setAttribute("students", students);
        application.setAttribute("preNum", preNum);     // 候补人数（可配置，默认4）
        application.setAttribute("teachers_all", teachers_all); // 参与投票的老师人数
        application.setAttribute("isRevote", 0);    // 是否
        application.setAttribute("revote", null);   // 重投的名单
        application.setAttribute("last", new ArrayList<User>());    // 正选名单
        application.setAttribute("pre", new ArrayList<User>()); // 候补名单
        application.setAttribute("isPreRevote", false); // 候补是否重投
        application.setAttribute("determineNum", 0);
        application.setAttribute("revoteTimes", 0); // 重投次数？
        // 每次正选重投的结果
        application.setAttribute("revoteResult", new HashMap<Integer, Map<String, Object>>());
        application.setAttribute("preRevoteTimes", 0);  // 候补重投次数
        application.setAttribute("lastTimes", 0);  // 如果正选重投只能确认第一候补，那么第二候补就需要从第lastTimes次的投票结果中确定
        application.setAttribute("first", 0);   // 保证一次投票结果只处理1次
        // 每次候补重投的结果
        application.setAttribute("preRevoteResult", new HashMap<Integer, Map<String, Object>>());
        application.setAttribute("all", null);
        boolean success = userService.setPollZero();    // 将票数归0
        // 按设备限一票：重置“本轮已投设备集合/轮次”并新建审计场次（无需分发链接）
        userService.resetVotingState(application);
        System.out.println("初始化完成， students:"+students + ",preNum:"+preNum + ",tearchs:"+teachers_all);
        return rt;
    }

    /**
     * 处理投票（计票/平票判定），并返回投票结果。
     *
     * 计票触发已统一收敛到 UserService 的单把状态锁内（recordVote 在名额归零时自动触发，
     * 本接口为管理员手动触发/兜底）。仅供管理员使用（已被 WebConfig 注册的鉴权拦截器保护）。
     *
     * @param request
     * @return 哪些学生平票了，需要重新投票
     */
    @RequestMapping("/getVoteResult")
    public ResultDto<Object> getVotesNum(HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>();
        ServletContext application = request.getServletContext();   // 获取application域

        if (application.getAttribute("limit") == null) {
            rt.setResult(false);
            rt.setMsg("请管理员先设置投票限制和老师数量");
            return rt;
        }

        Map<String, Object> data = userService.processVoteResultIfReady(application);
        if (data == null) {
            rt.setResult(false);
            rt.setMsg("投票结果处理失败，请检查名单与参数");
            return rt;
        }
        rt.setResult(true);
        rt.setMsg("success");
        rt.setData(data);
        return rt;
    }
}
