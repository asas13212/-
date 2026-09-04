package com.ncu.patient.view;

import com.ncu.common.ui.Ui;
import com.ncu.patient.controller.PatientController;
import com.ncu.patient.model.RegistrationVO;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 我的预约面板：查看自己的预约（含时间/地点）、取消进行中的预约
 */
public class MyRegistrationPanel extends JPanel
{
    private final PatientController controller = new PatientController();
    private final String tel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"预约id", "套餐名", "预约时间", "体检地点", "状态"};
    private List<RegistrationVO> regs;
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public MyRegistrationPanel(String tel)
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
        table.setShowVerticalLines(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        add(Ui.header("我的预约",
                Ui.actionDanger("取消预约", e -> cancel()),
                Ui.action("刷新", e -> refresh())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        regs = controller.listMyRegistrations(tel);
        for (RegistrationVO r : regs)
        {
            tableModel.addRow(new Object[]{r.getId(), r.getGroupName(),
                    r.getRegTime() == null ? "" : TIME_FMT.format(r.getRegTime()),
                    r.getLocation(), statusText(r.getStatus())});
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
