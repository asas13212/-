package com.ncu.fee.view;

import com.ncu.fee.controller.FeeController;
import com.ncu.fee.model.FeeRegVO;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 * 收费登记面板：列出还没有收费记录的"已预约"，选中后确认收费（生成一条已缴记录）
 */
public class ChargePanel extends JPanel
{
    private final FeeController controller = new FeeController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String operatorTel; // 当前收费员(管理员)账号
    private final String[] columns = {"预约id", "患者账号", "患者姓名", "套餐id", "套餐名称", "预约时间"};

    public ChargePanel(String operatorTel)
    {
        this.operatorTel = operatorTel;
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("刷新");
        refresh.addActionListener(e -> refresh());
        btnPanel.add(refresh);
        JButton charge = new JButton("确认收费");
        charge.addActionListener(e -> charge());
        btnPanel.add(charge);
        add(btnPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (FeeRegVO v : controller.listUnchargedRegs())
        {
            tableModel.addRow(new Object[]{v.getId(), v.getTel(), v.getPatientName(),
                    v.getGid(), v.getGroupName(), v.getRegTime() == null ? "" : sdf.format(v.getRegTime())});
        }
    }

    private void charge()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先在表格里选中一行要收费的预约");
            return;
        }
        int regId = (Integer) tableModel.getValueAt(row, 0);
        String tel = (String) tableModel.getValueAt(row, 1);
        String gid = (String) tableModel.getValueAt(row, 3);
        String groupName = (String) tableModel.getValueAt(row, 4);

        String input = JOptionPane.showInputDialog(this,
                "对预约 #" + regId + "（" + groupName + "）收费\n请输入金额(元)：", "确认收费", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty())
        {
            return;
        }
        BigDecimal amount;
        try
        {
            amount = new BigDecimal(input.trim());
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "请输入正确的金额，如 388.00");
            return;
        }
        String err = controller.charge(regId, tel, gid, amount, operatorTel);
        if (err == null)
        {
            JOptionPane.showMessageDialog(this, "收费成功");
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, err);
            refresh(); // 可能因已被收费，刷新把该行移出待收费列表
        }
    }
}
