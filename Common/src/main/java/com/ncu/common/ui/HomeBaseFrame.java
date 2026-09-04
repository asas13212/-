package com.ncu.common.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * 侧边栏主窗骨架：左侧深色导航（顶部品牌+登录人，中部导航项，底部退出登录），
 * 右侧 CardLayout 内容区。所有"登录后主界面"都用它做外壳，仅替换导航项与页面。
 *
 * 退出登录用 Runnable 注入：正式入口由 MainModule 传「回到登录窗」的动作，
 * 避免 Common 反向依赖各模块 / MainModule；独立调试入口不传则退出进程。
 * 只做呈现层，不含业务。
 */
public class HomeBaseFrame extends JFrame
{
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel navBox = new JPanel();
    private final List<JButton> navItems = new ArrayList<>();
    private final Runnable onLogout;
    private int selectedIndex = -1;

    private static final int SIDEBAR_W = 224;
    private static final int NAV_W = 196;
    private static final int NAV_H = 42;

    public HomeBaseFrame(String title, String userName, String roleText, Runnable onLogout)
    {
        super(title);
        this.onLogout = onLogout;
        UiTheme.install();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 560));

        // ---- 左侧深色栏 ----
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Ui.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sidebar.add(buildUserHeader(userName, roleText), BorderLayout.NORTH);
        sidebar.add(buildNavArea(), BorderLayout.CENTER);
        sidebar.add(buildLogout(), BorderLayout.SOUTH);

        // ---- 右侧浅灰内容区 ----
        content.setBackground(Ui.CONTENT_BG);

        Container root = getContentPane();
        root.setLayout(new BorderLayout());
        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        setSize(1040, 680);
        setLocationRelativeTo(null);
    }

    /** 供子类按顺序添加导航页；第一个添加的页默认显示 */
    protected final void addNav(String name, JComponent page)
    {
        String key = "page" + navItems.size();

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Ui.CONTENT_BG);
        wrap.add(page, BorderLayout.CENTER);
        content.add(wrap, key);

        NavItem b = new NavItem(name);
        b.addActionListener(e -> showPage(key, b));
        navBox.add(b);
        navBox.add(Box.createVerticalStrut(2));
        navItems.add(b);

        if (selectedIndex < 0)
        {
            selectedIndex = 0;
            b.setActive(true);
        }
    }

    private void showPage(String key, NavItem clicked)
    {
        for (JButton item : navItems)
        {
            if (item instanceof NavItem)
            {
                ((NavItem) item).setActive(item == clicked);
            }
        }
        cards.show(content, key);
    }

    private void logout()
    {
        dispose();
        if (onLogout != null)
        {
            onLogout.run();
        }
        else
        {
            // 独立调试入口没有上级登录窗，关窗即退出（与原先 EXIT_ON_CLOSE 一致）
            System.exit(0);
        }
    }

    // ---------------- 侧栏：顶部品牌 + 登录人 ----------------
    private JPanel buildUserHeader(String userName, String roleText)
    {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Ui.SIDEBAR_BG);
        top.setBorder(new EmptyBorder(18, 18, 14, 18));

        JLabel brand = new JLabel("健康体检管理系统");
        brand.setFont(Ui.bold(17));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(brand);
        top.add(Box.createVerticalStrut(6));

        JLabel sub = new JLabel("MEDICAL CHECKUP");
        sub.setFont(Ui.font(9));
        sub.setForeground(new Color(0x5E7EA0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(sub);
        top.add(Box.createVerticalStrut(16));

        // 分隔细线
        JPanel line = new JPanel();
        line.setBackground(new Color(0x3A4A66));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(1, 1));
        top.add(line);
        top.add(Box.createVerticalStrut(14));

        // 头像圆标 + 姓名/角色
        JPanel userRow = new JPanel();
        userRow.setLayout(new BoxLayout(userRow, BoxLayout.X_AXIS));
        userRow.setOpaque(false);
        userRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel avatar = new Avatar(userName);
        avatar.setAlignmentY(Component.CENTER_ALIGNMENT);
        userRow.add(avatar);
        userRow.add(Box.createHorizontalStrut(10));

        JPanel userText = new JPanel();
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));
        userText.setOpaque(false);
        userText.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(userName);
        name.setFont(Ui.bold(13));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        userText.add(name);

        JLabel role = new JLabel(roleText == null ? "" : roleText);
        role.setFont(Ui.font(11));
        role.setForeground(new Color(0x8FA5C2));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        userText.add(role);

        userRow.add(userText);
        top.add(userRow);
        return top;
    }

    // ---------------- 侧栏：导航项区 ----------------
    private JPanel buildNavArea()
    {
        navBox.setLayout(new BoxLayout(navBox, BoxLayout.Y_AXIS));
        navBox.setBackground(Ui.SIDEBAR_BG);
        navBox.setBorder(new EmptyBorder(4, 14, 4, 14));
        return navBox;
    }

    // ---------------- 侧栏：底部退出登录 ----------------
    private JPanel buildLogout()
    {
        JPanel foot = new JPanel();
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        foot.setBackground(Ui.SIDEBAR_BG);
        foot.setBorder(new EmptyBorder(4, 14, 14, 14));

        NavItem lo = new NavItem("退 出 登 录");
        lo.addActionListener(e -> logout());
        foot.add(lo);
        return foot;
    }

    // ---------------- 自绘导航按钮 ----------------
    private static final class NavItem extends JButton
    {
        private boolean active;

        NavItem(String text)
        {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setFont(Ui.font(14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 8));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Dimension d = new Dimension(NAV_W, NAV_H);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        void setActive(boolean active)
        {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean hover = getModel().isRollover();

            if (active)
            {
                g2.setColor(new Color(255, 255, 255, 0x12));
                g2.fillRoundRect(0, 0, w, h, 9, 9);
            }
            else if (hover)
            {
                g2.setColor(new Color(255, 255, 255, 0x08));
                g2.fillRoundRect(0, 0, w, h, 9, 9);
            }
            if (active)
            {
                g2.setColor(Ui.ACCENT);
                g2.fillRoundRect(0, (h - 22) / 2, 3, 22, 2, 2);
            }

            String t = getText();
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(active ? Color.WHITE : (hover ? Ui.NAV_HOVER : Ui.NAV_TEXT));
            if (t != null)
            {
                g2.drawString(t, getInsets().left, y);
            }
            g2.dispose();
        }
    }

    // ---------------- 侧栏：姓名首字圆形头像 ----------------
    private static final class Avatar extends JPanel
    {
        private final String ch;

        Avatar(String name)
        {
            String n = name == null ? "" : name.trim();
            ch = n.isEmpty() ? "?" : n.substring(0, 1);
            setOpaque(false);
            Dimension d = new Dimension(40, 40);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            g2.setColor(Ui.ACCENT);
            g2.fillOval(0, 0, s, s);
            g2.setColor(Color.WHITE);
            g2.setFont(Ui.bold(16));
            FontMetrics fm = g2.getFontMetrics();
            int x = (s - fm.stringWidth(ch)) / 2;
            int y = (s - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(ch, x, y);
            g2.dispose();
        }
    }
}
