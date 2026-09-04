package com.ncu.admin.model;

import java.util.Date;

/**
 * 日历预约 VO：某天的一条预约 + 患者姓名 + 套餐名称 + 检查项目名（顿号分隔）。
 * 供医生端「预约日历」按天展示当天预约的具体项目与时间。
 */
public class CalendarAppointment
{
    private int id;             // 预约id
    private String tel;         // 患者账号
    private String patientName; // 患者姓名
    private String groupName;   // 套餐名称
    private Date regTime;       // 预约时间
    private int status;         // 状态:0已预约|1已完成|2已取消
    private String items;       // 检查项目名称（顿号分隔）

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
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

    public String getItems()
    {
        return items;
    }

    public void setItems(String items)
    {
        this.items = items;
    }
}
