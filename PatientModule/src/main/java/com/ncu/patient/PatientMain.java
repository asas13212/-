package com.ncu.patient;

import com.ncu.patient.view.PatientHomeFrame;

import javax.swing.SwingUtilities;

/**
 * 患者模块入口（可独立运行调试；正式入口由 MainModule 登录后打开 PatientHomeFrame）
 */
public class PatientMain
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new PatientHomeFrame("13700137000", "患者小王").setVisible(true));
    }
}
