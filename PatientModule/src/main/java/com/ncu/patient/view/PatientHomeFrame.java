package com.ncu.patient.view;

import com.ncu.common.ui.HomeBaseFrame;

/**
 * 患者主窗口：左侧导航（体检预约 / 我的预约 / 体检报告 / 健康趋势 / 个人资料）+ 右侧内容区
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
        addNav("体检预约", new PackagePanel(tel));
        addNav("我的预约", new MyRegistrationPanel(tel));
        addNav("体检报告", new MyResultPanel(tel));
        addNav("健康趋势", new HealthTrendPanel(tel));
        addNav("个人资料", new ProfilePanel(tel));
    }
}
