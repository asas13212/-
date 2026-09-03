package com.ncu.patient.view;

import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.ui.Ui;
import com.ncu.patient.controller.PatientController;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/**
 * 套餐浏览面板：查看可预约套餐、明细，并可预约
 */
public class PackagePanel extends JPanel
{
    private final PatientController controller = new PatientController();
    private final String tel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"编号", "套餐名", "备注"};
    private List<CheckGroup> groups;

    public PackagePanel(String tel)
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

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        add(Ui.header("套餐浏览",
                Ui.actionPrimary("预约", e -> register()),
                Ui.action("查看明细", e -> showDetail()),
                Ui.action("刷新", e -> refresh())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        groups = controller.listPackages();
        for (CheckGroup g : groups)
        {
            tableModel.addRow(new Object[]{g.getBh(), g.getGname(), g.getRemark()});
        }
    }

    private CheckGroup selectedGroup()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先选中一个套餐");
            return null;
        }
        return groups.get(row);
    }

    private void showDetail()
    {
        CheckGroup g = selectedGroup();
        if (g == null) return;
        List<CheckItem> items = controller.listGroupItems(g.getGid());
        StringBuilder sb = new StringBuilder();
        sb.append("套餐：").append(g.getGname()).append("\n");
        sb.append("编号：").append(g.getBh() == null ? "" : g.getBh()).append("\n\n");
        if (items.isEmpty())
        {
            sb.append("该套餐暂无检查项。");
        }
        else
        {
            for (CheckItem c : items)
            {
                sb.append("· ").append(c.getCname());
                if (c.getDw() != null && !c.getDw().isEmpty())
                {
                    sb.append("（").append(c.getDw()).append("）");
                }
                if (c.getCkfw() != null && !c.getCkfw().isEmpty())
                {
                    sb.append("  参考范围：").append(c.getCkfw());
                }
                sb.append("\n");
            }
        }
        JTextArea area = new JTextArea(sb.toString(), 12, 30);
        area.setEditable(false);
        area.setLineWrap(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "套餐明细", JOptionPane.INFORMATION_MESSAGE);
    }

    private void register()
    {
        CheckGroup g = selectedGroup();
        if (g == null) return;
        if (controller.hasActiveRegistration(tel, g.getGid()))
        {
            JOptionPane.showMessageDialog(this, "您已预约过该套餐（进行中），请先取消原预约。");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "确定预约套餐「" + g.getGname() + "」吗？",
                "确认预约", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;
        if (controller.register(tel, g.getGid()))
        {
            JOptionPane.showMessageDialog(this, "预约成功");
        }
        else
        {
            JOptionPane.showMessageDialog(this, "预约失败");
        }
    }
}
