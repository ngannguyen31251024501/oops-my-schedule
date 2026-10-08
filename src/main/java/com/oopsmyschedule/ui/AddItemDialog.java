package com.oopsmyschedule.ui;

import com.oopsmyschedule.domain.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Dialog dùng chung cho cả "+ Thêm việc" (tạo mới) và "✏️ Sửa việc" (chỉnh
 * sửa 1 {@link ScheduleItem} đã có) — sinh ra 1 ScheduleItem đúng loại con
 * tương ứng. Khi sửa, vì mỗi loại con có cấu trúc khác nhau (Deadline có
 * hạn chót, Lặp lại có streak...), cách đơn giản nhất là tạo LẠI 1 item mới
 * từ dữ liệu đã chỉnh rồi để MainFrame thay thế item cũ — không cố "vá" tại
 * chỗ vào object cũ.
 * <p>
 * Có thêm ô "Ngày áp dụng": vì "không phải ngày nào lịch cũng giống nhau",
 * người dùng có thể tạo/chuyển 1 việc sang bất kỳ ngày nào ngay tại đây,
 * không bắt buộc phải lật thanh tuần tới đúng ngày rồi mới bấm "+ Thêm việc".
 * MainFrame đọc lại ngày đã chọn qua {@link #layNgayDaChon()} để biết nên
 * lưu/chuyển item vào {@code DaySchedule} nào.
 */
public class AddItemDialog extends JDialog {

    private ScheduleItem ketQua = null;
    private LocalDate ngayDaChon;
    private JSpinner oNgay;

    /** Tạo mới, áp dụng cho {@code ngayMacDinh} (thường là ngày đang xem trên UI). */
    public AddItemDialog(Frame parent, LocalDate ngayMacDinh) {
        this(parent, null, ngayMacDinh);
    }

    /**
     * @param dangSua     nếu khác null, dialog mở ở chế độ SỬA, các ô đã điền sẵn theo item này.
     * @param ngayMacDinh ngày hiển thị sẵn trong ô "Ngày áp dụng" (với chế độ sửa: ngày item đang thuộc về).
     */
    public AddItemDialog(Frame parent, ScheduleItem dangSua, LocalDate ngayMacDinh) {
        super(parent, dangSua == null ? "+ Thêm việc mới" : "✏️ Sửa việc", true);
        setLayout(new GridLayout(0, 2, 8, 8));
        getContentPane().setBackground(VintageTheme.NEN_CHINH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        boolean sua = dangSua != null;
        LocalDate ngayBanDau = ngayMacDinh != null ? ngayMacDinh : LocalDate.now();

        JTextField oTen = new JTextField(sua ? dangSua.getTen() : "Làm bài tập");
        JComboBox<String> oLoai = new JComboBox<>(new String[]{"Cố định", "Linh hoạt", "Deadline", "Lặp lại"});
        JSpinner oGioBatDau = spinnerGio(sua ? dangSua.getGioBatDau().getHour() : 9, sua ? dangSua.getGioBatDau().getMinute() : 0);
        JSpinner oGioKetThuc = spinnerGio(sua ? dangSua.getGioKetThuc().getHour() : 10, sua ? dangSua.getGioKetThuc().getMinute() : 0);
        JComboBox<PriorityLevel> oUuTien = new JComboBox<>(PriorityLevel.values());
        if (sua) oUuTien.setSelectedItem(dangSua.getDoUuTien());
        LocalTime hanChotMacDinh = LocalTime.of(23, 59);
        int streakMacDinh = 0;
        if (sua && dangSua instanceof DeadlineTask dt) hanChotMacDinh = dt.getHanChot().toLocalTime();
        if (sua && dangSua instanceof RecurringEvent re) streakMacDinh = re.getSoNgayLienTiep();
        JSpinner oHanChotGio = spinnerGio(hanChotMacDinh.getHour(), hanChotMacDinh.getMinute());
        JSpinner oStreak = new JSpinner(new SpinnerNumberModel(streakMacDinh, 0, 365, 1));

        oNgay = spinnerNgay(ngayBanDau);

        if (sua) {
            String loaiHienTai = dangSua.loaiViec();
            oLoai.setSelectedItem(loaiHienTai);
            // FIX (feedback UX 2.2): trước đây khoá cứng ô Loại khi sửa vì lo
            // đổi loại giữa chừng dữ liệu nửa vời — nhưng MainFrame#moDialogSuaViec
            // vốn đã xoá item cũ rồi thêm lại item mới (không sửa tại chỗ),
            // nên đổi Loại lúc sửa cũng an toàn y hệt xoá rồi thêm mới. Mở khoá
            // để "Sửa việc" nhất quán với "Thêm việc mới".
        }

        JLabel nhanNgay = new JLabel(sua ? "Ngày (đổi để chuyển ngày khác):" : "Ngày áp dụng:");
        add(nhanNgay); add(oNgay);
        add(new JLabel("Tên việc:")); add(oTen);
        add(new JLabel("Loại:")); add(oLoai);
        add(new JLabel("Giờ bắt đầu:")); add(oGioBatDau);
        add(new JLabel("Giờ kết thúc:")); add(oGioKetThuc);
        add(new JLabel("Mức ưu tiên:")); add(oUuTien);
        JLabel nhanHanChot = new JLabel("Hạn chót (giờ, cùng ngày áp dụng):");
        add(nhanHanChot); add(oHanChotGio);
        JLabel nhanStreak = new JLabel("Đã làm liên tiếp (ngày):");
        add(nhanStreak); add(oStreak);

        boolean laDeadlineBanDau = "Deadline".equals(oLoai.getSelectedItem());
        boolean laLapLaiBanDau = "Lặp lại".equals(oLoai.getSelectedItem());
        nhanHanChot.setVisible(laDeadlineBanDau); oHanChotGio.setVisible(laDeadlineBanDau);
        nhanStreak.setVisible(laLapLaiBanDau); oStreak.setVisible(laLapLaiBanDau);
        oLoai.addActionListener(e -> {
            boolean laDeadline = "Deadline".equals(oLoai.getSelectedItem());
            boolean laLapLai = "Lặp lại".equals(oLoai.getSelectedItem());
            nhanHanChot.setVisible(laDeadline); oHanChotGio.setVisible(laDeadline);
            nhanStreak.setVisible(laLapLai); oStreak.setVisible(laLapLai);
        });

        NutTron luuNut = new NutTron(sua ? "Lưu thay đổi" : "Thêm", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        NutTron huyNut = new NutTron("Hủy");
        luuNut.addActionListener(e -> {
            try {
                String ten = oTen.getText().trim();
                LocalTime bd = layGio(oGioBatDau);
                LocalTime kt = layGio(oGioKetThuc);
                PriorityLevel uuTien = (PriorityLevel) oUuTien.getSelectedItem();
                String loai = (String) oLoai.getSelectedItem();
                LocalDate ngayApDung = layNgay(oNgay);
                // FIX BUG (audit bản 10, mục 4): khi SỬA, phải giữ nguyên id
                // (nếu không SkipDisruption/tham chiếu khác sẽ không đối
                // chiếu lại được item cũ) + các state không nằm trên form
                // (hoàn thành, số lần đã dời, streak, bỏ qua hôm nay, cảnh
                // báo trễ hạn) — trước đây tạo hẳn 1 object mới với id mới
                // và các state phụ này luôn bị reset về mặc định.
                String id = sua ? dangSua.getId() : java.util.UUID.randomUUID().toString();
                boolean hoanThanh = sua && dangSua.isHoanThanh();
                ketQua = switch (loai) {
                    case "Cố định" -> new FixedEvent(id, ten, bd, kt, uuTien, hoanThanh);
                    case "Deadline" -> {
                        boolean canhBaoCu = sua && dangSua instanceof DeadlineTask dt0 && dt0.isCanhBaoTreHan();
                        // FIX (audit bản 12, mục 5): trước đây ngayThucHien
                        // luôn giữ nguyên giá trị CŨ khi sửa, kể cả khi người
                        // dùng đổi "Ngày áp dụng" sang ngày khác -> tính sai
                        // LocalDateTime.of(ngayThucHien, gioKetThuc) khi kiểm
                        // tra trễ hạn. Nay: ngayThucHien luôn đi theo ngày áp
                        // dụng hiện tại trên form.
                        yield new DeadlineTask(id, ten, bd, kt, uuTien, hoanThanh,
                            LocalDateTime.of(ngayApDung, layGio(oHanChotGio)), ngayApDung, canhBaoCu);
                    }
                    case "Lặp lại" -> {
                        boolean boQuaCu = sua && dangSua instanceof RecurringEvent re0 && re0.isBoQuaHomNay();
                        yield new RecurringEvent(id, ten, bd, kt, uuTien, hoanThanh, (Integer) oStreak.getValue(), boQuaCu);
                    }
                    default -> {
                        int soLanDaDoiCu = sua && dangSua instanceof FlexibleTask ft0 ? ft0.getSoLanDaDoi() : 0;
                        yield new FlexibleTask(id, ten, bd, kt, uuTien, hoanThanh, soLanDaDoiCu);
                    }
                };
                ngayDaChon = ngayApDung;
                setVisible(false);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
            }
        });
        huyNut.addActionListener(e -> setVisible(false));
        add(huyNut); add(luuNut);

        pack();
        setLocationRelativeTo(parent);
    }

    private JSpinner spinnerGio(int gio, int phut) {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        s.setEditor(new JSpinner.DateEditor(s, "HH:mm"));
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.HOUR_OF_DAY, gio);
        c.set(java.util.Calendar.MINUTE, phut);
        s.setValue(c.getTime());
        return s;
    }

    private JSpinner spinnerNgay(LocalDate ngay) {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy (EEE)"));
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(ngay.getYear(), ngay.getMonthValue() - 1, ngay.getDayOfMonth(), 0, 0, 0);
        s.setValue(c.getTime());
        return s;
    }

    private LocalTime layGio(JSpinner s) {
        java.util.Date d = (java.util.Date) s.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
            .withSecond(0).withNano(0);
    }

    private LocalDate layNgay(JSpinner s) {
        java.util.Date d = (java.util.Date) s.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    public ScheduleItem layKetQua() {
        return ketQua;
    }

    /** Ngày người dùng chọn để áp dụng việc này — chỉ có giá trị đáng tin sau khi {@link #layKetQua()} khác null. */
    public LocalDate layNgayDaChon() {
        return ngayDaChon;
    }
}
