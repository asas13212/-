# 健康管理系统 · 开发总览（dev.md）

> 给全队看的工程说明：**先读这一份**。
> 目标：让每个人知道「项目在做什么」「我负责的模块要做什么」「核心概念（role）是什么意思」「数据库有哪几张表、各自字段」「登录系统怎么接入」。
> 代码细节请看 Common / AccessModule 源码，格式参照其中 DAO/实体写法。

---

## 1. 项目是什么

一个 **体检（健康管理）系统**：患者在线选套餐预约体检 → 医生录入各检查项结果 → 系统汇总生成体检报告，管理员维护基础数据。

系统有 **三类角色**，同一个 `users` 表用 `role` 字段区分：

| role 值 | 角色 | 用哪个模块 |
|:---:|:---|:---|
| 0 | 患者 patient | PatientModule |
| 1 | 医生 doctor | AdminModule（与管理员一起，后台） |
| 2 | 管理员 admin | AdminModule |

登录账号 = **手机号**（`users.tel`，主键）。

---

## 2. 技术栈与工程结构

- JDK 17 + Maven **多模块** + MySQL 8 + JDBC（原生，不用框架）+ Swing 界面。
- 模块关系：**Common 谁都能依赖；要复用登录框的角色模块还依赖 AccessModule**（AccessModule 不反向依赖它们，所以不会成环）；各角色模块之间不互相依赖；每个模块都能**独立运行**自己的入口类。

```
healthysystem（父 pom，聚合下面 6 个模块）
├─ Common         公共基础层（实体 / DAO / 工具）——已完成，所有人依赖它
├─ AccessModule   统一登录入口（Swing）——已完成（队长），提供登录框给各模块复用
├─ PatientModule  患者端    ——待实现
├─ AdminModule    后台（管理员+医生）——待实现
├─ ReportModule   报告模块  ——待实现
└─ MainModule     占位（AppMain 现为 HelloWorld；是否做“一键启动所有模块”的总入口，由团队自行决定）
```

给队友的落点：你的模块 pom 要依赖 **Common**（必须）+ **AccessModule**（要做登录就加），写法抄 `AccessModule/pom.xml`。

---

## 3. 核心概念与业务对象（看懂数据库）

表名全部小写，字符集 utf8mb4，Common 实体与表一一对应：

| 表 | Common 实体 | 一句话含义 |
|:---|:---|:---|
| `users` | `User` | 用户（患者/医生/管理员一张表，`role` 区分） |
| `checkitem` | `CheckItem` | 单个**检查项**（如“空腹血糖”） |
| `checkgroup` | `CheckGroup` | **检查组 = 体检套餐**（多个检查项组成一套） |
| `checkgroup_item` | `CheckGroupItem` | 套餐-检查项 关联表 |
| `registration` | `Registration` | 患者对某套餐的**预约** |
| `check_result` | `CheckResult` | 某次预约里每个检查项的**结果值**（报告数据源） |

**一句话业务链路**：患者登录 → 选套餐预约（写 `registration`）→ 医生给预约内各检查项录结果（写 `check_result`）→ 预约完成 → ReportModule 汇总出报告 → 患者可查。

**每张表的字段（SQL 和界面都按这个写）**：

- **users**：`tel` 手机号(主键,账号) / `pwd` 密码 / `name` 姓名 / `idcard` 身份证 / `birthday` 出生日期 / `sex` 性别 / `role` 0患者|1医生|2管理员
- **checkitem**：`cid` 主键 / `bh` 编号 / `cname` 检查名称 / `dw` 单位 / `ckfw` 参考范围 / `status` 0正常|1下架
- **checkgroup**：`gid` 主键 / `gname` 套餐名称 / `bh` 编号 / `remark` 备注 / `status` 0正常|1停用
- **checkgroup_item**：`id` 自增主键 / `gid`→checkgroup / `cid`→checkitem
- **registration**：`id` 自增主键 / `tel`→users / `gid`→checkgroup / `reg_time` 预约时间 / `status` 0已预约|1已完成|2已取消
- **check_result**：`id` 自增主键 / `reg_id`→registration / `tel`→users(患者) / `cid`→checkitem / `result_value` 结果值 / `doctor_tel` 录入医生 / `check_time` 检查时间

（建表 SQL 以根目录 `schema.sql` 为准，这里只是速览。）

---

## 4. 各模块职责

| 模块 | 状态 | 职责 |
|:---|:---|:---|
| **Common** | ✅ 已完成 | 6 个实体、6 个 DAO、`JdbcUtil`、`EncryptUtil`。需要的数据读写先查 §5，**别重复造轮子**。 |
| **AccessModule** | ✅ 已完成 | **统一登录入口**（Swing）。手机号+密码校验角色后交给回调；自身带 `AccessApp` 演示。其他模块把它当登录闸门用，用法见 §7。 |
| **PatientModule** | ⬜ 待分 | 角色 0 **患者**：浏览体检套餐、在线预约/取消、看自己的结果/历史报告、改资料/密码。 |
| **AdminModule** | ⬜ 待分 | 角色 2 **管理员**：维护检查项/套餐及关联、用户/预约管理。角色 1 **医生**：给预约患者按检查项录 `check_result`。 |
| **ReportModule** | ⬜ 待分 | 按一次预约汇总 `check_result` 生成体检报告（导出/打印预览）。 |
| **MainModule** | ⬜ 待定 | 现仅占位。若做总入口可“先登录再按 role 打开模块”。非必须。 |

> 具体菜单/功能清单以队长分工时的说明为准，本表给的是模块定位。

