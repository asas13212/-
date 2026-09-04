package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐（检查组）管理面板
 */
public class CheckGroupPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"主键id", "名称", "编号", "套餐价(合计)(元)", "备注", "状态"};

    public CheckGroupPanel()
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
        add(Ui.header("套餐管理",
                Ui.actionPrimary("新增套餐", e -> addGroup()),
                Ui.action("刷新", e -> refresh()),
                Ui.action("修改", e -> editGroup()),
                Ui.actionDanger("删除", e -> deleteGroup()),
                Ui.action("添加检查项", e -> addItem()),
                Ui.action("查看明细", e -> showDetail())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        for (CheckGroup g : controller.listGroups())
        {
            tableModel.addRow(new Object[]{g.getGid(), g.getGname(), g.getBh(),
                    priceText(g.getPrice()), g.getRemark(), g.getStatus() == 0 ? "正常" : "停用"});
        }
    }

    private String selectedGid()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先在表格里选中一行");
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    private void addGroup()
    {
        String[] vals = DialogUtil.prompt(this, "新增套餐",
                new String[]{"主键id", "名称", "编号", "备注"}, null);
        if (vals == null) return;
        CheckGroup g = new CheckGroup();
        g.setGid(vals[0]);
        g.setGname(vals[1]);
        g.setBh(vals[2]);
        g.setRemark(vals[3]);
        g.setStatus(0);
        if (controller.addGroup(g))
        {
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "新增失败（可能是主键重复）");
        }
    }

    private void editGroup()
    {
        String gid = selectedGid();
        if (gid == null) return;
        CheckGroup g = controller.findGroup(gid);
        if (g == null) return;
        String[] vals = DialogUtil.prompt(this, "修改套餐",
                new String[]{"名称", "编号", "备注"},
                new String[]{g.getGname(), g.getBh(), g.getRemark()});
        if (vals == null) return;
        g.setGname(vals[0]);
        g.setBh(vals[1]);
        g.setRemark(vals[2]);
        if (controller.editGroup(g))
        {
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "修改失败");
        }
    }

    private void deleteGroup()
    {
        String gid = selectedGid();
        if (gid == null) return;
        int r = JOptionPane.showConfirmDialog(this, "确定删除套餐 " + gid + " 吗？",
                "删除", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION)
        {
            controller.removeGroup(gid);
            refresh();
        }
    }

    private void addItem()
    {
        String gid = selectedGid();
        if (gid == null) return;
        String cid = DialogUtil.promptSingle(this, "添加检查项", "检查项id", null);
        if (cid == null || cid.isEmpty()) return;
        if (controller.addItemToGroup(gid, cid))
        {
            JOptionPane.showMessageDialog(this, "添加成功");
        }
        else
        {
            JOptionPane.showMessageDialog(this, "添加失败（检查项不存在或已存在）");
        }
    }

    private void showDetail()
    {
        String gid = selectedGid();
        if (gid == null) return;
        List<CheckItem> items = controller.listGroupItems(gid);
        if (items.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "该套餐还没有检查项");
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder sb = new StringBuilder("套餐 " + gid + " 包含的检查项（套餐价=各单项之和）：\n");
        for (CheckItem c : items)
        {
            BigDecimal p = c.getPrice();
            total = total.add(p == null ? BigDecimal.ZERO : p);
            sb.append("  ").append(c.getCname()).append("（").append(c.getCid())
              .append("）单价 ").append(priceText(p)).append(" 元\n");
        }
        sb.append("套餐合计：").append(priceText(total)).append(" 元");
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    /** 金额展示：未定价显示占位 */
    private String priceText(BigDecimal p)
    {
        return p == null ? "未定价" : p.toPlainString();
    }
}
