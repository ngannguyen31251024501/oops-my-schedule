package com.oopsmyschedule.ui;

import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.CanhBaoViec;
import com.oopsmyschedule.service.MucDoCanhBao;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Màn hình "Trang chủ" — mở app là thấy ngay: bầu trời hoạt hình đổi màu
 * theo buổi (sáng/trưa/chiều/tối/đêm khuya, có mặt trời/mặt trăng/mây/sao),
 * lời chào, rồi CHỈ 3 nhóm việc:
 * <ol>
 *   <li>⚠ Đang bị trễ — nếu chỉ 1 việc thì hiện thẻ chi tiết kèm 2 nút riêng;
 *       nếu NHIỀU việc cùng lúc thì gộp lại thành 1 thẻ cảnh báo duy nhất với
 *       1 nút "ĐIỀU CHỈNH LỊCH" chung (tránh dashboard bị "tham" khi có 4-5 việc trễ
 *       một lúc — xem thiết kế đã bàn).</li>
 *   <li>🕐 Đang diễn ra — việc vừa mới bắt đầu (dưới 5 phút), chưa cần lo.</li>
 *   <li>📅 Sắp tới hôm nay — việc chưa tới giờ, chỉ nổi bật việc gần nhất +
 *       preview tối đa 3 việc kế tiếp.</li>
 * </ol>
 * Việc đã đánh dấu Xong hoặc đã kết thúc từ lâu sẽ KHÔNG hiện ở đây nữa.
 * Cuối danh sách luôn có nút "📅 Xem cả ngày" để qua tab đầy đủ (xem/sửa mọi
 * việc, kể cả những việc đã xong) — Trang chủ chỉ trả lời "bây giờ tôi cần
 * quan tâm gì", không phải nơi quản lý toàn bộ lịch.
 */
public class TrangChuPanel extends JPanel {

    private final SkyPanel skyPanel = new SkyPanel();
    private final JLabel nhanChao = new JLabel();
    private final JLabel nhanNgay = new JLabel();
    private final JLabel bieuTuongBuoi = new JLabel();
    private final JPanel noiDung = new JPanel();
    private final MeoNho meoNho = new MeoNho();

    private Consumer<ScheduleItem> khiBamXong;
    private Consumer<Integer> khiBamDieuChinhLich;
    private Runnable khiBamXemCaNgay;

    public TrangChuPanel() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        add(taoHeaderChao(), BorderLayout.NORTH);

        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setOpaque(false);
        noiDung.setBorder(BorderFactory.createEmptyBorder(2, 2, 8, 2));
        JScrollPane scroll = new JScrollPane(noiDung);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel taoHeaderChao() {
        skyPanel.setBorder(BorderFactory.createEmptyBorder());

        RoundedPanel theChao = new RoundedPanel(18);
        theChao.setBackground(new Color(255, 255, 255, 232));
        theChao.setLayout(new BorderLayout(12, 0));
        theChao.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        bieuTuongBuoi.setFont(new Font("SansSerif", Font.PLAIN, 36));
        theChao.add(bieuTuongBuoi, BorderLayout.WEST);

        JPanel chuVien = new JPanel();
        chuVien.setOpaque(false);
        chuVien.setLayout(new BoxLayout(chuVien, BoxLayout.Y_AXIS));
        nhanChao.setFont(VintageTheme.FONT_TIEU_DE);
        nhanChao.setForeground(VintageTheme.CHU_CHINH);
        nhanNgay.setFont(VintageTheme.FONT_PHU_DE);
        nhanNgay.setForeground(VintageTheme.CHU_MO);
        chuVien.add(nhanChao);
        chuVien.add(Box.createVerticalStrut(2));
        chuVien.add(nhanNgay);
        theChao.add(chuVien, BorderLayout.CENTER);

        JPanel hangMeo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangMeo.setOpaque(false);
        hangMeo.add(meoNho);

        JPanel demTrong = new JPanel(new BorderLayout());
        demTrong.setOpaque(false);
        demTrong.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        demTrong.add(hangMeo, BorderLayout.NORTH);
        demTrong.add(theChao, BorderLayout.SOUTH);

        skyPanel.add(demTrong, BorderLayout.CENTER);
        return skyPanel;
    }

    public void datHanhDongXong(Consumer<ScheduleItem> callback) {
        this.khiBamXong = callback;
    }

    /** callback nhận vào "số phút trễ gợi ý" (null nếu gộp nhiều việc, không rõ dùng số nào làm gợi ý). */
    public void datHanhDongDieuChinhLich(Consumer<Integer> callback) {
        this.khiBamDieuChinhLich = callback;
    }

    public void datHanhDongXemCaNgay(Runnable callback) {
        this.khiBamXemCaNgay = callback;
    }

