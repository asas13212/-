package com.ncu.patient.view;

import com.ncu.common.ui.Ui;
import com.ncu.patient.controller.PatientController;
import com.ncu.patient.model.RegistrationVO;
import com.ncu.patient.model.ResultVO;
import com.ncu.report.view.ReportFrame;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
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

        // 页头：左侧标题，右侧 选择预约 + 动作
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Ui.CONTENT_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        west.setOpaque(false);
        west.add(Ui.title("我的结果"));
        bar.add(west, BorderLayout.WEST);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);
        JLabel sel = new JLabel("选择预约:");
        sel.setFont(Ui.font(13));
        east.add(sel);
        regBox.setPreferredSize(new Dimension(280, 28));
        east.add(regBox);
        east.add(Ui.actionPrimary("查看结果", e -> query()));
        east.add(Ui.action("打印报告", e -> new ReportFrame(tel).setVisible(true)));
        east.add(Ui.action("刷新", e -> refreshRegs()));
        bar.add(east, BorderLayout.EAST);
        add(bar, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        add(Ui.card(sp), BorderLayout.CENTER);

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
