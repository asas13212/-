package com.ncu.patient.dao;

import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.util.JdbcUtil;
import com.ncu.patient.model.RegistrationVO;
import com.ncu.patient.model.ResultVO;
import com.ncu.patient.model.TrendItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 患者专用数据访问类：套餐明细、我的预约、检查结果的联表查询 + 预约写入 + 状态更新
 */
public class PatientDao
{
    /** 按主键查套餐（查看套餐详情用）；套餐价=所含各项单价之和（显式列 + SUM 子查询，别名 price） */
    public CheckGroup findGroupByGid(String gid)
    {
        String sql = "SELECT g.gid, g.gname, g.bh, g.remark, g.status, "
                + "(SELECT IFNULL(SUM(ci.price), 0) FROM checkgroup_item gi "
                + "  JOIN checkitem ci ON ci.cid = gi.cid WHERE gi.gid = g.gid) AS price "
                + "FROM checkgroup g WHERE g.gid = ?";
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

    /** 查某套餐包含的所有检查项（带名称/单位/参考范围，展示套餐明细） */
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

    /** 新增预约（带体检地点，状态固定为 0 已预约） */
    public boolean insertRegistration(String tel, String gid, Date regTime, String location)
    {
        String sql = "INSERT INTO registration(tel, gid, reg_time, location, status) VALUES(?,?,?,?,0)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, tel);
            ps.setString(2, gid);
            ps.setTimestamp(3, regTime == null ? null : new java.sql.Timestamp(regTime.getTime()));
            ps.setString(4, location);
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

    /** 新增单项预约（指定检查项，gid 为空，状态固定为 0 已预约） */
    public boolean insertSingleRegistration(String tel, String cid, Date regTime, String location)
    {
        String sql = "INSERT INTO registration(tel, cid, reg_time, location, status) VALUES(?,?,?,?,0)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, tel);
            ps.setString(2, cid);
            ps.setTimestamp(3, regTime == null ? null : new java.sql.Timestamp(regTime.getTime()));
            ps.setString(4, location);
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

    /** 查某患者的全部预约，带套餐/检查项名称 + 体检地点 */
    public List<RegistrationVO> findMyRegistrations(String tel)
    {
        String sql = "SELECT r.id, r.gid, r.cid, COALESCE(g.gname, ci.cname) AS item_name, r.reg_time, r.location, r.status " +
                "FROM registration r " +
                "LEFT JOIN checkgroup g ON g.gid = r.gid " +
                "LEFT JOIN checkitem ci ON ci.cid = r.cid " +
                "WHERE r.tel = ? ORDER BY r.reg_time DESC";
        List<RegistrationVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, tel);
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

    /** 查某次预约的全部检查结果，带检查项名称/单位/参考范围 */
    public List<ResultVO> findResultsByRegId(int regId)
    {
        String sql = "SELECT cr.id, cr.reg_id, ci.cname, cr.result_value, ci.dw, ci.ckfw, cr.check_time " +
                "FROM check_result cr JOIN checkitem ci ON ci.cid = cr.cid " +
                "WHERE cr.reg_id = ? ORDER BY cr.check_time DESC";
        List<ResultVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, regId);
            rs = ps.executeQuery();
            while (rs.next())
            {
                list.add(mapResultVO(rs));
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

    /** 更新预约状态（取消预约等） */
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

    /** 查某患者的全部检查结果历史（按检查项、时间排序），用于健康趋势 */
    public List<TrendItem> findHistory(String tel)
    {
        String sql = "SELECT cr.cid, ci.cname, ci.dw, ci.ckfw, cr.result_value, cr.check_time " +
                "FROM check_result cr JOIN checkitem ci ON ci.cid = cr.cid " +
                "WHERE cr.tel = ? ORDER BY cr.cid, cr.check_time";
        List<TrendItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, tel);
            rs = ps.executeQuery();
            while (rs.next())
            {
                TrendItem t = new TrendItem();
                t.setCid(rs.getString("cid"));
                t.setCname(rs.getString("cname"));
                t.setDw(rs.getString("dw"));
                t.setResultValue(rs.getString("result_value"));
                t.setCkfw(rs.getString("ckfw"));
                t.setCheckTime(rs.getTimestamp("check_time"));
                list.add(t);
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

    private CheckGroup mapGroup(ResultSet rs) throws SQLException
    {
        CheckGroup g = new CheckGroup();
        g.setGid(rs.getString("gid"));
        g.setGname(rs.getString("gname"));
        g.setBh(rs.getString("bh"));
        g.setRemark(rs.getString("remark"));
        g.setPrice(rs.getBigDecimal("price"));
        g.setStatus(rs.getInt("status"));
        return g;
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

    private RegistrationVO mapRegVO(ResultSet rs) throws SQLException
    {
        RegistrationVO vo = new RegistrationVO();
        vo.setId(rs.getInt("id"));
        vo.setGid(rs.getString("gid"));
        vo.setCid(rs.getString("cid"));
        vo.setGroupName(rs.getString("item_name"));
        vo.setRegTime(rs.getTimestamp("reg_time"));
        vo.setLocation(rs.getString("location"));
        vo.setStatus(rs.getInt("status"));
        return vo;
    }

    private ResultVO mapResultVO(ResultSet rs) throws SQLException
    {
        ResultVO vo = new ResultVO();
        vo.setId(rs.getInt("id"));
        vo.setRegId(rs.getInt("reg_id"));
        vo.setCname(rs.getString("cname"));
        vo.setResultValue(rs.getString("result_value"));
        vo.setDw(rs.getString("dw"));
        vo.setCkfw(rs.getString("ckfw"));
        vo.setCheckTime(rs.getTimestamp("check_time"));
        return vo;
    }
}
