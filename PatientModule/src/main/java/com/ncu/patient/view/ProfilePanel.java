package com.ncu.patient.view;

import com.ncu.common.model.User;
import com.ncu.common.ui.Ui;
import com.ncu.patient.controller.PatientController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;

/**
 * 个人资料面板：查看/修改本人基本信息（密码与角色由系统管理，此处不修改）
 */
public class ProfilePanel extends JPanel
{
    private final PatientController controller = new PatientController();
    private final String tel;
    private final JTextField nameField = new JTextField(15);
    private final JTextField idcardField = new JTextField(15);
    private final JTextField birthdayField = new JTextField(15);
    private final JComboBox<String> sexBox = new JComboBox<>(new String[]{"男", "女"});
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    public ProfilePanel(String tel)
    {
        this.tel = tel;
        setLayout(new BorderLayout());
        Ui.content(this);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        add(Ui.header("个人资料"), BorderLayout.NORTH);

        // 表单放进白色卡片，整体在上方居中
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Ui.CARD_BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 18, 30));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(9, 8, 9, 8);
        addRow(form, c, 0, "账号(手机号)：", new JLabel(tel));
        addRow(form, c, 1, "姓名：", nameField);
        addRow(form, c, 2, "身份证：", idcardField);
        addRow(form, c, 3, "出生日期：", birthdayField);
        addRow(form, c, 4, "性别：", sexBox);

        JButton save = Ui.primary("保  存");
        save.addActionListener(e -> save());
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        form.add(save, c);

        JPanel holder = new JPanel(new GridBagLayout());
        holder.setBackground(Ui.CONTENT_BG);
        GridBagConstraints hc = new GridBagConstraints();
        hc.gridx = 0;
        hc.gridy = 0;
        hc.weightx = 1;
        hc.weighty = 1;
        hc.anchor = GridBagConstraints.NORTH;
        holder.add(Ui.card(form), hc);
        add(holder, BorderLayout.CENTER);

        load();
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component comp)
    {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        panel.add(comp, c);
    }

    private void load()
    {
        User u = controller.getProfile(tel);
        if (u == null) return;
        nameField.setText(u.getName() == null ? "" : u.getName());
        idcardField.setText(u.getIdcard() == null ? "" : u.getIdcard());
        birthdayField.setText(u.getBirthday() == null ? "" : DATE_FMT.format(u.getBirthday()));
        if ("女".equals(u.getSex()))
        {
            sexBox.setSelectedIndex(1);
        }
        else
        {
            sexBox.setSelectedIndex(0);
        }
    }

    private void save()
    {
        String name = nameField.getText().trim();
        if (name.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "姓名不能为空");
            return;
        }
        User u = controller.getProfile(tel);
        if (u == null)
        {
            JOptionPane.showMessageDialog(this, "找不到当前用户");
            return;
        }
        u.setName(name);
        u.setIdcard(idcardField.getText().trim());
        u.setSex((String) sexBox.getSelectedItem());

        String birthdayStr = birthdayField.getText().trim();
        if (birthdayStr.isEmpty())
        {
            u.setBirthday(null);
        }
        else
        {
            try
            {
                u.setBirthday(DATE_FMT.parse(birthdayStr));
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "出生日期格式应为 yyyy-MM-dd");
                return;
            }
        }

        if (controller.updateProfile(u))
        {
            JOptionPane.showMessageDialog(this, "保存成功");
        }
        else
        {
            JOptionPane.showMessageDialog(this, "保存失败");
        }
    }
}
