package com.ncu.fee.view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * 收费管理主窗口：收费登记 + 收费记录 两个页签。
 *
 * <p>收费操作由管理员/收费台完成，构造器需传入当前收费员的账号(tel)。
 * 将来若要把收费并入系统入口，可在 AdminFrame 或 MainModule 里放一个
 * "收费管理"入口，用登录用户的 tel 构造本窗口/面板即可。
 */
public class FeeFrame extends JFrame
{
    public FeeFrame(String operatorTel)
    {
        setTitle("健康体检管理系统 - 收费管理(收费员:" + operatorTel + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("收费登记", new ChargePanel(operatorTel));
        tabs.addTab("收费记录", new FeeListPanel());
        add(tabs);

        setSize(820, 560);
        setLocationRelativeTo(null);
    }
}
