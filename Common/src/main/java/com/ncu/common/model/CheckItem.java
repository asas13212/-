package com.ncu.common.model;

/**
 * 检查项实体类，对应 checkitem 表
 */
public class CheckItem
{
    private String cid;      // 主键id
    private String bh;       // 编号
    private String cname;    // 检查名称
    private String dw;       // 单位
    private String ckfw;     // 参考范围
    private int status;      // 状态:0正常|1下架

    public String getCid()
    {
        return cid;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
    }

    public String getBh()
    {
        return bh;
    }

    public void setBh(String bh)
    {
        this.bh = bh;
    }

    public String getCname()
    {
        return cname;
    }

    public void setCname(String cname)
    {
        this.cname = cname;
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

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }
}
