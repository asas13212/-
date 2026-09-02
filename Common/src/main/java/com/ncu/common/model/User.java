package com.ncu.common.model;

import java.util.Date;

/**
 * 用户实体类，对应 users 表
 */
public class User
{
    private String tel;      // 账号(手机号)
    private String pwd;      // 密码
    private String name;     // 姓名
    private String idcard;   // 身份证
    private Date birthday;   // 出生日期
    private String sex;      // 性别
    private int role;        // 角色:0患者|1医生|2管理员

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd)
    {
        this.pwd = pwd;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIdcard()
    {
        return idcard;
    }

    public void setIdcard(String idcard)
    {
        this.idcard = idcard;
    }

    public Date getBirthday()
    {
        return birthday;
    }

    public void setBirthday(Date birthday)
    {
        this.birthday = birthday;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
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
