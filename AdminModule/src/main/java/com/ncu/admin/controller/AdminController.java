package com.ncu.admin.controller;

import com.ncu.admin.dao.AdminDao;
import com.ncu.admin.model.CalendarAppointment;
import com.ncu.admin.model.RegistrationVO;
import com.ncu.common.dao.CheckGroupDao;
import com.ncu.common.dao.CheckGroupItemDao;
import com.ncu.common.dao.CheckItemDao;
import com.ncu.common.dao.CheckResultDao;
import com.ncu.common.dao.RegistrationDao;
import com.ncu.common.dao.UserDao;
import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckGroupItem;
import com.ncu.common.model.CheckItem;
import com.ncu.common.model.CheckResult;
import com.ncu.common.model.Registration;
import com.ncu.common.model.User;

import java.util.Date;
import java.util.List;

/**
 * 管理员业务控制层：接收 view 的参数，调用 dao 完成业务
 */
public class AdminController
{
    private final CheckItemDao itemDao = new CheckItemDao();
    private final CheckGroupDao groupDao = new CheckGroupDao();
    private final CheckGroupItemDao groupItemDao = new CheckGroupItemDao();
    private final UserDao userDao = new UserDao();
    private final RegistrationDao regDao = new RegistrationDao();
    private final CheckResultDao resultDao = new CheckResultDao();
    private final AdminDao adminDao = new AdminDao();

    // ===== 检查项 =====
    public List<CheckItem> listItems()
    {
        return itemDao.findAll();
    }

    public CheckItem findItem(String cid)
    {
        return adminDao.findItemByCid(cid);
    }

    public boolean addItem(CheckItem c)
    {
        return itemDao.insert(c);
    }

    public boolean editItem(CheckItem c)
    {
        return itemDao.update(c);
    }

    public boolean removeItem(String cid)
    {
        return itemDao.delete(cid);
    }

    // ===== 套餐 =====
    public List<CheckGroup> listGroups()
    {
        return groupDao.findAll();
    }

    public CheckGroup findGroup(String gid)
    {
        return adminDao.findGroupByGid(gid);
    }

    public boolean addGroup(CheckGroup g)
    {
        return groupDao.insert(g);
    }

    public boolean editGroup(CheckGroup g)
    {
        return groupDao.update(g);
    }

    public boolean removeGroup(String gid)
    {
        groupItemDao.deleteByGid(gid); // 先删套餐下的关联，否则外键会阻止删除
        return groupDao.delete(gid);
    }

    public boolean addItemToGroup(String gid, String cid)
    {
        CheckGroupItem item = new CheckGroupItem();
        item.setGid(gid);
        item.setCid(cid);
        return groupItemDao.insert(item);
    }

    public List<CheckItem> listGroupItems(String gid)
    {
        return adminDao.findItemsByGid(gid);
    }

    public boolean removeGroupItem(String gid, String cid)
    {
        return adminDao.deleteGroupItem(gid, cid);
    }

    // ===== 用户 =====
    public List<User> listUsers()
    {
        return userDao.findAll();
    }

    public List<User> listPatients()
    {
        return userDao.findByRole(0);
    }

    public boolean removeUser(String tel)
    {
        return userDao.delete(tel);
    }

    // ===== 预约 =====
    public List<RegistrationVO> listRegistrations()
    {
        return adminDao.findAllRegistration();
    }

    public List<RegistrationVO> listPendingRegistrations()
    {
        return adminDao.findPendingRegistration();
    }

    public boolean updateRegStatus(int id, int status)
    {
        return adminDao.updateRegStatus(id, status);
    }

    // ===== 预约日历 =====
    public List<CalendarAppointment> listAppointments(Date from, Date to)
    {
        return adminDao.findAppointmentsByRange(from, to);
    }

    // ===== 检查结果 =====
    public Registration findRegById(int id)
    {
        return adminDao.findRegById(id);
    }

    public boolean addResult(CheckResult r)
    {
        return resultDao.insert(r);
    }

    public List<CheckResult> listResults(int regId)
    {
        return resultDao.findByRegId(regId);
    }
}
