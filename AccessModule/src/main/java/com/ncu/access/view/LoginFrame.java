package com.ncu.access.view;

import com.ncu.access.model.LoginResult;
import com.ncu.access.service.LoginService;
import com.ncu.common.model.User;

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
import java.util.function.Consumer;

/**
 * 统一登录窗口（Swing）。
 *
 * 界面：整窗铺一张登录背景图（1920x1080 模板，资源见
 * AccessModule/src/main/resources/login_background.png，设计源图在仓库根 Sources/ 下），
 * 中间叠一张半透明卡片，卡片内只放【手机号】【密码】两个输入框 + 登录/重置按钮。
 *
 * 登录成功后关闭本窗口，并把登录用户交给宿主 onLoginSuccess 回调，
 * 由宿主决定打开哪个角色模块 —— 从而 AccessModule 与
 * PatientModule / AdminModule / ReportModule 完全解耦，互不依赖。
 * 宿主用法：new LoginFrame(user -> { ... 按 user.getRole() 分流 ... });
 */
public class LoginFrame extends JFrame
{
    private final LoginService loginService = new LoginService();
    private final Consumer<User> onLoginSuccess;

    private final JTextField telField = new JTextField();
    private final JPasswordField pwdField = new JPasswordField();
    private final JButton loginBtn = new JButton("登  录");
    private final JButton resetBtn = new JButton("重  置");

    /** 窗口宽 = 背景图按 16:9 缩到 920 宽后的尺寸 */
    private static final int WINDOW_W = 920;
    private static final int WINDOW_H = (int) (WINDOW_W * 1080.0 / 1920.0); // 518

    public LoginFrame(Consumer<User> onLoginSuccess)
    {
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI()
    {
        setTitle("健康管理系统 · 登录");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // ---- 加载背景图（classpath 根下），失败则用纯色底，不影响使用 ----
        Image bgImage = null;
        URL imgUrl = LoginFrame.class.getResource("/login_background.png");
        if (imgUrl != null)
        {
            bgImage = new ImageIcon(imgUrl).getImage();
        }
        BackgroundPanel background = new BackgroundPanel(bgImage);
        background.setPreferredSize(new Dimension(WINDOW_W, WINDOW_H));
        setContentPane(background);

        // ---- 中部半透明登录卡片：只放 手机号 + 密码 ----
        // 手动定位：以“居中”为基准，整体向右 90px、向下 30px
        JPanel card = buildLoginCard();
        background.setLayout(null);
        background.add(card);
        Dimension cardSize = card.getPreferredSize();
        int cardX = (WINDOW_W - cardSize.width) / 2 + 90;
        int cardY = (WINDOW_H - cardSize.height) / 2 + 30;
        card.setBounds(cardX, cardY, cardSize.width, cardSize.height);

        // ---- 事件：回车=登录、关闭清空 ----
        getRootPane().setDefaultButton(loginBtn);

        // ---- 窗口 ----
        pack();
        setLocationRelativeTo(null);
        telField.requestFocusInWindow();
    }

    /** 构建白色半透明卡片：标题 + 手机号 + 密码 + 两个按钮 */
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

        // 标题
        JLabel title = new JLabel("健康管理系统");
        title.setFont(yaheiBold);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        card.add(title, c);

        // 手机号
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

        // 密码
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

        // 按钮行
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

        // 事件
        loginBtn.addActionListener(e -> doLogin());
        resetBtn.addActionListener(e -> reset());

        return card;
    }

    /** 清空输入 */
    private void reset()
    {
        telField.setText("");
        pwdField.setText("");
        telField.requestFocusInWindow();
    }

    /** 点击登录 / 回车触发 */
    private void doLogin()
    {
        String tel = telField.getText().trim();
        String pwd = new String(pwdField.getPassword());

        LoginResult result = loginService.login(tel, pwd);
        if (!result.isSuccess())
        {
            JOptionPane.showMessageDialog(this, result.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = result.getUser();
        dispose();
        if (onLoginSuccess != null)
        {
            onLoginSuccess.accept(user);
        }
    }

    /** 背景面板：把整张背景图拉伸铺满窗口（16:9，不裁剪）；图加载失败就显示默认浅色 */
    private static class BackgroundPanel extends JPanel
    {
        private final Image image;

        BackgroundPanel(Image image)
        {
            this.image = image;
            setOpaque(false);
            setBackground(new Color(230, 240, 250));
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            if (image == null)
            {
                super.paintComponent(g); // 无图时画默认背景色
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}
