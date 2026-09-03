package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.common.model.CheckItem;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

/**
 * 检查项管理面板
 */
public class CheckItemPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"主键id", "编号", "名称", "单位", "参考范围", "状态"};

    public CheckItemPanel()
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

        JPanel btnPanel = new JPanel();
        addButton(btnPanel, "刷新", e -> refresh());
        addButton(btnPanel, "新增", e -> addItem());
        addButton(btnPanel, "修改", e -> editItem());
        addButton(btnPanel, "删除", e -> deleteItem());
        addButton(btnPanel, "上/下架", e -> toggle());
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
        for (CheckItem c : controller.listItems())
        {
            tableModel.addRow(new Object[]{c.getCid(), c.getBh(), c.getCname(),
                    c.getDw(), c.getCkfw(), c.getStatus() == 0 ? "正常" : "下架"});
        }
    }

    private String selectedCid()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先在表格里选中一行");
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    private void addItem()
    {
        String[] vals = DialogUtil.prompt(this, "新增检查项",
                new String[]{"主键id", "编号", "名称", "单位", "参考范围"}, null);
        if (vals == null) return;
        CheckItem c = new CheckItem();
        c.setCid(vals[0]);
        c.setBh(vals[1]);
        c.setCname(vals[2]);
        c.setDw(vals[3]);
        c.setCkfw(vals[4]);
        c.setStatus(0);
        if (controller.addItem(c))
        {
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "新增失败（可能是主键重复）");
        }
    }

    private void editItem()
    {
        String cid = selectedCid();
        if (cid == null) return;
        CheckItem c = controller.findItem(cid);
        if (c == null) return;
        String[] vals = DialogUtil.prompt(this, "修改检查项",
                new String[]{"编号", "名称", "单位", "参考范围"},
                new String[]{c.getBh(), c.getCname(), c.getDw(), c.getCkfw()});
        if (vals == null) return;
        c.setBh(vals[0]);
        c.setCname(vals[1]);
        c.setDw(vals[2]);
        c.setCkfw(vals[3]);
        if (controller.editItem(c))
        {
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "修改失败");
        }
    }

    private void deleteItem()
    {
        String cid = selectedCid();
        if (cid == null) return;
        int r = JOptionPane.showConfirmDialog(this, "确定删除检查项 " + cid + " 吗？",
                "删除", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION)
        {
            controller.removeItem(cid);
            refresh();
        }
    }

    private void toggle()
    {
        String cid = selectedCid();
        if (cid == null) return;
        CheckItem c = controller.findItem(cid);
        if (c == null) return;
        c.setStatus(c.getStatus() == 0 ? 1 : 0);
        controller.editItem(c);
        refresh();
    }
}
