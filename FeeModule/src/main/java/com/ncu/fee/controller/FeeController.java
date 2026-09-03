package com.ncu.fee.controller;

import com.ncu.common.model.Fee;
import com.ncu.fee.dao.FeeDao;
import com.ncu.fee.model.FeeRegVO;
import com.ncu.fee.model.FeeVO;

import java.util.List;

/**
 * 收费业务控制层：接收 view 的参数，调用 dao 完成业务
 */
public class FeeController
{
    private final FeeDao feeDao = new FeeDao();

    /** 待收费的预约列表（还没有收费记录的"已预约"） */
    public List<FeeRegVO> listUnchargedRegs()
    {
        return feeDao.findUnchargedRegs();
    }

    /** 全部收费记录 */
    public List<FeeVO> listFees()
    {
        return feeDao.findAllFees();
    }

    /** 该预约是否已经收费（防重复收费） */
    public boolean regHasFee(int regId)
    {
        return feeDao.findByRegId(regId) != null;
    }

    /**
     * 收费入账：给预约登记一条已缴(1)的收费记录。
     * 返回 null 表示成功；否则返回给界面提示的错误信息。
     */
    public String charge(int regId, String tel, String gid, java.math.BigDecimal amount, String operator)
    {
        if (regHasFee(regId))
        {
            return "该预约已经收过费，不能重复收费";
        }
        if (amount == null || amount.signum() <= 0)
        {
            return "金额必须大于 0";
        }
        Fee f = new Fee();
        f.setRegId(regId);
        f.setTel(tel);
        f.setGid(gid);
        f.setAmount(amount);
        f.setStatus(1);                    // 1=已缴（登记即入账）
        f.setPayTime(new java.util.Date());
        f.setOperator(operator);
        f.setRemark("体检收费");
        return feeDao.insert(f) ? null : "收费保存失败，请重试";
    }

    /** 退款：把一条"已缴"记录置为"已退款(2)" */
    public String refund(int feeId)
    {
        return feeDao.updateStatus(feeId, 2) ? null : "退款失败，请重试";
    }
}
