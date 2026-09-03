package com.ncu.access;

import com.ncu.access.model.Role;
import com.ncu.access.view.LoginFrame;
import com.ncu.common.model.User;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * AccessModule 独立运行入口（演示用）：
 * 弹出登录窗 -> 登录成功按角色弹欢迎框。
 *
 * 队友接入方式（例：PatientModule 自己的 main 里）：
 *   SwingUtilities.invokeLater(() -> {
 *       new LoginFrame(user -> {
 *           if (user.getRole() != Role.PATIENT) {
 *               JOptionPane.showMessageDialog(null, "请使用患者账号登录本模块");
 *               return; // 结束，可再次弹出登录
 *           }
 *           new 你模块的主界面(user).setVisible(true); // 打开自己模块
 *       }).setVisible(true);
 *   });
 * 详见根目录 dev.md §4/§8。
 */
public class AccessApp
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception e)
            {
                // 系统外观不可用时用默认外观即可
            }
            new LoginFrame(AccessApp::handleLoginSuccess).setVisible(true);
        });
    }

    /** 登录成功后的回调：这里仅演示“按角色分流到对应模块”，真实分流由各模块自行提供 */
    private static void handleLoginSuccess(User user)
    {
        String message = "欢迎，" + user.getName() + "（" + Role.name(user.getRole()) + "）\n\n"
                       + "您已通过统一登录。\n"
                       + "【" + Role.name(user.getRole()) + "端】功能由对应队友模块实现，"
                       + "接入方式见 AccessApp 注释与 dev.md §4/§8。";
        JOptionPane.showMessageDialog(null, message, "登录成功", JOptionPane.INFORMATION_MESSAGE);
    }
}
