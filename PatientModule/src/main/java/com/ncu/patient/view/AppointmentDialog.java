package com.ncu.patient.view;

import com.ncu.common.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * 预约对话框：选择日期 + 整点时间 + 体检地点
 */
public class AppointmentDialog extends JDialog
{
    private final JComboBox<String> dateBox = new JComboBox<>();
    private final JComboBox<String> timeBox = new JComboBox<>();
    private final JComboBox<String> locationBox = new JComboBox<>();
    private boolean confirmed = false;

    private static final String[] HOURS = {"08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
    private static final String[] LOCATIONS = {"总院体检中心（一楼）", "总院体检中心（三楼）", "东院体检中心", "西院体检中心"};
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public AppointmentDialog(Frame owner, String groupName, String priceText)
    {
        super(owner, "预约体检", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        addRow(form, c, 0, "项目：", new JLabel(groupName));
        addRow(form, c, 1, "费用：", new JLabel(priceText));

        LocalDate start = LocalDate.now();
        for (int i = 0; i < 15; i++)
        {
            dateBox.addItem(start.plusDays(i).toString());
        }
        addRow(form, c, 2, "日期：", dateBox);

        for (String h : HOURS) timeBox.addItem(h);
        addRow(form, c, 3, "时间：", timeBox);

        for (String l : LOCATIONS) locationBox.addItem(l);
        addRow(form, c, 4, "地点：", locationBox);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        buttons.add(Ui.actionPrimary("确定预约", e -> { confirmed = true; dispose(); }));
        buttons.add(Ui.action("取消", e -> dispose()));

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, Component comp)
    {
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(comp, c);
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public Date getRegTime()
    {
        String d = (String) dateBox.getSelectedItem();
        String t = (String) timeBox.getSelectedItem();
        try
        {
            return TIME_FMT.parse(d + " " + t);
        }
        catch (Exception e)
        {
            return new Date();
        }
    }

    public String getSelectedLocation()
    {
        return (String) locationBox.getSelectedItem();
    }
}
