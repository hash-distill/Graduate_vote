# 研究生支教团投票系统 — 代码问题分析报告

> 本文档基于对前后端完整代码的审查，总结了当前投票系统中存在的逻辑缺陷、安全隐患、并发风险和架构设计问题。

---

## 一、安全问题

### 1.1 认证拦截器未生效（严重 🔴）
**文件**: `AuthenticationInterceptor.java`

拦截器的 `preHandle` 方法直接 `return true`，且没有找到对应的 `WebMvcConfigurer` 配置类，这意味着：
- 所有管理员接口（`/admin/*`）**无需任何身份验证**即可访问
- 任何人可以调用 `/admin/setMsg` 重置投票参数、`/admin/uploadExcel` 上传候选人，或 `/admin/getVoteResult` 获取投票结果
- 存在严重的越权风险

```java
// AuthenticationInterceptor.java - 拦截器完全无效
public boolean preHandle(...) {
    return true; // 直接放行，没有任何鉴权逻辑
}
```

### 1.2 数据库密码硬编码（中等 🟡）
**文件**: `application.yml`

数据库连接密码 `123zxhs321` 直接明文写在配置文件中并提交至版本库，存在密码泄露的安全风险。建议使用环境变量或加密配置。

```yaml
# application.yml
password: 123zxhs321  # 硬编码密码，应使用环境变量替代
```

### 1.3 CORS 全局开放（中等 🟡）
**文件**: `AdminController.java`, `UserController.java`

所有 Controller 都使用了 `@CrossOrigin` 注解且未限制来源域，等于任何网站都能发起跨域请求调用投票接口，容易被恶意利用。

---

## 二、并发与线程安全问题

### 2.1 多把锁导致的竞态条件（严重 🔴）
**文件**: `UserController.java`, `AdminController.java`

`UserController` 和 `AdminController` 各自声明了一个 `private static final Object lock`，但它们操作的是**同一个** `ServletContext application` 域对象。由于使用不同的锁对象，无法保证跨 Controller 的线程安全。

```java
// UserController.java
private static final Object lock = new Object(); // 锁 A

// AdminController.java
private static final Object lock = new Object(); // 锁 B（不同对象！）
```

**风险场景**: 当多位教师同时投票（修改 `teachers` 计数器）时，`UserController.vote()` 中的 `synchronized(lock)` 与 `AdminController.getVoteResult()` 中的 `synchronized(lock)` 使用的**不是同一把锁**，可能导致 `teachers` 计数器的读写不一致。

### 2.2 `vote()` 方法中锁使用粒度不当（中等 🟡）
**文件**: `UserController.java`（第 92-116 行）

投票逻辑被拆分成了两个独立的 `synchronized` 块（第 92-107 行和第 112-116 行），中间存在非同步代码段（第 111 行 `application.setAttribute("first", 0)`），这可能导致中间状态被其他线程读到。

```java
synchronized(lock){
    // 第一段：更新票数
}
// ← 这里存在间隙，first 已设置但 teachers 尚未递减
rt.setResult(success);
if(success){
    application.setAttribute("first", 0);  // 在锁外操作
    synchronized(lock){
        // 第二段：递减 teachers
    }
}
```

### 2.3 `first` 标志的竞态风险（中等 🟡）
**文件**: `UserController.java` 第 111 行, `AdminController.java` 第 243 行

`first` 标志用于确保投票结果只被处理一次，但 `application.setAttribute("first", 0)` 的设置在 `UserController` 中是在锁外执行的。如果最后一位教师投票后、`teachers` 减为 0 时，另一个线程正好在调用 `getVoteResult()`，可能导致投票结果被多次处理或遗漏。

---

## 三、业务逻辑问题

### 3.1 `uploadExcel` 读取工作表索引硬编码（中等 🟡）
**文件**: `AdminController.java` 第 59 行

```java
Sheet sheet = workbook.getSheetAt(1); // 获取第2个工作表（索引从0开始）
```
读取的是 Excel 的**第 2 个工作表**（index=1），而非第 1 个。如果上传的 Excel 只有一个工作表，将直接抛出 `IndexOutOfBoundsException`。这个硬编码的假设很容易让使用者产生困惑。

### 3.2 `uploadExcel` 中性别解析不完整（低 🟢）
**文件**: `AdminController.java` 第 82-85 行

