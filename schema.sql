-- ============================================
-- 健康管理系统 数据库建表脚本（整库重建版）
-- 数据库名：healthysystem
-- 用法：在 MySQL 里整体执行本文件即可（会先 DROP 旧库再重建空库）
-- 注意：表之间有外键依赖，已按依赖顺序排好，勿打乱顺序
-- 执行完本脚本后，再执行 data.sql 灌入演示数据
-- ============================================

DROP DATABASE IF EXISTS healthysystem;
CREATE DATABASE healthysystem DEFAULT CHARACTER SET utf8mb4;
USE healthysystem;

-- 1. 用户表（患者/医生，用 role 区分：0患者 | 1医生）
CREATE TABLE users (
    tel      VARCHAR(11) PRIMARY KEY COMMENT '账号(手机号)',
    pwd      VARCHAR(20)  COMMENT '密码',
    name     VARCHAR(20)  COMMENT '姓名',
    idcard   VARCHAR(18)  COMMENT '身份证',
    birthday DATE         COMMENT '出生日期',
    sex      VARCHAR(6)   COMMENT '性别',
    role     INT          COMMENT '角色:0患者|1医生'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 检查项表
CREATE TABLE checkitem (
    cid    VARCHAR(50) PRIMARY KEY COMMENT '主键id',
    bh     VARCHAR(20) COMMENT '编号',
    cname  VARCHAR(20) COMMENT '检查名称',
    dw     VARCHAR(20) COMMENT '单位',
    ckfw   VARCHAR(40) COMMENT '参考范围',
    status INT         COMMENT '状态:0正常|1下架'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 检查组表（体检套餐）
CREATE TABLE checkgroup (
    gid    VARCHAR(50)   PRIMARY KEY COMMENT '主键id',
    gname  VARCHAR(50)   COMMENT '套餐名称',
    bh     VARCHAR(20)   COMMENT '编号',
    remark VARCHAR(200)  COMMENT '备注',
    price  DECIMAL(10,2) COMMENT '套餐价(元);收费按此入账',
    status INT           COMMENT '状态:0正常|1停用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 检查组-检查项 关联表（哪个套餐包含哪些检查项）
CREATE TABLE checkgroup_item (
    id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    gid VARCHAR(50) COMMENT '套餐id',
    cid VARCHAR(50) COMMENT '检查项id',
    FOREIGN KEY (gid) REFERENCES checkgroup(gid),
    FOREIGN KEY (cid) REFERENCES checkitem(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 预约表（患者预约一个套餐，或单项预约一个具体检查项）
CREATE TABLE registration (
    id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    tel      VARCHAR(11) COMMENT '患者账号',
    gid      VARCHAR(50) COMMENT '套餐id(单项预约时为空)',
    cid      VARCHAR(50) COMMENT '检查项id(单项预约时用)',
    reg_time DATETIME    COMMENT '预约时间',
    location VARCHAR(50) COMMENT '体检地点',
    status   INT         COMMENT '状态:0已预约|1已完成|2已取消',
    FOREIGN KEY (tel) REFERENCES users(tel),
    FOREIGN KEY (gid) REFERENCES checkgroup(gid),
    FOREIGN KEY (cid) REFERENCES checkitem(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 检查结果表（医生对预约逐项录入结果，也是报告的数据源）
CREATE TABLE check_result (
    id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    reg_id       INT         COMMENT '预约id',
    tel          VARCHAR(11) COMMENT '患者账号',
    cid          VARCHAR(50) COMMENT '检查项id',
    result_value VARCHAR(50) COMMENT '结果值',
    doctor_tel   VARCHAR(11) COMMENT '录入医生账号',
    check_time   DATETIME    COMMENT '检查时间',
    FOREIGN KEY (reg_id) REFERENCES registration(id),
    FOREIGN KEY (tel) REFERENCES users(tel),
    FOREIGN KEY (cid) REFERENCES checkitem(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 收费表（一条预约对应一条收费记录；收费按套餐 price 入账）
CREATE TABLE fee (
    id       INT            AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    reg_id   INT            COMMENT '预约id',
    tel      VARCHAR(11)    COMMENT '患者账号',
    gid      VARCHAR(50)    COMMENT '套餐id',
    amount   DECIMAL(10,2)  COMMENT '收费金额(元)',
    status   INT            COMMENT '状态:0待缴|1已缴|2已退款',
    pay_time DATETIME       COMMENT '缴费时间',
    operator VARCHAR(11)    COMMENT '收费员(医生)账号',
    remark   VARCHAR(200)   COMMENT '备注',
    FOREIGN KEY (reg_id) REFERENCES registration(id),
    FOREIGN KEY (tel) REFERENCES users(tel),
    FOREIGN KEY (gid) REFERENCES checkgroup(gid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
