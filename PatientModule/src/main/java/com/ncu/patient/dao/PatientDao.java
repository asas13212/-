package com.ncu.patient.dao;

import com.ncu.common.model.CheckGroup;
import com.ncu.common.model.CheckItem;
import com.ncu.common.util.JdbcUtil;
import com.ncu.patient.model.RegistrationVO;
import com.ncu.patient.model.ResultVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 患者专用数据访问类：套餐明细、我的预约、检查结果的联表查询 + 状态更新
 */
public class PatientDao
{
    /** 按主键查套餐（查看套餐详情用） */
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

    /** 查某患者的全部预约，带套餐名称 */
    public List<RegistrationVO> findMyRegistrations(String tel)
    {
        String sql = "SELECT r.id, r.gid, g.gname AS group_name, r.reg_time, r.status " +
                "FROM registration r JOIN checkgroup g ON g.gid = r.gid " +
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
        c.setStatus(rs.getInt("status"));
        return c;
    }

    private RegistrationVO mapRegVO(ResultSet rs) throws SQLException
    {
        RegistrationVO vo = new RegistrationVO();
        vo.setId(rs.getInt("id"));
        vo.setGid(rs.getString("gid"));
        vo.setGroupName(rs.getString("group_name"));
        vo.setRegTime(rs.getTimestamp("reg_time"));
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
