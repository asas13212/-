package com.ncu.report.view;

import com.ncu.report.controller.ReportController;
import com.ncu.report.model.ReportItem;
import com.ncu.report.model.ReportRegVO;
import com.ncu.report.model.ReportVO;
import com.ncu.report.util.PdfReportExporter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
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
        setTitle("健康体检管理系统 - 体检报告");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("选择已完成预约:"));
        top.add(regBox);
        JButton genBtn = new JButton("生成报告");
        genBtn.addActionListener(e -> generate());
        top.add(genBtn);
        JButton printBtn = new JButton("打印");
        printBtn.addActionListener(e -> print());
        top.add(printBtn);
        JButton exportBtn = new JButton("导出PDF文件");
        exportBtn.addActionListener(e -> exportPdf());
        top.add(exportBtn);
        add(top, BorderLayout.NORTH);

        headerArea.setEditable(false);
        headerArea.setOpaque(false);
        headerArea.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 14));

        tableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        JTable table = new JTable(tableModel);

        JPanel center = new JPanel(new BorderLayout());
        center.add(headerArea, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        setSize(760, 560);
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
