package com.oopsmyschedule.ui;

import com.oopsmyschedule.domain.Disruption;
import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cửa sổ chính của Oops! My Schedule.
 * <p>
 * Tab mặc định là "🏠 Trang chủ" ({@link TrangChuPanel}) — chỉ trả lời "lịch
 * có vấn đề gì không / đang làm gì / sắp làm gì", KHÔNG liệt kê cả ngày.
 * Thanh trên cùng chỉ còn giờ hệ thống + menu Demo Mode (gộp gọn +30/60/90
 * phút vào 1 dropdown) + chuông thông báo — để không "tham" như trước.
 * Xem đầy đủ cả ngày thì qua tab "📅 Xem cả ngày" ({@link TimelinePanel}).
 */
public class MainFrame extends JFrame {

    private static final DateTimeFormatter GIO_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Lịch của "hôm nay" — nguồn duy nhất cho Trang chủ, cảnh báo trễ, và Replan Engine (chỉ có ý nghĩa với NGAY BÂY GIỜ). */
    private final DaySchedule ds;
    private final DongHo dongHo = new DongHo();
    private final ReplanEngine engine = new ReplanEngine();
    private final TrungTamThongBao trungTamThongBao = new TrungTamThongBao();

    /**
     * Reminder System (nhắc "sắp tới giờ" + "deadline gần kề") — CHỦ ĐỘNG,
     * khác {@link BoTheoDoiLich} vốn chỉ phản ứng SAU khi đã trễ. Quét mỗi
     * giây cùng nhịp với {@code kiemTraCanhBao}, xem {@link #kiemTraNhacLich()}.
     */
    private final ReminderService reminderService = new ReminderService();

    /**
     * SystemTray thật (nếu máy hỗ trợ) để bắn notification desktop ngoài app
     * — best-effort, {@code null} khi môi trường không hỗ trợ (headless, 1
     * số bản Linux dùng để demo/chấm bài). App KHÔNG phụ thuộc vào cái này:
     * {@link TrungTamThongBao} nội bộ luôn là nơi ghi nhận chính, xem
     * {@link #guiThongBaoHeThong(String, String)}.
     */
    private TrayIcon trayIcon;

    /**
     * Không phải ngày nào lịch cũng giống nhau: mỗi ngày có 1 {@link DaySchedule}
     * riêng, được cache lại sau khi xem/sửa lần đầu. Ngày chưa từng mở thì sinh
     * mặc định từ {@link LichMauTuan} (khác nhau giữa ngày thường/cuối tuần).
     */
    private final Map<LocalDate, DaySchedule> theoNgay = new HashMap<>();
    private LocalDate ngayDangXem;

    private final MascotPanel mascotPanel = new MascotPanel();
    private final LichTuanStrip tuanStrip = new LichTuanStrip();
    private final TimelinePanel timelinePanel = new TimelinePanel();
    private final TrangChuPanel trangChuPanel = new TrangChuPanel();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel nhanGio = new JLabel();
    private final JLabel nhanNgayDangXem = new JLabel();
    private final JLabel nhanTrangThaiLuu = new JLabel(" ");
    private final NutTron nutThongBao = new NutTron("🔔");

    // Ghi nhớ đã nhắc "item::mứcđộ" nào rồi, tránh popup lặp lại mỗi giây.
    private final Set<String> daNhac = new HashSet<>();

    public MainFrame(DaySchedule ds) {
        super("Oops! My Schedule 😭");
        this.ds = ds;
        this.theoNgay.put(ds.getNgay(), ds);
        this.ngayDangXem = ds.getNgay();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(VintageTheme.NEN_CHINH);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(taoTopPanel(), BorderLayout.NORTH);
        add(taoTabPanel(), BorderLayout.CENTER);
        add(taoBottomPanel(), BorderLayout.SOUTH);

        trangChuPanel.datHanhDongXong(item -> lamMoiTatCa());
        trangChuPanel.datHanhDongDieuChinhLich(this::moDialogBaoSuCo);
        trangChuPanel.datHanhDongXemCaNgay(() -> { ngayDangXem = ds.getNgay(); tabs.setSelectedIndex(1); capNhatTimelineChoNgayDangXem(); });
        timelinePanel.datHanhDongHoanThanh(item -> {
            lamMoiTatCa();
            capNhatTimelineChoNgayDangXem();
            Toast.thanhCongCoHoanTac(this, "✓ Đã hoàn thành \"" + item.getTen() + "\"", () -> {
                item.boDanhDauHoanThanh();
                lamMoiTatCa();
                capNhatTimelineChoNgayDangXem();
            });
        });
        timelinePanel.datHanhDongSua(this::moDialogSuaViec);
        timelinePanel.datHanhDongXoa(this::xoaViecDangXem);

        khoiTaoSystemTray();

        Timer nhipDongHo = new Timer(1000, e -> { capNhatGio(); kiemTraCanhBao(false); kiemTraNhacLich(); });
        nhipDongHo.start();

        lamMoiTatCa();
        capNhatGio();
        capNhatTimelineChoNgayDangXem();
    }

