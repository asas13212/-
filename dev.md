# 健康管理系统 · 开发总览（dev.md）

> 给全队看的工程说明：**先读这一份**。
> 目标：让每个人知道「项目在做什么」「模块怎么分工」「核心概念（role/表/业务链路）」「系统怎么跑起来、登录怎么进、各角色主界面怎么接」。

---

## 1. 项目是什么

一个 **体检（健康管理）系统**：患者选套餐预约体检 → 医生录检查项结果 → 系统出报告，管理员维护基础数据。**三类角色**放在同一个 `users` 表，用 `role` 区分：

| role 值 | 角色 | 归属模块 |
|:---:|:---|:---|
| 0 | 患者 patient | PatientModule |
| 1 | 医生 doctor | AdminModule（结果录入） |
| 2 | 管理员 admin | AdminModule（基础数据管理） |

登录账号 = **手机号**（`users.tel`，主键）。**整个系统只有一个登录入口**：MainModule。

---

## 2. 技术栈与模块结构

JDK 17 + Maven 多模块 + MySQL 8 + JDBC（无框架）+ Swing。

```
healthysystem（父 pom）
├─ Common        实体/DAO/JdbcUtil ——✅已完成，谁都能依赖
├─ MainModule    系统总入口：含唯一登录窗（背景图）+ 登录后按 role 打开各模块主窗 ——✅已跑通(管理员)
├─ AdminModule   后台：管理员(role2)+医生(role1) ——🟡基本完成(检查项/套餐/用户/预约/结果录入)
├─ PatientModule 患者端 ——⬜待开发
├─ FeeModule     收费管理(队长维护)：待收费登记/收费记录/退款 ——✅已完成，独立运行 FeeMain
├─ ReportModule  报告模块 ——⬜待开发
└─ Sources/      设计素材源图（如登录背景 login-bg-source.png）
```

依赖关系（都不成环）：`Common` ← `AdminModule`、`Common` ← `FeeModule`；`MainModule` 依赖 `Common + AdminModule`（将来患者/报告/收费等做好，MainModule 再加对应依赖以便按 role/入口打开各自主窗）。

---

## 3. 核心概念与业务对象

| 表 | Common 实体 | 一句话 |
|:---|:---|:---|
| `users` | `User` | 三类用户一张表，`role` 区分 |
| `checkitem` | `CheckItem` | 单个检查项（空腹血糖等） |
| `checkgroup` | `CheckGroup` | 套餐（多个检查项组成） |
| `checkgroup_item` | `CheckGroupItem` | 套餐-检查项关联 |
| `registration` | `Registration` | 患者对套餐的预约（0已约/1已完成/2已取消） |
| `check_result` | `CheckResult` | 一次预约里各项的结果值（报告数据源） |
| `fee` | `Fee` | 收费(队长新增)：一条预约一次收费 |

**业务链路**：管理员先维护检查项/套餐 → 患者预约（写 `registration`）→ 医生对预约逐项录结果（写 `check_result`）→ 预约标记完成 → ReportModule 汇总出报告 → 患者查看。

**各表字段**（SQL/界面照这个写，建表以根目录 `schema.sql` 为准）：
- **users**：`tel`(主键,账号) `pwd` `name` `idcard` `birthday` `sex` `role`
- **checkitem**：`cid`(主键) `bh`(编号) `cname` `dw`(单位) `ckfw`(参考范围) `status`(0正常|1下架)
- **checkgroup**：`gid`(主键) `gname` `bh` `remark` `status`(0正常|1停用)
- **checkgroup_item**：`id`(自增) `gid`→checkgroup `cid`→checkitem
- **registration**：`id`(自增) `tel`→users `gid`→checkgroup `reg_time` `status`
- **check_result**：`id`(自增) `reg_id`→registration `tel`→users `cid`→checkitem `result_value` `doctor_tel` `check_time`
- **fee**：`id`(自增) `reg_id`→registration `tel`→users `gid`→checkgroup `amount`(金额,元) `status`(0待缴|1已缴|2已退款) `pay_time` `operator`(收费员账号) `remark`

---

## 4. 各模块职责

