package com.ncu.patient.controller;

import com.ncu.common.dao.CheckGroupDao;
import com.ncu.common.dao.RegistrationDao;
import com.ncu.common.dao.UserDao;
import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.model.Registration;
import com.ncu.common.model.User;
import com.ncu.patient.dao.PatientDao;
import com.ncu.patient.model.RegistrationVO;
import com.ncu.patient.model.ResultVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 患者业务控制层：接收 view 的参数，调用 dao 完成业务
 */
public class PatientController
{
    private final CheckGroupDao groupDao = new CheckGroupDao();
    private final RegistrationDao regDao = new RegistrationDao();
    private final UserDao userDao = new UserDao();
    private final PatientDao patientDao = new PatientDao();

    // ===== 套餐浏览 =====

    /** 只返回正常状态(status=0)的套餐 */
    public List<CheckGroup> listPackages()
    {
        List<CheckGroup> active = new ArrayList<>();
        for (CheckGroup g : groupDao.findAll())
        {
            if (g.getStatus() == 0)
            {
                active.add(g);
            }
        }
        return active;
    }

    public CheckGroup findGroup(String gid)
    {
        return patientDao.findGroupByGid(gid);
    }

    public List<CheckItem> listGroupItems(String gid)
    {
        return patientDao.findItemsByGid(gid);
    }

    // ===== 预约 =====

    /** 是否已有该套餐进行中(status=0)的预约 */
    public boolean hasActiveRegistration(String tel, String gid)
    {
        for (RegistrationVO vo : patientDao.findMyRegistrations(tel))
        {
            if (vo.getStatus() == 0 && gid.equals(vo.getGid()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean register(String tel, String gid)
    {
        Registration r = new Registration();
        r.setTel(tel);
        r.setGid(gid);
        r.setRegTime(new Date());
        r.setStatus(0);
        return regDao.insert(r);
    }

    /** 取消预约：把状态改成 2(已取消) */
    public boolean cancelRegistration(int id)
    {
        return patientDao.updateRegStatus(id, 2);
    }

    public List<RegistrationVO> listMyRegistrations(String tel)
    {
        return patientDao.findMyRegistrations(tel);
    }

    // ===== 检查结果 =====

    public List<ResultVO> listResults(int regId)
    {
        return patientDao.findResultsByRegId(regId);
    }

    // ===== 个人资料 =====

    public User getProfile(String tel)
    {
        return userDao.findByTel(tel);
    }

    public boolean updateProfile(User u)
    {
        return userDao.update(u);
    }
}