    public void capNhat(List<CanhBaoViec> canhBaoList, List<ScheduleItem> sapToi, LocalTime hienTai) {
        capNhatLoiChao(hienTai);

        noiDung.removeAll();

        List<CanhBaoViec> dangTre = canhBaoList.stream()
            .filter(c -> c.getMucDo() != MucDoCanhBao.MOI_BAT_DAU).toList();
        List<CanhBaoViec> vuaBatDau = canhBaoList.stream()
            .filter(c -> c.getMucDo() == MucDoCanhBao.MOI_BAT_DAU).toList();

        // Mèo nhỏ ở đầu Trang chủ đổi biểu cảm theo đúng trạng thái lịch hiện
        // tại — trở thành 1 phần của feedback UI thay vì chỉ trang trí.
        if (!dangTre.isEmpty()) {
            meoNho.datBieuCam(MascotArt.BieuCam.OOPS);
        } else if (!vuaBatDau.isEmpty()) {
            meoNho.datBieuCam(MascotArt.BieuCam.VUI);
        } else if (dangTre.isEmpty() && vuaBatDau.isEmpty() && sapToi.isEmpty()) {
            meoNho.datBieuCam(MascotArt.BieuCam.VUI);
        } else {
            meoNho.datBieuCam(MascotArt.BieuCam.BINH_THUONG);
        }

        if (dangTre.size() == 1) {
            noiDung.add(nhanTieuDe("⚠️ Đang bị trễ", VintageTheme.BAT_BUOC));
            noiDung.add(theTreChiTiet(dangTre.get(0)));
            noiDung.add(Box.createVerticalStrut(10));
        } else if (dangTre.size() > 1) {
            noiDung.add(nhanTieuDe("⚠️ Bạn đang trễ " + dangTre.size() + " hoạt động", VintageTheme.BAT_BUOC));
            noiDung.add(theTreGop(dangTre));
            noiDung.add(Box.createVerticalStrut(10));
        }

        if (!vuaBatDau.isEmpty()) {
            noiDung.add(nhanTieuDe("🕐 Đang diễn ra", VintageTheme.CHU_CHINH));
            for (CanhBaoViec c : vuaBatDau) {
                noiDung.add(theBinhThuong(c.getItem(), "Vừa bắt đầu " + DinhDang.phut(c.getSoPhutTre()) + " trước"));
                noiDung.add(Box.createVerticalStrut(8));
            }
            noiDung.add(Box.createVerticalStrut(10));
        }

        if (!sapToi.isEmpty()) {
            noiDung.add(nhanTieuDe("📅 Sắp tới hôm nay", VintageTheme.CHU_CHINH));
            ScheduleItem ganNhat = sapToi.get(0);
            noiDung.add(theSapToiNoiBat(ganNhat));
            noiDung.add(Box.createVerticalStrut(8));
            List<ScheduleItem> conLai = sapToi.size() > 1 ? sapToi.subList(1, Math.min(sapToi.size(), 4)) : List.of();
            for (ScheduleItem item : conLai) {
                noiDung.add(theBinhThuong(
                        item,
                        "Bắt đầu lúc " + item.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm"))
                ));                noiDung.add(Box.createVerticalStrut(8));
            }
            if (sapToi.size() > 4) {
                JLabel conNua = new JLabel("… và " + (sapToi.size() - 4) + " việc nữa, xem ở \"Xem cả ngày\"");
                conNua.setFont(VintageTheme.FONT_PHU_DE);
                conNua.setForeground(VintageTheme.CHU_MO);
                conNua.setBorder(BorderFactory.createEmptyBorder(0, 6, 8, 0));
                noiDung.add(conNua);
            }
        }

        if (dangTre.isEmpty() && vuaBatDau.isEmpty() && sapToi.isEmpty()) {
            JLabel trong = new JLabel("<html><body style='width:400px'>🎉 Không còn việc nào trong hôm nay cả — "
                + "nghỉ ngơi thôi, hoặc bấm \"+ Thêm việc\" để lên kế hoạch tiếp!</body></html>");
            trong.setForeground(VintageTheme.CHU_MO);
            trong.setBorder(BorderFactory.createEmptyBorder(24, 4, 24, 4));
            noiDung.add(trong);
        }

        noiDung.add(Box.createVerticalStrut(6));
        noiDung.add(taoNutXemCaNgay());

        noiDung.revalidate();
        noiDung.repaint();
    }

