package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.admin.model.CalendarAppointment;
import com.ncu.common.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预约日历面板（医生端）：月历网格 + 月份导航，点击某天在下方显示当天预约明细。
 * 每条预约展示：时间 / 患者姓名 / 套餐名 / 该套餐包含的具体检查项目 / 状态（彩色标签）。
 *
 * 视觉：低饱和蓝白医疗风；圆角规整、间距统一、无阴影渐变，贴近 Windows 桌面软件。
 * 本类自包含色板与圆角绘制，不依赖/不改动 Common 的 Ui 调色板。
 */
public class CalendarPanel extends JPanel
{
    // ---- 色板：低饱和蓝白 · 医疗风 ----
    private static final Color BG = new Color(0xF5F7FA);        // 内容区底
    private static final Color CARD = new Color(0xFFFFFF);      // 卡片白
    private static final Color PRIMARY = new Color(0x4A7CA5);   // 主色低饱和蓝
    private static final Color PRIMARY_BG = new Color(0xEAF1F7);// 主色浅底（今日/选中）
    private static final Color HOVER = new Color(0xE3ECF3);     // 按钮/格子悬停
    private static final Color PRESSED = new Color(0xD7E0E8);   // 按钮按下
    private static final Color TEXT = new Color(0x2C3A47);      // 文字主
    private static final Color SUB = new Color(0x8A97A6);       // 文字次
    private static final Color BORDER = new Color(0xE3E8EE);    // 1px 描边
    private static final Color MUTED_BG = new Color(0xF7F9FB);  // 非本月底
    private static final Color MUTED_FG = new Color(0xC3CAD2);  // 非本月文字
    private static final Color GREEN = new Color(0x5BA07E);     // 已完成标签
    private static final Color GRAY = new Color(0xA5AEB8);      // 已取消标签

    private final AdminController controller = new AdminController();

    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel grid = new JPanel(new GridLayout(6, 7, 4, 4));
    private final JLabel dayTitle = new JLabel();
    private final DefaultTableModel detailModel;
    private final JTable detailTable;
    private final String[] columns = {"时间", "患者", "项目名称", "检查项目", "状态"};

    private final Calendar cursor = Calendar.getInstance(); // 当前显示月份（固定到 1 号）
    private DayCell[] cells = new DayCell[42];              // 月历 6x7 个格
    private DayCell selectedCell;                            // 当前高亮的格
    private Date selectedDay;                                // 当前选中的某天（0 点）
    private Map<Integer, List<CalendarAppointment>> byDay = new HashMap<>(); // 号数 -> 当天预约

