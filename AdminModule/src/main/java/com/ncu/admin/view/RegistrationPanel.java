package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.admin.model.RegistrationVO;
import com.ncu.common.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/**
 * 预约管理面板
 */
public class RegistrationPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"预约id", "患者账号", "患者姓名", "类型", "项目id", "项目名称", "预约时间", "状态"};

    public RegistrationPanel()
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
        add(Ui.header("预约管理",
                Ui.actionPrimary("修改状态", e -> changeStatus()),
                Ui.action("刷新", e -> refresh())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        for (RegistrationVO r : controller.listRegistrations())
        {
            tableModel.addRow(new Object[]{r.getId(), r.getTel(), r.getPatientName(),
                    r.getGid() != null ? "套餐" : "单项",
                    r.getGid() != null ? r.getGid() : r.getCid(),
                    r.getGroupName(), r.getRegTime(), statusText(r.getStatus())});
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
