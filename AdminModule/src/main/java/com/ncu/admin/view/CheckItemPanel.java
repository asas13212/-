package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
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

/**
 * 检查项管理面板
 */
public class CheckItemPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"主键id", "编号", "名称", "单位", "参考范围", "单价(元)", "状态"};

    public CheckItemPanel()
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
        add(Ui.header("检查项管理",
                Ui.actionPrimary("新增", e -> addItem()),
                Ui.action("刷新", e -> refresh()),
                Ui.action("修改", e -> editItem()),
                Ui.actionDanger("删除", e -> deleteItem()),
                Ui.action("上/下架", e -> toggle())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        for (CheckItem c : controller.listItems())
        {
            tableModel.addRow(new Object[]{c.getCid(), c.getBh(), c.getCname(),
                    c.getDw(), c.getCkfw(), priceText(c.getPrice()), c.getStatus() == 0 ? "正常" : "下架"});
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
                new String[]{"主键id", "编号", "名称", "单位", "参考范围", "单价(元)"}, null);
        if (vals == null) return;
        BigDecimal price = parsePrice(vals[5]);
        if (price == null) return;
        CheckItem c = new CheckItem();
        c.setCid(vals[0]);
        c.setBh(vals[1]);
        c.setCname(vals[2]);
        c.setDw(vals[3]);
        c.setCkfw(vals[4]);
        c.setPrice(price);
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
                new String[]{"编号", "名称", "单位", "参考范围", "单价(元)"},
                new String[]{c.getBh(), c.getCname(), c.getDw(), c.getCkfw(),
                        c.getPrice() == null ? "" : c.getPrice().toPlainString()});
        if (vals == null) return;
        BigDecimal price = parsePrice(vals[4]);
        if (price == null) return;
        c.setBh(vals[0]);
        c.setCname(vals[1]);
        c.setDw(vals[2]);
        c.setCkfw(vals[3]);
        c.setPrice(price);
        if (controller.editItem(c))
        {
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "修改失败");
        }
    }

    /** 单价列展示：未定价显示占位 */
    private String priceText(BigDecimal p)
    {
        return p == null ? "未定价" : p.toPlainString();
    }

    /** 解析并校验单价：必须为正数；非法返回 null（已弹提示） */
    private BigDecimal parsePrice(String s)
    {
        if (s == null || s.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(this, "请填写单价(元)");
            return null;
        }
        try
        {
            BigDecimal p = new BigDecimal(s.trim());
            if (p.compareTo(BigDecimal.ZERO) <= 0)
            {
                JOptionPane.showMessageDialog(this, "单价必须大于 0");
                return null;
            }
            return p;
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "单价必须是数字（如 100 或 100.50）");
            return null;
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
