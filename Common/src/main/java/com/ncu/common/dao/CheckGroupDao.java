package com.ncu.common.dao;

import com.ncu.common.model.CheckGroup;
import com.ncu.common.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查组表数据访问类
 * 注意:checkgroup.price 列已退役;套餐价一律由所含检查项单价求和得出(别名 price),代码不再读库存套餐价
 */
public class CheckGroupDao
{
    /** 套餐列表查询列:显式列 + 求所含各项单价之和,结果别名为 price */
    private static final String SELECT_COLS =
            "SELECT g.gid, g.gname, g.bh, g.remark, g.status, "
          + "       (SELECT IFNULL(SUM(ci.price), 0) FROM checkgroup_item gi "
          + "          JOIN checkitem ci ON ci.cid = gi.cid "
          + "         WHERE gi.gid = g.gid) AS price "
          + "  FROM checkgroup g ";

    /** 新增检查组 */
    public boolean insert(CheckGroup g)
    {
        String sql = "INSERT INTO checkgroup(gid, gname, bh, remark, status) VALUES(?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, g.getGid());
            ps.setString(2, g.getGname());
            ps.setString(3, g.getBh());
            ps.setString(4, g.getRemark());
            ps.setInt(5, g.getStatus());
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

    /** 查询所有检查组(套餐价=所含各项单价之和) */
    public List<CheckGroup> findAll()
    {
        String sql = SELECT_COLS;
        List<CheckGroup> list = new ArrayList<>();
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

    /** 按名称模糊搜索(套餐价=所含各项单价之和) */
    public List<CheckGroup> findByName(String name)
    {
        String sql = SELECT_COLS + "WHERE g.gname LIKE ?";
        List<CheckGroup> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + name + "%");
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

    /** 修改检查组 */
    public boolean update(CheckGroup g)
    {
        String sql = "UPDATE checkgroup SET gname=?, bh=?, remark=?, status=? WHERE gid=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, g.getGname());
            ps.setString(2, g.getBh());
            ps.setString(3, g.getRemark());
            ps.setInt(4, g.getStatus());
            ps.setString(5, g.getGid());
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

    /** 删除检查组 */
    public boolean delete(String gid)
    {
        String sql = "DELETE FROM checkgroup WHERE gid = ?";
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

    /** 把一行结果集转成 CheckGroup 对象 */
    private CheckGroup mapRow(ResultSet rs) throws SQLException
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
}
