package com.oopsmyschedule.ui;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

/** Hiển thị {@link ScheduleItem} trong ngày dạng danh sách thẻ dọc, tô màu theo PriorityLevel. */
public class TimelinePanel extends JPanel {

    private final JPanel danhSachPanel = new JPanel();
    private Consumer<ScheduleItem> khiBamHoanThanh;
    private Consumer<ScheduleItem> khiBamSua;
    private Consumer<ScheduleItem> khiBamXoa;
    private LocalTime gioHienTaiDeToDam = null;

    public TimelinePanel() {
        setLayout(new BorderLayout());
        setBackground(VintageTheme.NEN_CHINH);
        danhSachPanel.setLayout(new BoxLayout(danhSachPanel, BoxLayout.Y_AXIS));
        danhSachPanel.setBackground(VintageTheme.NEN_CHINH);
        JScrollPane scroll = new JScrollPane(danhSachPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void datHanhDongHoanThanh(Consumer<ScheduleItem> callback) {
        this.khiBamHoanThanh = callback;
    }

    /** Callback khi bấm "✏️ Sửa" trên 1 thẻ việc — dùng để mở AddItemDialog ở chế độ sửa. */
    public void datHanhDongSua(Consumer<ScheduleItem> callback) {
        this.khiBamSua = callback;
    }

    /** Callback khi bấm "🗑️ Xóa" trên 1 thẻ việc — MainFrame sẽ tự hỏi xác nhận trước khi xóa thật. */
    public void datHanhDongXoa(Consumer<ScheduleItem> callback) {
        this.khiBamXoa = callback;
    }

    public void capNhat(List<ScheduleItem> danhSach, LocalTime gioHienTai) {
        this.gioHienTaiDeToDam = gioHienTai;
        danhSachPanel.removeAll();
        if (danhSach.isEmpty()) {
            JLabel trong = new JLabel("Chưa có việc nào trong lịch — bấm \"+ Thêm việc\" để bắt đầu.");
            trong.setForeground(VintageTheme.CHU_MO);
            trong.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
            danhSachPanel.add(trong);
        }
        for (ScheduleItem item : danhSach) {
            danhSachPanel.add(taoThe(item));
            danhSachPanel.add(Box.createVerticalStrut(8));
        }
        danhSachPanel.revalidate();
        danhSachPanel.repaint();
    }

    private JPanel taoThe(ScheduleItem item) {
        JPanel the = new JPanel(new BorderLayout(10, 0));
        the.setBackground(VintageTheme.NEN_THE);
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        boolean dangDienRa = gioHienTaiDeToDam != null
            && !item.getGioBatDau().isAfter(gioHienTaiDeToDam) && item.getGioKetThuc().isAfter(gioHienTaiDeToDam);
        Color vienMau = dangDienRa ? VintageTheme.XANH_DUONG : VintageTheme.VIEN;
        the.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(vienMau, dangDienRa ? 2 : 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // Dải màu ưu tiên bên trái
        JPanel dai = new JPanel();
        dai.setBackground(VintageTheme.mauTheoUuTien(item.getDoUuTien()));
        dai.setPreferredSize(new Dimension(8, 10));
        the.add(dai, BorderLayout.WEST);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        String gio = item.getGioBatDau() + " – " + item.getGioKetThuc();
        // FIX: rút gọn tên việc dài (giữ nguyên phần giờ/icon/trạng thái ở
        // trước và sau), kèm tooltip để vẫn xem được tên đầy đủ khi cần.
        String tenRutGon = DinhDang.rutGon(item.getTen(), 40);
        JLabel dong1 = new JLabel(gio + "   " + bieuTuongLoai(item) + " " + tenRutGon
            + (item.isHoanThanh() ? "  ✅" : ""));
        if (!tenRutGon.equals(item.getTen())) {
            dong1.setToolTipText(item.getTen());
        }
        dong1.setFont(VintageTheme.FONT_NHAN);
        dong1.setForeground(VintageTheme.CHU_CHINH);
        JLabel dong2 = new JLabel(item.loaiViec() + " · " + item.getDoUuTien() + " · " + item.moTaTrangThai());
        dong2.setFont(VintageTheme.FONT_PHU_DE);
        dong2.setForeground(VintageTheme.CHU_MO);
        giua.add(dong1);
        giua.add(dong2);
        the.add(giua, BorderLayout.CENTER);

        JPanel nutBen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        nutBen.setOpaque(false);
        NutTron sua = new NutTron("✏️ Sửa");
        sua.addActionListener(e -> { if (khiBamSua != null) khiBamSua.accept(item); });
        nutBen.add(sua);
        if (!item.isHoanThanh()) {
            NutTron xong = new NutTron("Xong", NutTron.Kieu.CHINH, VintageTheme.XANH_MINT);
            xong.addActionListener(e -> {
                item.danhDauHoanThanh();
                if (khiBamHoanThanh != null) khiBamHoanThanh.accept(item);
            });
            nutBen.add(xong);
        }
        NutTron xoa = new NutTron("🗑️ Xóa", NutTron.Kieu.PHU, VintageTheme.BAT_BUOC);
        xoa.setToolTipText("Xóa/Hủy hoạt động này khỏi lịch");
        xoa.addActionListener(e -> { if (khiBamXoa != null) khiBamXoa.accept(item); });
        nutBen.add(xoa);
        the.add(nutBen, BorderLayout.EAST);
        return the;
    }

    private String bieuTuongLoai(ScheduleItem item) {
        return BieuTuongViec.cua(item);
    }
}
