package com.ncu.fee;

import com.ncu.fee.view.FeeFrame;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * 收费模块独立入口（可单独运行演示）。
 * 演示用 data.sql 里的医生账号 13800138000 作为收费员。
 * 单独运行前请先执行根目录 schema.sql(含新增 fee 表) 与 data.sql。
 */
public class FeeMain
{
    public static void main(String[] args)
    {
        FeeFrame frame = new FeeFrame("13800138000");
        // 独立演示：关窗即退出（FeeFrame 默认 DISPOSE，供将来嵌入时避免误杀整个系统）
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
