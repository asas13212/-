package com.ncu.fee.view;

import com.ncu.common.ui.Ui;
import com.ncu.fee.controller.FeeController;
import com.ncu.fee.model.FeeVO;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.text.SimpleDateFormat;

/**
 * 收费记录面板：展示全部收费记录，已缴记录可退款
 */
public class FeeListPanel extends JPanel
{
    private final FeeController controller = new FeeController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"收费id", "预约id", "患者", "套餐", "金额(元)", "状态", "收费员", "缴费时间", "备注"};

    public FeeListPanel()
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

        // 状态列单元格存数字(0/1/2)，用渲染器显示成中文并居中
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, column);
                if (value instanceof Integer)
                {
                    setText(statusText((Integer) value));
                }
                return c;
            }
        };
        statusRenderer.setFont(Ui.font(13));
        statusRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(5).setCellRenderer(statusRenderer);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        add(Ui.header("收费记录",
                Ui.actionDanger("退款", e -> refund()),
                Ui.action("刷新", e -> refresh())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (FeeVO f : controller.listFees())
        {
            tableModel.addRow(new Object[]{f.getId(), f.getRegId(),
                    f.getPatientName() + "(" + f.getTel() + ")", f.getGroupName(),
                    f.getAmount() == null ? "" : f.getAmount().toPlainString(),
                    f.getStatus(), f.getOperator(),
                    f.getPayTime() == null ? "" : sdf.format(f.getPayTime()),
                    f.getRemark()});
        }
    }

    private void refund()
    {
        int row = table.getSelectedRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "请先在表格里选中一行要退款的收费记录");
            return;
        }
        int feeId = (Integer) tableModel.getValueAt(row, 0);
        int status = parseStatus(row);
        if (status != 1)
        {
            JOptionPane.showMessageDialog(this, "只有已缴(1)的记录才能退款");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "确定要退掉这条收费记录吗？", "退款确认",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION)
        {
            return;
        }
        String err = controller.refund(feeId);
        if (err == null)
        {
            JOptionPane.showMessageDialog(this, "退款成功");
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, err);
        }
    }

    /** 从表格第 5 列(状态)取当前行的数字状态 */
    private int parseStatus(int row)
    {
        Object v = tableModel.getValueAt(row, 5);
        return v instanceof Integer ? (Integer) v : -1;
    }

    private String statusText(int status)
    {
        switch (status)
        {
            case 0: return "待缴";
            case 1: return "已缴";
            case 2: return "已退款";
            default: return "未知";
        }
    }
}
