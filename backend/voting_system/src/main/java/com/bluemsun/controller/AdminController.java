package com.bluemsun.controller;


import com.bluemsun.entity.User;
import com.bluemsun.entity.dto.ResultDto;
import com.bluemsun.service.UserService;
import com.bluemsun.utils.CustomUserComparator;
import com.bluemsun.utils.CustomUserComparatorInsti;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.tomcat.util.bcel.classfile.ElementValue.STRING;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {
    private final Integer PRENUM = 2; // 预选的数量，最初商量是2个，害怕以后会更改，写成属性吧

    private static final Object lock = new Object();
//    private static int first = 0;   // 确保一次投票结果只处理一次
    @Autowired
    UserService userService;
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
     * 设置每个人最多投多少票，以及有多少位老师参与投票，选中几位同学
     * @param map
     * @param request
     * @return
     */
    @RequestMapping("/setMsg")
    public ResultDto<Object> setLimitAndTeachers(@RequestBody Map<String,Integer> map, HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>(false, "设置失败", null);

        // 获取前端传送的设置信息
        Integer limit = map.get("limit");   // 限投几票
        Integer teachers = map.get("teachers"); // 参与投票的老师人数
        Integer teachers_all = teachers;    // 使用teachers_all存储老师人数
        Integer students = map.get("students"); // 正选需要选出多少人
        if(limit!=null && teachers!=null && students!=null){
            // 数据不为空，则设置成功
            rt = new ResultDto<>(true, "设置成功", null);
        }
        ServletContext application = request.getServletContext();   // application域对象
        // 将用到的各种参数存入数据域
        application.setAttribute("limit", limit);
        application.setAttribute("teachers", teachers); // 当前投票的老师人数，可能会变
        application.setAttribute("students", students);
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
        System.out.println("初始化完成， students:"+students + ",tearchs:"+teachers_all);
        return rt;
    }

    /**
     * 处理投票，并返回投票结果
     * @param request
     * @return 哪些学生平票了，需要重新投票
     */
    @RequestMapping("/getVoteResult")
    public ResultDto<Object> getVotesNum(HttpServletRequest request){
        ResultDto<Object> rt = new ResultDto<>();
        ServletContext application = request.getServletContext();   // 获取application域
        CustomUserComparator comparator = new CustomUserComparator();   // 学院比较器

        // 从application域中获取相关信息
        int limit = 0;
        int teachers = 0;
        int students = 0;
        int teachers_all = 0;
        try {
            limit = (int) application.getAttribute("limit");
            teachers = (int) application.getAttribute("teachers");
            students = (int) application.getAttribute("students");
            teachers_all = (int) application.getAttribute("teachers_all");
        } catch (Exception e){
            rt.setResult(false);
            rt.setMsg("请管理员先设置投票限制和老师数量");
            return rt;
        }

        // 使用map封装需要的响应数据
        Map<String, Object> map = new HashMap<>();
        map.put("limit", limit);
        map.put("teachersNum", teachers);
        map.put("revote", application.getAttribute("revote"));

        map.put("teachers_all", teachers_all);
        map.put("isRevote", application.getAttribute("isRevote"));
        map.put("last", application.getAttribute("last"));

        map.put("pre", application.getAttribute("pre"));
        map.put("isPreRevote", application.getAttribute("isPreRevote"));    // 重投的是候补，是：true，否：false
        map.put("revoteTimes", application.getAttribute("revoteTimes"));
        map.put("revoteResult", application.getAttribute("revoteResult"));
        map.put("preRevoteTimes", application.getAttribute("preRevoteTimes"));
        map.put("preRevoteResult", application.getAttribute("preRevoteResult"));
        map.put("lastTimes", application.getAttribute("lastTimes"));
        map.put("determineNum", application.getAttribute("determineNum"));
//        map.put("first", application.getAttribute("first"));
        //已经开始重投后，将数据库中的所有学生信息存入all中
        if(application.getAttribute("all") == null && (int)application.getAttribute("isRevote")!=0){
            List<User> list = userService.getAllUsers();
            list.sort(comparator);
            application.setAttribute("all", list);
        }
        map.put("all", application.getAttribute("all"));

        // 若老师们已经投完，并且还没确定最终结果，则处理本次投票结果
        synchronized(lock) {
            map.put("first", application.getAttribute("first"));    // 确保一次投票结果，只被处理1次
            List<User> pre = (List<User>) map.get("pre");
            int first = (int) map.get("first");
            if (teachers == 0 && pre.size() != PRENUM) {
                if(first == 0) {
                    System.out.println("第一次处理本次投票结果");

                    map = userService.vote(application, map, PRENUM);

                    if (map != null) {
                        // 将map中的信息同步到application中
                        application.setAttribute("limit", map.get("limit"));
                        application.setAttribute("teachers", map.get("teachersNum")); // 当前投票的老师人数，可能会变
                        application.setAttribute("isRevote", map.get("isRevote"));    // 是否
                        application.setAttribute("revote", map.get("revote"));   // 重投的名单
                        application.setAttribute("last", map.get("last"));    // 正选名单
                        application.setAttribute("pre", map.get("pre")); // 候补名单
                        application.setAttribute("isPreRevote", map.get("isPreRevote")); // 候补是否重投
                        application.setAttribute("determineNum", map.get("determineNum"));
                        application.setAttribute("revoteTimes", map.get("revoteTimes")); // 重投次数？
                        // 每次正选重投的结果
                        application.setAttribute("revoteResult", map.get("revoteResult"));
                        application.setAttribute("preRevoteTimes", map.get("preRevoteTimes"));  // 候补重投次数
                        application.setAttribute("lastTimes", map.get("lastTimes"));  // 如果正选重投只能确认第一候补，那么第二候补就需要从第lastTimes次的投票结果中确定
                        application.setAttribute("first", map.get("first"));
                        // 每次候补重投的结果
                        application.setAttribute("preRevoteResult", map.get("preRevoteResult"));
                        application.setAttribute("all", map.get("all"));


                    }
                } else {
                    System.out.println("屏蔽后续处理");
                }
            }
        }

        if(map == null){
            rt.setResult(false);
            rt.setMsg("failed");
        } else {
            rt.setResult(true);
            rt.setMsg("success");
            List<User> last = (List<User>) map.get("last");
            List<User> pre = (List<User>) map.get("pre");
            map.put("determineNum", last.size()+pre.size());

            /*
             响应数据中的students中存放当前需要展示的学生，有以下3种情况
                1、还未进行投票获证正在进行第一轮投票：存放数据库种所有的学生信息
                2、已经进行完第一轮投票，但是需要重投：存放当前需要重投的学生信息
                3、已经投票完毕：存放正选学生信息
             */
            map.put("students", map.get("revote"));    // 默认存放当前需要重投的学生信息
            // 处理students中应该存放什么！！！！！！！！！！！！好像还有问题，应该放在处理结果之后？
            if(map.get("students") == null){
                if(pre.size() == PRENUM){
                    // 预选名单已经确定，说明投票结束，对应3
                    map.put("students", (List<User>)map.get("last"));
                }
                else {
                    // 投票没结束，并且revote是空，对应1
                    List<User> u = userService.getAllUsers();
                    u.sort(new CustomUserComparator());
                    map.put("students", u);
                }
            }
        }

        List<User> pre = (List<User>) application.getAttribute("pre");
        if(pre.size() != 2){
            // 候补全部确定之后，一起返回给前端
            map.put("pre", null);
        }
        rt.setData(map);

        return rt;
    }
}
