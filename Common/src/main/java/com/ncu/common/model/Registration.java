package com.ncu.common.model;

import java.util.Date;

/**
 * 预约实体类，对应 registration 表
 */
public class Registration
{
    private int id;          // 主键id
    private String tel;      // 患者账号
    private String gid;      // 套餐id(单项预约时为空)
    private String cid;      // 检查项id(单项预约时用;套餐预约时为空)
    private Date regTime;    // 预约时间
    private int status;      // 状态:0已预约|1已完成|2已取消

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

    public String getGid()
    {
        return gid;
    }

    public void setGid(String gid)
    {
        this.gid = gid;
    }

    public String getCid()
    {
        return cid;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
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
