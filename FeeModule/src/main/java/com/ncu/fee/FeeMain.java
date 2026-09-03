package com.ncu.fee;

import com.ncu.fee.view.FeeFrame;

import javax.swing.SwingUtilities;

/**
 * 收费模块独立入口（可单独运行演示）。
 * 演示用 data.sql 里的管理员账号 13800138000 作为收费员。
 * 单独运行前请先执行根目录 schema.sql(含新增 fee 表) 与 data.sql。
 */
public class FeeMain
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new FeeFrame("13800138000").setVisible(true));
    }
}
