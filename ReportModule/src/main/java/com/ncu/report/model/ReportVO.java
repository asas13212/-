package com.ncu.report.model;

import java.util.Date;
import java.util.List;

/**
 * 体检报告聚合 VO：患者/套餐/医生信息 + 各检查项明细
 */
public class ReportVO
{
    private String patientName;  // 患者姓名
    private String sex;          // 性别
    private Date birthday;       // 出生日期
    private String groupName;    // 套餐名称
    private Date regTime;        // 预约时间
    private String doctorName;   // 录入医生姓名
    private List<ReportItem> items; // 检查项明细

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public Date getBirthday()
    {
        return birthday;
    }

    public void setBirthday(Date birthday)
    {
        this.birthday = birthday;
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

    public String getDoctorName()
    {
        return doctorName;
    }

    public void setDoctorName(String doctorName)
    {
        this.doctorName = doctorName;
    }

    public List<ReportItem> getItems()
    {
        return items;
    }

    public void setItems(List<ReportItem> items)
    {
        this.items = items;
    }
}
