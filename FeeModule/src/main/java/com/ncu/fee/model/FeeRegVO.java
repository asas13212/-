package com.ncu.fee.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 待收费预约 VO：一条还没收费记录的预约（已预约状态、且 fee 里没有对应行）
 * 字段与展示列一一对应，避免界面直接依赖多个实体
 * <p>
 * price = 该预约套餐(checkgroup.price)的套餐价。收费登记按套餐价直接入账，
 * 由 FeeModule 联表查出并写回收费记录，界面不手输金额。
 */
public class FeeRegVO
{
    private int id;             // 预约id
    private String tel;         // 患者账号
    private String patientName; // 患者姓名
    private String gid;         // 套餐id
    private String groupName;   // 套餐名称
    private BigDecimal price;   // 套餐价(元)
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
