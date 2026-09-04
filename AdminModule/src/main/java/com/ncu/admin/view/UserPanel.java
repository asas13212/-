package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.common.model.User;
import com.ncu.common.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/**
 * 用户管理面板
 */
public class UserPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"手机号", "姓名", "身份证", "性别", "出生日期", "角色"};

    public UserPanel()
    {
        setLayout(new BorderLayout());
        Ui.content(this);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        tableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        table = Ui.table(tableModel);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        add(Ui.header("用户管理",
                Ui.actionPrimary("刷新", e -> refresh()),
                Ui.action("只看患者", e -> refreshPatients()),
                Ui.actionDanger("删除用户", e -> deleteUser())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        show(controller.listUsers());
    }

    private void refreshPatients()
    {
        show(controller.listPatients());
    }

    private void show(java.util.List<User> list)
    {
        tableModel.setRowCount(0);
        for (User u : list)
        {
            tableModel.addRow(new Object[]{u.getTel(), u.getName(), u.getIdcard(),
                    u.getSex(), u.getBirthday(), roleText(u.getRole())});
        }
    }

    private void deleteUser()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先在表格里选中一行");
            return;
        }
        String tel = (String) tableModel.getValueAt(row, 0);
        int r = JOptionPane.showConfirmDialog(this, "确定删除用户 " + tel + " 吗？",
                "删除", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION)
        {
            controller.removeUser(tel);
            refresh();
        }
    }

    private String roleText(int role)
    {
        switch (role)
        {
            case 0: return "患者";
            case 1: return "医生";
            default: return "未知";
        }
    }
}
