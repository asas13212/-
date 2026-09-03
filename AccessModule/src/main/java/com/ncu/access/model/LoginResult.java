package com.ncu.access.model;

import com.ncu.common.model.User;

/**
 * 登录结果对象：成功与否 + 提示信息 + 登录成功的用户
 */
public class LoginResult
{
    private boolean success;
    private String message;
    private User user;

    private LoginResult()
    {
    }

    /** 登录成功 */
    public static LoginResult ok(User user, String message)
    {
        LoginResult r = new LoginResult();
        r.success = true;
        r.user = user;
        r.message = message;
        return r;
    }

    /** 登录失败（账号不存在 / 密码错误 / 输入为空等） */
    public static LoginResult fail(String message)
    {
        LoginResult r = new LoginResult();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }
}
