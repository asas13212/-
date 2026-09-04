package com.ncu.admin.view;

import com.ncu.admin.controller.AdminController;
import com.ncu.admin.model.RegistrationVO;
import com.ncu.common.model.CheckItem;
import com.ncu.common.model.CheckResult;
import com.ncu.common.model.Registration;
import com.ncu.common.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检查结果录入面板：顶部下拉选择一条「待录入(已预约)」的预约，
 * 选中后自动列出该预约已录的结果。
 * - 录入医生 = 当前登录人（构造器传入的 operatorTel），不再手工填账号；
 * - 「自动生成全部结果」：按套餐补全所有未录检查项的正常范围演示值，一次生成。
 */
public class CheckResultPanel extends JPanel
{
    private final AdminController controller = new AdminController();
    private final String operatorTel;   // 当前登录医生账号，作为录入人
    private final JComboBox<String> regBox = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final String[] columns = {"id", "预约id", "患者账号", "检查项", "结果值", "医生", "检查时间"};
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final Pattern NUM = Pattern.compile("[0-9]+(\\.[0-9]+)?");

    private List<RegistrationVO> regs;   // 下拉当前对应的待录入预约
    private boolean syncing;             // 重建下拉时忽略选中事件

    public CheckResultPanel(String operatorTel)
    {
        this.operatorTel = operatorTel;
        setLayout(new BorderLayout());
        Ui.content(this);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        tableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        table = Ui.table(tableModel);

        // ---- 页头两行：标题一行，选择+动作一行（避免窄窗口把控件挤出） ----
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel rowTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rowTitle.setOpaque(false);
        rowTitle.add(Ui.title("检查结果录入"));
        top.add(rowTitle);

        JPanel rowCtl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        rowCtl.setOpaque(false);
        JLabel sel = new JLabel("待录入预约:");
        sel.setFont(Ui.font(13));
        rowCtl.add(sel);
        regBox.setPreferredSize(new Dimension(280, 28));
        rowCtl.add(regBox);
        rowCtl.add(Ui.actionPrimary("自动生成全部结果", e -> autoGenerateAll()));
        rowCtl.add(Ui.action("录入结果", e -> addResult()));
        rowCtl.add(Ui.action("标记已完成", e -> markDone()));
        top.add(rowCtl);

        add(top, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        add(Ui.card(sp), BorderLayout.CENTER);

        regBox.addItemListener(this::onRegSelected);
        refresh();
    }

    /** 重建下拉：只列出「已预约(status=0)」、还没录完的预约 */
    private void refresh()
    {
        syncing = true;
        regBox.removeAllItems();
        regs = controller.listPendingRegistrations();
        for (RegistrationVO r : regs)
        {
            regBox.addItem(itemText(r));
        }
        syncing = false;

        if (regs.isEmpty())
        {
            tableModel.setRowCount(0);
            return;
        }
        regBox.setSelectedIndex(0);
        querySelected();
    }

    private void onRegSelected(ItemEvent e)
    {
        if (syncing || e.getStateChange() != ItemEvent.SELECTED)
        {
            return;
        }
        querySelected();
    }

    /** 当前下拉选中的预约 id；无选中返回 -1 */
    private int selectedRegId()
    {
        int idx = regBox.getSelectedIndex();
        if (regs == null || idx < 0 || idx >= regs.size())
        {
            return -1;
        }
        return regs.get(idx).getId();
    }

    private Registration selectedReg()
    {
        int regId = selectedRegId();
        if (regId < 0)
        {
            return null;
        }
        return controller.findRegById(regId);
    }

    private void querySelected()
    {
        int regId = selectedRegId();
        if (regId < 0)
        {
            tableModel.setRowCount(0);
            return;
        }
        tableModel.setRowCount(0);
        for (CheckResult r : controller.listResults(regId))
        {
            tableModel.addRow(new Object[]{r.getId(), r.getRegId(), r.getTel(), r.getCid(),
                    r.getResultValue(), r.getDoctorTel(),
                    r.getCheckTime() == null ? "" : TIME_FMT.format(r.getCheckTime())});
        }
    }

    /** 手工录入单条：医生账号直接用当前登录人，不再手工填 */
    private void addResult()
    {
        Registration reg = selectedReg();
        if (reg == null)
        {
            JOptionPane.showMessageDialog(this, "没有待录入的预约，请先在顶部下拉框选择一条");
            return;
        }
        if (reg.getStatus() == 1)
        {
            JOptionPane.showMessageDialog(this, "该预约已完成，无需再录入");
            refresh();
            return;
        }
        String[] vals = DialogUtil.prompt(this, "录入检查结果",
                new String[]{"检查项id", "结果值"}, null);
        if (vals == null) return;
        CheckResult r = new CheckResult();
        r.setRegId(reg.getId());
        r.setTel(reg.getTel());
        r.setCid(vals[0]);
        r.setResultValue(vals[1]);
        r.setDoctorTel(operatorTel);
        r.setCheckTime(new Date());
        if (controller.addResult(r))
        {
            querySelected();
            maybeAutoComplete(reg.getId()); // 录齐后询问是否直接标记完成
        }
        else
        {
            JOptionPane.showMessageDialog(this, "录入失败，请检查检查项id是否正确");
        }
    }

    /** 一键按套餐自动补全所有未录检查项（正常范围演示值），录齐后询问是否标完成 */
    private void autoGenerateAll()
    {
        Registration reg = selectedReg();
        if (reg == null)
        {
            JOptionPane.showMessageDialog(this, "没有待录入的预约，请先在顶部下拉框选择一条");
            return;
        }
        if (reg.getStatus() == 1)
        {
            JOptionPane.showMessageDialog(this, "该预约已完成，无需再录入");
            refresh();
            return;
        }
        List<CheckItem> needed = controller.listGroupItems(reg.getGid());
        if (needed.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "该套餐没有检查项，无法自动生成");
            return;
        }
        int added = autoFill(reg, needed);
        querySelected();
        if (added > 0)
        {
            JOptionPane.showMessageDialog(this, "已自动生成 " + added + " 项检查结果");
        }
        else
        {
            JOptionPane.showMessageDialog(this, "该套餐的检查项此前都已录入，无需生成");
        }
        maybeAutoComplete(reg.getId());
    }

