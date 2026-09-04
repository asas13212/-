package com.ncu.main.dao;

import com.ncu.common.dao.UserDao;
import com.ncu.common.model.User;
import com.ncu.main.model.CurrentUser;

/**
 * 登录相关数据访问：复用 Common 的 UserDao
 */
public class MainDao
{
    private final UserDao userDao = new UserDao();

    /** 登录校验：账号(手机号) + 密码，成功返回当前用户，失败返回 null */
    public CurrentUser login(String tel, String pwd)
    {
        User u = userDao.findByTel(tel);
        if (u != null && u.getPwd() != null && u.getPwd().equals(pwd))
        {
            CurrentUser cu = new CurrentUser();
            cu.setTel(u.getTel());
            cu.setName(u.getName());
            cu.setRole(u.getRole());
            return cu;
        }
        return null;
    }

    /** 注册（患者自助建号，role=0）：复用 Common 的 UserDao.insert */
    public boolean register(User u)
    {
        return userDao.insert(u);
    }

    /** 按账号查用户（注册判重用） */
    public User findByTel(String tel)
    {
        return userDao.findByTel(tel);
    }
}
