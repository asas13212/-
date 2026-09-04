package com.ncu.fee.view;

import com.ncu.common.ui.Ui;
import com.ncu.fee.controller.FeeController;
import com.ncu.fee.model.FeeRegVO;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 收费登记面板：列出还没有收费记录的"已预约"，选中后按套餐价入账（只弹确认，不手输金额）。
 * 金额 = 该预约套餐在 checkgroup.price 上的套餐价，由 FeeDao 联表带出。
 */
public class ChargePanel extends JPanel
{
    private final FeeController controller = new FeeController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String operatorTel; // 当前收费员(医生)账号
    private final String[] columns = {"预约id", "患者账号", "患者姓名", "套餐id", "套餐名称", "套餐价(元)", "预约时间"};

    private List<FeeRegVO> uncharged = new ArrayList<>(); // 与表格行一一对应

    public ChargePanel(String operatorTel)
    {
        this.operatorTel = operatorTel;
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
        add(Ui.header("收费登记",
                Ui.actionPrimary("确认收费", e -> charge()),
                Ui.action("刷新", e -> refresh())), BorderLayout.NORTH);
        add(Ui.card(sp), BorderLayout.CENTER);

        refresh();
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        uncharged = controller.listUnchargedRegs();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (FeeRegVO v : uncharged)
        {
            tableModel.addRow(new Object[]{v.getId(), v.getTel(), v.getPatientName(),
                    v.getGid(), v.getGroupName(),
                    v.getPrice() == null ? "-" : v.getPrice().toPlainString(),
                    v.getRegTime() == null ? "" : sdf.format(v.getRegTime())});
        }
    }

    /** 按套餐价入账：金额取该行套餐的 price，确认后直接生成一条已缴收费记录 */
    private void charge()
    {
        int row = table.getSelectedRow();
        if (row < 0 || row >= uncharged.size())
        {
            JOptionPane.showMessageDialog(this, "请先在表格里选中一行要收费的预约");
            return;
        }
        FeeRegVO v = uncharged.get(row);
        if (v.getPrice() == null || v.getPrice().signum() <= 0)
        {
            JOptionPane.showMessageDialog(this, "该套餐还没有设置价格，无法按套餐价收费\n请先在数据中给套餐补上 price");
            refresh();
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "对预约 #" + v.getId() + " 收取【" + v.getGroupName() + "】费用：\n\n"
                        + "        套餐价 ￥" + v.getPrice().toPlainString() + "\n\n确认入账吗？",
                "确认收费", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION)
        {
            return;
        }
        String err = controller.charge(v.getId(), v.getTel(), v.getGid(), v.getPrice(), operatorTel);
        if (err == null)
        {
            JOptionPane.showMessageDialog(this, "收费成功：" + v.getGroupName() + " ￥" + v.getPrice().toPlainString());
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, err);
            refresh(); // 可能因已被收费，刷新把该行移出待收费列表
        }
    }
}
