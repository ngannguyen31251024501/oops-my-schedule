package com.oopsmyschedule.ui;

import com.oopsmyschedule.domain.DelayDisruption;
import com.oopsmyschedule.domain.Disruption;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.domain.SkipDisruption;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Dialog "🚨 Báo sự cố" — 3 lựa chọn "chuyện gì đang xảy ra?" thay vì gộp
 * chung 2 lựa chọn lệch cấp độ ("Tôi bị trễ" là nguyên nhân, "Tôi phải bỏ 1
 * việc" là hậu quả/quyết định — cái quyết định đó nên để Replan Engine giúp
 * người dùng, không bắt họ tự chọn việc cần hy sinh):
 * <ul>
 *   <li><b>Tôi đang bị trễ</b> — nhập số phút trễ, y hệt trước giờ.</li>
 *   <li><b>Có việc phát sinh</b> — 1 hoạt động mới chen ngang lịch (không
 *       phải "dời/bỏ" 1 việc cũ, mà THÊM 1 việc mới) — trả về qua
 *       {@link #layViecPhatSinh()} thay vì {@link Disruption}.</li>
 *   <li><b>Tôi không thể làm một việc</b> — chọn việc bị ảnh hưởng (không
 *       còn gọi là "việc cần bỏ" nữa, vì Replan Engine mới là nơi quyết định
 *       dời hay bỏ), kèm lý do không bắt buộc.</li>
 * </ul>
 */
public class DisruptionDialog extends JDialog {

    private static final String THE_TRE = "TRE";
    private static final String THE_PHAT_SINH = "PHAT_SINH";
    private static final String THE_KHONG_THE_LAM = "KHONG_THE_LAM";

    private Disruption ketQua = null;
    private String lyDo = null;
    private ScheduleItem viecPhatSinh = null;

    public DisruptionDialog(Frame parent, List<ScheduleItem> cacViecChuaXong, Integer soPhutGoiY) {
        super(parent, "🚨 Báo sự cố", true);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(VintageTheme.NEN_CHINH);
        setResizable(false);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(14, 18, 4, 18));

        JLabel tieuDe = new JLabel("Điều gì đang xảy ra?");
        tieuDe.setFont(VintageTheme.FONT_NHAN.deriveFont(Font.BOLD, 14f));
        tieuDe.setForeground(VintageTheme.CHU_CHINH);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.add(tieuDe);
        noiDung.add(Box.createVerticalStrut(10));

        CardLayout theCard = new CardLayout();
        JPanel theSubForm = new JPanel(theCard);
        theSubForm.setOpaque(false);
        theSubForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- Lựa chọn 1: Tôi đang bị trễ ----
        JRadioButton radioTre = new JRadioButton("Tôi đang bị trễ", true);
        JPanel theTre = taoTheLuaChon(radioTre, "Lịch hiện tại bị chậm so với kế hoạch");

        JSpinner spinnerPhut = new JSpinner(new SpinnerNumberModel(soPhutGoiY != null ? soPhutGoiY : 30, 1, 600, 5));
        JPanel formTre = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        formTre.setOpaque(false);
        formTre.setBorder(BorderFactory.createEmptyBorder(2, 6, 8, 0));
        formTre.add(new JLabel("Số phút trễ:"));
        formTre.add(spinnerPhut);
        theSubForm.add(boc(formTre), THE_TRE);

        // ---- Lựa chọn 2: Có việc phát sinh ----
        JRadioButton radioPhatSinh = new JRadioButton("Có việc phát sinh");
        JPanel thePhatSinh = taoTheLuaChon(radioPhatSinh, "Một hoạt động mới chen vào lịch");

        JTextField oTen = new JTextField(16);
        JSpinner oGioBatDau = spinnerGio(LocalTime.now().getHour(), 0);
        JSpinner oGioKetThuc = spinnerGio((LocalTime.now().getHour() + 1) % 24, 0);
        JPanel formPhatSinh = new JPanel();
        formPhatSinh.setOpaque(false);
        formPhatSinh.setLayout(new BoxLayout(formPhatSinh, BoxLayout.Y_AXIS));
        formPhatSinh.setBorder(BorderFactory.createEmptyBorder(2, 6, 8, 0));
        JPanel hangTen = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        hangTen.setOpaque(false);
        hangTen.add(new JLabel("Tên việc:"));
        hangTen.add(oTen);
        JPanel hangGio = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        hangGio.setOpaque(false);
        hangGio.add(new JLabel("Thời gian:"));
        hangGio.add(oGioBatDau);
        hangGio.add(new JLabel("–"));
        hangGio.add(oGioKetThuc);
        formPhatSinh.add(hangTen);
        formPhatSinh.add(hangGio);
        theSubForm.add(boc(formPhatSinh), THE_PHAT_SINH);

        // ---- Lựa chọn 3: Tôi không thể làm một việc ----
        JRadioButton radioKhongTheLam = new JRadioButton("Tôi không thể làm một việc");
        JPanel theKhongTheLam = taoTheLuaChon(radioKhongTheLam, "Một hoạt động hôm nay không thể thực hiện được");

        JComboBox<ScheduleItem> comboViec = new JComboBox<>(cacViecChuaXong.toArray(new ScheduleItem[0]));
        comboViec.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ScheduleItem item) {
                    setText(BieuTuongViec.cua(item) + " " + item.getTen()
                        + "  (" + item.getGioBatDau() + "–" + item.getGioKetThuc() + ")");
                }
                return this;
            }
        });
        JTextField oLyDo = new JTextField(16);
        JPanel formKhongTheLam = new JPanel();
        formKhongTheLam.setOpaque(false);
        formKhongTheLam.setLayout(new BoxLayout(formKhongTheLam, BoxLayout.Y_AXIS));
        formKhongTheLam.setBorder(BorderFactory.createEmptyBorder(2, 6, 8, 0));
        JPanel hangViec = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        hangViec.setOpaque(false);
        hangViec.add(new JLabel("Việc bị ảnh hưởng:"));
        hangViec.add(comboViec);
        JPanel hangLyDo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        hangLyDo.setOpaque(false);
        hangLyDo.add(new JLabel("Lý do (không bắt buộc):"));
        hangLyDo.add(oLyDo);
        formKhongTheLam.add(hangViec);
        formKhongTheLam.add(hangLyDo);
        theSubForm.add(boc(formKhongTheLam), THE_KHONG_THE_LAM);

        ButtonGroup nhom = new ButtonGroup();
        nhom.add(radioTre);
        nhom.add(radioPhatSinh);
        nhom.add(radioKhongTheLam);
        radioTre.addActionListener(e -> theCard.show(theSubForm, THE_TRE));
        radioPhatSinh.addActionListener(e -> theCard.show(theSubForm, THE_PHAT_SINH));
        radioKhongTheLam.addActionListener(e -> theCard.show(theSubForm, THE_KHONG_THE_LAM));

        noiDung.add(theTre);
        noiDung.add(Box.createVerticalStrut(6));
        noiDung.add(thePhatSinh);
        noiDung.add(Box.createVerticalStrut(6));
        noiDung.add(theKhongTheLam);
        noiDung.add(Box.createVerticalStrut(4));
        noiDung.add(theSubForm);
        theCard.show(theSubForm, THE_TRE);

        if (cacViecChuaXong.isEmpty()) {
            radioKhongTheLam.setEnabled(false);
        }

        NutTron xacNhan = new NutTron("✨ Điều chỉnh lịch", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        NutTron dong = new NutTron("Đóng");
        xacNhan.addActionListener(e -> {
            try {
                if (radioTre.isSelected()) {
                    ketQua = new DelayDisruption((Integer) spinnerPhut.getValue());
                } else if (radioPhatSinh.isSelected()) {
                    String ten = oTen.getText().trim();
                    if (ten.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Bạn chưa nhập tên việc phát sinh.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    viecPhatSinh = new FixedEvent(ten, layGio(oGioBatDau), layGio(oGioKetThuc), PriorityLevel.BAT_BUOC);
                } else if (radioKhongTheLam.isSelected()) {
                    if (comboViec.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Không có việc nào để chọn.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    ketQua = new SkipDisruption((ScheduleItem) comboViec.getSelectedItem());
                    String nhap = oLyDo.getText().trim();
                    lyDo = nhap.isEmpty() ? null : nhap;
                }
                setVisible(false);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
            }
        });
        dong.addActionListener(e -> setVisible(false));

        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        duoi.setOpaque(false);
        duoi.setBorder(BorderFactory.createEmptyBorder(4, 18, 12, 18));
        duoi.add(dong);
        duoi.add(xacNhan);

        add(noiDung, BorderLayout.CENTER);
        add(duoi, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    /** 1 "thẻ" lựa chọn: radio button ở trên + mô tả xám nhỏ bên dưới, giống đúng mockup đã bàn. */
    private JPanel taoTheLuaChon(JRadioButton radio, String moTa) {
        RoundedPanel the = new RoundedPanel(10);
        the.setBackground(VintageTheme.NEN_THE);
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(1000, 60));
        the.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));

        radio.setOpaque(false);
        radio.setFont(VintageTheme.FONT_NHAN.deriveFont(Font.BOLD, 13f));
        radio.setForeground(VintageTheme.CHU_CHINH);
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nhanMoTa = new JLabel(moTa);
        nhanMoTa.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11.5f));
        nhanMoTa.setForeground(VintageTheme.CHU_MO);
        nhanMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        nhanMoTa.setBorder(BorderFactory.createEmptyBorder(2, 24, 0, 0));

        the.add(radio);
        the.add(nhanMoTa);
        return the;
    }

    private JPanel boc(JPanel noiDung) {
        JPanel bao = new JPanel(new BorderLayout());
        bao.setOpaque(false);
        bao.add(noiDung, BorderLayout.WEST);
        return bao;
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

    private LocalTime layGio(JSpinner s) {
        Date d = (Date) s.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    /** Trả về sự cố (Trễ / Không thể làm 1 việc), hoặc null nếu chọn "Có việc phát sinh" hoặc bấm Đóng. */
    public Disruption layKetQua() {
        return ketQua;
    }

    /** Lý do không bắt buộc khi chọn "Tôi không thể làm một việc" — null nếu không nhập hoặc không áp dụng. */
    public String layLyDo() {
        return lyDo;
    }

    /** Việc mới khi chọn "Có việc phát sinh" — null nếu không phải lựa chọn này hoặc bấm Đóng. */
    public ScheduleItem layViecPhatSinh() {
        return viecPhatSinh;
    }
}
