package com.ncu.report.model;

import java.util.Date;

/**
 * 报告模块：可选预约列表项 VO（预约 + 套餐名）
 */
public class ReportRegVO
{
    private int id;           // 预约id
    private String groupName; // 套餐名称
    private Date regTime;     // 预约时间
    private int status;       // 状态:0已预约|1已完成|2已取消

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public Date getRegTime()
    {
        return regTime;
    }

    public void setRegTime(Date regTime)
    {
        this.regTime = regTime;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }
}
