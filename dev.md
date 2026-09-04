# 健康管理系统 · 开发总览（dev.md）

> 给全队看的工程说明：**先读这一份**。
> 目标：让每个人知道「项目在做什么」「模块怎么分工」「核心概念（role/表/业务链路）」「系统怎么跑起来、登录怎么进、各角色主界面怎么接」。

> ⚠️ **拉取最新代码后，第一件事：整库重建数据库**（角色已收敛、收费已并入后台，旧库结构已过时）
> 1) `DROP DATABASE IF EXISTS healthysystem;`
> 2) 整体执行根目录 `schema.sql`（重建库 + 7 张表，含 `fee`）
> 3) 整体执行根目录 `data.sql`（3 个登录账号等演示数据，**注意 UTF-8**）
> 登录框标签是「账号」= 注册手机号（`users.tel`）；角色只剩两种 `0患者 | 1医生`（原 role2 管理员的职责已并入医生，代码里不再有 role=2 分支）。
> 测试账号：医生 `13800138000 / 123456`（王医生）、`13900139000 / 123456`（张医生）；患者 `13700137000 / 123456`（患者小王）。

---

## 1. 项目是什么

一个 **体检（健康管理）系统**：患者选套餐预约体检 → 医生录检查项结果 → 系统出报告；维护基础数据、用户、预约、收费等后台职责也并入医生。**两类角色**放在同一个 `users` 表，用 `role` 区分：

| role 值 | 角色 | 归属模块 |
|:---:|:---|:---|
| 0 | 患者 patient | PatientModule |
| 1 | 医生 doctor | AdminModule（含原管理员职责：检查项/套餐/用户/预约/结果录入/收费） |

登录账号 = **注册手机号**（`users.tel`，主键），登录框标签叫「账号」。**整个系统只有一个登录入口**：MainModule。

---

## 2. 技术栈与模块结构

JDK 17 + Maven 多模块 + MySQL 8 + JDBC（无框架）+ Swing + OpenPDF（报告导出 PDF）。

```
healthysystem（父 pom）
├─ Common        实体/DAO/JdbcUtil ——✅已完成，谁都能依赖
├─ MainModule    系统总入口：含唯一登录窗（背景图）+ 登录后按 role 打开各模块主窗 ——✅已跑通(0患者/1医生)
├─ AdminModule   后台（role1 医生，含原管理员职责） ——🟡基本完成(检查项/套餐/用户/预约/结果录入/收费)
├─ PatientModule 患者端 ——🟡基本完成(套餐浏览/预约/结果/资料/打印报告)，独立运行 PatientMain
├─ ReportModule  报告模块 ——🟡基本完成(报告预览/打印/导出PDF)，独立运行 ReportMain
├─ FeeModule     收费管理(队长维护)：收费登记/收费记录/退款 ——✅已完成，收费面板已并入 AdminModule 后台页签，另保留独立入口 FeeMain
└─ Sources/      设计素材源图（如登录背景 login-bg-source.png）
```

依赖关系（都不成环）：`Common` 被各模块依赖；`AdminModule`→Common+FeeModule；`ReportModule`→Common(+OpenPDF)；`PatientModule`→Common+ReportModule；`FeeModule`→Common；`MainModule`→Common+AdminModule+PatientModule（FeeModule 随 AdminModule 传递依赖，收费已并入后台页签）。

---

## 3. 核心概念与业务对象

| 表 | Common 实体 | 一句话 |
|:---|:---|:---|
| `users` | `User` | 患者/医生一张表，`role` 区分 |
| `checkitem` | `CheckItem` | 单个检查项（空腹血糖等） |
| `checkgroup` | `CheckGroup` | 套餐（多个检查项组成） |
| `checkgroup_item` | `CheckGroupItem` | 套餐-检查项关联 |
| `registration` | `Registration` | 患者对套餐的预约（0已约/1已完成/2已取消） |
| `check_result` | `CheckResult` | 一次预约里各项的结果值（报告数据源） |
| `fee` | `Fee` | 收费(队长新增)：一条预约一次收费 |

**业务链路**：医生先维护检查项/套餐 → 患者预约（写 `registration`）→ 医生对预约逐项录结果（写 `check_result`）→ 预约标记完成 → ReportModule 汇总出报告 → 患者查看。

**各表字段**（SQL/界面照这个写，建表以根目录 `schema.sql` 为准）：
- **users**：`tel`(主键,账号) `pwd` `name` `idcard` `birthday` `sex` `role`
- **checkitem**：`cid`(主键) `bh`(编号) `cname` `dw`(单位) `ckfw`(参考范围) `status`(0正常|1下架)
- **checkgroup**：`gid`(主键) `gname` `bh` `remark` `price`(套餐价,元；收费按此入账) `status`(0正常|1停用)
- **checkgroup_item**：`id`(自增) `gid`→checkgroup `cid`→checkitem
- **registration**：`id`(自增) `tel`→users `gid`→checkgroup `reg_time` `status`
- **check_result**：`id`(自增) `reg_id`→registration `tel`→users `cid`→checkitem `result_value` `doctor_tel` `check_time`
- **fee**：`id`(自增) `reg_id`→registration `tel`→users `gid`→checkgroup `amount`(金额,元) `status`(0待缴|1已缴|2已退款) `pay_time` `operator`(收费员账号) `remark`

