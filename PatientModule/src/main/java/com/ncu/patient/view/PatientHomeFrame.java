package com.ncu.patient.view;

import com.ncu.common.ui.HomeBaseFrame;

/**
 * 患者主窗口：左侧导航（套餐浏览 / 我的预约 / 我的结果 / 个人资料）+ 右侧内容区
 */
public class PatientHomeFrame extends HomeBaseFrame
{
    public PatientHomeFrame(String tel, String name)
    {
        this(tel, name, null);
    }

    public PatientHomeFrame(String tel, String name, Runnable onLogout)
    {
        super("健康体检管理系统 - 患者(" + name + ")", name, "患者", onLogout);
        addNav("套餐浏览", new PackagePanel(tel));
        addNav("我的预约", new MyRegistrationPanel(tel));
        addNav("我的结果", new MyResultPanel(tel));
        addNav("个人资料", new ProfilePanel(tel));
    }
}
