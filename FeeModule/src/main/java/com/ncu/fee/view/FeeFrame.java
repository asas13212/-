package com.ncu.fee.view;

import com.ncu.common.ui.Ui;
import com.ncu.common.ui.UiTheme;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * 收费管理主窗口：收费登记 + 收费记录 两个页签（独立入口，仅供调试；正式入口已并入后台页签）。
 *
 * <p>收费操作由医生（后台收费台）完成，构造器需传入当前收费员的账号(tel)。
 * 关窗采用 DISPOSE_ON_CLOSE：不误杀整个进程。
 */
public class FeeFrame extends JFrame
{
    public FeeFrame(String operatorTel)
    {
        UiTheme.install();
        setTitle("健康体检管理系统 - 收费管理(收费员:" + operatorTel + ")");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Ui.font(13));
        tabs.setFocusable(false);
        tabs.addTab("收费登记", new ChargePanel(operatorTel));
        tabs.addTab("收费记录", new FeeListPanel());
        add(tabs);

        setSize(860, 560);
        setLocationRelativeTo(null);
    }
}