---

## 4. 各模块职责

| 模块 | 状态 | 职责 |
|:---|:---|:---|
| **Common** | ✅ 完成 | 6 实体、6 DAO、`JdbcUtil`。需要读写先查 §5，别重复造轮子。 |
| **MainModule** | ✅ 登录入口已通 | 唯一登录窗（账号+密码，账号即注册手机号，背景图在 `MainModule/src/main/resources/login_background.png`，源图在 `Sources/login-bg-source.png`）。登录成功按 role 打开对应主窗：role0→`PatientHomeFrame`；role1 医生（含原管理员职责）→ `AdminFrame` 后台窗。**各角色模块不再自己做登录。** |
| **AdminModule** | 🟡 基本完成 | **医生(role1，含原管理员职责)**：维护检查项/套餐及关联、用户管理、预约状态、给预约录 `check_result`（"结果录入"）、收费登记/记录。role1 医生登录进 `AdminFrame` 后台窗（侧边导航七个页签），不再单独开医生窗。 |
| **PatientModule** | 🟡 基本完成 | 患者：浏览套餐、预约/取消、看自己结果、改资料；主窗 `PatientHomeFrame(tel,name)` 已接 LoginFrame。报告入口在「我的结果」→ 打印报告（打开 `ReportFrame`）。独立运行 `PatientMain`（演示患者 13700137000）。 |
| **ReportModule** | 🟡 基本完成 | 按预约汇总结果出报告：预览 + 打印（`java.awt.print`）+ 导出 PDF（OpenPDF，中文用 `STSong-Light`）。独立运行 `ReportMain`（演示患者 13700137000）。 |
| **FeeModule** | ✅ 已完成(队长) | 收费台：对"已预约且尚未收费"的预约收费入账（写 `fee`，状态 1 已缴）、查看收费记录、退款（置 2 已退款）。**收费面板已并入 AdminModule 后台页签**（AdminModule 依赖 FeeModule，`AdminFrame` 增加「收费登记/收费记录」两页签，用登录 tel 作收费员）；另保留独立入口 `com.ncu.fee.FeeMain`。 |
| **MainModule 的 role 分发** | ✅ 已接 | `LoginFrame.onLogin()` switch：case 0→`PatientHomeFrame`；case 1（医生）→ `AdminFrame`。 |

> 具体功能清单以队长分工说明为准，本表是模块定位。

---

## 5. Common 能直接给我什么（API 速查）

DAO 都是直连 JDBC 的简单类，`new XxxDao()` 即用：

- **UserDao**：`insert` / `findByTel`(登录) / `findAll` / `findByRole(int)` / `update` / `delete(tel)`
- **CheckItemDao**：`insert` / `findAll` / `findPage(page,size)` / `count` / `findByBh` / `update` / `delete(cid)`
- **CheckGroupDao**：`insert` / `findAll` / `findByName` / `update` / `delete(gid)`
- **CheckGroupItemDao**：`insert` / `findByGid` / `deleteByGid` / `delete(id)`
- **RegistrationDao**：`insert` / `findByTel` / `findAll` / `update` / `delete(id)`
- **CheckResultDao**：`insert` / `findByTel` / `findByRegId` / `update` / `delete(id)`

实体：`User / CheckItem / CheckGroup / CheckGroupItem / Registration / CheckResult`（`com.ncu.common.model`）。工具：`com.ncu.common.util.JdbcUtil`（`getConnection`/`close`，照抄 DAO 写法）。

> 需要"按主键查某条 / 联表带名称"这类 Common 没有的方法时，参照 `AdminModule/dao/AdminDao.java` 的写法，在**自己模块**里补一个 Dao，别改 Common 里已有的（避免大家冲突）。

> **fee 是队长新增的表**：实体 `com.ncu.common.model.Fee` 已放 Common；但收费读写目前只有 FeeModule 用，所以 DAO 没进 Common，放在 `FeeModule/dao/FeeDao.java`（含按预约查判重、待收费预约、联表带姓名等）。将来报告/患者端要读收费，由队长把基础 CRUD 提升到 Common。

---

## 6. 建库 / 数据 / 配置

- **从零建库**：整体执行根目录 `schema.sql`（`CREATE DATABASE IF NOT EXISTS healthysystem` + 全部 7 张表，含 `fee`）。
- **本地库是旧的（含 role=2 的账号、或缺 fee 表）**：直接**整库重建**（见文首三条），别只补脚本片段——`schema.sql`/`data.sql` 是结构、注释与演示数据的唯一权威。
- **演示数据**：执行根目录 `data.sql`（3 个登录账号、10 检查项、3 套餐、12 条套餐-项关联、2 预约、1 次结果、1 条收费）。**三个套餐价格**：入职 268 / 常规 588 / 心血管 888（存 `checkgroup.price`），收费登记按套餐价直接入账。**执行 schema.sql/data.sql 必须用 UTF-8**（IDEA 控制台跑，或 `mysql --default-character-set=utf8mb4 < xxx.sql`），否则中文字段变空串，导致姓名/套餐名/检查项名显示空白。
- **登录账号（账号 = 注册手机号 `users.tel`，登录框标签叫「账号」）**：
  - 医生(role1) `13800138000 / 123456`（王医生）——后台全职责：检查项/套餐/用户/预约/结果录入 + 收费
  - 医生(role1) `13900139000 / 123456`（张医生）
  - 患者(role0) `13700137000 / 123456`（患者小王）
