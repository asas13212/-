package com.ncu.patient.view;

import com.ncu.patient.controller.PatientController;
import com.ncu.patient.model.RegistrationVO;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 我的预约面板：查看自己的预约、取消进行中的预约
 */
public class MyRegistrationPanel extends JPanel
{
    private final PatientController controller = new PatientController();
    private final String tel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"预约id", "套餐名", "预约时间", "状态"};
    private List<RegistrationVO> regs;

    public MyRegistrationPanel(String tel)
    {
        this.tel = tel;
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
        addButton(btnPanel, "取消预约", e -> cancel());
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
        regs = controller.listMyRegistrations(tel);
        for (RegistrationVO r : regs)
        {
            tableModel.addRow(new Object[]{r.getId(), r.getGroupName(), r.getRegTime(), statusText(r.getStatus())});
        }
    }

    private void cancel()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先选中一条预约");
            return;
        }
        RegistrationVO r = regs.get(row);
        if (r.getStatus() != 0)
        {
            JOptionPane.showMessageDialog(this, "只有「已预约」状态的预约可以取消");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确定取消该预约吗？", "确认取消",
                JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) return;
        if (controller.cancelRegistration(r.getId()))
        {
            JOptionPane.showMessageDialog(this, "已取消");
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "取消失败");
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
