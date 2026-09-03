package com.ncu.main.view;

import com.ncu.admin.view.AdminFrame;
import com.ncu.main.controller.MainController;
import com.ncu.main.model.CurrentUser;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.URL;

/**
 * 系统统一登录窗口（Swing）。
 *
 * 整窗铺登录背景图（资源 MainModule/src/main/resources/login_background.png，
 * 设计源图在仓库根 Sources/login-bg-source.png），中间白色半透明卡片只放
 * 手机号 + 密码。登录成功后按 role 打开对应角色模块的主界面。
 * 注：原 AccessModule 登录已并入本类，作为整个系统的唯一登录入口。
 */
public class LoginFrame extends JFrame
{
    private final MainController controller = new MainController();
    private final JTextField telField = new JTextField();
    private final JPasswordField pwdField = new JPasswordField();
    private final JButton loginBtn = new JButton("登  录");
    private final JButton resetBtn = new JButton("重  置");

    /** 窗口宽 = 背景图按 16:9 缩到 920 宽后的尺寸 */
    private static final int WINDOW_W = 920;
    private static final int WINDOW_H = (int) (WINDOW_W * 1080.0 / 1920.0); // 518

    public LoginFrame()
    {
        initUI();
    }

    private void initUI()
    {
        setTitle("健康体检管理系统 - 登录");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // ---- 背景图（classpath 根下）；缺失则纯色底，不影响使用 ----
        Image bgImage = null;
        URL imgUrl = LoginFrame.class.getResource("/login_background.png");
        if (imgUrl != null)
        {
            bgImage = new ImageIcon(imgUrl).getImage();
        }
        BackgroundPanel background = new BackgroundPanel(bgImage);
        background.setPreferredSize(new Dimension(WINDOW_W, WINDOW_H));
        setContentPane(background);

        // ---- 中部半透明登录卡片：手动定位，居中整体向右 90px / 向下 30px ----
        JPanel card = buildLoginCard();
        background.setLayout(null);
        background.add(card);
        Dimension cardSize = card.getPreferredSize();
        int cardX = (WINDOW_W - cardSize.width) / 2 + 90;
        int cardY = (WINDOW_H - cardSize.height) / 2 + 30;
        card.setBounds(cardX, cardY, cardSize.width, cardSize.height);

        getRootPane().setDefaultButton(loginBtn);

        pack();
        setLocationRelativeTo(null);
        telField.requestFocusInWindow();
    }

    /** 白色半透明卡片：标题 + 手机号 + 密码 + 两个按钮 */
    private JPanel buildLoginCard()
    {
        Font yahei = new Font("Microsoft YaHei", Font.PLAIN, 16);
        Font yaheiBold = new Font("Microsoft YaHei", Font.BOLD, 24);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255, 255, 255, 225));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 140)),
                BorderFactory.createEmptyBorder(26, 36, 22, 36)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);

        JLabel title = new JLabel("健康管理系统");
        title.setFont(yaheiBold);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        card.add(title, c);

        JLabel telLabel = new JLabel("手机号：");
        telLabel.setFont(yahei);
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        card.add(telLabel, c);

        telField.setFont(yahei);
        telField.setColumns(14);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        card.add(telField, c);

        JLabel pwdLabel = new JLabel("密  码：");
        pwdLabel.setFont(yahei);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        card.add(pwdLabel, c);

        pwdField.setFont(yahei);
        pwdField.setColumns(14);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        card.add(pwdField, c);

        JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 18, 0));
        btnPanel.setOpaque(false);
        loginBtn.setFont(yahei);
        resetBtn.setFont(yahei);
        btnPanel.add(loginBtn);
        btnPanel.add(resetBtn);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        card.add(btnPanel, c);

        loginBtn.addActionListener(e -> onLogin());
        resetBtn.addActionListener(e -> reset());

        return card;
    }

    private void reset()
    {
        telField.setText("");
        pwdField.setText("");
        telField.requestFocusInWindow();
    }

    private void onLogin()
    {
        String tel = telField.getText().trim();
        String pwd = new String(pwdField.getPassword());
        if (tel.isEmpty() || pwd.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "手机号和密码不能为空");
            return;
        }
        CurrentUser u = controller.login(tel, pwd);
        if (u == null)
        {
            JOptionPane.showMessageDialog(this, "手机号或密码错误");
            return;
        }
        switch (u.getRole())
        {
            case 0:
                JOptionPane.showMessageDialog(this, "【患者端】暂未接入（由其他同学负责开发）。");
                break;
            case 1:
                JOptionPane.showMessageDialog(this, "【医生端】暂未接入（由其他同学负责开发）。");
                break;
            case 2:
                dispose();
                new AdminFrame(u.getName()).setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "未知角色，无法登录。");
        }
    }

    /** 背景面板：把背景图拉伸铺满窗口；无图时显示默认浅色 */
    private static class BackgroundPanel extends JPanel
    {
        private final Image image;

        BackgroundPanel(Image image)
        {
            this.image = image;
            setBackground(new Color(230, 240, 250));
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            if (image == null)
            {
                super.paintComponent(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}