```java
int gender_temp = 0;
if(gender.equals("男")){
    gender_temp = 1;
}
```
只判断了"男"为 1，其余情况（包括"女"）都默认为 0。但数据库 SQL 中所有候选人的 `vote_gender` 均为 0，且注释为"性别"，逻辑含义不明确——0 到底代表男还是女？注释缺失，易引起维护困难。

### 3.3 `AdminController.getVoteResult()` 中候补人数硬编码为 2 （中等 🟡）
**文件**: `AdminController.java` 第 36 行, 第 309 行

```java
private final Integer PRENUM = 2; // 硬编码候补数为 2

// 第 309 行
if(pre.size() != 2){ // 再次硬编码 2
    map.put("pre", null);
}
```
候补人数作为常量 `PRENUM = 2` 声明，但在第 309 行又直接使用了字面量 `2` 而非 `PRENUM`。如果未来修改候补人数，很容易漏掉这里，造成逻辑不一致。

### 3.4 `getRevote()` 中正选名单收集逻辑可能遗漏候选人（严重 🔴）
**文件**: `UserServiceImpl.java` 第 55-80 行

```java
if(Objects.equals(list.get(students - 1).getVotePoll(), list.get(students).getVotePoll())) {
    boolean isFirst = false;
    int targetVotes = list.get(students).getVotePoll();
    for (int i = 0; i < list.size(); i++) {
        if (list.get(i).getVotePoll() == targetVotes) {
            // ...
            if(!isFirst) { isFirst = true; }
        }
        if(!isFirst){
            last.add(list.get(i)); // 将非平票的高票者加入正选
        }
    }
}
```

此处逻辑存在隐患：`isFirst` 在第一个平票者处被设为 `true`，但 **恰好在同一轮循环中**（因为 `isFirst` 在 `if` 块内才被设为 `true`，而 `if(!isFirst)` 在 `if` 块之后判断），**第一个平票者也会被错误地添加到 `last` 正选名单中**，因为 `isFirst` 在进入下一个条件判断时已经为 `true`，实际上第一个平票人不会被加入 last。但整体逻辑依赖于排序后票数恒为降序这一前提——如果前面存在与平票者票数不同但未被选入正选的人（边界情况），可能出现错误。

### 3.5 `getPreRevote()` 中候补确定逻辑的边界情况（中等 🟡）
**文件**: `UserServiceImpl.java` 第 100-161 行

当 `students == 2`（即需要选出 2 名候补）且参与人数恰好为 2 人时（第 130-141 行），如果两人平票，将进入无限循环的重投——因为只有 2 个人竞争 2 个位置，即使重投也必然再次平票（二人重投后必须选出 2 人，而只有 2 人参与，票数为 1:0 或 0:1）。
但实际上这种边界情况在正常投票流程中较少出现。

### 3.6 `UserDao.updatePollToFirst` 方法命名误导（低 🟢）
**文件**: `UserDao.java` 第 32 行

```java
@Select("select ... from vote01 where vote_id = #{id}")
User updatePollToFirst(int id);
```
方法名为 `updatePollToFirst`（暗示更新操作），但实际 SQL 是 `SELECT` 查询语句。这会误导维护者以为该方法会修改数据。

### 3.7 `setMsg` 接口未做参数校验（中等 🟡）
**文件**: `AdminController.java` 第 144-181 行

在 `limit`、`teachers`、`students` 均为 `null` 时仍会继续执行后续的 `application.setAttribute()` 和 `userService.setPollZero()`，将 `null` 存入 application 域，后续读取时会发生 `NullPointerException`。

```java
if(limit!=null && teachers!=null && students!=null){
    rt = new ResultDto<>(true, "设置成功", null);
}
// ↓ 即使参数为 null，以下代码仍然会执行！
application.setAttribute("limit", limit);  // limit 可能是 null
application.setAttribute("teachers", teachers);
// ...
boolean success = userService.setPollZero();  // 无论参数是否合法都会清零
```

---

## 四、架构与设计问题

### 4.1 使用 `ServletContext` 作为全局状态存储（严重 🔴）

当前系统将所有投票状态（`teachers`、`limit`、`students`、`revote`、`last`、`pre` 等十几个变量）存储在 `ServletContext`（application 域）中。

