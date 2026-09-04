package com.ncu.admin;

import com.ncu.admin.view.AdminFrame;

import javax.swing.SwingUtilities;

/**
 * 后台模块入口（role1 医生，含原管理员职责；可独立运行调试；正式入口由 MainModule 登录后打开 AdminFrame）
 */
public class AdminMain
{
    public static void main(String[] args)
    {
        // 独立调试入口（收费登记需收费员账号，用演示数据医生账号 13800138000）
        SwingUtilities.invokeLater(() -> new AdminFrame("13800138000", "王医生").setVisible(true));
    }
}
