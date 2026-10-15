package com.oopsmyschedule.ui;

import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.KetQuaSoSanh;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/** Dialog "So sánh lịch trình" — hiển thị trước/sau, danh sách thay đổi, cảnh báo. */
public class ComparisonDialog extends JDialog {

    private boolean daXacNhan = false;

    public ComparisonDialog(Frame parent, KetQuaSoSanh ketQua) {
        super(parent, "So sánh lịch trình", true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(VintageTheme.NEN_CHINH);
        setSize(680, 480);
        setLocationRelativeTo(parent);

        JPanel coTruocSau = new JPanel(new GridLayout(1, 2, 10, 0));
        coTruocSau.setBackground(VintageTheme.NEN_CHINH);
        coTruocSau.add(taoCotLich("Trước điều chỉnh", ketQua.getLichTruoc().layLichHienTai()));
        coTruocSau.add(taoCotLich("Sau điều chỉnh", ketQua.getLichSau().layLichHienTai()));

        JPanel thongTin = new JPanel();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.setBackground(VintageTheme.NEN_CHINH);
        thongTin.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel tieuDeThayDoi = new JLabel("Các thay đổi:");
        tieuDeThayDoi.setFont(VintageTheme.FONT_NHAN);
        thongTin.add(tieuDeThayDoi);
        for (String td : ketQua.getDanhSachThayDoi()) {
            thongTin.add(nhanChu("• " + td, VintageTheme.CHU_CHINH));
        }
        if (!ketQua.getDanhSachCanhBao().isEmpty()) {
            JLabel tieuDeCanhBao = new JLabel("⚠ Cảnh báo:");
            tieuDeCanhBao.setFont(VintageTheme.FONT_NHAN);
            tieuDeCanhBao.setForeground(VintageTheme.BAT_BUOC);
            thongTin.add(Box.createVerticalStrut(6));
            thongTin.add(tieuDeCanhBao);
            for (String cb : ketQua.getDanhSachCanhBao()) {
                thongTin.add(nhanChu("• " + cb, VintageTheme.BAT_BUOC));
            }
        }

        JScrollPane cuonThongTin = new JScrollPane(thongTin);
        cuonThongTin.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, coTruocSau, cuonThongTin);
        split.setResizeWeight(0.6);
        split.setBorder(null);

        NutTron xacNhan = new NutTron("✅ Xác nhận áp dụng", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        NutTron chinhSua = new NutTron("Chỉnh sửa lại");
        xacNhan.addActionListener(e -> { daXacNhan = true; setVisible(false); });
        chinhSua.addActionListener(e -> { daXacNhan = false; setVisible(false); });

        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        duoi.setBackground(VintageTheme.NEN_CHINH);
        duoi.add(chinhSua);
        duoi.add(xacNhan);

        add(split, BorderLayout.CENTER);
        add(duoi, BorderLayout.SOUTH);
    }

    private JLabel nhanChu(String text, Color mau) {
        JLabel l = new JLabel("<html><body style='width:280px'>" + text + "</body></html>");
        l.setFont(VintageTheme.FONT_PHU_DE);
        l.setForeground(mau);
        return l;
    }

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private JPanel taoCotLich(String tieuDe, java.util.List<ScheduleItem> danhSach) {
        JPanel cot = new JPanel();
        cot.setLayout(new BoxLayout(cot, BoxLayout.Y_AXIS));
        cot.setBackground(VintageTheme.NEN_THE);
        cot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VintageTheme.VIEN),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel tieu = new JLabel(tieuDe);
        tieu.setFont(VintageTheme.FONT_NHAN);
        cot.add(tieu);
        cot.add(Box.createVerticalStrut(6));

        for (ScheduleItem item : danhSach) {JLabel dong = new JLabel(item.getGioBatDau().format(DINH_DANG_GIO) + " – " + item.getGioKetThuc().format(DINH_DANG_GIO) + "  " + BieuTuongViec.cua(item) + " " + DinhDang.rutGon(item.getTen(), 20));
            dong.setFont(VintageTheme.FONT_PHU_DE);
            dong.setForeground(VintageTheme.mauTheoUuTien(item.getDoUuTien()).darker());
            dong.setToolTipText(item.getTen());
            cot.add(dong);
        }
        JScrollPane cuon = new JScrollPane(cot);
        cuon.setBorder(BorderFactory.createEmptyBorder());
        JPanel boc = new JPanel(new BorderLayout());
        boc.add(cuon);
        return boc;
    }

    public boolean daDuocXacNhan() {
        return daXacNhan;
    }
}
