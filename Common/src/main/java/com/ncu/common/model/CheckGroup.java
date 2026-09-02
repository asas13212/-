package com.ncu.common.model;

/**
 * 检查组实体类，对应 checkgroup 表
 */
public class CheckGroup
{
    private String gid;      // 主键id
    private String gname;    // 检查组名称
    private String bh;       // 编号
    private String remark;   // 备注
    private int status;      // 状态:0正常|1停用

    public String getGid()
    {
        return gid;
    }

    public void setGid(String gid)
    {
        this.gid = gid;
    }

    public String getGname()
    {
        return gname;
    }

    public void setGname(String gname)
    {
        this.gname = gname;
    }

    public String getBh()
    {
        return bh;
    }

    public void setBh(String bh)
    {
        this.bh = bh;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
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
