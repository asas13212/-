package com.ncu.patient.model;

import java.util.Date;

/**
 * 健康趋势原始数据项：一次检查结果 + 检查项名称/单位/参考范围（用于折线图）
 */
public class TrendItem
{
    private String cid;          // 检查项id
    private String cname;        // 检查项名称
    private String dw;           // 单位
    private String ckfw;         // 参考范围（如 90~139，用于标准线）
    private String resultValue;  // 结果值（字符串，可能为数值）
    private Date checkTime;      // 检查时间

    public String getCid()
    {
        return cid;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
    }

    public String getCname()
    {
        return cname;
    }

    public void setCname(String cname)
    {
        this.cname = cname;
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

    public String getResultValue()
    {
        return resultValue;
    }

    public void setResultValue(String resultValue)
    {
        this.resultValue = resultValue;
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
