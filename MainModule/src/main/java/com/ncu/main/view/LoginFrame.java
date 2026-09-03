package com.ncu.main.view;

import com.ncu.admin.view.AdminFrame;
import com.ncu.main.controller.MainController;
import com.ncu.main.model.CurrentUser;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * 登录窗口
 */
public class LoginFrame extends JFrame
{
    private final MainController controller = new MainController();
    private final JTextField telField = new JTextField(15);
    private final JPasswordField pwdField = new JPasswordField(15);

    public LoginFrame()
    {
        setTitle("健康体检管理系统 - 登录");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.EAST;

        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("手机号:"), g);
        g.gridx = 1;
        form.add(telField, g);
        g.gridx = 0;
        g.gridy = 1;
        form.add(new JLabel("密码:"), g);
        g.gridx = 1;
        form.add(pwdField, g);

        JButton loginBtn = new JButton("登录");
        loginBtn.addActionListener(e -> onLogin());
        g.gridx = 1;
        g.gridy = 2;
        g.anchor = GridBagConstraints.CENTER;
        form.add(loginBtn, g);

        add(form, BorderLayout.CENTER);
        setSize(360, 220);
        setLocationRelativeTo(null);
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
}
