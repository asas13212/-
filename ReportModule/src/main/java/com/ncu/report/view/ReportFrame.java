package com.ncu.report.view;

import com.ncu.common.ui.Ui;
import com.ncu.common.ui.UiTheme;
import com.ncu.report.controller.ReportController;
import com.ncu.report.model.ReportItem;
import com.ncu.report.model.ReportRegVO;
import com.ncu.report.model.ReportVO;
import com.ncu.report.util.PdfReportExporter;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 报告窗口：选择一个已完成预约 → 预览报告 → 打印/导出 PDF
 */
public class ReportFrame extends JFrame
{
    private final ReportController controller = new ReportController();
    private final String tel;
    private final JComboBox<String> regBox = new JComboBox<>();
    private final JTextArea headerArea = new JTextArea();
    private final DefaultTableModel tableModel;
    private final String[] columns = {"检查项目", "结果值", "单位", "参考范围", "检查时间"};
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private List<ReportRegVO> regs;
    private ReportVO currentReport;

    public ReportFrame(String tel)
    {
        this.tel = tel;
        UiTheme.install();
        setTitle("健康体检管理系统 - 体检报告");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Ui.CONTENT_BG);
        page.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        setContentPane(page);

        // 页头：左侧选择已完成预约，右侧动作
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Ui.CONTENT_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        west.setOpaque(false);
        JLabel sel = new JLabel("选择已完成预约:");
        sel.setFont(Ui.font(13));
        west.add(sel);
        regBox.setPreferredSize(new Dimension(330, 28));
        west.add(regBox);
        bar.add(west, BorderLayout.WEST);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);
        east.add(Ui.actionPrimary("生成报告", e -> generate()));
        east.add(Ui.action("打印", e -> print()));
        east.add(Ui.action("导出PDF文件", e -> exportPdf()));
        bar.add(east, BorderLayout.EAST);
        page.add(bar, BorderLayout.NORTH);

        // 白色"报告纸"：上部患者信息，中部结果表格
        headerArea.setEditable(false);
        headerArea.setOpaque(false);
        headerArea.setFont(Ui.font(14));
        headerArea.setForeground(Ui.TEXT);

        JPanel headerPad = new JPanel(new BorderLayout());
        headerPad.setBackground(Color.WHITE);
        headerPad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(14, 20, 10, 20),
                new MatteBorder(0, 0, 1, 0, Ui.BORDER)));
        headerPad.add(headerArea, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        JTable table = Ui.table(tableModel);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);

        JPanel sheet = new JPanel(new BorderLayout());
        sheet.setBackground(Color.WHITE);
        sheet.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        sheet.add(headerPad, BorderLayout.NORTH);
        sheet.add(sp, BorderLayout.CENTER);
        page.add(sheet, BorderLayout.CENTER);

        setSize(860, 600);
        setLocationRelativeTo(null);

        loadRegs();
    }

    private void loadRegs()
    {
        regBox.removeAllItems();
        regs = controller.listCompletedRegs(tel);
        for (ReportRegVO r : regs)
        {
            regBox.addItem("#" + r.getId() + " " + r.getGroupName() + " (" + TIME_FMT.format(r.getRegTime()) + ")");
        }
        if (regs.isEmpty())
        {
            headerArea.setText("暂无已完成的预约，无法生成报告。\n（需医生完成结果录入并把预约标记为「已完成」后，这里才会出现可选报告。）");
        }
    }

    private void generate()
    {
        int idx = regBox.getSelectedIndex();
        if (idx < 0 || regs == null || regs.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "没有可生成报告的预约");
            return;
        }
        ReportRegVO reg = regs.get(idx);
        ReportVO report = controller.buildReport(reg.getId());
        if (report == null)
        {
            JOptionPane.showMessageDialog(this, "报告生成失败");
            return;
        }
        currentReport = report;

        StringBuilder sb = new StringBuilder();
        sb.append("姓名：").append(nvl(report.getPatientName())).append("    ");
        sb.append("性别：").append(nvl(report.getSex())).append("    ");
        sb.append("出生日期：").append(report.getBirthday() == null ? "" : TIME_FMT.format(report.getBirthday())).append("\n");
        sb.append("体检套餐：").append(nvl(report.getGroupName())).append("    ");
        sb.append("预约时间：").append(report.getRegTime() == null ? "" : TIME_FMT.format(report.getRegTime())).append("\n");
        sb.append("体检医生：").append(nvl(report.getDoctorName()));
        headerArea.setText(sb.toString());

        tableModel.setRowCount(0);
        if (report.getItems() != null)
        {
            for (ReportItem it : report.getItems())
            {
                tableModel.addRow(new Object[]{nvl(it.getCname()), nvl(it.getResultValue()),
                        nvl(it.getDw()), nvl(it.getCkfw()),
                        it.getCheckTime() == null ? "" : TIME_FMT.format(it.getCheckTime())});
            }
        }
    }

    private void print()
    {
        if (currentReport == null)
        {
            JOptionPane.showMessageDialog(this, "请先生成报告");
            return;
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("体检报告");
        job.setPrintable(new ReportPrintable(currentReport));
        if (job.printDialog())
        {
            try
            {
                job.print();
            }
            catch (PrinterException e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "打印失败：" + e.getMessage());
            }
        }
    }

    private void exportPdf()
    {
        if (currentReport == null)
        {
            JOptionPane.showMessageDialog(this, "请先生成报告");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("体检报告_" + nvl(currentReport.getPatientName()) + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        String path = fc.getSelectedFile().getPath();
        if (!path.toLowerCase().endsWith(".pdf"))
        {
            path += ".pdf";
        }
        try
        {
            PdfReportExporter.export(currentReport, path);
            JOptionPane.showMessageDialog(this, "导出成功：" + path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage());
        }
    }

    private String nvl(String s)
    {
        return s == null ? "" : s;
    }
}
