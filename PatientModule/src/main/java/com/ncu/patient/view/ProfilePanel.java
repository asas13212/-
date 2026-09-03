package com.ncu.patient.view;

import com.ncu.common.model.User;
import com.ncu.patient.controller.PatientController;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.EAST;

        addRow(c, 0, "手机号(账号)：", new JLabel(tel));
        addRow(c, 1, "姓名：", nameField);
        addRow(c, 2, "身份证：", idcardField);
        addRow(c, 3, "出生日期：", birthdayField);
        addRow(c, 4, "性别：", sexBox);

        JButton save = new JButton("保存");
        save.addActionListener(e -> save());
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        add(save, c);

        load();
    }

    private void addRow(GridBagConstraints c, int row, String label, java.awt.Component comp)
    {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        add(new JLabel(label), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        add(comp, c);
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
