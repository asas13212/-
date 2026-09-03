package com.ncu.report.controller;

import com.ncu.report.dao.ReportDao;
import com.ncu.report.model.ReportRegVO;
import com.ncu.report.model.ReportVO;

import java.util.List;

/**
 * 报告业务控制层：接收 view 参数，调用 dao 汇总生成报告
 */
public class ReportController
{
    private final ReportDao dao = new ReportDao();

    /** 某患者所有已完成预约（可出报告） */
    public List<ReportRegVO> listCompletedRegs(String tel)
    {
        return dao.findCompletedRegs(tel);
    }

    /** 按预约id生成报告 */
    public ReportVO buildReport(int regId)
    {
        return dao.buildReport(regId);
    }
}
