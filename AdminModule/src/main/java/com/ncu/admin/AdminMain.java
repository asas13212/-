package com.ncu.admin;

import com.ncu.admin.view.AdminFrame;

import javax.swing.SwingUtilities;

/**
 * 管理员模块入口（可独立运行调试；正式入口由 MainModule 登录后打开 AdminFrame）
 */
public class AdminMain
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new AdminFrame("管理员").setVisible(true));
    }
}
