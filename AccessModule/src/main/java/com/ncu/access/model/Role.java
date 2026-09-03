package com.ncu.access.model;

/**
 * 角色常量，与 users.role 列一一对应。
 * 0 患者 | 1 医生 | 2 管理员（见根目录 dev.md §1）
 */
public final class Role
{
    public static final int PATIENT = 0;   // 患者
    public static final int DOCTOR  = 1;   // 医生
    public static final int ADMIN   = 2;   // 管理员

    private Role()
    {
    }

    /** 角色数值转中文名，用于界面提示 */
    public static String name(int role)
    {
        switch (role)
        {
            case PATIENT:
                return "患者";
            case DOCTOR:
                return "医生";
            case ADMIN:
                return "管理员";
            default:
                return "未知角色";
        }
    }
}
