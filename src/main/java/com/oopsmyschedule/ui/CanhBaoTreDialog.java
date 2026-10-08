package com.oopsmyschedule.ui;

import com.oopsmyschedule.service.KetQuaSoSanh;
import com.oopsmyschedule.service.PhuongAn;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog "🚨 Cảnh báo & Điều chỉnh" — hiện ĐỦ 3 {@link PhuongAn} do
 * {@code ReplanEngine} tính sẵn (rule-based, KHÔNG dùng AI) dưới dạng thẻ.
 * <p>
 * Flow áp dụng NGAY khi bấm "Chọn phương án" trên 1 thẻ (không bắt người
 * dùng đi qua thêm bảng so sánh trước/sau bắt buộc) — vì người đang trễ giờ
 * muốn "chọn xong là xong", không muốn đọc thêm 1 màn hình nữa. Ai tò mò thì
 * bấm "Xem thay đổi ▾" để xổ chi tiết ngay tại chỗ (progressive disclosure),
 * không bắt buộc.
 */
public class CanhBaoTreDialog extends JDialog {

    private boolean daChon = false;
    private PhuongAn phuongAnDaChon;

    public CanhBaoTreDialog(Frame parent, String tieuDeSuCo, String moTaSuCo, List<PhuongAn> cacPhuongAn) {
        super(parent, "Cảnh báo & Điều chỉnh", true);
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(VintageTheme.NEN_CHINH);
        setSize(480, 700);
        setLocationRelativeTo(parent);

        JPanel goc = new JPanel();
        goc.setLayout(new BoxLayout(goc, BoxLayout.Y_AXIS));
        goc.setOpaque(false);
        goc.setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));

        goc.add(taoBannerDo(tieuDeSuCo, moTaSuCo));
        goc.add(Box.createVerticalStrut(14));

        goc.add(nhanMuc("Các phương án đề xuất"));
        goc.add(Box.createVerticalStrut(2));
        JLabel phuDe = new JLabel("Hệ thống KHÔNG tự quyết định thay bạn — xem qua rồi bấm \"Chọn phương án\":");
        phuDe.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11f));
        phuDe.setForeground(VintageTheme.CHU_MO);
        phuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        goc.add(phuDe);
        goc.add(Box.createVerticalStrut(8));

        for (int i = 0; i < cacPhuongAn.size(); i++) {
            goc.add(thePhuongAn(goc, cacPhuongAn.get(i), i + 1));
            goc.add(Box.createVerticalStrut(8));
        }

        JScrollPane cuon = new JScrollPane(goc);
        cuon.setBorder(BorderFactory.createEmptyBorder());
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        cuon.getViewport().setOpaque(false);
        cuon.setOpaque(false);
        add(cuon, BorderLayout.CENTER);

        NutTron huy = new NutTron("Để sau");
        huy.addActionListener(e -> { daChon = false; setVisible(false); });
        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        duoi.setBackground(VintageTheme.NEN_CHINH);
        duoi.add(huy);
        add(duoi, BorderLayout.SOUTH);
    }

    private RoundedPanel taoBannerDo(String tieuDe, String moTa) {
        RoundedPanel banner = new RoundedPanel(16);
        banner.setBackground(VintageTheme.BAT_BUOC);
        banner.setLayout(new BorderLayout(10, 4));
        banner.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel icon = new JLabel("🚨");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 30));
        banner.add(icon, BorderLayout.WEST);

        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        JLabel dong1 = new JLabel(tieuDe);
        dong1.setFont(VintageTheme.FONT_TIEU_DE.deriveFont(17f));
        dong1.setForeground(Color.WHITE);
        String phu = moTa != null ? moTa
            : "Hệ thống đã tính ra 3 phương án điều chỉnh khác nhau — bạn tự chọn phương án phù hợp nhất.";
        JLabel dong2 = new JLabel("<html><body style='width:280px'>" + phu + "</body></html>");
        dong2.setFont(VintageTheme.FONT_PHU_DE);
        dong2.setForeground(new Color(255, 255, 255, 235));
        chu.add(dong1);
        chu.add(Box.createVerticalStrut(4));
        chu.add(dong2);
        banner.add(chu, BorderLayout.CENTER);

        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        return banner;
    }

    private JLabel nhanMuc(String text) {
        JLabel l = new JLabel(text);
        l.setFont(VintageTheme.FONT_NHAN.deriveFont(14f));
        l.setForeground(VintageTheme.CHU_CHINH);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private RoundedPanel thePhuongAn(JPanel goc, PhuongAn pa, int soThuTu) {
        KetQuaSoSanh kq = pa.getKetQua();
        RoundedPanel the = new RoundedPanel(12, VintageTheme.XANH_DUONG, soThuTu == 1 ? 1.8f : 1.1f);
        the.setBackground(soThuTu == 1 ? VintageTheme.VANG_KEM : VintageTheme.NEN_THE);
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        the.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tieuDe = new JLabel(pa.getBieuTuong() + " Phương án " + soThuTu + " — " + pa.getTen());
        tieuDe.setFont(VintageTheme.FONT_NHAN.deriveFont(13f));
        tieuDe.setForeground(VintageTheme.CHU_CHINH);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.add(tieuDe);

        JLabel moTa = new JLabel("<html><body style='width:300px'>" + pa.getMoTa() + "</body></html>");
        moTa.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11.5f));
        moTa.setForeground(VintageTheme.CHU_MO);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.add(Box.createVerticalStrut(4));
        the.add(moTa);
        the.add(Box.createVerticalStrut(4));

        List<String> thayDoi = kq.getDanhSachThayDoi();
        JLabel tomTat = new JLabel(tomTatSoLuong(kq));
        tomTat.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11f));
        tomTat.setForeground(VintageTheme.CHU_CHINH);
        tomTat.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.add(tomTat);

        // ---- Chi tiết đầy đủ, mặc định ẨN — progressive disclosure ----
        JPanel chiTiet = new JPanel();
        chiTiet.setOpaque(false);
        chiTiet.setLayout(new BoxLayout(chiTiet, BoxLayout.Y_AXIS));
        chiTiet.setAlignmentX(Component.LEFT_ALIGNMENT);
        chiTiet.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 0));
        for (String dong : thayDoi) {
            JLabel l = new JLabel("<html><body style='width:290px'>• " + dong + "</body></html>");
            l.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11f));
            l.setForeground(VintageTheme.CHU_CHINH);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            chiTiet.add(l);
        }
        for (String canhBao : kq.getDanhSachCanhBao()) {
            JLabel l = new JLabel("<html><body style='width:290px'>⚠ " + canhBao + "</body></html>");
            l.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11f));
            l.setForeground(VintageTheme.BAT_BUOC);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            chiTiet.add(Box.createVerticalStrut(2));
            chiTiet.add(l);
        }
        chiTiet.setVisible(false);
        the.add(chiTiet);

        JToggleButton xemThayDoi = new JToggleButton("Xem thay đổi ▾");
        xemThayDoi.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11f));
        xemThayDoi.setFocusPainted(false);
        xemThayDoi.setContentAreaFilled(false);
        xemThayDoi.setBorderPainted(false);
        xemThayDoi.setForeground(VintageTheme.XANH_DUONG);
        xemThayDoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        xemThayDoi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        xemThayDoi.addActionListener(e -> {
            boolean moRa = xemThayDoi.isSelected();
            chiTiet.setVisible(moRa);
            xemThayDoi.setText(moRa ? "Ẩn thay đổi ▴" : "Xem thay đổi ▾");
            goc.revalidate();
            goc.repaint();
        });

        JPanel hangDuoi = new JPanel(new BorderLayout());
        hangDuoi.setOpaque(false);
        hangDuoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangDuoi.add(xemThayDoi, BorderLayout.WEST);
        NutTron chonNut = new NutTron("Chọn phương án", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        chonNut.addActionListener(e -> {
            phuongAnDaChon = pa;
            daChon = true;
            setVisible(false);
        });
        hangDuoi.add(chonNut, BorderLayout.EAST);

        the.add(Box.createVerticalStrut(6));
        the.add(hangDuoi);

        return the;
    }

    private String tomTatSoLuong(KetQuaSoSanh kq) {
        long soDoi = kq.getDanhSachThayDoi().stream()
            .filter(s -> !s.startsWith("Không có việc nào")).count();
        long soDeXuatBo = kq.getDanhSachDeXuatBo().size();
        StringBuilder sb = new StringBuilder();
        if (soDoi == 0 && soDeXuatBo == 0) {
            return "Không có hoạt động nào cần điều chỉnh.";
        }
        if (soDoi > 0) {
            sb.append(soDoi).append(" hoạt động được điều chỉnh");
        }
        if (soDeXuatBo > 0) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(soDeXuatBo).append(" hoạt động đề xuất chuyển sang ngày mai");
        }
        if (kq.coCanhBao()) {
            sb.append(" · ⚠ ").append(kq.getDanhSachCanhBao().size()).append(" cảnh báo");
        }
        return sb.toString();
    }

    /** true nếu người dùng đã bấm "Chọn phương án" ở 1 thẻ nào đó (false = bấm "Để sau"). */
    public boolean daChonPhuongAn() {
        return daChon;
    }

    /** Phương án người dùng đã chọn — chỉ có giá trị khi {@link #daChonPhuongAn()} == true. */
    public PhuongAn getPhuongAnDaChon() {
        return phuongAnDaChon;
    }
}
