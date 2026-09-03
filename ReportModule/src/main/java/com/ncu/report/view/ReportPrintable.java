package com.ncu.report.view;

import com.ncu.report.model.ReportItem;
import com.ncu.report.model.ReportVO;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 体检报告打印实现：把报告内容绘制到打印页（配合 Windows「Microsoft Print to PDF」可导出 PDF）
 */
public class ReportPrintable implements Printable
{
    private final ReportVO report;
    private static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 20);
    private static final Font INFO_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    private static final Font TABLE_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final String[] COLUMNS = {"检查项目", "结果值", "单位", "参考范围", "检查时间"};
    private static final double[] COL_WIDTH = {0.24, 0.18, 0.10, 0.28, 0.20};

    public ReportPrintable(ReportVO report)
    {
        this.report = report;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
    {
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(Color.BLACK);

        int startX = (int) pageFormat.getImageableX();
        int startY = (int) pageFormat.getImageableY();
        int width = (int) pageFormat.getImageableWidth();
        int height = (int) pageFormat.getImageableHeight();

        int margin = 24;
        int x = startX + margin;
        int tableW = width - margin * 2;
        int lineH = 20;
        int titleH = 34;

        List<ReportItem> items = report.getItems();
        int totalItems = items == null ? 0 : items.size();

        // 计算每页能放多少行明细
        int infoLineCount = 6;
        int headerH = titleH + infoLineCount * lineH + 10; // 首页标题 + 信息 + 空行
        int usableH = height - margin * 2;
        int firstPageRows = Math.max(1, (usableH - headerH - lineH) / lineH);   // 首页(减去表头)
        int otherPageRows = Math.max(1, (usableH - lineH) / lineH);             // 后续页(只有表头)

        int totalPages;
        if (totalItems <= firstPageRows)
        {
            totalPages = 1;
        }
        else
        {
            totalPages = 1 + (int) Math.ceil((totalItems - firstPageRows) * 1.0 / otherPageRows);
        }
        if (pageIndex < 0 || pageIndex >= totalPages)
        {
            return NO_SUCH_PAGE;
        }

        // 当前页的明细起止下标
        int from, to;
        if (pageIndex == 0)
        {
            from = 0;
            to = Math.min(firstPageRows, totalItems);
        }
        else
        {
            from = firstPageRows + (pageIndex - 1) * otherPageRows;
            to = Math.min(from + otherPageRows, totalItems);
        }

        int y = startY + margin;

        // 首页画标题与信息
        if (pageIndex == 0)
        {
            g2.setFont(TITLE_FONT);
            FontMetrics fm = g2.getFontMetrics();
            String title = "体 检 报 告";
            g2.drawString(title, startX + (width - fm.stringWidth(title)) / 2, y);
            y += titleH;

            g2.setFont(INFO_FONT);
            String[] info = {
                    "姓名：" + nvl(report.getPatientName()),
                    "性别：" + nvl(report.getSex()),
                    "出生日期：" + (report.getBirthday() == null ? "" : DATE_FMT.format(report.getBirthday())),
                    "体检套餐：" + nvl(report.getGroupName()),
                    "预约时间：" + (report.getRegTime() == null ? "" : TIME_FMT.format(report.getRegTime())),
                    "体检医生：" + nvl(report.getDoctorName())
            };
            for (String line : info)
            {
                g2.drawString(line, x, y);
                y += lineH;
            }
            y += 10;
        }

        // 表头
        g2.setFont(TABLE_FONT);
        int[] colX = new int[COLUMNS.length];
        int cx = x;
        for (int i = 0; i < COLUMNS.length; i++)
        {
            colX[i] = cx;
            g2.drawString(COLUMNS[i], cx, y);
            cx += (int) (tableW * COL_WIDTH[i]);
        }
        y += lineH;
        g2.drawLine(x, y - 4, x + tableW, y - 4);

        // 明细行
        for (int i = from; i < to; i++)
        {
            ReportItem it = items.get(i);
            String[] cells = {
                    nvl(it.getCname()),
                    nvl(it.getResultValue()),
                    nvl(it.getDw()),
                    nvl(it.getCkfw()),
                    it.getCheckTime() == null ? "" : TIME_FMT.format(it.getCheckTime())
            };
            for (int c = 0; c < cells.length; c++)
            {
                g2.drawString(cells[c], colX[c], y);
            }
            y += lineH;
        }

        // 页脚：打印时间
        g2.setFont(INFO_FONT);
        String footer = "报告打印时间：" + TIME_FMT.format(new Date());
        g2.drawString(footer, x, startY + height - margin);

        return PAGE_EXISTS;
    }

    private String nvl(String s)
    {
        return s == null ? "" : s;
    }
}
