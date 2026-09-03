package com.ncu.main.controller;

import com.ncu.main.dao.MainDao;
import com.ncu.main.model.CurrentUser;

/**
 * 主控制层：登录校验（分发逻辑在 view 层 LoginFrame 里完成）
 */
public class MainController
{
    private final MainDao dao = new MainDao();

    /** 登录校验；成功返回当前用户，失败返回 null */
    public CurrentUser login(String tel, String pwd)
    {
        return dao.login(tel, pwd);
    }
}
