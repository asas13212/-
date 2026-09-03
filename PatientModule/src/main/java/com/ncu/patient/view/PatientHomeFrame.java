package com.ncu.patient.view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * 患者主窗口：用选项卡组织套餐浏览 / 我的预约 / 我的结果 / 个人资料
 */
public class PatientHomeFrame extends JFrame
{
    public PatientHomeFrame(String tel, String name)
    {
        setTitle("健康体检管理系统 - 患者(" + name + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("套餐浏览", new PackagePanel(tel));
        tabs.addTab("我的预约", new MyRegistrationPanel(tel));
        tabs.addTab("我的结果", new MyResultPanel(tel));
        tabs.addTab("个人资料", new ProfilePanel(tel));
        add(tabs);

        setSize(840, 600);
        setLocationRelativeTo(null);
    }
}
