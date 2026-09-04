package com.ncu.report;

import com.ncu.report.view.ReportFrame;

import javax.swing.SwingUtilities;

/**
 * 报告模块入口（可独立运行调试：按演示患者直接打开报告窗口）
 */
public class ReportMain
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new ReportFrame("13700137000").setVisible(true));
    }
}
