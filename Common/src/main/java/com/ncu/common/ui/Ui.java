package com.ncu.common.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

/**
 * 统一 UI 工具箱：调色板常量 + 常用"好看"组件的工厂。
 *
 * 全项目所有窗口统一从这里取配色 / 字体 / 按钮 / 表格，避免各模块各写一套观感。
 * 只做呈现层，不含任何业务逻辑；依赖只有 JDK 自带的 Swing。
 */
public final class Ui
{
    private Ui() {}

    // ---------------- 字体 ----------------
    public static final String FONT_NAME = "Microsoft YaHei";

    public static Font font(int size) { return new Font(FONT_NAME, Font.PLAIN, size); }

    public static Font bold(int size) { return new Font(FONT_NAME, Font.BOLD, size); }

    // ---------------- 调色板 ----------------
    /** 侧栏深蓝灰 */
    public static final Color SIDEBAR_BG = new Color(0x24344D);
    /** 顶部品牌区底色（略深一档） */
    public static final Color SIDEBAR_DARK = new Color(0x1D2A3F);
    /** 导航默认文字 */
    public static final Color NAV_TEXT = new Color(0xAEBBD0);
    /** 导航悬停文字 */
    public static final Color NAV_HOVER = new Color(0xDCE4EE);
    /** 青绿点缀（导航高亮 / 左竖条） */
    public static final Color ACCENT = new Color(0x1FAF93);
    /** 主操作蓝 */
    public static final Color PRIMARY = new Color(0x2E7DD1);
    /** 危险操作红 */
    public static final Color DANGER = new Color(0xE0533D);
    /** 内容区浅灰底 */
    public static final Color CONTENT_BG = new Color(0xF4F6F9);
    /** 白卡片 / 输入底 */
    public static final Color CARD_BG = Color.WHITE;
    /** 描边 / 分隔线 */
    public static final Color BORDER = new Color(0xE0E6EE);
    /** 正文 */
    public static final Color TEXT = new Color(0x333B45);
    /** 次要文字 */
    public static final Color SUB = new Color(0x8A97A6);
    /** 表头底 */
    public static final Color TABLE_HEAD = new Color(0xEDF1F6);
    /** 表头文字 */
    public static final Color TABLE_HEAD_TEXT = new Color(0x46535F);
    /** 斑马纹 */
    public static final Color ROW_ALT = new Color(0xF8FAFD);
    /** 行选中 */
    public static final Color SELECT_BG = new Color(0xD9E9FB);
    /** 行选中文字 */
    public static final Color SELECT_FG = new Color(0x1C3A5E);
    /** 表格横网格线 */
    public static final Color GRID = new Color(0xEDF1F5);

    // ---------------- 按钮 ----------------
    public enum Kind
    {
        /** 实心主蓝 */
        PRIMARY,
        /** 白底灰描边（次要） */
        DEFAULT,
        /** 实心红（删除/退款） */
        DANGER
    }

    /** 主操作按钮 */
    public static JButton primary(String text) { return button(text, Kind.PRIMARY); }

    /** 次要按钮 */
    public static JButton button(String text) { return button(text, Kind.DEFAULT); }

    /** 危险按钮 */
    public static JButton danger(String text) { return button(text, Kind.DANGER); }

    public static JButton button(String text, Kind kind)
    {
        Color base;
        Color hover;
        Color pressed;
        Color fg;
        switch (kind)
        {
            case PRIMARY:
                base = PRIMARY;
                hover = new Color(0x3B8ADF);
                pressed = new Color(0x246FC0);
                fg = Color.WHITE;
                break;
            case DANGER:
                base = DANGER;
                hover = new Color(0xE66955);
                pressed = new Color(0xC9452F);
                fg = Color.WHITE;
                break;
            default:
                base = CARD_BG;
                hover = new Color(0xF2F5F8);
                pressed = new Color(0xE3E9EF);
                fg = TEXT;
        }
        return new FlatButton(text, base, hover, pressed, fg, kind == Kind.DEFAULT ? BORDER : null);
    }

