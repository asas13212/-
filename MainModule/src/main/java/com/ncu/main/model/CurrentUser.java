package com.ncu.main.model;

/**
 * 当前登录用户（会话上下文）
 */
public class CurrentUser
{
    private String tel;   // 账号(手机号)
    private String name;  // 姓名
    private int role;     // 角色:0患者|1医生

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getRole()
    {
        return role;
    }

    public void setRole(int role)
    {
        this.role = role;
    }
}
