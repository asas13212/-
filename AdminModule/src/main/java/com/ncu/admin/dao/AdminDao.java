package com.ncu.admin.dao;

import com.ncu.admin.model.CalendarAppointment;
import com.ncu.admin.model.RegistrationVO;
import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.model.Registration;
import com.ncu.common.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理员专用数据访问类：联表查询 + Common 缺少的按主键查询
 */
public class AdminDao
{
    /** 按主键查检查项（Common 缺此方法，编辑时用） */
    public CheckItem findItemByCid(String cid)
    {
        String sql = "SELECT * FROM checkitem WHERE cid = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, cid);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return mapItem(rs);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, rs);
        }
        return null;
    }

    /** 按主键查套餐（Common 缺此方法，编辑时用） */
    public CheckGroup findGroupByGid(String gid)
    {
        String sql = "SELECT * FROM checkgroup WHERE gid = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, gid);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return mapGroup(rs);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, rs);
        }
        return null;
    }

    /** 查某个套餐包含的所有检查项（带检查项信息，用于展示套餐明细） */
    public List<CheckItem> findItemsByGid(String gid)
    {
        String sql = "SELECT ci.* FROM checkitem ci JOIN checkgroup_item gi ON ci.cid = gi.cid WHERE gi.gid = ?";
        List<CheckItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, gid);
            rs = ps.executeQuery();
            while (rs.next())
            {
                list.add(mapItem(rs));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, rs);
        }
        return list;
    }

    /** 查询所有预约，带患者姓名和项目名称（套餐=套餐名；单项预约=该检查项名，LEFT JOIN 保证 gid 空的单项也在） */
    public List<RegistrationVO> findAllRegistration()
    {
        String sql = "SELECT r.id, r.tel, u.name AS patient_name, r.gid, r.cid, " +
                "COALESCE(g.gname, ci.cname) AS group_name, r.reg_time, r.status " +
                "FROM registration r " +
                "JOIN users u ON u.tel = r.tel " +
                "LEFT JOIN checkgroup g ON g.gid = r.gid " +
                "LEFT JOIN checkitem ci ON ci.cid = r.cid " +
                "ORDER BY r.reg_time DESC";
        List<RegistrationVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next())
            {
                list.add(mapRegVO(rs));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, rs);
        }
        return list;
    }

    /** 查询所有「待录入」预约（status=0 已预约），带患者姓名和项目名称，供结果录入下拉选择（含单项预约） */
    public List<RegistrationVO> findPendingRegistration()
    {
        String sql = "SELECT r.id, r.tel, u.name AS patient_name, r.gid, r.cid, " +
                "COALESCE(g.gname, ci.cname) AS group_name, r.reg_time, r.status " +
                "FROM registration r " +
                "JOIN users u ON u.tel = r.tel " +
                "LEFT JOIN checkgroup g ON g.gid = r.gid " +
                "LEFT JOIN checkitem ci ON ci.cid = r.cid " +
                "WHERE r.status = 0 ORDER BY r.reg_time ASC";
        List<RegistrationVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next())
            {
                list.add(mapRegVO(rs));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, rs);
        }
        return list;
    }

    /** 查询某时间范围内的预约，带患者姓名、项目名、检查项目名（顿号分隔），供日历按天展示（含单项预约） */
    public List<CalendarAppointment> findAppointmentsByRange(Date from, Date to)
    {
        String sql = "SELECT r.id, r.tel, u.name AS patient_name, " +
                "COALESCE(g.gname, ci.cname) AS group_name, r.reg_time, r.status, " +
                // 套餐→所含检查项名顿号拼接；单项预约(gid 空)→该项自身名称
                "CASE WHEN r.gid IS NOT NULL " +
                "     THEN (SELECT GROUP_CONCAT(x.cname ORDER BY x.bh SEPARATOR '、') FROM checkgroup_item gi " +
                "             JOIN checkitem x ON x.cid = gi.cid WHERE gi.gid = r.gid) " +
                "     ELSE ci.cname END AS items " +
                "FROM registration r " +
                "JOIN users u ON u.tel = r.tel " +
                "LEFT JOIN checkgroup g ON g.gid = r.gid " +
                "LEFT JOIN checkitem ci ON ci.cid = r.cid " +
                "WHERE r.reg_time >= ? AND r.reg_time < ? " +
                "ORDER BY r.reg_time ASC";
        List<CalendarAppointment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            rs = ps.executeQuery();
            while (rs.next())
            {
                list.add(mapAppointment(rs));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, rs);
        }
        return list;
    }

    /** 按主键查预约 */
    public Registration findRegById(int id)
    {
        String sql = "SELECT * FROM registration WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return mapReg(rs);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, rs);
        }
        return null;
    }

    /** 更新预约状态 */
    public boolean updateRegStatus(int id, int status)
    {
        String sql = "UPDATE registration SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, null);
        }
        return false;
    }

    /** 删除套餐里的某个检查项（按 gid + cid） */
    public boolean deleteGroupItem(String gid, String cid)
    {
        String sql = "DELETE FROM checkgroup_item WHERE gid = ? AND cid = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, gid);
            ps.setString(2, cid);
            return ps.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JdbcUtil.close(conn, ps, null);
        }
        return false;
    }

    private CheckItem mapItem(ResultSet rs) throws SQLException
    {
        CheckItem c = new CheckItem();
        c.setCid(rs.getString("cid"));
        c.setBh(rs.getString("bh"));
        c.setCname(rs.getString("cname"));
        c.setDw(rs.getString("dw"));
        c.setCkfw(rs.getString("ckfw"));
        c.setPrice(rs.getBigDecimal("price"));
        c.setStatus(rs.getInt("status"));
        return c;
    }

    private CheckGroup mapGroup(ResultSet rs) throws SQLException
    {
        CheckGroup g = new CheckGroup();
        g.setGid(rs.getString("gid"));
        g.setGname(rs.getString("gname"));
        g.setBh(rs.getString("bh"));
        g.setRemark(rs.getString("remark"));
        g.setStatus(rs.getInt("status"));
        return g;
    }

    private Registration mapReg(ResultSet rs) throws SQLException
    {
        Registration r = new Registration();
        r.setId(rs.getInt("id"));
        r.setTel(rs.getString("tel"));
        r.setGid(rs.getString("gid"));
        r.setCid(rs.getString("cid"));
        r.setRegTime(rs.getTimestamp("reg_time"));
        r.setStatus(rs.getInt("status"));
        return r;
    }

    private CalendarAppointment mapAppointment(ResultSet rs) throws SQLException
    {
        CalendarAppointment a = new CalendarAppointment();
        a.setId(rs.getInt("id"));
        a.setTel(rs.getString("tel"));
        a.setPatientName(rs.getString("patient_name"));
        a.setGroupName(rs.getString("group_name"));
        a.setRegTime(rs.getTimestamp("reg_time"));
        a.setStatus(rs.getInt("status"));
        a.setItems(rs.getString("items"));
        return a;
    }

    private RegistrationVO mapRegVO(ResultSet rs) throws SQLException
    {
        RegistrationVO vo = new RegistrationVO();
        vo.setId(rs.getInt("id"));
        vo.setTel(rs.getString("tel"));
        vo.setPatientName(rs.getString("patient_name"));
        vo.setGid(rs.getString("gid"));
        vo.setCid(rs.getString("cid"));
        vo.setGroupName(rs.getString("group_name"));
        vo.setRegTime(rs.getTimestamp("reg_time"));
        vo.setStatus(rs.getInt("status"));
        return vo;
    }
}
