package com.ncu.patient.view;

import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.ui.Ui;
import com.ncu.patient.controller.PatientController;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 套餐浏览面板：左侧套餐列表，右侧套餐明细（含检查项表格）；支持预约（选日期/时间/地点）
 */
public class PackagePanel extends JPanel
{
    private final PatientController controller = new PatientController();
    private final String tel;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"编号", "套餐名", "费用(元)", "备注"};
    private List<CheckGroup> groups;

    private final JLabel detailName = new JLabel();
    private final JLabel detailPrice = new JLabel();
    private final JLabel detailRemark = new JLabel();
    private final DefaultTableModel itemModel;
    private final JTable itemTable;
    private final String[] itemColumns = {"检查项", "单位", "参考范围", "单价(元)"};

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
        table.setShowVerticalLines(true);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateDetail(); });

        itemModel = new DefaultTableModel(itemColumns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        itemTable = Ui.table(itemModel);
        itemTable.setShowVerticalLines(true);

        JPanel detail = buildDetailCard();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                Ui.card(new JScrollPane(table)), detail);
        split.setDividerLocation(430);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        add(Ui.header("体检预约",
                Ui.actionPrimary("预约", e -> register()),
                Ui.action("单项预约", e -> openSingleItem()),
                Ui.action("刷新", e -> refresh())), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildDetailCard()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Ui.CARD_BG);

        JPanel info = new JPanel(new GridLayout(0, 1, 0, 4));
        info.setOpaque(false);
        detailName.setFont(Ui.bold(14));
        detailName.setForeground(Ui.TEXT);
        detailPrice.setFont(Ui.bold(14));
        detailPrice.setForeground(Ui.ACCENT);
        detailRemark.setFont(Ui.font(12));
        detailRemark.setForeground(Ui.SUB);

        info.add(Ui.title("套餐明细"));
        info.add(detailName);
        info.add(detailPrice);
        info.add(detailRemark);

        content.add(info, BorderLayout.NORTH);
        content.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        return Ui.card(content);
    }

    private void refresh()
    {
        tableModel.setRowCount(0);
        groups = controller.listPackages();
        for (CheckGroup g : groups)
        {
            tableModel.addRow(new Object[]{g.getBh(), g.getGname(), formatPrice(g.getPrice()), g.getRemark()});
        }
        updateDetail();
    }

    private String formatPrice(BigDecimal price)
    {
        return price == null ? "未定价" : "¥" + price.toPlainString();
    }

    private String nvl(String s)
    {
        return s == null ? "" : s;
    }

    private void updateDetail()
    {
        int row = table.getSelectedRow();
        if (row < 0 || groups == null || groups.isEmpty())
        {
            detailName.setText("请选择左侧套餐");
            detailPrice.setText("");
            detailRemark.setText("");
            itemModel.setRowCount(0);
            return;
        }
        CheckGroup g = groups.get(row);
        detailName.setText("套餐：" + nvl(g.getGname()));
        detailPrice.setText("费用：" + formatPrice(g.getPrice()));
        detailRemark.setText("备注：" + nvl(g.getRemark()));

        itemModel.setRowCount(0);
        for (CheckItem c : controller.listGroupItems(g.getGid()))
        {
            itemModel.addRow(new Object[]{c.getCname(), nvl(c.getDw()), nvl(c.getCkfw()),
                    formatPrice(c.getPrice())});
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

    private void register()
    {
        CheckGroup g = selectedGroup();
        if (g == null) return;
        if (controller.hasActiveRegistration(tel, g.getGid()))
        {
            JOptionPane.showMessageDialog(this, "您已预约过该套餐（进行中），请先取消原预约。");
            return;
        }
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        AppointmentDialog dlg = new AppointmentDialog(owner, g.getGname(), formatPrice(g.getPrice()));
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;

        Date regTime = dlg.getRegTime();
        String location = dlg.getSelectedLocation();
        if (controller.register(tel, g.getGid(), regTime, location))
        {
            JOptionPane.showMessageDialog(this, "预约成功");
            refresh();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "预约失败");
        }
    }

    private void openSingleItem()
    {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        new SingleItemDialog(owner, tel).setVisible(true);
    }
}
