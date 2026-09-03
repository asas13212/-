package com.ncu.patient.model;

import java.util.Date;

/**
 * 患者端检查结果 VO：结果 + 检查项名称/单位/参考范围（联表查询结果）
 */
public class ResultVO
{
    private int id;             // 结果id
    private int regId;          // 预约id
    private String cname;       // 检查项名称
    private String resultValue; // 结果值
    private String dw;          // 单位
    private String ckfw;        // 参考范围
    private Date checkTime;     // 检查时间

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getRegId()
    {
        return regId;
    }

    public void setRegId(int regId)
    {
        this.regId = regId;
    }

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
