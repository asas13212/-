package com.ncu.patient.controller;

import com.ncu.common.dao.CheckGroupDao;
import com.ncu.common.dao.CheckItemDao;
import com.ncu.common.dao.UserDao;
import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.model.User;
import com.ncu.patient.dao.PatientDao;
import com.ncu.patient.model.RegistrationVO;
import com.ncu.patient.model.ResultVO;
import com.ncu.patient.model.TrendItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 患者业务控制层：接收 view 的参数，调用 dao 完成业务
 */
public class PatientController
{
    private final CheckGroupDao groupDao = new CheckGroupDao();
    private final CheckItemDao itemDao = new CheckItemDao();
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

    /** 全部正常状态(status=0)的检查项，供单项预约选择 */
    public List<CheckItem> listItems()
    {
        List<CheckItem> active = new ArrayList<>();
        for (CheckItem c : itemDao.findAll())
        {
            if (c.getStatus() == 0)
            {
                active.add(c);
            }
        }
        return active;
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

    public boolean register(String tel, String gid, Date regTime, String location)
    {
        return patientDao.insertRegistration(tel, gid, regTime, location);
    }

    /** 是否已有该检查项进行中(status=0)的单项预约 */
    public boolean hasActiveItemRegistration(String tel, String cid)
    {
        for (RegistrationVO vo : patientDao.findMyRegistrations(tel))
        {
            if (vo.getStatus() == 0 && cid.equals(vo.getCid()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean registerSingle(String tel, String cid, Date regTime, String location)
    {
        return patientDao.insertSingleRegistration(tel, cid, regTime, location);
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

    // ===== 健康趋势 =====

    public List<TrendItem> listHistory(String tel)
    {
        return patientDao.findHistory(tel);
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