    /**
     * Khởi tạo SystemTray thật — best-effort. Icon vẽ bằng Graphics2D (đúng
     * tinh thần "không dùng ảnh ngoài" của app, giống {@code MascotArt}),
     * KHÔNG đọc file resource nào. Nếu {@code SystemTray.isSupported()} trả
     * về false (headless, hoặc desktop environment không hỗ trợ tray) thì
     * {@link #trayIcon} giữ nguyên {@code null} và app vẫn chạy bình thường
     * — chỉ mất phần notification desktop, Trung tâm thông báo nội bộ vẫn
     * hoạt động đầy đủ.
     */
    private void khoiTaoSystemTray() {
        if (!SystemTray.isSupported()) {
            return;
        }
        try {
            java.awt.image.BufferedImage anh = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = anh.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(VintageTheme.XANH_DUONG);
            g2.fillOval(1, 1, 14, 14);
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
            g2.drawString("!", 6, 12);
            g2.dispose();

            trayIcon = new TrayIcon(anh, "Oops! My Schedule 😭");
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException | RuntimeException ex) {
            // Best-effort: không để lỗi tray phá vỡ app chính, ví dụ chạy
            // trong môi trường không có desktop notification daemon.
            trayIcon = null;
        }
    }

    /** Quét Reminder System, ghi vào Trung tâm thông báo + thử bắn SystemTray thật (nếu có). */
    private void kiemTraNhacLich() {
        List<String> danhSachNhac = reminderService.quet(ds, ds.getNgay(), dongHo.layGioHienTai());
        if (danhSachNhac.isEmpty()) {
            return;
        }
        for (String noiDung : danhSachNhac) {
            trungTamThongBao.them(noiDung, dongHo.layGioHienTai());
            guiThongBaoHeThong("Oops! My Schedule", noiDung);
        }
        lamMoiTatCa();
    }

    /** Bắn 1 notification desktop thật qua SystemTray — im lặng bỏ qua nếu không hỗ trợ/lỗi. */
    private void guiThongBaoHeThong(String tieuDe, String noiDung) {
        if (trayIcon == null) {
            return;
        }
        try {
            trayIcon.displayMessage(tieuDe, noiDung, TrayIcon.MessageType.INFO);
        } catch (RuntimeException ignored) {
            // Một số desktop environment (đặc biệt trên Linux) cho add() TrayIcon
            // thành công nhưng displayMessage() vẫn lỗi tùy cấu hình — bỏ qua,
            // Trung tâm thông báo nội bộ đã ghi nhận nội dung này rồi.
        }
    }

