package com.ncu.common.model;

import java.util.Date;

/**
 * 检查结果实体类，对应 check_result 表
 */
public class CheckResult
{
    private int id;            // 主键id
    private int regId;         // 预约id
    private String tel;        // 患者账号
    private String cid;        // 检查项id
    private String resultValue; // 结果值
    private String doctorTel;   // 录入医生账号
    private Date checkTime;    // 检查时间

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

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getCid()
    {
        return cid;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
    }

    public String getResultValue()
    {
        return resultValue;
    }

    public void setResultValue(String resultValue)
    {
        this.resultValue = resultValue;
    }

    public String getDoctorTel()
    {
        return doctorTel;
    }

    public void setDoctorTel(String doctorTel)
    {
        this.doctorTel = doctorTel;
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
