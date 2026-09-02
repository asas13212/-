package com.ncu.common.dao;

import com.ncu.common.model.CheckItem;
import com.ncu.common.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查项表数据访问类
 */
public class CheckItemDao
{
    /** 新增检查项 */
    public boolean insert(CheckItem c)
    {
        String sql = "INSERT INTO checkitem(cid, bh, cname, dw, ckfw, status) VALUES(?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, c.getCid());
            ps.setString(2, c.getBh());
            ps.setString(3, c.getCname());
            ps.setString(4, c.getDw());
            ps.setString(5, c.getCkfw());
            ps.setInt(6, c.getStatus());
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

    /** 查询所有检查项 */
    public List<CheckItem> findAll()
    {
        String sql = "SELECT * FROM checkitem";
        List<CheckItem> list = new ArrayList<>();
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

    /** 分页查询（page 从 1 开始） */
    public List<CheckItem> findPage(int page, int pageSize)
    {
        String sql = "SELECT * FROM checkitem LIMIT ? OFFSET ?";
        List<CheckItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
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

    /** 查询总条数（分页用） */
    public int count()
    {
        String sql = "SELECT COUNT(*) FROM checkitem";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getInt(1);
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
        return 0;
    }

    /** 按编号精确查询（编号搜索） */
    public CheckItem findByBh(String bh)
    {
        String sql = "SELECT * FROM checkitem WHERE bh = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, bh);
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

    /** 修改检查项 */
    public boolean update(CheckItem c)
    {
        String sql = "UPDATE checkitem SET bh=?, cname=?, dw=?, ckfw=?, status=? WHERE cid=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, c.getBh());
            ps.setString(2, c.getCname());
            ps.setString(3, c.getDw());
            ps.setString(4, c.getCkfw());
            ps.setInt(5, c.getStatus());
            ps.setString(6, c.getCid());
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

    /** 删除检查项 */
    public boolean delete(String cid)
    {
        String sql = "DELETE FROM checkitem WHERE cid = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, cid);
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

    /** 把一行结果集转成 CheckItem 对象 */
    private CheckItem mapRow(ResultSet rs) throws SQLException
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
}
