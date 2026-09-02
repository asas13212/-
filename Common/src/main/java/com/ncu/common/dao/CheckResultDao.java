package com.ncu.common.dao;

import com.ncu.common.model.CheckResult;
import com.ncu.common.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查结果表数据访问类
 */
public class CheckResultDao
{
    /** 新增检查结果 */
    public boolean insert(CheckResult r)
    {
        String sql = "INSERT INTO check_result(reg_id, tel, cid, result_value, doctor_tel, check_time) VALUES(?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, r.getRegId());
            ps.setString(2, r.getTel());
            ps.setString(3, r.getCid());
            ps.setString(4, r.getResultValue());
            ps.setString(5, r.getDoctorTel());
            ps.setTimestamp(6, r.getCheckTime() == null ? null : new java.sql.Timestamp(r.getCheckTime().getTime()));
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

    /** 查询某个患者的所有检查结果（病史跟踪用） */
    public List<CheckResult> findByTel(String tel)
    {
        String sql = "SELECT * FROM check_result WHERE tel = ? ORDER BY check_time DESC";
        List<CheckResult> list = new ArrayList<>();
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
                list.add(mapRow(rs));
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

    /** 查询某次预约的所有检查结果 */
    public List<CheckResult> findByRegId(int regId)
    {
        String sql = "SELECT * FROM check_result WHERE reg_id = ?";
        List<CheckResult> list = new ArrayList<>();
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
                list.add(mapRow(rs));
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

    /** 修改检查结果 */
    public boolean update(CheckResult r)
    {
        String sql = "UPDATE check_result SET reg_id=?, tel=?, cid=?, result_value=?, doctor_tel=?, check_time=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, r.getRegId());
            ps.setString(2, r.getTel());
            ps.setString(3, r.getCid());
            ps.setString(4, r.getResultValue());
            ps.setString(5, r.getDoctorTel());
            ps.setTimestamp(6, r.getCheckTime() == null ? null : new java.sql.Timestamp(r.getCheckTime().getTime()));
            ps.setInt(7, r.getId());
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

    /** 删除检查结果 */
    public boolean delete(int id)
    {
        String sql = "DELETE FROM check_result WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
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

    /** 把一行结果集转成 CheckResult 对象 */
    private CheckResult mapRow(ResultSet rs) throws SQLException
    {
        CheckResult r = new CheckResult();
        r.setId(rs.getInt("id"));
        r.setRegId(rs.getInt("reg_id"));
        r.setTel(rs.getString("tel"));
        r.setCid(rs.getString("cid"));
        r.setResultValue(rs.getString("result_value"));
        r.setDoctorTel(rs.getString("doctor_tel"));
        r.setCheckTime(rs.getTimestamp("check_time"));
        return r;
    }
}