    /** 自绘圆角扁平按钮：不依赖任何 L&F 的默认渲染，保证各处观感一致 */
    private static final class FlatButton extends JButton
    {
        private final Color base;
        private final Color hover;
        private final Color pressed;
        private final Color fg;
        private final Color stroke;

        FlatButton(String text, Color base, Color hover, Color pressed, Color fg, Color stroke)
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
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setFont(font(13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = base;
            Color text = fg;
            if (!isEnabled())
            {
                bg = new Color(0xC7CFDA);
                text = Color.WHITE;
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
            int r = Math.min(8, h / 2);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, r, r);
            if (stroke != null)
            {
                g2.setColor(stroke);
                g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
            }

            String t = getText();
            if (t != null && !t.isEmpty())
            {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets in = getInsets();
                int tw = fm.stringWidth(t);
                int x = (w - tw) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(text);
                g2.drawString(t, x, y);
            }
            g2.dispose();
        }
    }

    // ---------------- 标题 / 提示 ----------------
    public static JLabel title(String text)
    {
        JLabel l = new JLabel(text);
        l.setFont(bold(18));
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel hint(String text)
    {
        JLabel l = new JLabel(text);
        l.setFont(font(12));
        l.setForeground(SUB);
        return l;
    }

    // ---------------- 表格 ----------------
    /**
     * 统一表格：浅色表头 / 30px 行高 / 淡网格线 / 斑马纹 / 选中高亮。
     * 传入的 model 仍是原业务用的 DefaultTableModel，只是观感统一。
     */
    public static JTable table(DefaultTableModel model)
    {
        JTable t = new JTable(model)
        {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
            {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row))
                {
                    c.setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                }
                return c;
            }
        };
        t.setFont(font(13));
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(GRID);
        t.setSelectionBackground(SELECT_BG);
        t.setSelectionForeground(SELECT_FG);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);

        // 单元格左对齐、垂直居中
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setFont(font(13));
        cell.setVerticalAlignment(SwingConstants.CENTER);
        t.setDefaultRenderer(Object.class, cell);

        // 表头：浅色 + 居中 + 底部细线
        JTableHeader header = t.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 32));
        header.setFont(bold(13));
        DefaultTableCellRenderer head = new DefaultTableCellRenderer();
        head.setHorizontalAlignment(SwingConstants.CENTER);
        head.setBackground(TABLE_HEAD);
        head.setForeground(TABLE_HEAD_TEXT);
        header.setDefaultRenderer(head);

        return t;
    }

    /** 表格专用滚动容器：白底 + 浅描边，去掉默认灰框 */
    public static JScrollPane pane(JTable t)
    {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(CARD_BG);
        return sp;
    }

    // ---------------- 面板底色 ----------------
    /** 让某个面板落到浅灰内容区底色（用于各功能面板顶层） */
    public static void content(JPanel p)
    {
        p.setBackground(CONTENT_BG);
    }

    // ---------------- 带监听的动作按钮 ----------------
    public static JButton actionPrimary(String text, ActionListener l)
    {
        return withListener(primary(text), l);
    }

    public static JButton action(String text, ActionListener l)
    {
        return withListener(button(text), l);
    }

    public static JButton actionDanger(String text, ActionListener l)
    {
        return withListener(danger(text), l);
    }

    private static JButton withListener(JButton b, ActionListener l)
    {
        b.addActionListener(l);
        return b;
    }

    // ---------------- 页面头部（标题 + 右侧动作） ----------------
    /** 左标题、右按钮的一行页头；返回浅灰底面板，自带底部留白 */
    public static JPanel header(String titleText, JButton... actions)
    {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CONTENT_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        west.setOpaque(false);
        west.add(title(titleText));
        bar.add(west, BorderLayout.WEST);

        if (actions.length > 0)
        {
            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            east.setOpaque(false);
            for (JButton b : actions)
            {
                east.add(b);
            }
            bar.add(east, BorderLayout.EAST);
        }
        return bar;
    }

    // ---------------- 白卡片容器 ----------------
    /** 用白色圆角卡片包住内容（通常是表格），卡片自带描边与内边距 */
    public static JPanel card(Component inner)
    {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(CARD_BG);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        c.add(inner, BorderLayout.CENTER);
        return c;
    }
}