    private JPanel taoNutXemCaNgay() {
        JPanel hang = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        hang.setOpaque(false);
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        NutTron nut = new NutTron("📅 Xem cả ngày →", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        nut.addActionListener(e -> { if (khiBamXemCaNgay != null) khiBamXemCaNgay.run(); });
        hang.add(nut);
        return hang;
    }

    private JLabel nhanTieuDe(String text, Color mau) {
        JLabel l = new JLabel(text);
        l.setFont(VintageTheme.FONT_NHAN.deriveFont(15f));
        l.setForeground(mau);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 0));
        return l;
    }

    private JPanel theTreChiTiet(CanhBaoViec c) {
        ScheduleItem item = c.getItem();
        RoundedPanel the = new RoundedPanel(14, VintageTheme.BAT_BUOC, 2f);
        the.setLayout(new BorderLayout(10, 0));
        the.setBackground(VintageTheme.nhat(VintageTheme.BAT_BUOC, 0.9f));
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        the.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 12));

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        DateTimeFormatter dinhDangGio = DateTimeFormatter.ofPattern("HH:mm");

        JLabel dong1 = new JLabel(BieuTuongViec.cua(item) + " " + DinhDang.rutGon(item.getTen(), 28)
                + "  (" + item.getGioBatDau().format(dinhDangGio)
                + " – " + item.getGioKetThuc().format(dinhDangGio) + ")");
        dong1.setFont(VintageTheme.FONT_NHAN);
        dong1.setToolTipText(item.getTen());
        JLabel dong2 = new JLabel("🚨 App phát hiện: đã trễ " + DinhDang.phut(c.getSoPhutTre()) + " — " + c.getMucDo().getNhan());
        dong2.setFont(VintageTheme.FONT_PHU_DE);
        dong2.setForeground(VintageTheme.BAT_BUOC.darker());
        giua.add(dong1);
        giua.add(dong2);
        the.add(giua, BorderLayout.CENTER);

        JPanel nutBen = new JPanel(new GridLayout(2, 1, 0, 4));
        nutBen.setOpaque(false);
        NutTron dieuChinhLichNut = new NutTron("✨ Điều chỉnh lịch", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        dieuChinhLichNut.addActionListener(e -> { if (khiBamDieuChinhLich != null) khiBamDieuChinhLich.accept((int) c.getSoPhutTre()); });
        NutTron xongNut = new NutTron("Đã xong");
        xongNut.addActionListener(e -> {
            item.danhDauHoanThanh();
            if (khiBamXong != null) khiBamXong.accept(item);
        });
        nutBen.add(dieuChinhLichNut);
        nutBen.add(xongNut);
        the.add(nutBen, BorderLayout.EAST);
        return the;
    }

    private JPanel theTreGop(List<CanhBaoViec> dangTre) {
        RoundedPanel the = new RoundedPanel(14, VintageTheme.BAT_BUOC, 2f);
        the.setLayout(new BorderLayout(0, 8));
        the.setBackground(VintageTheme.nhat(VintageTheme.BAT_BUOC, 0.9f));
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel danhSach = new JPanel();
        danhSach.setOpaque(false);
        danhSach.setLayout(new BoxLayout(danhSach, BoxLayout.Y_AXIS));
        for (CanhBaoViec c : dangTre) {
            JLabel dong = new JLabel(BieuTuongViec.cua(c.getItem()) + " " + DinhDang.rutGon(c.getItem().getTen(), 24)
                + " — trễ " + DinhDang.phut(c.getSoPhutTre()));
            dong.setFont(VintageTheme.FONT_PHU_DE);
            dong.setForeground(VintageTheme.CHU_CHINH);
            dong.setAlignmentX(Component.LEFT_ALIGNMENT);
            dong.setToolTipText(c.getItem().getTen());
            danhSach.add(dong);
        }
        the.add(danhSach, BorderLayout.CENTER);

        NutTron dieuChinhLichNut = new NutTron("🚨 ĐIỀU CHỈNH LỊCH", NutTron.Kieu.CHINH, VintageTheme.BAT_BUOC);
        dieuChinhLichNut.addActionListener(e -> { if (khiBamDieuChinhLich != null) khiBamDieuChinhLich.accept(null); });
        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        hangNut.setOpaque(false);
        hangNut.add(dieuChinhLichNut);
        the.add(hangNut, BorderLayout.SOUTH);

        return the;
    }

    private JPanel theSapToiNoiBat(ScheduleItem item) {
        RoundedPanel the = new RoundedPanel(16, VintageTheme.XANH_DUONG, 1.6f);
        the.setBackground(VintageTheme.VANG_KEM);
        the.setLayout(new BorderLayout(10, 4));
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        the.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 14));

        RoundedPanel dai = new RoundedPanel(4);
        dai.setBackground(VintageTheme.mauTheoUuTien(item.getDoUuTien()));
        dai.setPreferredSize(new Dimension(8, 10));
        the.add(dai, BorderLayout.WEST);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        JLabel dong1 = new JLabel(BieuTuongViec.cua(item) + " " + DinhDang.rutGon(item.getTen(), 30));
        dong1.setFont(VintageTheme.FONT_TIEU_DE.deriveFont(17f));
        dong1.setForeground(VintageTheme.CHU_CHINH);
        dong1.setToolTipText(item.getTen());
        JLabel dong2 = new JLabel(DinhDang.gio(item.getGioBatDau()) + " – " + DinhDang.gio(item.getGioKetThuc())
                + "   ·   " + item.loaiViec() + " · " + item.getDoUuTien());
        dong2.setFont(VintageTheme.FONT_PHU_DE);
        dong2.setForeground(VintageTheme.CHU_MO);
        giua.add(dong1);
        giua.add(dong2);
        the.add(giua, BorderLayout.CENTER);

        NutTron xongNut = new NutTron("Xong", NutTron.Kieu.CHINH, VintageTheme.XANH_MINT);
        xongNut.addActionListener(e -> {
            item.danhDauHoanThanh();
            if (khiBamXong != null) khiBamXong.accept(item);
        });
        the.add(xongNut, BorderLayout.EAST);
        return the;
    }

    private JPanel theBinhThuong(ScheduleItem item, String phuDe) {
        RoundedPanel the = new RoundedPanel(14, VintageTheme.VIEN, 1.1f);
        the.setLayout(new BorderLayout(10, 0));
        the.setBackground(VintageTheme.NEN_THE);
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        the.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        RoundedPanel dai = new RoundedPanel(4);
        dai.setBackground(VintageTheme.mauTheoUuTien(item.getDoUuTien()));
        dai.setPreferredSize(new Dimension(8, 10));
        the.add(dai, BorderLayout.WEST);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        JLabel dong1 = new JLabel(item.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")) + " – " + item.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm")) + "   "
            + BieuTuongViec.cua(item) + " " + DinhDang.rutGon(item.getTen(), 26));
        dong1.setFont(VintageTheme.FONT_NHAN);
        dong1.setToolTipText(item.getTen());
        JLabel dong2 = new JLabel(phuDe + " · " + item.loaiViec() + " · " + item.getDoUuTien());
        dong2.setFont(VintageTheme.FONT_PHU_DE);
        dong2.setForeground(VintageTheme.CHU_MO);
        giua.add(dong1);
        giua.add(dong2);
        the.add(giua, BorderLayout.CENTER);

        NutTron xongNut = new NutTron("Xong", NutTron.Kieu.CHINH, VintageTheme.XANH_MINT);
        xongNut.addActionListener(e -> {
            item.danhDauHoanThanh();
            if (khiBamXong != null) khiBamXong.accept(item);
        });
        the.add(xongNut, BorderLayout.EAST);
        return the;
    }

    private void capNhatLoiChao(LocalTime hienTai) {
        skyPanel.capNhatTheoGio(hienTai);

        int gio = hienTai.getHour();
        String buoi;
        String icon;
        if (gio >= 5 && gio < 11) { buoi = "Buổi sáng"; icon = "🌅"; }
        else if (gio >= 11 && gio < 14) { buoi = "Buổi trưa"; icon = "🌞"; }
        else if (gio >= 14 && gio < 18) { buoi = "Buổi chiều"; icon = "🌤️"; }
        else if (gio >= 18 && gio < 22) { buoi = "Buổi tối"; icon = "🌆"; }
        else { buoi = "Đêm khuya"; icon = "🌙"; }

        bieuTuongBuoi.setText(icon);
        nhanChao.setText("Xin chào! " + buoi + " tốt lành ✨");
        nhanNgay.setText(
                tenThu(LocalDate.now()) + ", " + LocalDate.now()
                        + "  ·  " + hienTai.format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

    /**
     * Mèo nhỏ (nhỏ hơn hẳn con mèo lớn ở tab "Xem cả ngày") đặt ở đầu Trang
     * chủ, ngay trên thẻ chào — chỉ vẽ, không có ô thoại, để không chiếm chỗ.
     */
    private static final class MeoNho extends JPanel {
        private MascotArt.BieuCam bieuCam = MascotArt.BieuCam.BINH_THUONG;

        MeoNho() {
            setOpaque(false);
            setPreferredSize(new Dimension(46, 46));
        }

        void datBieuCam(MascotArt.BieuCam moi) {
            if (moi != bieuCam) {
                bieuCam = moi;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            MascotArt.ve((Graphics2D) g, 0, 0, 46, bieuCam);
        }
    }

    private String tenThu(LocalDate ngay) {
        DayOfWeek t = ngay.getDayOfWeek();
        return switch (t) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }
}
