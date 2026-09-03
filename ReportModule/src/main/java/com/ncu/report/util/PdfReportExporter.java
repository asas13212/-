package com.ncu.report.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ncu.report.model.ReportItem;
import com.ncu.report.model.ReportVO;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 体检报告 PDF 导出（OpenPDF）。
 * 中文使用内置 STSong-Light + UniGB-UCS2-H，无需额外字体文件。
 */
public class PdfReportExporter
{
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static void export(ReportVO report, String path) throws Exception
    {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(path));
        doc.open();

        BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(bf, 20, Font.BOLD);
        Font infoFont = new Font(bf, 12);
        Font headFont = new Font(bf, 11, Font.BOLD);
        Font tableFont = new Font(bf, 11);

        // 标题
        Paragraph title = new Paragraph("体 检 报 告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        doc.add(title);

        // 基本信息
        doc.add(new Paragraph("姓名：" + nvl(report.getPatientName()) +
                "    性别：" + nvl(report.getSex()) +
                "    出生日期：" + (report.getBirthday() == null ? "" : DATE_FMT.format(report.getBirthday())), infoFont));
        doc.add(new Paragraph("体检套餐：" + nvl(report.getGroupName()) +
                "    预约时间：" + (report.getRegTime() == null ? "" : TIME_FMT.format(report.getRegTime())), infoFont));
        doc.add(new Paragraph("体检医生：" + nvl(report.getDoctorName()), infoFont));
        doc.add(new Paragraph(" "));

        // 明细表
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{24f, 18f, 10f, 28f, 20f});
        String[] heads = {"检查项目", "结果值", "单位", "参考范围", "检查时间"};
        for (String h : heads)
        {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        if (report.getItems() != null)
        {
            for (ReportItem it : report.getItems())
            {
                table.addCell(new Phrase(nvl(it.getCname()), tableFont));
                table.addCell(new Phrase(nvl(it.getResultValue()), tableFont));
                table.addCell(new Phrase(nvl(it.getDw()), tableFont));
                table.addCell(new Phrase(nvl(it.getCkfw()), tableFont));
                table.addCell(new Phrase(it.getCheckTime() == null ? "" : TIME_FMT.format(it.getCheckTime()), tableFont));
            }
        }
        doc.add(table);

        // 页脚
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("报告生成时间：" + TIME_FMT.format(new Date()), infoFont));

        doc.close();
    }

    private static String nvl(String s)
    {
        return s == null ? "" : s;
    }
}
