package com.ncu.common.ui;

import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Font;

/**
 * 全局 UI 主题：一次调用把整个应用的字号、面板底色、选中色等统一切到"医疗蓝+青绿"。
 *
 * 通过 UIManager 下发到 Swing 默认组件，因此即使某些面板没有逐个用 Ui 工具类，
 * 只要主题安装过，观感也基本统一。各入口（登录窗 / 各模块独立 main / 侧边栏主窗）
 * 都调一次 install()，方法幂等，重复调用无副作用。
 */
public final class UiTheme
{
    private UiTheme() {}

    private static volatile boolean installed;

    public static synchronized void install()
    {
        if (installed)
        {
            return;
        }
        installed = true;

        Font f13 = Ui.font(13);
        Font f14 = Ui.font(14);

        // ---- 全局默认字号：中文统一用微软雅黑 ----
        UIManager.put("defaultFont", f14);
        UIManager.put("Label.font", f14);
        UIManager.put("Button.font", f14);
        UIManager.put("CheckBox.font", f14);
        UIManager.put("RadioButton.font", f14);
        UIManager.put("ToggleButton.font", f14);
        UIManager.put("ComboBox.font", f14);
        UIManager.put("TextField.font", f14);
        UIManager.put("PasswordField.font", f14);
        UIManager.put("TextArea.font", f13);
        UIManager.put("EditorPane.font", f13);
        UIManager.put("Spinner.font", f14);
        UIManager.put("List.font", f13);
        UIManager.put("Table.font", f13);
        UIManager.put("TableHeader.font", Ui.bold(13));
        UIManager.put("Tree.font", f13);
        UIManager.put("ToolTip.font", f13);
        UIManager.put("TabbedPane.font", f13);
        UIManager.put("MenuItem.font", f14);
        UIManager.put("OptionPane.messageFont", f14);
        UIManager.put("OptionPane.buttonFont", f14);

        // ---- 文字与底色：面板统一浅灰内容色 ----
        ColorUIResource content = new ColorUIResource(Ui.CONTENT_BG);
        UIManager.put("Panel.background", content);
        UIManager.put("Panel.foreground", new ColorUIResource(Ui.TEXT));
        UIManager.put("Label.foreground", new ColorUIResource(Ui.TEXT));
        UIManager.put("TabbedPane.background", content);
        UIManager.put("TabbedPane.contentAreaColor", content);
        UIManager.put("ScrollPane.background", ColorUIResource.WHITE);
        UIManager.put("Viewport.background", ColorUIResource.WHITE);
        UIManager.put("ScrollBar.background", content);

        // ---- 输入框：白底 ----
        ColorUIResource white = new ColorUIResource(Color.WHITE);
        UIManager.put("TextField.background", white);
        UIManager.put("PasswordField.background", white);
        UIManager.put("TextArea.background", white);
        UIManager.put("EditorPane.background", white);
        UIManager.put("ComboBox.background", white);
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(Ui.SELECT_BG));
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Ui.SELECT_FG));
        UIManager.put("List.background", white);
        UIManager.put("List.selectionBackground", new ColorUIResource(Ui.SELECT_BG));
        UIManager.put("List.selectionForeground", new ColorUIResource(Ui.SELECT_FG));
        UIManager.put("List.focusCellHighlightBorder", new LineBorder(new ColorUIResource(Ui.BORDER)));
        UIManager.put("Table.background", white);
        UIManager.put("Table.selectionBackground", new ColorUIResource(Ui.SELECT_BG));
        UIManager.put("Table.selectionForeground", new ColorUIResource(Ui.SELECT_FG));
        UIManager.put("Table.gridColor", new ColorUIResource(Ui.GRID));
        UIManager.put("TableHeader.background", new ColorUIResource(Ui.TABLE_HEAD));
        UIManager.put("TableHeader.foreground", new ColorUIResource(Ui.TABLE_HEAD_TEXT));

        // ---- 弹窗（DialogUtil / JOptionPane）----
        UIManager.put("OptionPane.background", white);
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(Ui.TEXT));
        UIManager.put("ColorChooser.background", content);
    }
}
