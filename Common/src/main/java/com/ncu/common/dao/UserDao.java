package com.ncu.common.dao;

import com.ncu.common.model.User;
import com.ncu.common.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户表数据访问类
 */
public class UserDao
{
    /** 新增用户（注册） */
    public boolean insert(User u)
    {
        String sql = "INSERT INTO users(tel, pwd, name, idcard, birthday, sex, role) VALUES(?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, u.getTel());
            ps.setString(2, u.getPwd());
            ps.setString(3, u.getName());
            ps.setString(4, u.getIdcard());
            ps.setDate(5, u.getBirthday() == null ? null : new java.sql.Date(u.getBirthday().getTime()));
            ps.setString(6, u.getSex());
            ps.setInt(7, u.getRole());
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

    /** 按账号(手机号)查用户（登录用） */
    public User findByTel(String tel)
    {
        String sql = "SELECT * FROM users WHERE tel = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, tel);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return mapRow(rs);
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

    /** 查询所有用户 */
    public List<User> findAll()
    {
        String sql = "SELECT * FROM users";
        List<User> list = new ArrayList<>();
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

    /** 按角色查用户（0患者|1医生|2管理员） */
    public List<User> findByRole(int role)
    {
        String sql = "SELECT * FROM users WHERE role = ?";
        List<User> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, role);
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

    /** 修改用户资料 */
    public boolean update(User u)
    {
        String sql = "UPDATE users SET pwd=?, name=?, idcard=?, birthday=?, sex=?, role=? WHERE tel=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, u.getPwd());
            ps.setString(2, u.getName());
            ps.setString(3, u.getIdcard());
            ps.setDate(4, u.getBirthday() == null ? null : new java.sql.Date(u.getBirthday().getTime()));
            ps.setString(5, u.getSex());
            ps.setInt(6, u.getRole());
            ps.setString(7, u.getTel());
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

    /** 删除用户 */
    public boolean delete(String tel)
    {
        String sql = "DELETE FROM users WHERE tel = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, tel);
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

    /** 把一行结果集转成 User 对象 */
    private User mapRow(ResultSet rs) throws SQLException
    {
        User u = new User();
        u.setTel(rs.getString("tel"));
        u.setPwd(rs.getString("pwd"));
        u.setName(rs.getString("name"));
        u.setIdcard(rs.getString("idcard"));
        u.setBirthday(rs.getDate("birthday"));
        u.setSex(rs.getString("sex"));
        u.setRole(rs.getInt("role"));
        return u;
    }
}
