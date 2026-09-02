package com.ncu.common.model;

/**
 * 检查组-检查项关联实体类，对应 checkgroup_item 表
 */
public class CheckGroupItem
{
    private int id;          // 主键id
    private String gid;      // 检查组id
    private String cid;      // 检查项id

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
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
}