    /**
     * Lấy DaySchedule của 1 ngày — nếu chưa cache: thử đọc từ dữ liệu đã tự
     * lưu trước đó ({@link KhoDuLieu}), nếu ngày đó chưa từng lưu thì mới
     * sinh mặc định từ {@link LichMauTuan}.
     */
    private DaySchedule layHoacTaoLich(LocalDate ngay) {
        return theoNgay.computeIfAbsent(ngay, n -> {
            try {
                DaySchedule daLuu = KhoDuLieu.docNeuCo(n);
                return daLuu != null ? daLuu : LichMauTuan.taoLich(n);
            } catch (KhoDuLieu.DuLieuHongException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage() + "\nSẽ mở ngày này với 1 lịch tạm — dữ liệu cũ trên đĩa KHÔNG bị ghi đè cho tới khi bạn tự lưu.",
                    "Không thể đọc dữ liệu đã lưu", JOptionPane.WARNING_MESSAGE);
                return LichMauTuan.taoLich(n);
            }
        });
    }

    /**
     * Tự động lưu TẤT CẢ các ngày đã được mở/chỉnh trong phiên làm việc này
     * xuống {@link KhoDuLieu} — gọi sau mỗi thao tác thay đổi dữ liệu (thêm,
     * sửa, xóa, hoàn thành, điều chỉnh lịch...) thông qua {@link #lamMoiTatCa()}.
     * Không dùng JOptionPane/Toast cho mỗi lần lưu (sẽ làm phiền), chỉ cập
     * nhật 1 dòng trạng thái nhỏ ở góc trên.
     */
    private void tuDongLuu() {
        try {
            for (DaySchedule d : theoNgay.values()) {
                KhoDuLieu.luuNgay(d);
            }
            nhanTrangThaiLuu.setText("✓ Đã lưu lúc " + dongHo.layGioHienTai().withNano(0));
            nhanTrangThaiLuu.setForeground(VintageTheme.CHU_MO);
        } catch (IOException ex) {
            nhanTrangThaiLuu.setText("⚠ Chưa lưu được dữ liệu");
            nhanTrangThaiLuu.setForeground(VintageTheme.BAT_BUOC);
        }
    }

    /** Thanh trên cùng: giờ hệ thống bên trái, Demo Mode + chuông thông báo bên phải — gọn, không rối. */
    private JPanel taoTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        nhanGio.setFont(VintageTheme.FONT_NHAN);
        nhanTrangThaiLuu.setFont(VintageTheme.FONT_PHU_DE);
        nhanTrangThaiLuu.setForeground(VintageTheme.CHU_MO);
        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        trai.setOpaque(false);
        trai.add(nhanGio);
        trai.add(nhanTrangThaiLuu);
        top.add(trai, BorderLayout.WEST);

        NutTron nutDemo = new NutTron("🎬 Demo Mode ▾");
        nutDemo.addActionListener(e -> moMenuDemoMode(nutDemo));

        nutThongBao.setToolTipText("Xem lại thông báo app đã gửi");
        nutThongBao.addActionListener(e -> {
            new ThongBaoDialog(this, trungTamThongBao.layDanhSach()).setVisible(true);
            trungTamThongBao.danhDauDaDocHet();
            lamMoiTatCa();
        });

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        phai.setOpaque(false);
        phai.add(nutDemo);
        phai.add(nutThongBao);
        top.add(phai, BorderLayout.EAST);

        return top;
    }

    /** Menu thả xuống: bật/tắt Demo Mode + tua nhanh — thay vì bày hết nút ra ngoài. */
    private void moMenuDemoMode(JButton nut) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem batTat = new JMenuItem(dongHo.isCheDoMoPhong() ? "⏹ Tắt Demo Mode" : "🎬 Bật Demo Mode");
        batTat.addActionListener(e -> {
            if (dongHo.isCheDoMoPhong()) dongHo.tatCheDoMoPhong(); else dongHo.batCheDoMoPhong();
            capNhatGio();
        });
        menu.add(batTat);
        menu.addSeparator();
        for (int phut : new int[] {30, 60, 90}) {
            JMenuItem tua = new JMenuItem("⏩ +" + phut + " phút");
            tua.addActionListener(e -> {
                dongHo.tuaNhanh(phut);
                capNhatGio();
                kiemTraCanhBao(true);
            });
            menu.add(tua);
        }
        menu.show(nut, 0, nut.getHeight());
    }

    /** Trang chủ (mặc định) + Xem cả ngày (timeline đầy đủ, có linh vật, có chọn ngày). */
    private JTabbedPane taoTabPanel() {
        tabs.setFont(VintageTheme.FONT_NHAN);

        tabs.addTab("🏠 Trang chủ", trangChuPanel);

        JPanel xemCaNgay = new JPanel(new BorderLayout(6, 6));
        xemCaNgay.setOpaque(false);
        JPanel dau = new JPanel(new BorderLayout(0, 6));
        dau.setOpaque(false);
        dau.add(taoThanhChonNgay(), BorderLayout.NORTH);
        dau.add(mascotPanel, BorderLayout.SOUTH);
        xemCaNgay.add(dau, BorderLayout.NORTH);
        xemCaNgay.add(timelinePanel, BorderLayout.CENTER);
        tabs.addTab("📅 Xem cả ngày", xemCaNgay);

        return tabs;
    }

    /**
     * Thanh chọn ngày (◀ ngày ▶ + "Hôm nay") — thể hiện đúng ý "không phải
     * ngày nào lịch cũng giống nhau": mỗi ngày là 1 DaySchedule riêng, chuyển
     * qua ngày khác không ảnh hưởng gì tới lịch hôm nay / cảnh báo trễ.
     */
    private JPanel taoThanhChonNgay() {
        JPanel thanh = new JPanel(new BorderLayout(0, 4));
        thanh.setOpaque(false);

        nhanNgayDangXem.setFont(VintageTheme.FONT_NHAN);
        nhanNgayDangXem.setForeground(VintageTheme.CHU_CHINH);
        nhanNgayDangXem.setHorizontalAlignment(SwingConstants.CENTER);

        tuanStrip.datHanhDongChonNgay(ngay -> { ngayDangXem = ngay; capNhatTimelineChoNgayDangXem(); });

        NutTron homNay = new NutTron("Hôm nay");
        homNay.addActionListener(e -> { ngayDangXem = ds.getNgay(); capNhatTimelineChoNgayDangXem(); });
        JPanel hangHomNay = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        hangHomNay.setOpaque(false);
        hangHomNay.add(homNay);

        thanh.add(nhanNgayDangXem, BorderLayout.NORTH);
        thanh.add(tuanStrip, BorderLayout.CENTER);
        thanh.add(hangHomNay, BorderLayout.SOUTH);
        return thanh;
    }

    /** Cập nhật riêng tab "Xem cả ngày" theo ngày đang xem — KHÔNG đụng tới Trang chủ (luôn là hôm nay). */
    private void capNhatTimelineChoNgayDangXem() {
        DaySchedule lich = layHoacTaoLich(ngayDangXem);
        boolean laHomNay = ngayDangXem.equals(ds.getNgay());
        LocalTime gioToDam = laHomNay ? dongHo.layGioHienTai() : null;
        timelinePanel.capNhat(lich.layLichHienTai(), gioToDam);
        nhanNgayDangXem.setText((laHomNay ? "Hôm nay · " : "") + tenThu(ngayDangXem) + ", " + ngayDangXem);
        tuanStrip.capNhat(ngayDangXem, ds.getNgay());
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

    private JPanel taoBottomPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bottom.setOpaque(false);

        NutTron them = new NutTron("+ Thêm việc", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        them.addActionListener(e -> {
            // Dialog cho chọn NGÀY áp dụng (mặc định = ngày đang xem ở tab "Xem
            // cả ngày") — vì không phải ngày nào lịch cũng giống nhau, người
            // dùng có thể thêm thẳng việc cho 1 ngày khác mà không cần lật tuần trước.
            AddItemDialog dlg = new AddItemDialog(this, ngayDangXem);
            dlg.setVisible(true);
            ScheduleItem moi = dlg.layKetQua();
            if (moi != null) {
                LocalDate ngayDich = dlg.layNgayDaChon();
                try {
                    layHoacTaoLich(ngayDich).themItem(moi);
                    lamMoiTatCa();
                    capNhatTimelineChoNgayDangXem();
                    String moTa = "\"" + moi.getTen() + "\" · " + moi.getGioBatDau()
                        + (ngayDich.equals(ngayDangXem) ? "" : " · ngày " + ngayDich);
                    trungTamThongBao.them("✅ Đã thêm 1 việc: " + moTa, dongHo.layGioHienTai());
                    Toast.thanhCong(this, "Đã thêm 1 việc", moTa);
                } catch (IllegalArgumentException ex) {
                    Toast.loi(this, "Không thể thêm việc", ex.getMessage());
                }
            }
        });

        NutTron baoSuCo = new NutTron("🚨 Báo sự cố", NutTron.Kieu.CHINH, VintageTheme.BAT_BUOC);
        baoSuCo.addActionListener(e -> moDialogBaoSuCo(null));

        NutTron tongKet = new NutTron("📊 Tổng kết");
        tongKet.setToolTipText("Xem tổng kết ngày đang xem: % hoàn thành, khối lượng, lời khuyên");
        tongKet.addActionListener(e -> moDialogTongKet());

        NutTron duLieu = new NutTron("⚙ Dữ liệu ▾");
        duLieu.setToolTipText("App tự lưu sau mỗi thay đổi — nút này chỉ để sao lưu/khôi phục file khi cần");
        duLieu.addActionListener(e -> moMenuDuLieu(duLieu));

        bottom.add(them);
        bottom.add(baoSuCo);
        bottom.add(tongKet);
        bottom.add(duLieu);
        return bottom;
    }

    private void capNhatGio() {
        LocalTime gio = dongHo.layGioHienTai();
        // Chỉ hiện giờ:phút:giây — tránh bug cũ hiện cả phần nano-giây dài ngoằng.
        String hau = dongHo.isCheDoMoPhong() ? "  🎬" : "";
        nhanGio.setText("🕐 " + gio.format(GIO_FMT) + hau);
        lamMoiTatCa();
        // Chỉ "tick" lại tab Xem cả ngày mỗi giây nếu đang xem đúng hôm nay
        // (để thanh "đang diễn ra" luôn đúng) — xem ngày khác thì đứng yên,
        // không bị timer hôm nay ghi đè mất lịch đang xem.
        if (ngayDangXem != null && ngayDangXem.equals(ds.getNgay())) {
            capNhatTimelineChoNgayDangXem();
        }
    }

    /** Làm mới Trang chủ + huy hiệu số thông báo — KHÔNG đụng tới tab "Xem cả ngày" (xem {@link #capNhatTimelineChoNgayDangXem}). */
    private void lamMoiTatCa() {
        LocalTime hienTai = dongHo.layGioHienTai();

        List<CanhBaoViec> canhBaoList = BoTheoDoiLich.quetChoNguoiDung(ds, hienTai);
        List<ScheduleItem> sapToi = ds.layLichHienTai().stream()
            .filter(it -> !it.isHoanThanh() && it.getGioBatDau().isAfter(hienTai))
            .sorted(Comparator.comparing(ScheduleItem::getGioBatDau))
            .toList();
        trangChuPanel.capNhat(canhBaoList, sapToi, hienTai);

        int soLuong = trungTamThongBao.soLuong();
        nutThongBao.setText(soLuong > 0 ? "🔔 " + soLuong : "🔔");
        nutThongBao.datHieuUngNhay(soLuong > 0); // chuông nảy nhẹ khi có thông báo chưa đọc, thu hút chú ý

        tuDongLuu();
    }

    /**
     * Quét lịch, hiện cảnh báo theo tầng. KHÔNG tự kết luận "trễ" — chỉ hiện
     * banner để người dùng tự chọn Bắt đầu / Bỏ qua / Tôi đang trễ.
     * @param buocXemNgay nếu true (do bấm nút tua nhanh) thì hiện kể cả đã nhắc rồi ở tầng thấp hơn trước đó trong cùng tick.
     */
    private void kiemTraCanhBao(boolean buocXemNgay) {
        List<CanhBaoViec> canhBaoList = BoTheoDoiLich.quetChoNguoiDung(ds, dongHo.layGioHienTai());
        if (canhBaoList.isEmpty()) {
            mascotPanel.capNhat(MascotArt.BieuCam.BINH_THUONG, "Lịch hôm nay ổn áp, cứ thế mà làm nha! 🐾");
            return;
        }
        CanhBaoViec nangNhat = BoTheoDoiLich.chonCanhBaoChinh(canhBaoList);
        capNhatMascotTheoMucDo(nangNhat);

        String khoa = nangNhat.getItem().getId() + "::" + nangNhat.getMucDo();
        if (!buocXemNgay && daNhac.contains(khoa)) {
            return; // đã nhắc mức này cho việc này rồi, tránh spam mỗi giây
        }
        daNhac.add(khoa);
        if (nangNhat.getMucDo() == MucDoCanhBao.MOI_BAT_DAU) {
            return; // mức nhẹ nhất, chỉ đổi mascot, không cần popup làm phiền
        }
        hienBannerCanhBao(nangNhat);
    }

    private void capNhatMascotTheoMucDo(CanhBaoViec c) {
        String ten = c.getItem().getTen();
        switch (c.getMucDo()) {
            case TRE_NANG -> mascotPanel.capNhat(MascotArt.BieuCam.OOPS,
                "🚨 \"" + ten + "\" đang trễ " + DinhDang.phut(c.getSoPhutTre()) + " rồi! Điều chỉnh lịch thôi?");
            case TRE_NHE -> mascotPanel.capNhat(MascotArt.BieuCam.NGU,
                "⚠️ Bạn chưa bắt đầu \"" + ten + "\" — đã trễ " + DinhDang.phut(c.getSoPhutTre()) + ".");
            default -> mascotPanel.capNhat(MascotArt.BieuCam.VUI,
                "🕐 \"" + ten + "\" vừa mới bắt đầu, cố lên nha!");
        }
    }

    /**
     * Xử lý "Có việc phát sinh" — KHÁC với Disruption thường: đây là THÊM 1
     * việc mới chen vào lịch, không phải dời/bỏ 1 việc cũ, nên không cần đi
     * qua ReplanEngine để chọn phương án. Nếu việc mới trùng giờ 1 việc Cố
     * định khác thì bị chặn (đúng guard clause của {@link DaySchedule}); nếu
     * chỉ chồng giờ với việc linh hoạt/deadline/lặp lại thì vẫn thêm được,
     * nhưng cảnh báo để người dùng biết dùng "Tôi không thể làm một việc"
     * dời các việc đó nếu cần.
     */
    private void xuLyViecPhatSinh(ScheduleItem viecMoi) {
        try {
            ds.themItem(viecMoi);
        } catch (IllegalArgumentException ex) {
            Toast.loi(this, "Không thể thêm việc phát sinh", ex.getMessage());
            return;
        }

        String moTa = "\"" + viecMoi.getTen() + "\" (" + viecMoi.getGioBatDau() + "–" + viecMoi.getGioKetThuc() + ")";
        List<ScheduleItem> bChongGio = ds.layLichHienTai().stream()
            .filter(it -> it != viecMoi && !it.isHoanThanh() && it.trung(viecMoi))
            .toList();

        daNhac.clear();
        if (bChongGio.isEmpty()) {
            trungTamThongBao.them("✅ Đã thêm việc phát sinh: " + moTa, dongHo.layGioHienTai());
            Toast.thanhCong(this, "Đã thêm việc phát sinh", moTa);
        } else {
            String tenChong = bChongGio.stream().map(ScheduleItem::getTen)
                .collect(java.util.stream.Collectors.joining(", "));
            trungTamThongBao.them("⚠️ Đã thêm " + moTa + " nhưng bị chồng giờ với: " + tenChong,
                dongHo.layGioHienTai());
            Toast.loi(this, "Đã thêm nhưng bị chồng giờ",
                "Chồng với: " + tenChong + " — dùng \"Báo sự cố → Tôi không thể làm một việc\" để dời chúng.");
        }
        lamMoiTatCa();
        capNhatTimelineChoNgayDangXem();
    }

    /** Popup app tự tính sẵn số phút trễ — người dùng chỉ cần chọn hướng xử lý. Đồng thời lưu vào 🔔 Thông báo. */
    private void hienBannerCanhBao(CanhBaoViec c) {
        String noiDung = c.getMucDo().getNhan() + ": \"" + c.getItem().getTen() + "\" đã trễ "
            + DinhDang.phut(c.getSoPhutTre()) + " (app tự tính). Bạn muốn làm gì?";
        trungTamThongBao.them(noiDung, dongHo.layGioHienTai());
        lamMoiTatCa();

        Object[] lua = {"Bắt đầu", "Bỏ qua", "Tôi đang trễ"};
        int chon = JOptionPane.showOptionDialog(this, noiDung, "Nhắc lịch",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, lua, lua[2]);
        if (chon == 1) { // Bỏ qua
            c.getItem().danhDauHoanThanh();
            lamMoiTatCa();
        } else if (chon == 2) { // Tôi đang trễ -> mở dialog báo sự cố, gợi ý sẵn số phút
            moDialogBaoSuCo((int) c.getSoPhutTre());
        }
        // chọn "Bắt đầu" thì chỉ đóng banner, không làm gì thêm (người dùng tự biết đang làm).
    }

    private void moDialogBaoSuCo(Integer soPhutGoiY) {
        List<ScheduleItem> chuaXong = ds.layLichHienTai().stream()
            .filter(it -> !it.isHoanThanh()).toList();
        if (chuaXong.isEmpty()) {
            Toast.thanhCong(this, "Không còn việc nào để báo sự cố cả", "Bạn xong hết rồi! 🎉");
            return;
        }
        DisruptionDialog dlg = new DisruptionDialog(this, chuaXong, soPhutGoiY);
        dlg.setVisible(true);

        ScheduleItem viecPhatSinh = dlg.layViecPhatSinh();
        if (viecPhatSinh != null) {
            xuLyViecPhatSinh(viecPhatSinh);
            return;
        }
        Disruption disruption = dlg.layKetQua();
        if (disruption == null) return;
        String lyDoNguoiDung = dlg.layLyDo();

        // Không còn tính 1 "phương án tối ưu" duy nhất: chạy cả 3 chiến lược
        // rule-based (Ít thay đổi / Ưu tiên deadline / Giảm tải) rồi để người
        // dùng tự so sánh & chọn — xem ReplanEngine#taoCacPhuongAn.
        List<PhuongAn> cacPhuongAn = engine.taoCacPhuongAn(ds, disruption, dongHo.layGioHienTai());

        // Phân loại nhanh các việc CÒN LẠI trong ngày (chưa xong, chưa tới giờ)
        // thành 3 nhóm để báo mức độ ảnh hưởng thật ("khóa cứng" / "có thể dời"
        // / "có thể hy sinh") trước khi đưa ra 3 phương án — đúng tinh thần
        // "không nên chỉ nói bị trễ X phút mà không rõ ảnh hưởng tới đâu".
        LocalTime hienTai = dongHo.layGioHienTai();
        List<ScheduleItem> bAnhHuong = ds.layLichHienTai().stream()
            .filter(it -> !it.isHoanThanh() && !it.getGioBatDau().isBefore(hienTai))
            .toList();
        long soKhoaCung = bAnhHuong.stream()
            .filter(it -> it instanceof com.oopsmyschedule.domain.FixedEvent
                || it instanceof com.oopsmyschedule.domain.DeadlineTask)
            .count();
        long soHySinhDuoc = bAnhHuong.stream()
            .filter(it -> it.getDoUuTien() == com.oopsmyschedule.domain.PriorityLevel.CO_THE_BO)
            .count();
        long soDiChuyenDuoc = bAnhHuong.size() - soKhoaCung - soHySinhDuoc;
        String moTaSuCo = bAnhHuong.isEmpty() ? null
            : bAnhHuong.size() + " hoạt động còn lại bị ảnh hưởng — 🔴 " + soKhoaCung + " khóa cứng (cố định/deadline) · 🟡 "
              + soDiChuyenDuoc + " có thể dời · 🟢 " + soHySinhDuoc + " có thể hy sinh.";

        String tieuDe = disruption instanceof com.oopsmyschedule.domain.DelayDisruption dd
            ? "🚨 Bạn đã bị trễ " + DinhDang.phut(dd.getSoPhutTre()) + "!"
            : "🚨 Bạn vừa báo không thể làm 1 hoạt động!" + (lyDoNguoiDung != null ? " (Lý do: " + lyDoNguoiDung + ")" : "");
        CanhBaoTreDialog canhBao = new CanhBaoTreDialog(this, tieuDe, moTaSuCo, cacPhuongAn);
        canhBao.setVisible(true);
        if (!canhBao.daChonPhuongAn()) return;

        // Bấm "Chọn phương án" là ÁP DỤNG NGAY — không bắt đi qua thêm 1 màn
        // hình xác nhận. Bảng before/after (nếu muốn) đã có thể xem ngay tại
        // thẻ phương án qua "Xem thay đổi ▾", không cần 1 bước riêng nữa.
        PhuongAn daChon = canhBao.getPhuongAnDaChon();
        KetQuaSoSanh ketQua = daChon.getKetQua();
        ds.thayTheToanBo(ketQua.getLichSau().layLichHienTai());

        // "Đề xuất bỏ" không có nghĩa là biến mất hoàn toàn: cố gắng chuyển
        // từng việc đó sang lịch NGÀY MAI (best-effort) thay vì tự ý xóa hẳn —
        // người dùng vẫn giữ được việc đó, chỉ là không làm hôm nay nữa.
        int soChuyenNgayMai = 0;
        int soKhongTheSapXep = 0;
        List<String> tenKhongTheSapXep = new java.util.ArrayList<>();
        LocalDate ngayMai = ds.getNgay().plusDays(1);
        DaySchedule lichNgayMai = layHoacTaoLich(ngayMai);
        for (ScheduleItem bi : ketQua.getDanhSachDeXuatBo()) {
            // FIX: dò khe hở LIÊN TỤC thật sự bằng timKhoangTrongDauTien thay
            // vì chỉ hỏi "có slot nào không" (coDuCho) rồi vẫn nhét vào giờ
            // CŨ của task — giờ cũ có thể đã bị chiếm bởi việc khác dù ngày
            // mai vẫn còn khe trống ở chỗ khác (xem DaySchedule#timKhoangTrongDauTien).
            LocalTime slotTrong = lichNgayMai.timKhoangTrongGanGio(bi.layThoiLuong(), bi.getGioBatDau());
            if (slotTrong == null) {
                soKhongTheSapXep++;
                tenKhongTheSapXep.add(bi.getTen());
                giuLaiHomNay(bi); // không để việc biến mất: ngày mai kín thì giữ lại ở lịch hôm nay
                continue;
            }
            try {
                lichNgayMai.themItem(DaySchedule.taoBanSaoSangNgayKhac(bi, ngayMai, slotTrong));
                soChuyenNgayMai++;
            } catch (IllegalArgumentException ex) {
                soKhongTheSapXep++; // trùng giờ cố định ở ngày mai luôn -> đành chịu, không ép được
                tenKhongTheSapXep.add(bi.getTen());
                giuLaiHomNay(bi);
            }
        }

        daNhac.clear();
        mascotPanel.capNhat(MascotArt.BieuCam.VUI, "Lịch mới đã được áp dụng! Cố lên nha bạn ơi 💪");

        ds.ghiNhatKySuCo(new SuCoNhatKy(dongHo.layGioHienTai(), tieuDe, daChon.getTen(),
            ketQua.getDanhSachThayDoi(), tenKhongTheSapXep));

        int soDaDieuChinh = (int) ketQua.getDanhSachThayDoi().stream()
            .filter(s -> !s.startsWith("Không có việc nào")).count();
        StringBuilder moTaToast = new StringBuilder();
        if (soDaDieuChinh > 0) moTaToast.append(soDaDieuChinh).append(" hoạt động đã được điều chỉnh");
        if (soChuyenNgayMai > 0) {
            if (moTaToast.length() > 0) moTaToast.append(" · ");
            moTaToast.append(soChuyenNgayMai).append(" hoạt động chuyển sang ngày mai");
        }
        if (soKhongTheSapXep > 0) {
            if (moTaToast.length() > 0) moTaToast.append(" · ");
            moTaToast.append("⚠ ").append(soKhongTheSapXep).append(" hoạt động chưa thể sắp xếp (")
                .append(String.join(", ", tenKhongTheSapXep)).append(")");
        }
        trungTamThongBao.them("✅ Đã áp dụng \"" + daChon.getTen() + "\": " + moTaToast, dongHo.layGioHienTai());
        Toast.thanhCong(this, "Đã áp dụng \"" + daChon.getTen() + "\"", moTaToast.toString());
        lamMoiTatCa();
    }

    /** Giữ lại việc (giờ gốc) trong lịch hôm nay khi không chuyển được sang ngày mai — tránh mất việc âm thầm. */
    private void giuLaiHomNay(ScheduleItem bi) {
        try {
            if (!ds.layLichHienTai().contains(bi)) ds.themItem(bi);
        } catch (IllegalArgumentException ignored) {
            // trùng việc cố định: vẫn được ghi trong nhật ký sự cố + thông báo "chưa thể sắp xếp"
        }
    }

    /**
     * Mở dialog sửa 1 việc đã có trong ngày đang xem. Dialog cho chọn lại
     * ngày áp dụng — nếu người dùng đổi sang ngày khác, item sẽ được CHUYỂN
     * (xoá khỏi ngày cũ, thêm vào ngày mới) thay vì chỉ sửa tại chỗ, vì
     * "không phải ngày nào lịch cũng giống nhau".
     */
    private void moDialogSuaViec(ScheduleItem item) {
        AddItemDialog dlg = new AddItemDialog(this, item, ngayDangXem);
        dlg.setVisible(true);
        ScheduleItem daSua = dlg.layKetQua();
        if (daSua == null) return;
        LocalDate ngayMoi = dlg.layNgayDaChon();
        try {
            layHoacTaoLich(ngayDangXem).xoaItem(item);
            layHoacTaoLich(ngayMoi).themItem(daSua);
            lamMoiTatCa();
            capNhatTimelineChoNgayDangXem();
            String moTa = "\"" + daSua.getTen() + "\" → " + daSua.getGioBatDau() + "-" + daSua.getGioKetThuc()
                + (ngayMoi.equals(ngayDangXem) ? "" : " · chuyển sang ngày " + ngayMoi);
            trungTamThongBao.them("✅ Đã cập nhật lịch: " + moTa, dongHo.layGioHienTai());
            Toast.thanhCong(this, "Đã cập nhật lịch", moTa);
        } catch (IllegalArgumentException ex) {
            // Lưu lại thất bại (VD: trùng giờ với việc Cố định khác) -> khôi phục item cũ, báo lỗi.
            layHoacTaoLich(ngayDangXem).themItem(item);
            capNhatTimelineChoNgayDangXem();
            Toast.loi(this, "Không thể lưu thay đổi", ex.getMessage());
        }
    }

    /**
     * "🗑️ Xóa" trên 1 thẻ việc ở tab "Xem cả ngày" — luôn hỏi xác nhận trước
     * (thao tác không thể hoàn tác) rồi mới xóa thật khỏi {@code DaySchedule}
     * của NGÀY ĐANG XEM (không phải lúc nào cũng là hôm nay). Dùng
     * {@link DaySchedule#huyItem} (không phải {@code xoaItem}) để "Tổng kết
     * ngày" đếm đúng số việc đã hủy.
     */
    private void xoaViecDangXem(ScheduleItem item) {
        int xacNhan = JOptionPane.showConfirmDialog(this,
            "Xóa/Hủy hoạt động \"" + item.getTen() + "\" (" + item.getGioBatDau() + " - " + item.getGioKetThuc()
                + ") khỏi lịch? Thao tác này không thể hoàn tác.",
            "Xác nhận xóa/hủy hoạt động", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        boolean daXoa = layHoacTaoLich(ngayDangXem).huyItem(item);
        if (!daXoa) return;
        daNhac.remove(item.getId() + "::" + com.oopsmyschedule.service.MucDoCanhBao.TRE_NHE);
        daNhac.remove(item.getId() + "::" + com.oopsmyschedule.service.MucDoCanhBao.TRE_NANG);
        lamMoiTatCa();
        capNhatTimelineChoNgayDangXem();
        String moTa = "\"" + item.getTen() + "\" · " + item.getGioBatDau() + " - " + item.getGioKetThuc();
        trungTamThongBao.them("🗑️ Đã xóa/hủy 1 hoạt động: " + moTa, dongHo.layGioHienTai());
        Toast.thanhCong(this, "Đã xóa/hủy hoạt động", moTa);
    }

    /**
     * "📊 Tổng kết" cho NGÀY ĐANG XEM (không nhất thiết là hôm nay). Nếu đang
     * xem đúng hôm nay thì mốc "trễ" dùng giờ hệ thống hiện tại; xem ngày
     * khác (đã qua hoặc chưa tới) thì coi như đã hết ngày (23:59) để tính
     * toán ổn định, không phụ thuộc đồng hồ đang chạy của "hôm nay".
     */
    private void moDialogTongKet() {
        DaySchedule lich = layHoacTaoLich(ngayDangXem);
        boolean laHomNay = ngayDangXem.equals(ds.getNgay());
        LocalTime moc = laHomNay ? dongHo.layGioHienTai() : LocalTime.of(23, 59);
        TongKetNgay tk = TongKetNgay.tinhToan(lich, moc);
        new TongKetNgayDialog(this, ngayDangXem, tk).setVisible(true);
    }

    /**
     * Menu "⚙ Dữ liệu" — thay cho cặp nút Lưu/Mở kiểu JFileChooser cũ.
     * App đã tự lưu sau mỗi thao tác ({@link #tuDongLuu()}), nên ở đây chỉ
     * còn 2 việc thật sự cần người dùng tự chọn file: sao lưu ra 1 nơi khác,
     * và khôi phục từ 1 file backup — đây là 2 trường hợp hợp lý duy nhất để
     * dùng {@code JFileChooser}.
     */
    private void moMenuDuLieu(JButton nut) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem luuNgay = new JMenuItem("💾 Lưu ngay");
        luuNgay.addActionListener(e -> {
            tuDongLuu();
            Toast.thanhCong(this, "Đã lưu dữ liệu", "Lưu vào " + KhoDuLieu.thuMucDuLieu());
        });
        menu.add(luuNgay);
        menu.addSeparator();

        JMenuItem saoLuu = new JMenuItem("📤 Sao lưu ra file...");
        saoLuu.addActionListener(e -> saoLuuFile());
        menu.add(saoLuu);

        JMenuItem khoiPhuc = new JMenuItem("📥 Khôi phục từ file...");
        khoiPhuc.addActionListener(e -> khoiPhucFile());
        menu.add(khoiPhuc);

        menu.show(nut, 0, nut.getHeight());
    }

    private void saoLuuFile() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("lich_" + ngayDangXem + "_backup.json"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                LuuLich.luu(layHoacTaoLich(ngayDangXem), Path.of(fc.getSelectedFile().getAbsolutePath()));
                Toast.thanhCong(this, "Đã sao lưu", fc.getSelectedFile().getName());
            } catch (IOException ex) {
                Toast.loi(this, "Không thể sao lưu", ex.getMessage());
            }
        }
    }

    private void khoiPhucFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DaySchedule docDuoc = LuuLich.doc(Path.of(fc.getSelectedFile().getAbsolutePath()));
                DaySchedule dich = layHoacTaoLich(ngayDangXem);
                dich.thayTheToanBo(docDuoc.layLichHienTai());
                daNhac.clear();
                lamMoiTatCa();
                capNhatTimelineChoNgayDangXem();
                Toast.thanhCong(this, "Đã khôi phục dữ liệu", "Áp dụng cho ngày " + ngayDangXem);
            } catch (IOException ex) {
                Toast.loi(this, "Không thể khôi phục", ex.getMessage());
            }
        }
    }
}
