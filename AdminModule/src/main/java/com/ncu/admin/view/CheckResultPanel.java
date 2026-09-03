package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.common.model.CheckResult;
import com.ncu.common.model.Registration;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Date;

/**
 * 检查结果录入面板
 */
public class CheckResultPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField regIdField = new JTextField(8);
    private final String[] columns = {"id", "预约id", "患者", "检查项", "结果值", "医生", "检查时间"};

    public CheckResultPanel()
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

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("预约id:"));
        top.add(regIdField);
        JButton query = new JButton("查询结果");
        query.addActionListener(e -> query());
        top.add(query);
        JButton add = new JButton("录入结果");
        add.addActionListener(e -> addResult());
        top.add(add);
        add(top, BorderLayout.NORTH);
    }

    private int currentRegId()
    {
        try
        {
            return Integer.parseInt(regIdField.getText().trim());
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "请输入正确的预约id");
            return -1;
        }
    }

    private void query()
    {
        int regId = currentRegId();
        if (regId < 0) return;
        tableModel.setRowCount(0);
        for (CheckResult r : controller.listResults(regId))
        {
            tableModel.addRow(new Object[]{r.getId(), r.getRegId(), r.getTel(), r.getCid(),
                    r.getResultValue(), r.getDoctorTel(), r.getCheckTime()});
        }
    }

    private void addResult()
    {
        int regId = currentRegId();
        if (regId < 0) return;
        Registration reg = controller.findRegById(regId);
        if (reg == null)
        {
            JOptionPane.showMessageDialog(this, "预约不存在");
            return;
        }
        String[] vals = DialogUtil.prompt(this, "录入检查结果",
                new String[]{"检查项id", "结果值", "医生账号"}, null);
        if (vals == null) return;
        CheckResult r = new CheckResult();
        r.setRegId(regId);
        r.setTel(reg.getTel());
        r.setCid(vals[0]);
        r.setResultValue(vals[1]);
        r.setDoctorTel(vals[2]);
        r.setCheckTime(new Date());
        if (controller.addResult(r))
        {
            query();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "录入失败");
        }
    }
}
