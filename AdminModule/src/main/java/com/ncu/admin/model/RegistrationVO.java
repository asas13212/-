package com.ncu.admin.model;

import java.util.Date;

/**
 * 预约详情 VO：预约 + 患者姓名 + 套餐名称（联表查询结果）
 */
public class RegistrationVO
{
    private int id;             // 预约id
    private String tel;         // 患者账号
    private String patientName; // 患者姓名
    private String gid;         // 套餐id
    private String groupName;   // 套餐名称
    private Date regTime;       // 预约时间
    private int status;         // 状态:0已预约|1已完成|2已取消

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

    public String getGid()
    {
        return gid;
    }

    public void setGid(String gid)
    {
        this.gid = gid;
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
