package com.ncu.report.dao;

import com.ncu.common.util.JdbcUtil;
import com.ncu.report.model.ReportItem;
import com.ncu.report.model.ReportRegVO;
import com.ncu.report.model.ReportVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 报告专用数据访问类：联表汇总患者/套餐/医生/检查项，生成体检报告
 */
public class ReportDao
{
    /** 查某患者所有已完成(status=1)的预约，用于选择要出报告的预约 */
    public List<ReportRegVO> findCompletedRegs(String tel)
    {
        String sql = "SELECT r.id, g.gname AS group_name, r.reg_time, r.status " +
                "FROM registration r JOIN checkgroup g ON g.gid = r.gid " +
                "WHERE r.tel = ? AND r.status = 1 ORDER BY r.reg_time DESC";
        List<ReportRegVO> list = new ArrayList<>();
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
                ReportRegVO vo = new ReportRegVO();
                vo.setId(rs.getInt("id"));
                vo.setGroupName(rs.getString("group_name"));
                vo.setRegTime(rs.getTimestamp("reg_time"));
                vo.setStatus(rs.getInt("status"));
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

    /** 按预约id汇总出一份完整报告 */
    public ReportVO buildReport(int regId)
    {
        ReportVO vo = new ReportVO();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = JdbcUtil.getConnection();

            // 1) 头部信息：患者 + 套餐 + 预约时间
            String headerSql = "SELECT u.name AS patient_name, u.sex, u.birthday, g.gname AS group_name, r.reg_time " +
                    "FROM registration r " +
                    "JOIN users u ON u.tel = r.tel " +
                    "JOIN checkgroup g ON g.gid = r.gid " +
                    "WHERE r.id = ?";
            ps = conn.prepareStatement(headerSql);
            ps.setInt(1, regId);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                return null;
            }
            vo.setPatientName(rs.getString("patient_name"));
            vo.setSex(rs.getString("sex"));
            vo.setBirthday(rs.getDate("birthday"));
            vo.setGroupName(rs.getString("group_name"));
            vo.setRegTime(rs.getTimestamp("reg_time"));
            JdbcUtil.close(null, ps, rs);

            // 2) 录入医生（取该预约第一条结果的医生）
            String doctorSql = "SELECT u.name AS doctor_name FROM check_result cr " +
                    "JOIN users u ON u.tel = cr.doctor_tel WHERE cr.reg_id = ? LIMIT 1";
            ps = conn.prepareStatement(doctorSql);
            ps.setInt(1, regId);
            rs = ps.executeQuery();
            if (rs.next())
            {
                vo.setDoctorName(rs.getString("doctor_name"));
            }
            JdbcUtil.close(null, ps, rs);

            // 3) 检查项明细
            String itemSql = "SELECT ci.cname, cr.result_value, ci.dw, ci.ckfw, cr.check_time " +
                    "FROM check_result cr JOIN checkitem ci ON ci.cid = cr.cid " +
                    "WHERE cr.reg_id = ? ORDER BY cr.check_time";
            ps = conn.prepareStatement(itemSql);
            ps.setInt(1, regId);
            rs = ps.executeQuery();
            List<ReportItem> items = new ArrayList<>();
            while (rs.next())
            {
                ReportItem item = new ReportItem();
                item.setCname(rs.getString("cname"));
                item.setResultValue(rs.getString("result_value"));
                item.setDw(rs.getString("dw"));
                item.setCkfw(rs.getString("ckfw"));
                item.setCheckTime(rs.getTimestamp("check_time"));
                items.add(item);
            }
            vo.setItems(items);
            return vo;
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
}
