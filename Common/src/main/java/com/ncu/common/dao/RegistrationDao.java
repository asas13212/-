package com.ncu.common.dao;

import com.ncu.common.model.Registration;
import com.ncu.common.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约表数据访问类
 */
public class RegistrationDao
{
    /** 新增预约 */
    public boolean insert(Registration r)
    {
        String sql = "INSERT INTO registration(tel, gid, reg_time, status) VALUES(?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, r.getTel());
            ps.setString(2, r.getGid());
            ps.setTimestamp(3, r.getRegTime() == null ? null : new java.sql.Timestamp(r.getRegTime().getTime()));
            ps.setInt(4, r.getStatus());
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

    /** 查询某个患者的所有预约 */
    public List<Registration> findByTel(String tel)
    {
        String sql = "SELECT * FROM registration WHERE tel = ? ORDER BY reg_time DESC";
        List<Registration> list = new ArrayList<>();
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

    /** 查询所有预约 */
    public List<Registration> findAll()
    {
        String sql = "SELECT * FROM registration ORDER BY reg_time DESC";
        List<Registration> list = new ArrayList<>();
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

    /** 修改预约（更新状态等） */
    public boolean update(Registration r)
    {
        String sql = "UPDATE registration SET tel=?, gid=?, reg_time=?, status=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, r.getTel());
            ps.setString(2, r.getGid());
            ps.setTimestamp(3, r.getRegTime() == null ? null : new java.sql.Timestamp(r.getRegTime().getTime()));
            ps.setInt(4, r.getStatus());
            ps.setInt(5, r.getId());
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

    /** 删除预约 */
    public boolean delete(int id)
    {
        String sql = "DELETE FROM registration WHERE id = ?";
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

    /** 把一行结果集转成 Registration 对象 */
    private Registration mapRow(ResultSet rs) throws SQLException
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
}
