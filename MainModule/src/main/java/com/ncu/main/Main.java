package com.ncu.main;

import com.ncu.main.view.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * 系统主入口（Swing）
 */
public class Main
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
