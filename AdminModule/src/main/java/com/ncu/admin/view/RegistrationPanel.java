package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.admin.model.RegistrationVO;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

/**
 * 预约管理面板
 */
public class RegistrationPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"预约id", "患者账号", "患者姓名", "套餐id", "套餐名", "预约时间", "状态"};

    public RegistrationPanel()
    {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        addButton(btnPanel, "刷新", e -> refresh());
        addButton(btnPanel, "修改状态", e -> changeStatus());
        add(btnPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void addButton(JPanel panel, String text, ActionListener listener)
    {
        JButton b = new JButton(text);
        b.addActionListener(listener);
        panel.add(b);
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        for (RegistrationVO r : controller.listRegistrations())
        {
            tableModel.addRow(new Object[]{r.getId(), r.getTel(), r.getPatientName(),
                    r.getGid(), r.getGroupName(), r.getRegTime(), statusText(r.getStatus())});
        }
    }

    private void changeStatus()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先在表格里选中一行");
            return;
        }
        int id = (Integer) tableModel.getValueAt(row, 0);
        String s = DialogUtil.promptSingle(this, "修改状态", "新状态(0已预约|1已完成|2已取消)", null);
        if (s == null || s.isEmpty()) return;
        try
        {
            int status = Integer.parseInt(s);
            if (status < 0 || status > 2)
            {
                JOptionPane.showMessageDialog(this, "状态只能是 0/1/2");
                return;
            }
            controller.updateRegStatus(id, status);
            refresh();
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "请输入数字 0/1/2");
        }
    }

    private String statusText(int status)
    {
        switch (status)
        {
            case 0: return "已预约";
            case 1: return "已完成";
            case 2: return "已取消";
            default: return "未知";
        }
    }
}
