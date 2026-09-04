package com.ncu.main.view;

import com.ncu.main.controller.MainController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/**
 * 患者自助注册弹窗（登录窗点「注册」打开）。
 * <p>
 * 只收精简字段：账号(手机号) + 姓名 + 密码 + 确认密码；
 * 注册固定建 role=0 患者账号（医生账号不开放自助注册），
 * 其余资料（身份证/性别/生日）留给患者登录后在「个人资料」里补。
 * 注册成功后把账号回填到登录框（onRegistered 回调）。
 */
public class RegisterDialog extends JDialog
{
    private final MainController controller = new MainController();
    private final JTextField telField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JPasswordField pwdField = new JPasswordField();
    private final JPasswordField pwd2Field = new JPasswordField();
    private final Consumer<String> onRegistered;

    public RegisterDialog(Frame owner, Consumer<String> onRegistered)
    {
        super(owner, "注册患者账号", true); // 模态：关掉才能回登录
        this.onRegistered = onRegistered;
        initUI();
    }

    private void initUI()
    {
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Font yahei = new Font("Microsoft YaHei", Font.PLAIN, 14);
        Font yaheiBold = new Font("Microsoft YaHei", Font.BOLD, 17);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 18, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);

        JLabel title = new JLabel("注册患者账号");
        title.setFont(yaheiBold);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        root.add(title, c);

        int row = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        addFieldRow(root, c, row++, "账  号：", telField, yahei);
        addFieldRow(root, c, row++, "姓  名：", nameField, yahei);
        addFieldRow(root, c, row++, "密  码：", pwdField, yahei);
        addFieldRow(root, c, row++, "确认密码：", pwd2Field, yahei);

        JLabel tip = new JLabel("密码至少 6 位；注册后请用该账号登录患者端");
        tip.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        c.gridx = 0;
        c.gridy = row++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 8, 6, 8);
        root.add(tip, c);

        JButton okBtn = new JButton("注  册");
        okBtn.setFont(yahei);
        JButton cancelBtn = new JButton("取  消");
        cancelBtn.setFont(yahei);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btns.setOpaque(false);
        btns.add(okBtn);
        btns.add(cancelBtn);
        c.gridy = row;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(6, 8, 0, 8);
        root.add(btns, c);

        okBtn.addActionListener(e -> doRegister());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okBtn);
        setContentPane(root);

        pack();
        setLocationRelativeTo(getOwner());
        telField.requestFocusInWindow();
    }

    private void addFieldRow(JPanel root, GridBagConstraints c, int row,
            String label, JTextField field, Font font)
    {
        field.setFont(font);
        field.setColumns(16);
        JLabel lb = new JLabel(label);
        lb.setFont(font);
        c.insets = new Insets(5, 8, 5, 8);
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        root.add(lb, c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        root.add(field, c);
    }

    private void doRegister()
    {
        String tel = telField.getText().trim();
        String name = nameField.getText().trim();
        String pwd = new String(pwdField.getPassword());
        String pwd2 = new String(pwd2Field.getPassword());

        if (tel.isEmpty() || name.isEmpty() || pwd.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "请把账号、姓名、密码填写完整");
            return;
        }
        if (!pwd.equals(pwd2))
        {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致");
            pwd2Field.setText("");
            pwd2Field.requestFocusInWindow();
            return;
        }
        String err = controller.register(tel, name, pwd);
        if (err != null)
        {
            JOptionPane.showMessageDialog(this, err);
            return;
        }
        JOptionPane.showMessageDialog(this, "注册成功！请用新账号「" + tel + "」登录");
        if (onRegistered != null)
        {
            onRegistered.accept(tel); // 回填到登录框账号
        }
        dispose();
    }
}