    /** 把套餐里还没录的检查项用"正常范围"演示值补全，返回新增条数 */
    private int autoFill(Registration reg, List<CheckItem> needed)
    {
        Set<String> recorded = new HashSet<>();
        for (CheckResult r : controller.listResults(reg.getId()))
        {
            recorded.add(r.getCid());
        }
        int added = 0;
        for (CheckItem it : needed)
        {
            if (recorded.contains(it.getCid()))
            {
                continue;
            }
            CheckResult r = new CheckResult();
            r.setRegId(reg.getId());
            r.setTel(reg.getTel());
            r.setCid(it.getCid());
            r.setResultValue(sampleValue(it));
            r.setDoctorTel(operatorTel);
            r.setCheckTime(new Date());
            if (controller.addResult(r))
            {
                added++;
            }
        }
        return added;
    }

    /** 按参考范围自动算一个"正常范围内"的演示值 */
    private String sampleValue(CheckItem it)
    {
        String cname = it.getCname();
        if (cname != null && cname.contains("身高"))
        {
            return "172/68"; // 身高体重两段式
        }
        String ckfw = it.getCkfw();
        if (ckfw == null || ckfw.trim().isEmpty())
        {
            return "正常";
        }
        String t = ckfw.trim();
        List<Double> nums = new ArrayList<>();
        Matcher m = NUM.matcher(t);
        while (m.find())
        {
            nums.add(Double.parseDouble(m.group()));
        }
        if (nums.size() >= 2)
        {
            double lo = nums.get(0);
            double hi = nums.get(nums.size() - 1);
            return fmtNum((lo + hi) / 2);
        }
        if (nums.size() == 1)
        {
            return fmtNum(nums.get(0));
        }
        return "—".equals(t) ? "正常" : t; // 阴性/未见异常/阳性 等直接作演示值
    }

    private String fmtNum(double d)
    {
        double r = Math.round(d * 10) / 10.0;
        long l = Math.round(r);
        return Math.abs(r - l) < 0.0001 ? String.valueOf(l) : String.format("%.1f", r);
    }

    /** 标记当前预约为已完成(status=1)，之后患者端才能出报告 */
    private void markDone()
    {
        Registration reg = selectedReg();
        if (reg == null)
        {
            JOptionPane.showMessageDialog(this, "没有待录入的预约，请先在顶部下拉框选择一条");
            return;
        }
        if (reg.getStatus() == 1)
        {
            JOptionPane.showMessageDialog(this, "该预约已完成");
            refresh();
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "确定把预约 #" + reg.getId() + " 标记为「已完成」吗？\n标记后患者即可在「我的结果」中看到并打印报告。",
                "标记完成", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;
        if (controller.updateRegStatus(reg.getId(), 1))
        {
            JOptionPane.showMessageDialog(this, "已标记为已完成");
        }
        else
        {
            JOptionPane.showMessageDialog(this, "操作失败，请重试");
        }
        refresh(); // 已完成的不再属于「待录入」，从下拉移除
    }

    /** 录入/自动生成一条结果后，若该预约套餐内的检查项已全部录齐，则询问是否直接标记完成 */
    private void maybeAutoComplete(int regId)
    {
        Registration reg = controller.findRegById(regId);
        if (reg == null || reg.getStatus() != 0) return;
        List<CheckItem> needed = controller.listGroupItems(reg.getGid());
        if (needed.isEmpty()) return;
        Set<String> recorded = new HashSet<>();
        for (CheckResult r : controller.listResults(regId))
        {
            recorded.add(r.getCid());
        }
        for (CheckItem it : needed)
        {
            if (!recorded.contains(it.getCid())) return; // 还没录齐
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "该套餐所有检查项已全部录入，是否立即将该预约标记为「已完成」？",
                "录入完毕", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION)
        {
            controller.updateRegStatus(regId, 1);
        }
        refresh();
    }

    private String itemText(RegistrationVO r)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(r.getId());
        if (r.getPatientName() != null) sb.append(" ").append(r.getPatientName());
        if (r.getGroupName() != null) sb.append(" ").append(r.getGroupName());
        if (r.getRegTime() != null) sb.append("  ").append(TIME_FMT.format(r.getRegTime()));
        return sb.toString();
    }
}
