package com.ncu.admin.view;

import com.ncu.common.ui.HomeBaseFrame;
import com.ncu.fee.view.ChargePanel;
import com.ncu.fee.view.FeeListPanel;

/**
 * 后台主窗口（role1 医生，含原管理员职责）：左侧导航 + 右侧内容区。
 * 构造需传当前登录人的 tel + name：tel 供收费登记记录收费员，name 显示在侧栏头部。
 */
public class AdminFrame extends HomeBaseFrame
{
    /** 独立调试入口（收费登记需收费员账号，用演示数据医生账号 13800138000） */
    public AdminFrame(String tel, String adminName)
    {
        this(tel, adminName, "医生", null);
    }

    public AdminFrame(String tel, String adminName, String roleText, Runnable onLogout)
    {
        super("健康体检管理系统 - " + roleText + "(" + adminName + ")", adminName, roleText, onLogout);
        addNav("检查项管理", new CheckItemPanel());
        addNav("套餐管理", new CheckGroupPanel());
        addNav("用户管理", new UserPanel());
        addNav("预约管理", new RegistrationPanel());
        addNav("预约日历", new CalendarPanel());
        addNav("结果录入", new CheckResultPanel(tel));
        addNav("收费登记", new ChargePanel(tel));
        addNav("收费记录", new FeeListPanel());
    }
}
