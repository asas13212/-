package com.ncu.patient.view;

import com.ncu.common.model.CheckItem;
import com.ncu.common.ui.Ui;
import com.ncu.patient.controller.PatientController;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 单项预约对话框：勾选一个或多个检查项，共用同一时间地点预约
 */
public class SingleItemDialog extends JDialog
{
    private final PatientController controller = new PatientController();
    private final String tel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"选择", "编号", "检查项", "单位", "参考范围"};
    private List<CheckItem> items;

    public SingleItemDialog(Frame owner, String tel)
    {
        super(owner, "单项预约 - 选择检查项目", true);
        this.tel = tel;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return column == 0; // 只有"选择"列可勾选
            }

            @Override
            public Class<?> getColumnClass(int column)
            {
                return column == 0 ? Boolean.class : Object.class;
            }
        };
        table = Ui.table(tableModel);
        table.setShowVerticalLines(true);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Ui.CONTENT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Ui.CONTENT_BG);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        top.add(Ui.title("选择检查项目"), BorderLayout.NORTH);
        top.add(Ui.hint("勾选一个或多个检查项目（可多选）"), BorderLayout.SOUTH);
        content.add(top, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        content.add(Ui.card(sp), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        buttons.add(Ui.actionPrimary("预约", e -> book()));
        buttons.add(Ui.action("取消", e -> dispose()));
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(660, 500);
        setLocationRelativeTo(owner);

        load();
    }

    private void load()
    {
        tableModel.setRowCount(0);
        items = controller.listItems();
        for (CheckItem c : items)
        {
            tableModel.addRow(new Object[]{Boolean.FALSE, c.getBh(), c.getCname(), nvl(c.getDw()), nvl(c.getCkfw())});
        }
    }

    private void book()
    {
        List<CheckItem> selected = new ArrayList<>();
        for (int i = 0; i < items.size(); i++)
        {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (Boolean.TRUE.equals(checked))
            {
                selected.add(items.get(i));
            }
        }
        if (selected.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "请先勾选至少一个检查项目");
            return;
        }

        List<CheckItem> todo = new ArrayList<>();
        for (CheckItem c : selected)
        {
            if (!controller.hasActiveItemRegistration(tel, c.getCid()))
            {
                todo.add(c);
            }
        }
        if (todo.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "所选项目都已预约过（进行中），请先取消原预约。");
            return;
        }

        AppointmentDialog dlg = new AppointmentDialog((Frame) getOwner(), "共 " + todo.size() + " 个检查项目", "单项预约");
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;

        Date regTime = dlg.getRegTime();
        String location = dlg.getSelectedLocation();
        int ok = 0;
        for (CheckItem c : todo)
        {
            if (controller.registerSingle(tel, c.getCid(), regTime, location))
            {
                ok++;
            }
        }
        JOptionPane.showMessageDialog(this, "成功预约 " + ok + " 个检查项目");
        dispose();
    }

    private String nvl(String s)
    {
        return s == null ? "" : s;
    }
}
