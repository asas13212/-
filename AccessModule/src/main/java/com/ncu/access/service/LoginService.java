package com.ncu.access.service;

import com.ncu.access.model.LoginResult;
import com.ncu.common.dao.UserDao;
import com.ncu.common.model.User;

/**
 * 登录业务逻辑：校验输入 -> 按手机号查用户 -> 比对密码。
 *
 * 说明：users.pwd 列为 VARCHAR(20)，明文存储，
 * EncryptUtil(AES 密文 Base64 >= 24 字符)放不进该列，
 * 故当前采用明文比对（见根目录 dev.md §7）。
 */
public class LoginService
{
    private final UserDao userDao = new UserDao();

    /** 登录校验，返回 LoginResult */
    public LoginResult login(String tel, String pwd)
    {
        if (tel == null || tel.trim().isEmpty())
        {
            return LoginResult.fail("请输入手机号");
        }
        if (pwd == null || pwd.isEmpty())
        {
            return LoginResult.fail("请输入密码");
        }

        User user = userDao.findByTel(tel.trim());
        if (user == null)
        {
            return LoginResult.fail("账号不存在");
        }
        if (!user.getPwd().equals(pwd))
        {
            return LoginResult.fail("密码错误");
        }
        return LoginResult.ok(user, "登录成功");
    }
}
