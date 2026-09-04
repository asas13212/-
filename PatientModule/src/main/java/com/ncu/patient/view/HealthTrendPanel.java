package com.ncu.patient.view;

import com.ncu.common.ui.Ui;
import com.ncu.patient.controller.PatientController;
import com.ncu.patient.model.TrendItem;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康趋势面板：选择检查项，用折线图展示历次体检的数值变化，并叠加参考范围标准线
 */
public class HealthTrendPanel extends JPanel
{
    private final PatientController controller = new PatientController();
    private final String tel;
    private final JComboBox<String> itemBox = new JComboBox<>();
    private final TrendChartPanel chart = new TrendChartPanel();
    private final Map<String, Series> seriesMap = new LinkedHashMap<>();

    public HealthTrendPanel(String tel)
    {
        this.tel = tel;
        setLayout(new BorderLayout());
        Ui.content(this);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Ui.CONTENT_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        west.setOpaque(false);
        west.add(Ui.title("健康趋势"));
        bar.add(west, BorderLayout.WEST);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);
        JLabel sel = new JLabel("检查项:");
        sel.setFont(Ui.font(13));
        east.add(sel);
        itemBox.setPreferredSize(new Dimension(220, 28));
        east.add(itemBox);
        bar.add(east, BorderLayout.EAST);

        add(bar, BorderLayout.NORTH);
        add(Ui.card(chart), BorderLayout.CENTER);

        load();
        itemBox.addActionListener(e -> updateChart());
        updateChart();
    }

    private void load()
    {
        List<TrendItem> history = controller.listHistory(tel);
        Map<String, List<TrendItem>> byCid = new LinkedHashMap<>();
        for (TrendItem t : history)
        {
            byCid.computeIfAbsent(t.getCid(), k -> new ArrayList<>()).add(t);
        }

        seriesMap.clear();
        itemBox.removeAllItems();
        for (Map.Entry<String, List<TrendItem>> e : byCid.entrySet())
        {
            Series s = new Series();
            for (TrendItem t : e.getValue())
            {
                Double v = parse(t.getResultValue());
                if (v == null) continue;
                if (s.cname == null)
                {
                    s.cname = t.getCname();
                    s.dw = t.getDw();
                    double[] r = parseRange(t.getCkfw());
                    if (r != null)
                    {
                        s.refLo = r[0];
                        s.refHi = r[1];
                    }
                }
                s.times.add(t.getCheckTime());
                s.values.add(v);
            }
            if (s.values.size() >= 2)
            {
                seriesMap.put(e.getKey(), s);
                itemBox.addItem(s.cname + (s.dw == null || s.dw.isEmpty() ? "" : "（" + s.dw + "）"));
            }
        }
    }

    private Double parse(String s)
    {
        if (s == null) return null;
        try
        {
            return Double.parseDouble(s.trim());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /** 解析参考范围（如 "90~139" → [90, 139]）；非数值范围返回 null */
    private double[] parseRange(String ckfw)
    {
        if (ckfw == null || ckfw.isEmpty()) return null;
        String[] parts = ckfw.split("[~～]");
        if (parts.length != 2) return null;
        try
        {
            double lo = Double.parseDouble(parts[0].trim());
            double hi = Double.parseDouble(parts[1].trim());
            return new double[]{lo, hi};
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private void updateChart()
    {
        int idx = itemBox.getSelectedIndex();
        if (idx < 0 || seriesMap.isEmpty())
        {
            chart.setData(null, null, "", Double.NaN, Double.NaN);
            return;
        }
        List<String> cids = new ArrayList<>(seriesMap.keySet());
        Series s = seriesMap.get(cids.get(idx));
        chart.setData(s.times, s.values, s.dw, s.refLo, s.refHi);
    }

    private static class Series
    {
        String cname;
        String dw;
        List<Date> times = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        double refLo = Double.NaN;
        double refHi = Double.NaN;
    }
}