**登录界面素材（AccessModule 在用）**：登录窗整窗背景使用 `AccessModule/src/main/resources/login_background.png`（设计源图在仓库根 `Sources/login-bg-source.png`，1920×1080）。界面上**只放「手机号」和「密码」两个输入框**（+登录/重置按钮），叠在背景图中央的半透明卡片上。要换背景：把新图放进 `Sources/`，再同名覆盖 `AccessModule/src/main/resources/login_background.png` 即可（建议 16:9，窗口按宽 920 等比缩放）。

---

## 5. Common 能直接给我什么（API 速查）

DAO 是直连 JDBC 的简单类，`new XxxDao()` 即用：

- **UserDao**：`insert`(注册) / `findByTel`(登录用) / `findAll` / `findByRole(int)` / `update` / `delete(tel)`
- **CheckItemDao**：`insert` / `findAll` / `findPage(page,size)` / `count` / `findByBh` / `update` / `delete(cid)`
- **CheckGroupDao**：`insert` / `findAll` / `findByName`(模糊) / `update` / `delete(gid)`
- **CheckGroupItemDao**：`insert` / `findByGid` / `deleteByGid` / `delete(id)`
- **RegistrationDao**：`insert` / `findByTel` / `findAll` / `update` / `delete(id)`
- **CheckResultDao**：`insert` / `findByTel` / `findByRegId` / `update` / `delete(id)`

实体：`User / CheckItem / CheckGroup / CheckGroupItem / Registration / CheckResult`（`com.ncu.common.model`），字段与表列一致，全部 getter/setter。

工具（`com.ncu.common.util`）：`JdbcUtil.getConnection()` 拿连接、`JdbcUtil.close(conn,ps,rs)` 关资源（照抄 DAO 写法）；`EncryptUtil` 目前未用于密码（见 §8）。

---

## 6. 建库与配置

- 建库建表：MySQL 里**整体执行根目录 `schema.sql`**（`healthysystem` 库 + 上面 6 张表，含外键，已排好序）。
- 连库配置在 **Common** 的 `src/main/resources`：`db.properties`（模板）可提交；`db-local.properties`（你自己机器真实密码）**已被 .gitignore 忽略、禁止提交**，`JdbcUtil` 优先读它。
- 测试账号自己插，例如：
  ```sql
  INSERT INTO users(tel,pwd,name,role) VALUES('13800138000','123456','测试管理员',2);
  ```

---

## 7. 登录系统怎么做（队友必读）

**不用自己写数据库校验** —— 登录框（AccessModule）已经做好：输错手机号/密码它会自己弹提示，只有验证成功才会回调你。

做法三步：
1. 你的模块 pom 加上对 **AccessModule**（连同 Common）的依赖，抄 `AccessModule/pom.xml`。
2. 写一个入口类 `main`，弹登录框；成功回调里用 `user.getRole()` 判断角色，不是本模块的账号就提示，是本模块就打开你的主界面。
3. 你的主界面构造器接收登录好的 `User`（要用姓名/手机号直接 `user.getName()`、`user.getTel()`）。

**模板（以 PatientModule 为例，包名换 `com.ncu.admin` / `com.ncu.report` 同理）：**

```java
package com.ncu.patient;

import com.ncu.access.model.Role;      // 角色常量
import com.ncu.access.view.LoginFrame; // 复用统一登录框
import com.ncu.common.model.User;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class PatientApp
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new LoginFrame(PatientApp::afterLogin).setVisible(true));
    }

    /** 登录成功回调：校验角色后进入本模块主界面 */
    private static void afterLogin(User user)
    {
        if (user.getRole() != Role.PATIENT)   // 管理员/医生不能进患者端
        {
            JOptionPane.showMessageDialog(null, "请使用患者账号登录患者端");
            return;                           // 可在此再次弹出登录框换账号
        }
        new PatientHomeFrame(user).setVisible(true);  // 换成你模块自己的主窗口类
    }
}
```

要点：
- 角色常量统一用 `com.ncu.access.model.Role`：`Role.PATIENT=0`、`Role.DOCTOR=1`、`Role.ADMIN=2`；显示用 `Role.name(role)` 得到“患者/医生/管理员”。
- 登录回调一定在 **Swing 事件线程**上，直接 `new 主界面(...).setVisible(true)` 即可，不需要再套 `invokeLater`。
- AdminModule 是“管理员+医生”两个角色共用：回调里 `role==DOCTOR || role==ADMIN` 都放行，进去后再按 role 展示各自的菜单/权限。

---

## 8. 构建 / 运行 / 注意点

- 环境：JDK 17、Maven、本机 MySQL 已执行 `schema.sql`。
- 编译（仓库根目录，连带编 Common）：`mvn -pl AccessModule -am compile`；或 IDEA 打开根 `pom.xml`。
- 运行：IDEA 里 Run 各模块入口类的 `main`。
- **`users.pwd` 是 VARCHAR(20) 明文存储**：`EncryptUtil` 的 AES 密文 Base64 ≥24 字符放不进列，登录目前明文比对（`LoginService` 已按此实现）。以后要加密得先把列改大再改 DAO/Service。
- 包名规范 `com.ncu.<模块小写>`；新增实体/DAO 放 Common（先找队长确认）；异常 `printStackTrace()` 打印即可、别抛崩界面；空值先判空再给友好提示。
- UI 文案中文；Git 提交信息中文、按功能一条；`db-local.properties`/`target/`/`.idea/` 不提交；一人一个分支，跑通再合并。

---

## 9. 交接前自查清单

- [ ] 我的模块 pom 依赖了 Common（+要做登录就加 AccessModule）？
- [ ] 按 §7 模板写了入口 main：先弹登录、按 `Role` 判断、进自己主界面？
- [ ] 我的模块能独立运行一个入口 `main`？
- [ ] 本地跑通 + 推分支 + 群里喊队长 code review？
