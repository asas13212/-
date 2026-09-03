package com.ncu.admin.view;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridLayout;

/**
 * 弹窗工具：用一个带多个输入框的对话框收集字段值
 */
public class DialogUtil
{
    /** 多字段输入；取消返回 null */
    public static String[] prompt(Component parent, String title, String[] labels, String[] initial)
    {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 6, 6));
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++)
        {
            panel.add(new JLabel(labels[i]));
            fields[i] = new JTextField(initial == null ? "" : initial[i], 15);
            panel.add(fields[i]);
        }
        int r = JOptionPane.showConfirmDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION)
        {
            return null;
        }
        String[] vals = new String[labels.length];
        for (int i = 0; i < labels.length; i++)
        {
            vals[i] = fields[i].getText().trim();
        }
        return vals;
    }

    /** 单字段输入；取消返回 null */
    public static String promptSingle(Component parent, String title, String label, String initial)
    {
        String[] vals = prompt(parent, title, new String[]{label},
                initial == null ? null : new String[]{initial});
        return vals == null ? null : vals[0];
    }
}