    private static final SimpleDateFormat MONTH_FMT = new SimpleDateFormat("yyyy年M月");
    private static final SimpleDateFormat DAY_FMT = new SimpleDateFormat("M月d日");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final String[] WEEKDAYS = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public CalendarPanel()
    {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ---- 顶部：标题 + 月份导航 ----
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel rowTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rowTitle.setOpaque(false);
        JLabel title = new JLabel("预约日历");
        title.setFont(Ui.bold(18));
        title.setForeground(TEXT);
        rowTitle.add(title);
        top.add(rowTitle);

        JPanel rowNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        rowNav.setOpaque(false);
        FlatBtn prev = nav("◀", "上月", 30);
        prev.addActionListener(e -> shiftMonth(-1));
        rowNav.add(prev);
        monthLabel.setFont(Ui.bold(15));
        monthLabel.setForeground(TEXT);
        monthLabel.setPreferredSize(new Dimension(132, 30));
        rowNav.add(monthLabel);
        FlatBtn next = nav("▶", "下月", 30);
        next.addActionListener(e -> shiftMonth(1));
        rowNav.add(next);
        rowNav.add(Box.createHorizontalStrut(4));
        FlatBtn today = nav("今天", "回到今天", 68);
        today.addActionListener(e -> goToday());
        rowNav.add(today);
        FlatBtn refresh = nav("刷新", "重新加载", 68);
        refresh.addActionListener(e -> rebuild());
        rowNav.add(refresh);
        top.add(rowNav);
        add(top, BorderLayout.NORTH);

        // ---- 中部：月历（星期表头 + 6x7 网格） ----
        JPanel calCard = new JPanel(new BorderLayout(0, 6));
        calCard.setBackground(CARD);
        calCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 10, 10)));

        JPanel weekHeader = new JPanel(new GridLayout(1, 7));
        weekHeader.setOpaque(false);
        String[] wk = {"一", "二", "三", "四", "五", "六", "日"};
        for (String w : wk)
        {
            JLabel l = new JLabel(w, SwingConstants.CENTER);
            l.setFont(Ui.bold(12));
            l.setForeground(SUB);
            weekHeader.add(l);
        }
        calCard.add(weekHeader, BorderLayout.NORTH);

        grid.setOpaque(false);
        calCard.add(grid, BorderLayout.CENTER);
        add(calCard, BorderLayout.CENTER);

        // ---- 底部：当天预约明细 ----
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        dayTitle.setFont(Ui.bold(14));
        dayTitle.setForeground(TEXT);
        south.add(dayTitle, BorderLayout.NORTH);

        detailModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        detailTable = Ui.table(detailModel);
        detailTable.setRowHeight(34);
        detailTable.setRowSelectionAllowed(false);
        detailTable.setFocusable(false);
        detailTable.getColumnModel().getColumn(3).setCellRenderer(new ItemsRenderer());
        detailTable.getColumnModel().getColumn(4).setCellRenderer(new StatusTagRenderer());

        JScrollPane sp = new JScrollPane(detailTable);
        sp.setBorder(null);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        tableCard.add(sp, BorderLayout.CENTER);
        south.add(tableCard, BorderLayout.CENTER);
        south.setPreferredSize(new Dimension(0, 214));
        add(south, BorderLayout.SOUTH);

        cursor.set(Calendar.DAY_OF_MONTH, 1);
        rebuild();
    }

    /** 创建一个圆角扁平按钮（自绘，主色/描边按本面板色板） */
    private static FlatBtn nav(String text, String tip, int width)
    {
        FlatBtn b = new FlatBtn(text, CARD, HOVER, PRESSED, TEXT, BORDER);
        b.setPreferredSize(new Dimension(width, 30));
        b.setToolTipText(tip);
        return b;
    }

    /** 重建整个月历：查询本月预约 -> 渲染 42 格 -> 恢复选中/明细 */
    private void rebuild()
    {
        Calendar from = (Calendar) cursor.clone();
        from.set(Calendar.DAY_OF_MONTH, 1);
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);
        Calendar to = (Calendar) from.clone();
        to.add(Calendar.MONTH, 1);

        List<CalendarAppointment> apps = controller.listAppointments(from.getTime(), to.getTime());
        byDay = new HashMap<>();
        for (CalendarAppointment a : apps)
        {
            int d = dayOfMonth(a.getRegTime());
            byDay.computeIfAbsent(d, k -> new ArrayList<>()).add(a);
        }

        monthLabel.setText(MONTH_FMT.format(cursor.getTime()));

        grid.removeAll();
        selectedCell = null;
        cells = new DayCell[42];

        Calendar today = Calendar.getInstance();
        boolean todayInMonth = sameMonth(today, cursor);

        int daysInMonth = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        int lead = (cursor.get(Calendar.DAY_OF_WEEK) + 5) % 7; // 周一为 0
        Calendar prev = (Calendar) cursor.clone();
        prev.add(Calendar.MONTH, -1);
        int prevMax = prev.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < 42; i++)
        {
            int dayNum;
            boolean inMonth;
            Calendar cellCal = (Calendar) cursor.clone();
            if (i < lead)
            {
                dayNum = prevMax - (lead - 1 - i);
                inMonth = false;
                cellCal.add(Calendar.MONTH, -1);
                cellCal.set(Calendar.DAY_OF_MONTH, dayNum);
            }
            else if (i - lead + 1 > daysInMonth)
            {
                dayNum = i - lead + 1 - daysInMonth;
                inMonth = false;
                cellCal.add(Calendar.MONTH, 1);
                cellCal.set(Calendar.DAY_OF_MONTH, dayNum);
            }
            else
            {
                dayNum = i - lead + 1;
                inMonth = true;
                cellCal.set(Calendar.DAY_OF_MONTH, dayNum);
            }
            cellCal.set(Calendar.HOUR_OF_DAY, 0);
            cellCal.set(Calendar.MINUTE, 0);
            cellCal.set(Calendar.SECOND, 0);
            cellCal.set(Calendar.MILLISECOND, 0);

            boolean isToday = inMonth && todayInMonth && today.get(Calendar.DAY_OF_MONTH) == dayNum;
            List<CalendarAppointment> dayApps = inMonth ? byDay.get(dayNum) : null;
            DayCell cell = new DayCell(cellCal.getTime(), dayNum, inMonth, isToday, dayApps);
            cells[i] = cell;
            grid.add(cell);
        }

        // 恢复选中：今天在本月则选中今天，否则清空
        if (todayInMonth)
        {
            selectByDate(normalize(today.getTime()));
        }
        else
        {
            selectedDay = null;
            updateDetail(null);
        }

        grid.revalidate();
        grid.repaint();
    }

    private void shiftMonth(int delta)
    {
        cursor.add(Calendar.MONTH, delta);
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        rebuild();
    }

    private void goToday()
    {
        Calendar t = Calendar.getInstance();
        cursor.setTime(t.getTime());
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        rebuild();
    }

    private void selectByDate(Date d)
    {
        for (DayCell c : cells)
        {
            if (c != null && c.date != null && sameDay(c.date, d))
            {
                selectDay(c, d);
                return;
            }
        }
        selectedDay = null;
        selectedCell = null;
        updateDetail(null);
    }

    private void selectDay(DayCell cell, Date d)
    {
        if (selectedCell != null)
        {
            selectedCell.setSelected(false);
        }
        selectedCell = cell;
        selectedCell.setSelected(true);
        selectedDay = d;
        updateDetail(d);
    }

    private void updateDetail(Date d)
    {
        detailModel.setRowCount(0);
        if (d == null)
        {
            dayTitle.setText("点击某天查看当天的预约详情");
            return;
        }
        List<CalendarAppointment> list = byDay.get(dayOfMonth(d));
        if (list == null)
        {
            list = Collections.emptyList();
        }
        dayTitle.setText(dayTitleText(d) + " · " + list.size() + " 个预约");
        for (CalendarAppointment a : list)
        {
            detailModel.addRow(new Object[]{
                    a.getRegTime() == null ? "" : TIME_FMT.format(a.getRegTime()),
                    a.getPatientName(),
                    a.getGroupName(),
                    a.getItems() == null ? "" : a.getItems(),
                    a.getStatus()});
        }
    }

    private String dayTitleText(Date d)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return DAY_FMT.format(d) + " " + WEEKDAYS[c.get(Calendar.DAY_OF_WEEK) - 1];
    }

    // ---- 日期工具 ----
    private static Date normalize(Date d)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static boolean sameDay(Date a, Date b)
    {
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean sameMonth(Calendar a, Calendar b)
    {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH);
    }

    private static int dayOfMonth(Date d)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    private static String shortName(String n)
    {
        if (n == null)
        {
            return "";
        }
        return n.length() > 4 ? n.substring(0, 4) + "…" : n;
    }

    private static String clip(String s, int n)
    {
        if (s == null)
        {
            return "";
        }
        return s.length() > n ? s.substring(0, n) + "…" : s;
    }

    // ---- 自绘圆角扁平按钮 ----
    private static class FlatBtn extends javax.swing.JButton
    {
        private final Color base, hover, pressed, fg, stroke;

        FlatBtn(String text, Color base, Color hover, Color pressed, Color fg, Color stroke)
        {
            super(text);
            this.base = base;
            this.hover = hover;
            this.pressed = pressed;
            this.fg = fg;
            this.stroke = stroke;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setFont(Ui.font(13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = base;
            Color txt = fg;
            if (!isEnabled())
            {
                bg = new Color(0xC7CFDA);
                txt = Color.WHITE;
            }
            else if (getModel().isPressed())
            {
                bg = pressed;
            }
            else if (getModel().isRollover())
            {
                bg = hover;
            }

            int w = getWidth();
            int h = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, 6, 6);
            if (stroke != null)
            {
                g2.setColor(stroke);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
            }

            String t = getText();
            if (t != null && !t.isEmpty())
            {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(t);
                int x = (w - tw) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(txt);
                g2.drawString(t, x, y);
            }
            g2.dispose();
        }
    }

    /** 月历里的一个格子：圆角卡片 + 日期号（右上角数量徽标）+ 当天预约摘要（时间+姓名，最多2条） */
    private class DayCell extends JPanel
    {
        final Date date;
        final boolean inMonth;
        private final Color bg;
        private final Color border;
        private boolean selected;

        DayCell(Date date, int dayNum, boolean inMonth, boolean isToday, List<CalendarAppointment> apps)
        {
            this.date = date;
            this.inMonth = inMonth;
            this.bg = inMonth ? (isToday ? PRIMARY_BG : CARD) : MUTED_BG;
            this.border = inMonth ? BORDER : BORDER;

            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            setLayout(new BorderLayout(0, 2));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // 头部：日期号（左）+ 数量徽标（右）
            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel day = new JLabel(String.valueOf(dayNum));
            day.setFont(Ui.bold(12));
            day.setForeground(!inMonth ? MUTED_FG : (isToday ? PRIMARY : TEXT));
            head.add(day, BorderLayout.WEST);

            if (inMonth && apps != null && !apps.isEmpty())
            {
                Pill badge = new Pill();
                badge.setText(String.valueOf(apps.size()));
                badge.setPreferredSize(new Dimension(20, 18));
                head.add(badge, BorderLayout.EAST);
            }
            add(head, BorderLayout.NORTH);

            // 摘要：时间 + 姓名，最多 2 条
            if (inMonth && apps != null)
            {
                JPanel body = new JPanel();
                body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
                body.setOpaque(false);
                int shown = 0;
                for (CalendarAppointment a : apps)
                {
                    if (shown >= 2)
                    {
                        break;
                    }
                    JLabel e = new JLabel((a.getRegTime() == null ? "" : TIME_FMT.format(a.getRegTime()))
                            + "  " + shortName(a.getPatientName()));
                    e.setFont(Ui.font(11));
                    e.setForeground(PRIMARY);
                    e.setAlignmentX(Component.LEFT_ALIGNMENT);
                    body.add(e);
                    shown++;
                }
                add(body, BorderLayout.CENTER);
            }

            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent ev)
                {
                    if (inMonth)
                    {
                        selectDay(DayCell.this, date);
                    }
                }
            });
        }

        void setSelected(boolean s)
        {
            this.selected = s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(selected ? PRIMARY_BG : bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);

            g2.setColor(selected ? PRIMARY : border);
            g2.setStroke(new BasicStroke(selected ? 2f : 1f));
            int inset = selected ? 1 : 0;
            g2.drawRoundRect(inset, inset, w - 1 - 2 * inset, h - 1 - 2 * inset, 8, 8);
            g2.dispose();
        }
    }

    /** 圆角小徽标（数量角标） */
    private static class Pill extends JLabel
    {
        private Color fill = PRIMARY;

        Pill()
        {
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(Ui.font(10));
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** 状态列渲染器：把状态 int 渲染成居中的彩色圆角标签 */
    private static class StatusTagRenderer extends DefaultTableCellRenderer
    {
        private int status = -1;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            status = value instanceof Integer ? (Integer) value : -1;
            setText(statusText(status));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String t = getText();
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(t);
            int w = tw + 20;
            int h = 22;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;

            g2.setColor(statusColor(status));
            g2.fillRoundRect(x, y, w, h, 6, 6);

            g2.setFont(Ui.font(11));
            g2.setColor(Color.WHITE);
            g2.drawString(t, x + (w - tw) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** 检查项目列渲染器：过长省略 + 悬浮完整提示 */
    private static class ItemsRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String s = value == null ? "" : value.toString();
            setText(clip(s, 22));
            setToolTipText(s.isEmpty() ? null : s);
            setForeground(TEXT);
            return this;
        }
    }

    private static String statusText(int status)
    {
        switch (status)
        {
            case 0: return "已预约";
            case 1: return "已完成";
            case 2: return "已取消";
            default: return "未知";
        }
    }

    private static Color statusColor(int status)
    {
        switch (status)
        {
            case 0: return PRIMARY;
            case 1: return GREEN;
            case 2: return GRAY;
            default: return GRAY;
        }
    }
}
