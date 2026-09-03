package com.ncu.main.controller;

import com.ncu.common.model.User;
import com.ncu.main.dao.MainDao;
import com.ncu.main.model.CurrentUser;

/**
 * 主控制层：登录校验（分发逻辑在 view 层 LoginFrame 里完成）、患者自助注册
 */
public class MainController
{
    private final MainDao dao = new MainDao();

    /** 登录校验；成功返回当前用户，失败返回 null */
    public CurrentUser login(String tel, String pwd)
    {
        return dao.login(tel, pwd);
    }

    /**
     * 患者自助注册（仅建 role=0 患者账号；医生账号不开放自助注册）。
     * 成功返回 null；失败返回给界面展示的错误文案。
     */
    public String register(String tel, String name, String pwd)
    {
        if (tel == null || !tel.matches("1\\d{10}"))
        {
            return "请输入正确的 11 位手机号作为账号";
        }
        if (name == null || name.trim().isEmpty())
        {
            return "姓名不能为空";
        }
        if (name.trim().length() > 20)
        {
            return "姓名过长";
        }
        if (pwd == null || pwd.length() < 6)
        {
            return "密码至少 6 位";
        }
        if (dao.findByTel(tel) != null)
        {
            return "该账号已注册，请直接登录";
        }
        User u = new User();
        u.setTel(tel);
        u.setName(name.trim());
        u.setPwd(pwd);
        u.setRole(0); // 注册固定为患者
        return dao.register(u) ? null : "注册失败，请重试";
    }
}
