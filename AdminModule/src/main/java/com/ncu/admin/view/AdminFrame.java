package com.ncu.admin.view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * 管理员主窗口：用选项卡组织各管理功能
 */
public class AdminFrame extends JFrame
{
    public AdminFrame(String adminName)
    {
        setTitle("健康体检管理系统 - 管理员(" + adminName + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("检查项管理", new CheckItemPanel());
        tabs.addTab("套餐管理", new CheckGroupPanel());
        tabs.addTab("用户管理", new UserPanel());
        tabs.addTab("预约管理", new RegistrationPanel());
        tabs.addTab("结果录入", new CheckResultPanel());
        add(tabs);

        setSize(840, 600);
        setLocationRelativeTo(null);
    }
}
