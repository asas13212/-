package com.ncu.report.model;

import java.util.Date;

/**
 * 报告明细项：单个检查项的结果 + 单位/参考范围
 */
public class ReportItem
{
    private String cname;       // 检查项名称
    private String resultValue; // 结果值
    private String dw;          // 单位
    private String ckfw;        // 参考范围
    private Date checkTime;     // 检查时间

    public String getCname()
    {
        return cname;
    }

    public void setCname(String cname)
    {
        this.cname = cname;
    }

    public String getResultValue()
    {
        return resultValue;
    }

    public void setResultValue(String resultValue)
    {
        this.resultValue = resultValue;
    }

    public String getDw()
    {
        return dw;
    }

    public void setDw(String dw)
    {
        this.dw = dw;
    }

    public String getCkfw()
    {
        return ckfw;
    }

    public void setCkfw(String ckfw)
    {
        this.ckfw = ckfw;
    }

    public Date getCheckTime()
    {
        return checkTime;
    }

    public void setCheckTime(Date checkTime)
    {
        this.checkTime = checkTime;
    }
}
