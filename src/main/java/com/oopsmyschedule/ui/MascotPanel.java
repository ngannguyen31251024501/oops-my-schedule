package com.oopsmyschedule.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel hiển thị linh vật mèo (vẽ bằng {@link MascotArt}) kèm 1 câu thoại
 * ngắn. Mèo "nảy" nhẹ lên xuống liên tục (biên độ nhỏ, theo hàm sin) để
 * giao diện có sức sống hơn thay vì đứng im hoàn toàn.
 */
public class MascotPanel extends JPanel {

    private MascotArt.BieuCam bieuCam = MascotArt.BieuCam.BINH_THUONG;
    private final JLabel loiThoai = new JLabel("Chào bạn! Hôm nay có gì hay ho nào? 🐾");
    private float phaNhay = 0f;
    private final Timer timerNhay;

    public MascotPanel() {
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(0, 110));

        JPanel veMeo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                float nay = (float) Math.sin(phaNhay) * 3.2f; // nảy nhẹ vài pixel, không "nhảy múa" quá mức
                MascotArt.ve(g2, 5, 5 + Math.round(nay), 90, bieuCam);
            }
        };
        veMeo.setOpaque(false);
        veMeo.setPreferredSize(new Dimension(100, 100));

        loiThoai.setFont(VintageTheme.FONT_NOI_DUNG);
        loiThoai.setForeground(VintageTheme.CHU_CHINH);
        loiThoai.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VintageTheme.XANH_DUONG, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        loiThoai.setOpaque(true);
        loiThoai.setBackground(VintageTheme.NEN_THE);

        add(veMeo, BorderLayout.WEST);
        add(loiThoai, BorderLayout.CENTER);

        timerNhay = new Timer(45, e -> {
            phaNhay += 0.10f;
            veMeo.repaint();
        });
        timerNhay.start();
    }

    public void capNhat(MascotArt.BieuCam bieuCamMoi, String cauThoai) {
        this.bieuCam = bieuCamMoi;
        this.loiThoai.setText("<html><body style='width:280px'>" + cauThoai + "</body></html>");
        repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timerNhay.stop();
    }
}