| 模块 | 状态 | 职责 |
|:---|:---|:---|
| **Common** | ✅ 完成 | 6 实体、6 DAO、`JdbcUtil`。需要读写先查 §5，别重复造轮子。 |
| **MainModule** | ✅ 登录入口已通 | 唯一登录窗（手机号+密码，背景图在 `MainModule/src/main/resources/login_background.png`，源图在 `Sources/login-bg-source.png`）。登录成功按 role 打开对应主窗：role2→`AdminFrame`；role0/1 目前提示"待接入"，等患者/医生主窗就绪后在这改。**各角色模块不再自己做登录。** |
| **AdminModule** | 🟡 基本完成 | **管理员(role2)**：维护检查项/套餐及关联、用户管理、预约状态（`AdminFrame` 五个页签）。**医生(role1)**：给预约录 `check_result`（"结果录入"页签已实现，医生入口待队长安排接入）。 |
| **FeeModule** | ✅ 已完成(队长) | 收费台：对"已预约且尚未收费"的预约收费入账（写 `fee`，状态 1 已缴）、查看收费记录、退款（置 2 已退款）。独立入口 `com.ncu.fee.FeeMain`（收费员=data.sql 管理员 13800138000）。收费员本质是管理员，将来要并入系统入口时由队长接入（把 `ChargePanel`/`FeeFrame` 挂到管理员入口即可）。 |
| **PatientModule** | ⬜ 待开发 | 患者：浏览套餐、预约/取消、看自己结果/报告、改资料。**要给 MainModule 提供一个患者主窗类**。 |
| **ReportModule** | ⬜ 待开发 | 按预约汇总结果出报告（打印/导出）。 |
| **MainModule 的 role 分发** | 需要补 | `MainModule/view/LoginFrame.java` 的 `onLogin()` switch：把 case 0/1 接上 Patient 主窗 / 医生（AdminModule 结果录入）入口。 |

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

- 建库建表：执行根目录 `schema.sql`。
- **数据库已建过、只想加 fee 表**：别整份重跑 schema.sql（表已存在会报错），只执行脚本末尾第 7 段 `fee` 的建表语句即可；fee 演示数据已并入 `data.sql`。
- **演示数据**：执行根目录 `data.sql`（含 3 个登录账号、10 检查项、3 套餐、2 预约、1 次结果、1 条收费）。**导入 data.sql/schema.sql 时必须用 UTF-8 方式**（如 IDEA 控制台跑、或 `mysql --default-character-set=utf8mb4 < data.sql`），否则中文字段会丢成空串，导致姓名/套餐名/检查项名显示空白。登录账号：
  - 管理员 `13800138000 / 123456`；医生 `13900139000 / 123456`；患者 `13700137000 / 123456`
- 连库配置在 **Common** `src/main/resources`：
  - `db.properties` = 模板，**只放占位（root/root），禁止把真实密码提交进去**；
  - `db-local.properties` = 你自己机器的真实密码，已被 .gitignore 忽略，`JdbcUtil` 优先读它。

**队长新增了 `fee`（收费）表 → 各模块同学看这里：**
- **库**：本地库若已建过，只补执行 `schema.sql` 第 7 段的 `fee` 建表语句即可（**别整份重跑** schema.sql，表已存在会报错）。fee 是全新表，不动任何旧表结构。
- **AdminModule（管理员/医生）**：可选。想让管理员顺手能收费：给 AdminModule 加 `FeeModule` 依赖，在 `AdminFrame` 加一个"收费管理"页签 `new com.ncu.fee.view.ChargePanel(登录管理员tel)`；不想要就先不动，队长已给独立入口 `FeeMain`。
- **PatientModule（患者端）**：预约/看结果**不用改**。将来想让患者看到"我的缴费记录"，**先跟队长说**，队长把 `FeeDao` 基础方法提升进 Common 你再调。
- **ReportModule（报告）**：同上，报告想带费用汇总就喊队长提升 Common FeeDao；报告本身可直接读 Common 的 `Fee` 实体。
- **通用红线**：任何模块想读写 fee，先找队长，统一走 Common 的实体/DAO；**不要各自另写一份 fee DAO**，避免两套逻辑打架。

---

## 7. 登录与角色主窗怎么接（重要）

**登录只发生在 MainModule**：`Main.java` 启动 → `LoginFrame`（背景图窗）→ `MainController.login` → 复用 `Common.UserDao.findByTel` 校验 → 成功拿到当前用户（`tel/name/role`）→ `onLogin()` 里 switch role 打开对应主窗。

**你要做的事**（按你负责的模块）：
- **写患者/医生/报告主窗的同学**：不需要再做登录框。只要提供"主窗类"，由队长把它的构造和 `role` 接进 `MainModule/view/LoginFrame.java` 的 switch：
  ```java
  case 0:  // 患者
      dispose();
      new com.ncu.patient.view.PatientHomeFrame(u.getTel(), u.getName()).setVisible(true);
      break;
  case 1:  // 医生 → AdminModule 的结果录入
      dispose();
      new com.ncu.admin.view.DoctorFrame(u.getName()).setVisible(true); // 例：医生主窗，含结果录入
      break;
  ```
- 主窗类构造器建议接收 `tel` + `name`（当前登录人），不要再传整个 DAO。
- 角色编号统一用整数并加注释：`0患者 / 1医生 / 2管理员`。

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
