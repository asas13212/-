package com.ncu.fee.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收费记录 VO：fee 表 + 患者姓名 + 项目名称（联表查询结果）
 * 单项收费记录（gid 为空）的项目名称与 cid 经 registration 反查 checkitem 带出。
 */
public class FeeVO
{
    private int id;            // 收费id
    private int regId;         // 预约id
    private String tel;        // 患者账号
    private String patientName;// 患者姓名
    private String gid;        // 套餐id(单项收费记录时为空)
    private String cid;        // 检查项id(单项收费记录时用;由 registration 带出)
    private String groupName;  // 项目名称:套餐名或检查项名
    private BigDecimal amount; // 收费金额(元)
    private int status;        // 状态:0待缴|1已缴|2已退款
    private String operator;   // 收费员(医生)账号
    private Date payTime;      // 缴费时间
    private String remark;     // 备注

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

    public String getCid()
    {
        return cid;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
