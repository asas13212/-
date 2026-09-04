package com.ncu.patient.view;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 简单折线图面板：数据折线 + 参考范围标准线（虚线），纯 Swing 自绘，无第三方依赖
 */
public class TrendChartPanel extends JPanel
{
    private List<Date> times;
    private List<Double> values;
    private String unit;
    private double refLo = Double.NaN;
    private double refHi = Double.NaN;
    private static final SimpleDateFormat FMT = new SimpleDateFormat("MM-dd");

    public TrendChartPanel()
    {
        setBackground(Color.WHITE);
    }

    public void setData(List<Date> times, List<Double> values, String unit, double refLo, double refHi)
    {
        this.times = times;
        this.values = values;
        this.unit = unit == null ? "" : unit;
        this.refLo = refLo;
        this.refHi = refHi;
        repaint();
    }

    private boolean hasRef()
    {
        return !Double.isNaN(refLo) && !Double.isNaN(refHi);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padL = 56, padR = 24, padT = 30, padB = 40;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        g2.setColor(new Color(0xE0E6EE));
        g2.drawRect(padL, padT, chartW, chartH);

        if (values == null || values.isEmpty())
        {
            g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            g2.setColor(new Color(0x8A97A6));
            g2.drawString("暂无趋势数据（需同一检查项至少两次体检结果）", padL + 16, padT + 28);
            return;
        }

        double min = values.get(0), max = values.get(0);
        for (double v : values)
        {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (hasRef())
        {
            min = Math.min(min, refLo);
            max = Math.max(max, refHi);
        }
        if (min == max)
        {
            min -= 1;
            max += 1;
        }
        double pad = (max - min) * 0.18;
        min -= pad;
        max += pad;

        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        for (int i = 0; i <= 4; i++)
        {
            double val = max - (max - min) * i / 4.0;
            int y = padT + (int) ((max - val) / (max - min) * chartH);
            g2.setColor(new Color(0xEDF1F5));
            g2.drawLine(padL, y, padL + chartW, y);
            g2.setColor(new Color(0x8A97A6));
            g2.drawString(String.format("%.1f", val), 6, y + 4);
        }

        // 参考范围标准线（橙色虚线）
        if (hasRef())
        {
            float[] dash = {5f, 5f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.setColor(new Color(0xE6A23C));
            int yLo = padT + (int) ((max - refLo) / (max - min) * chartH);
            int yHi = padT + (int) ((max - refHi) / (max - min) * chartH);
            g2.drawLine(padL, yLo, padL + chartW, yLo);
            g2.drawLine(padL, yHi, padL + chartW, yHi);
            g2.drawString(String.format("%.1f", refLo), padL + chartW - 30, yLo - 4);
            g2.drawString(String.format("%.1f", refHi), padL + chartW - 30, yHi - 4);
        }

        int n = values.size();
        double xStep = n == 1 ? 0 : chartW * 1.0 / (n - 1);
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++)
        {
            xs[i] = padL + (int) (i * xStep);
            ys[i] = padT + (int) ((max - values.get(i)) / (max - min) * chartH);
        }

        g2.setColor(new Color(0x2E7DD1));
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < n - 1; i++)
        {
            g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        for (int i = 0; i < n; i++)
        {
            g2.setColor(new Color(0x2E7DD1));
            g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
            g2.setColor(new Color(0x333B45));
            g2.drawString(String.format("%.1f", values.get(i)), xs[i] - 12, ys[i] - 8);
        }

        g2.setColor(new Color(0x8A97A6));
        for (int i = 0; i < n; i++)
        {
            String label = times.get(i) == null ? "" : FMT.format(times.get(i));
            g2.drawString(label, xs[i] - 16, padT + chartH + 18);
        }

        String top = "";
        if (unit != null && !unit.isEmpty() && !"-".equals(unit))
        {
            top = "单位：" + unit;
        }
        if (hasRef())
        {
            top += (top.isEmpty() ? "" : "    ") + "参考范围 " + refLo + "~" + refHi + "（橙色虚线）";
        }
        if (!top.isEmpty())
        {
            g2.setColor(new Color(0x8A97A6));
            g2.drawString(top, padL, padT - 10);
        }
    }
}
