package com.ncu.common.dao;

import com.ncu.common.model.CheckGroupItem;
import com.ncu.common.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查组-检查项关联表数据访问类
 */
public class CheckGroupItemDao
{
    /** 给检查组添加一个检查项 */
    public boolean insert(CheckGroupItem item)
    {
        String sql = "INSERT INTO checkgroup_item(gid, cid) VALUES(?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getGid());
            ps.setString(2, item.getCid());
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

    /** 查询某个检查组包含的所有检查项 */
    public List<CheckGroupItem> findByGid(String gid)
    {
        String sql = "SELECT * FROM checkgroup_item WHERE gid = ?";
        List<CheckGroupItem> list = new ArrayList<>();
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

    /** 删除某个检查组的全部关联（删检查组前先调用） */
    public boolean deleteByGid(String gid)
    {
        String sql = "DELETE FROM checkgroup_item WHERE gid = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, gid);
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

    /** 删除单条关联 */
    public boolean delete(int id)
    {
        String sql = "DELETE FROM checkgroup_item WHERE id = ?";
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

    /** 把一行结果集转成 CheckGroupItem 对象 */
    private CheckGroupItem mapRow(ResultSet rs) throws SQLException
    {
        CheckGroupItem item = new CheckGroupItem();
        item.setId(rs.getInt("id"));
        item.setGid(rs.getString("gid"));
        item.setCid(rs.getString("cid"));
        return item;
    }
}
