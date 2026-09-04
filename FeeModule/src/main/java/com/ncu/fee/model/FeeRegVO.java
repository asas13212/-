package com.ncu.fee.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 待收费预约 VO：一条还没收费记录的预约（已预约状态、且 fee 里没有对应行）
 * 字段与展示列一一对应，避免界面直接依赖多个实体
 * <p>
 * 预约可能是套餐预约（gid 非空）或单项预约（gid 空、cid 非空）。
 * price = 应收金额：套餐预约=所含各项单价之和；单项预约=该检查项单价。界面按此金额直接入账，不手输。
 * groupName = 项目名称（套餐名或检查项名），由联表查询 COALESCE 得出。
 */
public class FeeRegVO
{
    private int id;             // 预约id
    private String tel;         // 患者账号
    private String patientName; // 患者姓名
    private String gid;         // 套餐id(单项预约时为空)
    private String cid;         // 检查项id(单项预约时用;套餐预约时为空)
    private String groupName;   // 项目名称:套餐名或检查项名
    private BigDecimal price;   // 应收金额(元)
    private Date regTime;       // 预约时间

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

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public Date getRegTime()
    {
        return regTime;
    }

    public void setRegTime(Date regTime)
    {
        this.regTime = regTime;
    }
}