**问题**:
- **不可扩展**：如果部署多个实例或重启服务，所有状态丢失
- **无法持久化**：投票过程中服务器崩溃，数据全部丢失且无法恢复
- **代码可读性差**：大量 `application.getAttribute/setAttribute` 调用，状态管理分散在 Controller 和 Service 中，难以追踪数据流
- **类型不安全**：所有属性都以 `Object` 类型存取，需要强制类型转换

建议使用独立的状态管理类（如 `VoteSession` 单例）或数据库持久化投票过程数据。

### 4.2 Service 层直接操作 `ServletContext`（中等 🟡）
**文件**: `UserServiceImpl.java`

`vote()` 方法直接接收 `ServletContext` 参数，违反了分层架构原则。Service 层不应依赖 Servlet API，这使得：
- 单元测试困难（需要模拟 Servlet 容器）
- Service 与 Web 层紧耦合

### 4.3 缺少事务管理（中等 🟡）
**文件**: `UserServiceImpl.java`

`insertAll()` 和 `updateByIds()` 方法在循环中逐条执行数据库操作，如果中途失败，已经插入/更新的数据不会回滚，导致数据不一致。应添加 `@Transactional` 注解。

```java
// insertAll - 无事务保护
public boolean insertAll(List<User> users) {
    for(User user: users){
        int i = userDao.insertOne(user);
        if(i != 1){
            return false; // 已插入的数据不会回滚！
        }
    }
    return true;
}
```

### 4.4 未使用 RESTful 规范（低 🟢）

- `AdminController` 的所有接口都使用了 `@RequestMapping`（接受所有 HTTP 方法），应区分 `@GetMapping`、`@PostMapping` 等
- 接口路径命名不规范：如 `/admin/setMsg`、`/admin/uploadPeople` 应改为更具 REST 风格的命名

### 4.5 异常处理不规范（低 🟢）

- `uploadExcel` 中使用 `e.printStackTrace()` 打印异常，生产环境应使用日志框架
- 多处使用 `System.out.println()` 输出调试信息，应替换为 SLF4J / Logback 日志

---

## 五、前端相关问题

### 5.1 前端源代码缺失（严重 🔴）
`front-end/` 目录下仅包含构建产物 `dist/` 和配置文件，**不包含 `src/` 源代码目录**。这意味着：
- 无法对前端逻辑进行代码审查
- 无法进行前端代码的修改和重新构建
- 前端的问题排查只能通过分析压缩后的 JS 文件

### 5.2 前端 IP 地址硬编码
根据 README 描述，前端 JS 中硬编码了路由器的 IPv4 地址 `192.168.110.10`，每次部署环境变化都需要全局替换 URL，维护成本高。应使用环境变量或相对路径。

### 5.3 仅适配 Edge 浏览器
README 中提到系统只能适配 Edge 浏览器，在 Chrome 等其他浏览器中会出现页面错位和功能缺失。这通常是由 CSS 兼容性问题或浏览器特定 API 引起的，应排查并修复。

---

## 六、数据库设计问题

### 6.1 SQL 文件字符集不一致（低 🟢）
**文件**: `vote.sql`

表使用 `latin1` 字符集，但字段使用 `utf8`：
```sql
ENGINE = InnoDB ... CHARACTER SET = latin1 COLLATE = latin1_swedish_ci
```
而字段则声明为 `CHARACTER SET utf8`。虽然字段级字符集会覆盖表级设置，但这种不一致容易造成维护困惑，建议统一为 `utf8mb4`。

### 6.2 `vote_gender` 和 `vote_poll` 使用 `tinyint(255)`（低 🟢）
`tinyint` 的存储范围为 -128~127（有符号）或 0~255（无符号），`tinyint(255)` 中的 255 仅影响显示宽度，不影响存储。虽然功能上无问题，但显示宽度声明不规范。

---

## 七、总结

| 严重程度 | 问题数量 | 关键问题 |
|:---:|:---:|:---|
| 🔴 严重 | 4 | 认证缺失、并发锁不一致、正选收集逻辑隐患、ServletContext 状态管理 |
| 🟡 中等 | 8 | 密码硬编码、CORS 开放、锁粒度不当、参数校验缺失、事务管理等 |
| 🟢 低 | 4 | 性别解析、方法命名、RESTful 规范、SQL 字符集 |

> **建议优先级**：先解决安全问题（认证、CORS）和并发问题（统一锁对象），再优化架构设计和代码质量。
