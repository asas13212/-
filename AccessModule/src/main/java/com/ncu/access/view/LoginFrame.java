package com.ncu.access.view;

import com.ncu.access.model.LoginResult;
import com.ncu.access.service.LoginService;
import com.ncu.common.model.User;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/**
 * 统一登录窗口（Swing）。
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

        // ---- 表单区（手机号 / 密码）----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 5, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        form.add(new JLabel("手机号："), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        telField.setColumns(14);
        form.add(telField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        form.add(new JLabel("密  码："), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        pwdField.setColumns(14);
        form.add(pwdField, c);

        // ---- 按钮区 ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnPanel.add(loginBtn);
        btnPanel.add(resetBtn);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // ---- 事件 ----
        loginBtn.addActionListener(e -> doLogin());
        resetBtn.addActionListener(e -> reset());
        // 任意输入框内回车 = 点登录
        getRootPane().setDefaultButton(loginBtn);

        // ---- 窗口 ----
        pack();
        setLocationRelativeTo(null);
        telField.requestFocusInWindow();
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
}