- 连库配置在 **Common** `src/main/resources`：
  - `db.properties` = 模板，**只放占位（root/root），禁止把真实密码提交进去**；
  - `db-local.properties` = 你自己机器的真实密码，已被 .gitignore 忽略，`JdbcUtil` 优先读它。

**fee（收费）说明（队长维护）**：`fee` 实体在 Common、读写 DAO 在 `FeeModule/dao/FeeDao.java`；收费面板已并入医生后台 `AdminFrame`（「收费登记/收费记录」两页签，用登录医生 tel 作收费员），另保留独立入口 `com.ncu.fee.FeeMain`。任何模块想读收费，**先找队长**统一提升到 Common，不要各自另写一份 fee DAO。

> **收费登记 = 按套餐价入账（改版，金额不再手输）**：待收费列表由 `FeeDao.findUnchargedRegs` JOIN `checkgroup` 带出该预约套餐的 `price`，`ChargePanel` 点「确认收费」只弹「套餐价 ￥xxx 确认入账？」→ `FeeController.charge` 直接把套餐价写成 fee.amount。**若某套餐没有 price（如界面新增套餐没配价）**，收费时会提示先补 price。价格字段归属套餐（checkgroup），收费只读不写；谁要新增/改套餐价，改 `data.sql` 或库里的 `checkgroup.price` 即可，患者端/后台套餐管理暂不展示价格。

---

## 7. 登录与角色主窗怎么接（重要）

**登录只发生在 MainModule**：`Main.java` 启动 → `LoginFrame`（背景图窗）→ `MainController.login` → 复用 `Common.UserDao.findByTel` 校验 → 成功拿到当前用户（`tel/name/role`）→ `onLogin()` 里 switch role 打开对应主窗。

**你要做的事**（按你负责的模块）：
- **写患者/医生/报告主窗的同学**：不需要再做登录框。只要提供"主窗类"，由队长把它的构造和 `role` 接进 `MainModule/view/LoginFrame.java` 的 switch（现状只 0/1 两种角色）：
  ```java
  case 0:  // 患者
      dispose();
      new com.ncu.patient.view.PatientHomeFrame(u.getTel(), u.getName(), backToLogin).setVisible(true);
      break;
  case 1:  // 医生（含原管理员职责）→ AdminModule 后台窗
      dispose();
      new com.ncu.admin.view.AdminFrame(u.getTel(), u.getName(), "医生", backToLogin).setVisible(true);
      break;
  ```
- 主窗类构造器建议接收 `tel` + `name`（当前登录人），不要再传整个 DAO。
- 角色编号统一用整数并加注释：`0患者 / 1医生`（已无 2 管理员）。

**患者自助注册**（MainModule，已完成）：登录卡片下方「没有账号？注册」→ `RegisterDialog`，只收 **账号(手机号)/姓名/密码/确认密码** 四栏，校验后经 `MainController.register` → 复用 `Common.UserDao.insert`，**固定建 role=0 患者**（医生账号不开放自助注册，仍由数据脚本/后台维护）；注册成功回填账号到登录框。其余资料（身份证/性别/生日）留待患者登录后在「个人资料」里补。

---

## 8. 编码 / 协同约定

- 包名 `com.ncu.<模块小写>`；实体/DAO 放 Common 要**先跟队长确认**；自己的模块可加自己的 Dao/VO（如 AdminDao、RegistrationVO）。
- UI 文案中文；异常 `printStackTrace()` 打印、别抛崩界面；删改前判空、给用户提示，**删除类操作要判断 DAO 返回值**（外键引用会导致删除失败，别静默当成功）。
- **`users.pwd` 是明文**（VARCHAR(20) 放不下 AES 密文）；以后要加密须先扩列再改 DAO/登录，目前别动。
- Git：提交信息中文；**不提交** `db-local.properties`/`target/`/`.idea/`，也别把真实数据库密码写进 `db.properties`；协作者直接推分支，队长合并进 main（参考：`git fetch origin` → 检查分支 → `git merge origin/<分支>` → `git push origin main`）。

---

## 9. 交接前自查清单

- [ ] 我的模块能编译（`mvn -pl 我的模块 -am compile`，JDK17）？
- [ ] 需要登录数据时用 MainModule 入口测试，不自己再造登录框？
- [ ] 我的主窗类能让 MainModule 按 role 打开（告诉队长类名和构造参数）？
- [ ] 删除类操作处理了 DAO 返回 false / 外键失败？
- [ ] 本地跑通 + 推分支 + 群里喊队长合并？
