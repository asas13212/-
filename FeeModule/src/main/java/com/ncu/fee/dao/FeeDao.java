package com.ncu.fee.dao;

import com.ncu.common.model.Fee;
import com.ncu.common.util.JdbcUtil;
import com.ncu.fee.model.FeeRegVO;
import com.ncu.fee.model.FeeVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 收费数据访问类。
 * <p>
 * fee 是新建表，收费操作目前只有 FeeModule 使用，故基础增改查与联表查询都放本类
 * （相当于 AdminModule 里 AdminDao 的角色，Common 只放了 Fee 实体）。
 * 若将来报告/患者端也要读收费，再由队长把基础方法提升到 Common。
 */
public class FeeDao
{
    /**
     * 查还没有收费记录的预约（status=0 已预约 且 fee 表中无该预约），供"收费登记"用。
     * 金额按套餐价入账：JOIN checkgroup 时把 g.price 一并查出，由收费界面直接入账。
     */
    public List<FeeRegVO> findUnchargedRegs()
    {
        String sql = "SELECT r.id, r.tel, u.name AS patient_name, r.gid, g.gname AS group_name, " +
                "g.price AS price, r.reg_time " +
                "FROM registration r " +
                "JOIN users u ON u.tel = r.tel " +
                "JOIN checkgroup g ON g.gid = r.gid " +
                "LEFT JOIN fee f ON f.reg_id = r.id " +
                "WHERE r.status = 0 AND f.id IS NULL " +
                "ORDER BY r.reg_time DESC";
        List<FeeRegVO> list = new ArrayList<>();
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
                FeeRegVO vo = new FeeRegVO();
                vo.setId(rs.getInt("id"));
                vo.setTel(rs.getString("tel"));
                vo.setPatientName(rs.getString("patient_name"));
                vo.setGid(rs.getString("gid"));
                vo.setGroupName(rs.getString("group_name"));
                vo.setPrice(rs.getBigDecimal("price"));
                vo.setRegTime(rs.getTimestamp("reg_time"));
                list.add(vo);
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

    /** 查询所有收费记录（带患者姓名、套餐名称） */
    public List<FeeVO> findAllFees()
    {
        String sql = "SELECT f.id, f.reg_id, f.tel, u.name AS patient_name, f.gid, g.gname AS group_name, " +
                "f.amount, f.status, f.operator, f.pay_time, f.remark " +
                "FROM fee f " +
                "JOIN users u ON u.tel = f.tel " +
                "JOIN checkgroup g ON g.gid = f.gid " +
                "ORDER BY f.pay_time DESC, f.id DESC";
        List<FeeVO> list = new ArrayList<>();
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
                FeeVO vo = new FeeVO();
                vo.setId(rs.getInt("id"));
                vo.setRegId(rs.getInt("reg_id"));
                vo.setTel(rs.getString("tel"));
                vo.setPatientName(rs.getString("patient_name"));
                vo.setGid(rs.getString("gid"));
                vo.setGroupName(rs.getString("group_name"));
                vo.setAmount(rs.getBigDecimal("amount"));
                vo.setStatus(rs.getInt("status"));
                vo.setOperator(rs.getString("operator"));
                vo.setPayTime(rs.getTimestamp("pay_time"));
                vo.setRemark(rs.getString("remark"));
                list.add(vo);
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

    /** 按预约查收费记录（判重/看是否已收费用），无则返回 null */
    public Fee findByRegId(int regId)
    {
        String sql = "SELECT * FROM fee WHERE reg_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, regId);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return mapFee(rs);
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

    /** 新增一条收费记录 */
    public boolean insert(Fee f)
    {
        String sql = "INSERT INTO fee(reg_id, tel, gid, amount, status, pay_time, operator, remark) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, f.getRegId());
            ps.setString(2, f.getTel());
            ps.setString(3, f.getGid());
            ps.setBigDecimal(4, f.getAmount());
            ps.setInt(5, f.getStatus());
            ps.setTimestamp(6, f.getPayTime() == null ? null : new java.sql.Timestamp(f.getPayTime().getTime()));
            ps.setString(7, f.getOperator());
            ps.setString(8, f.getRemark());
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

    /** 更新收费状态（退款等） */
    public boolean updateStatus(int id, int status)
    {
        String sql = "UPDATE fee SET status = ? WHERE id = ?";
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

    /** 把一行结果集转成 Fee 对象 */
    private Fee mapFee(ResultSet rs) throws SQLException
    {
        Fee f = new Fee();
        f.setId(rs.getInt("id"));
        f.setRegId(rs.getInt("reg_id"));
        f.setTel(rs.getString("tel"));
        f.setGid(rs.getString("gid"));
        f.setAmount(rs.getBigDecimal("amount"));
        f.setStatus(rs.getInt("status"));
        f.setPayTime(rs.getTimestamp("pay_time"));
        f.setOperator(rs.getString("operator"));
        f.setRemark(rs.getString("remark"));
        return f;
    }
}
