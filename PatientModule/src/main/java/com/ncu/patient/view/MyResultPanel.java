package com.ncu.patient.view;

import com.ncu.patient.controller.PatientController;
import com.ncu.patient.model.RegistrationVO;
import com.ncu.patient.model.ResultVO;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * 我的结果面板：选择一次预约查看各检查项的结果
 */
public class MyResultPanel extends JPanel
{
    private final PatientController controller = new PatientController();
    private final String tel;
    private final JComboBox<String> regBox = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"检查项", "结果值", "单位", "参考范围", "检查时间"};
    private List<RegistrationVO> regs;

    public MyResultPanel(String tel)
    {
        this.tel = tel;
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("选择预约:"));
        top.add(regBox);
        JButton query = new JButton("查看结果");
        query.addActionListener(e -> query());
        top.add(query);
        JButton refresh = new JButton("刷新");
        refresh.addActionListener(e -> refreshRegs());
        top.add(refresh);
        add(top, BorderLayout.NORTH);

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

        refreshRegs();
    }

    private void refreshRegs()
    {
        regBox.removeAllItems();
        regs = controller.listMyRegistrations(tel);
        for (RegistrationVO r : regs)
        {
            regBox.addItem("#" + r.getId() + " " + r.getGroupName() + " (" + statusText(r.getStatus()) + ")");
        }
    }

    private void query()
    {
        int idx = regBox.getSelectedIndex();
        if (idx < 0 || regs == null || regs.isEmpty())
        {
            return;
        }
        RegistrationVO r = regs.get(idx);
        tableModel.setRowCount(0);
        for (ResultVO v : controller.listResults(r.getId()))
        {
            tableModel.addRow(new Object[]{v.getCname(), v.getResultValue(), v.getDw(), v.getCkfw(), v.getCheckTime()});
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
