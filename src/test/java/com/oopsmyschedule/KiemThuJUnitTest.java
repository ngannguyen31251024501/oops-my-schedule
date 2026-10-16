package com.oopsmyschedule;

import com.oopsmyschedule.domain.*;
import com.oopsmyschedule.service.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * [NG-02b] KiemThu.java chuyển sang JUnit 5: mỗi case 1 @Test, tên giữ mã
 * (tc01.., bonus_.., val01..). KiemThu.java vẫn giữ để chạy song song.
 */
class KiemThuJUnitTest {

// ---------------- TC-01 ----------------
    @Test
    void tc01_TrungGioFixedEvent() {
        try {
            DaySchedule ds = new DaySchedule(LocalDate.now());
            ds.themItem(new FixedEvent("Họp nhóm", LocalTime.of(8, 0), LocalTime.of(9, 0), PriorityLevel.BAT_BUOC));
            try {
                ds.themItem(new FixedEvent("Học Java", LocalTime.of(8, 30), LocalTime.of(9, 30), PriorityLevel.BAT_BUOC));
                bad("TC-01", "Phải ném IllegalArgumentException khi 2 FixedEvent trùng giờ.");
            } catch (IllegalArgumentException e) {
                ok("TC-01", ds.layLichHienTai().size() == 1, "Item trùng giờ không được lưu vào lịch.");
            }
        } catch (Exception e) {
            bad("TC-01", "Lỗi không mong đợi: " + e);
        }
    }

    // ---------------- TC-02 ----------------
    @Test
    void tc02_DoiLichFlexibleTask() {
        FlexibleTask t = new FlexibleTask("Làm bài", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.NEN_LAM);
        List<ScheduleItem> ds = new ArrayList<>(List.of(t));
        new DelayDisruption(90).apDung(ds);
        ok("TC-02", t.getGioBatDau().equals(LocalTime.of(11, 30)) && t.getGioKetThuc().equals(LocalTime.of(12, 30)),
            "FlexibleTask phải dời thành 11:30 - 12:30, thực tế: " + t.getGioBatDau() + " - " + t.getGioKetThuc());
    }

    // ---------------- TC-03 ----------------
    @Test
    void tc03_FixedEventKhongBiDoi() {
        FixedEvent e = new FixedEvent("Họp nhóm", LocalTime.of(14, 0), LocalTime.of(15, 0), PriorityLevel.BAT_BUOC);
        List<ScheduleItem> ds = new ArrayList<>(List.of(e));
        new DelayDisruption(90).apDung(ds);
        ok("TC-03", e.getGioBatDau().equals(LocalTime.of(14, 0)) && e.getGioKetThuc().equals(LocalTime.of(15, 0)),
            "FixedEvent phải giữ nguyên 14:00 - 15:00.");
    }

    // ---------------- TC-04 ----------------
    @Test
    void tc04_GoiTrucTiepXuLyKhiBiTreTrenFixedEvent() {
        FixedEvent e = new FixedEvent("Họp nhóm", LocalTime.of(14, 0), LocalTime.of(15, 0), PriorityLevel.BAT_BUOC);
        try {
            e.xuLyKhiBiTre(30);
            bad("TC-04", "Phải ném IllegalStateException khi gọi trực tiếp xuLyKhiBiTre() trên FixedEvent.");
        } catch (IllegalStateException ex) {
            pass("TC-04");
        }
    }

    // ---------------- TC-05 ----------------
    @Test
    void tc05_DeadlineTaskConKipHan() {
        LocalDate homNay = LocalDate.now();
        LocalDateTime hanChot = LocalDateTime.of(homNay.plusDays(1), LocalTime.of(23, 59));
        DeadlineTask t = new DeadlineTask("Nộp báo cáo", LocalTime.of(20, 0), LocalTime.of(21, 0),
            PriorityLevel.NEN_LAM, hanChot, homNay);
        t.xuLyKhiBiTre(60); // dời sang 21:00 - 22:00 hôm nay, còn cách hạn ~26 giờ
        ok("TC-05", !t.isCanhBaoTreHan() && t.getGioBatDau().equals(LocalTime.of(21, 0)),
            "Phải dời thành công, không cảnh báo trễ hạn. Thực tế canhBao=" + t.isCanhBaoTreHan()
            + ", gioBatDau=" + t.getGioBatDau());
    }

    // ---------------- TC-06 ----------------
    @Test
    void tc06_DeadlineTaskKhongKipHan() {
        LocalDate homNay = LocalDate.now();
        LocalDateTime hanChot = LocalDateTime.of(homNay, LocalTime.of(23, 59));
        DeadlineTask t = new DeadlineTask("Nộp báo cáo", LocalTime.of(21, 30), LocalTime.of(22, 0),
            PriorityLevel.NEN_LAM, hanChot, homNay);
        LocalTime gioTruoc = t.getGioBatDau();
        t.xuLyKhiBiTre(150); // 22:00 + 150p = 00:30 hôm sau -> nếu cộng ngày sẽ trễ hạn 23:59 hôm nay
        ok("TC-06", t.isCanhBaoTreHan() && t.getGioBatDau().equals(gioTruoc),
            "Phải bật canhBaoTreHan=true và KHÔNG tự dời giờ. Thực tế canhBao=" + t.isCanhBaoTreHan());
    }

    // ---------------- TC-07 ----------------
    @Test
    void tc07_BoQuaHomNayKhongAnhHuongStreak() {
        RecurringEvent gym = new RecurringEvent("Gym", LocalTime.of(18, 0), LocalTime.of(19, 0), PriorityLevel.NEN_LAM, 5);
        try {
            gym.boQuaHomNay();
            ok("TC-07", gym.getSoNgayLienTiep() == 0 && gym.isBoQuaHomNay(),
                "Streak phải reset về 0 và đánh dấu bỏ qua hôm nay, không ném lỗi.");
        } catch (Exception e) {
            bad("TC-07", "Không được ném lỗi: " + e);
        }
    }

    // ---------------- TC-08 ----------------
    @Test
    void tc08_SkipDisruptionGiaiPhongKhungGio() {
        FlexibleTask docTaiLieu = new FlexibleTask("Đọc tài liệu", LocalTime.of(15, 0), LocalTime.of(16, 0), PriorityLevel.CO_THE_BO);
        List<ScheduleItem> ds = new ArrayList<>(List.of(docTaiLieu));
        new SkipDisruption(docTaiLieu).apDung(ds);
        ok("TC-08", ds.isEmpty(), "Khung giờ 15:00-16:00 phải được giải phóng (item bị xóa khỏi danh sách).");
    }

    // ---------------- Bonus: ReplanEngine ----------------
    @Test
    void bonus_ReplanEngine_HySinhViecCoTheBo() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Làm bài tập", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Đọc truyện giải trí", LocalTime.of(11, 0), LocalTime.of(14, 0), PriorityLevel.CO_THE_BO));
        ds.themItem(new FixedEvent("Học môn An ninh thông tin", LocalTime.of(13, 0), LocalTime.of(15, 0), PriorityLevel.BAT_BUOC));

        ReplanEngine engine = new ReplanEngine();
        KetQuaSoSanh kq = engine.reSapXep(ds, new DelayDisruption(90), LocalTime.of(9, 0));

        boolean docTruyenBiDeXuatBo = kq.getDanhSachDeXuatBo().stream().anyMatch(i -> i.getTen().equals("Đọc truyện giải trí"));
        boolean khongCanhBaoXungDotVoiFixed = kq.getDanhSachCanhBao().stream().noneMatch(s -> s.contains("Học môn An ninh thông tin"));
        ok("BONUS-1", docTruyenBiDeXuatBo && khongCanhBaoXungDotVoiFixed,
            "Việc 'Có thể bỏ' (đọc truyện) phải bị đề xuất bỏ để giữ chỗ cho việc cố định. deXuatBo=" + kq.getDanhSachDeXuatBo());
    }

    @Test
    void bonus_ReplanEngine_CanhBaoKhiVanKetDinh() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Deadline gấp", LocalTime.of(10, 0), LocalTime.of(13, 30), PriorityLevel.BAT_BUOC));
        ds.themItem(new FixedEvent("Học môn Lập trình ứng dụng", LocalTime.of(13, 0), LocalTime.of(15, 0), PriorityLevel.BAT_BUOC));

        ReplanEngine engine = new ReplanEngine();
        KetQuaSoSanh kq = engine.reSapXep(ds, new DelayDisruption(60), LocalTime.of(9, 0));
        ok("BONUS-2", kq.coCanhBao(), "Phải có cảnh báo vì việc Bắt buộc bị dồn ép chạm giờ cố định.");
    }

    @Test
    void bonus_GuardClause_GioBatDauSauGioKetThuc() {
        try {
            new FlexibleTask("Sai giờ", LocalTime.of(10, 0), LocalTime.of(9, 0), PriorityLevel.CO_THE_BO);
            bad("BONUS-3", "Phải ném IllegalArgumentException khi gioBatDau >= gioKetThuc.");
        } catch (IllegalArgumentException e) {
            pass("BONUS-3");
        }
    }

    @Test
    void bonus_LuuVaDocFileJson() {
        try {
            java.nio.file.Path tam = java.nio.file.Files.createTempFile("oops-lich-", ".json");
            DaySchedule ds = new DaySchedule(LocalDate.of(2026, 9, 12));
            ds.themItem(new FixedEvent("Học môn A", LocalTime.of(7, 10), LocalTime.of(11, 10), PriorityLevel.BAT_BUOC));
            ds.themItem(new RecurringEvent("Học tiếng Anh", LocalTime.of(20, 0), LocalTime.of(21, 0), PriorityLevel.NEN_LAM, 3));
            LuuLich.luu(ds, tam);
            DaySchedule docLai = LuuLich.doc(tam);
            java.nio.file.Files.deleteIfExists(tam);
            ok("BONUS-4", docLai.layLichHienTai().size() == 2,
                "Đọc lại file JSON phải ra đúng 2 item, thực tế: " + docLai.layLichHienTai().size());
        } catch (Exception e) {
            bad("BONUS-4", "Lỗi khi lưu/đọc JSON: " + e);
        }
    }

    // ---------------- BONUS: BoTheoDoiLich (giờ giấc) ----------------
    @Test
    void bonus_BoTheoDoiLich_PhanLoaiMucDoTre() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Làm báo cáo", LocalTime.of(13, 0), LocalTime.of(14, 0), PriorityLevel.NEN_LAM));
        // Giờ hiện tại giả lập: 13:25 -> trễ 25 phút -> phải rơi vào mức TRE_NANG (ngưỡng 20')
        List<CanhBaoViec> canhBao = BoTheoDoiLich.quet(ds, LocalTime.of(13, 25));
        ok("BONUS-5", canhBao.size() == 1 && canhBao.get(0).getMucDo() == MucDoCanhBao.TRE_NANG,
            "Trễ 25 phút phải được phân loại TRE_NANG, thực tế: " + (canhBao.isEmpty() ? "không có cảnh báo" : canhBao.get(0).getMucDo()));
    }

    @Test
    void bonus_BoTheoDoiLich_BoQuaViecDaHoanThanh() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ScheduleItem viec = new FlexibleTask("Tắm rửa", LocalTime.of(6, 0), LocalTime.of(6, 15), PriorityLevel.BAT_BUOC);
        viec.danhDauHoanThanh();
        ds.themItem(viec);
        List<CanhBaoViec> canhBao = BoTheoDoiLich.quet(ds, LocalTime.of(8, 0));
        ok("BONUS-6", canhBao.isEmpty(), "Việc đã đánh dấu hoàn thành thì không được nhắc trễ nữa.");
    }

    // ---------------- Bổ sung: fix bug "Bỏ việc đang trễ" ----------------
    /**
     * Tái hiện đúng tình huống người dùng báo cáo: đang 06:32, đã trễ giờ
     * Tắm rửa (06:00-06:20) và Ăn sáng (06:20-06:50) — cả 2 đều đã "đã qua"
     * so với 06:32. Bấm "Bỏ việc Tắm rửa" thì Tắm rửa PHẢI biến mất khỏi
     * lịch, dù nó đã bắt đầu trong quá khứ. Trước khi fix, bug nằm ở chỗ
     * ReplanEngine tách "đã qua/còn lại" TRƯỚC rồi mới áp dụng disruption,
     * khiến SkipDisruption không bao giờ thấy được item đã trễ.
     */
    @Test
    void bonus_SkipDisruption_BoDuocViecDaTre() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ScheduleItem tamRua = new FlexibleTask("Tắm rửa", LocalTime.of(6, 0), LocalTime.of(6, 20), PriorityLevel.BAT_BUOC);
        ds.themItem(tamRua);
        ds.themItem(new FlexibleTask("Ăn sáng", LocalTime.of(6, 20), LocalTime.of(6, 50), PriorityLevel.BAT_BUOC));
        ds.themItem(new FixedEvent("Học môn A", LocalTime.of(7, 10), LocalTime.of(11, 10), PriorityLevel.BAT_BUOC));

        ReplanEngine engine = new ReplanEngine();
        KetQuaSoSanh kq = engine.reSapXep(ds, new SkipDisruption(tamRua), LocalTime.of(6, 32));

        boolean daBoTamRua = kq.getLichSau().layLichHienTai().stream().noneMatch(i -> i.getTen().equals("Tắm rửa"));
        boolean anSangConGiuNguyen = kq.getLichSau().layLichHienTai().stream()
            .anyMatch(i -> i.getTen().equals("Ăn sáng") && i.getGioBatDau().equals(LocalTime.of(6, 20)));
        ok("BONUS-7", daBoTamRua && anSangConGiuNguyen,
            "Bỏ việc đang trễ (Tắm rửa) phải thực sự xoá nó khỏi lịch, các việc khác giữ nguyên. "
            + "Lịch sau: " + kq.getLichSau().layLichHienTai());
    }

    // ---------------- Bổ sung: ReplanEngine sinh đủ 3 phương án ----------------
    @Test
    void bonus_ReplanEngine_TaoDu3PhuongAn() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Làm bài tập", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Tập thể dục", LocalTime.of(11, 30), LocalTime.of(12, 0), PriorityLevel.CO_THE_BO));
        ds.themItem(new FixedEvent("Làm project", LocalTime.of(13, 0), LocalTime.of(15, 0), PriorityLevel.BAT_BUOC));

        ReplanEngine engine = new ReplanEngine();
        List<PhuongAn> cacPhuongAn = engine.taoCacPhuongAn(ds, new DelayDisruption(90), LocalTime.of(9, 0));

        boolean du3PhuongAn = cacPhuongAn.size() == 3;
        boolean tenKhacNhau = cacPhuongAn.stream().map(PhuongAn::getTen).distinct().count() == 3;
        ok("BONUS-8", du3PhuongAn && tenKhacNhau,
            "ReplanEngine phải sinh ra đúng 3 phương án với tên khác nhau, thực tế: "
            + (cacPhuongAn.isEmpty() ? "rỗng" : cacPhuongAn.stream().map(PhuongAn::getTen).toList()));
    }

    @Test
    void bonus_DeadlineFirstStrategy_KhongBoDeadlineTask() {
        // Kẹp DeadlineTask giữa 2 FixedEvent sao cho sau khi bị trễ nhẹ, nó
        // không còn đủ khe hở để vừa -> đúng nhánh "hết chỗ" (khongDuCho).
        DaySchedule ds = new DaySchedule(LocalDate.now());
        LocalDate homNay = LocalDate.now();
        ds.themItem(new FixedEvent("Họp đầu ngày", LocalTime.of(9, 0), LocalTime.of(9, 30), PriorityLevel.BAT_BUOC));
        ds.themItem(new DeadlineTask("Nộp báo cáo", LocalTime.of(9, 30), LocalTime.of(10, 30), PriorityLevel.NEN_LAM,
            LocalDateTime.of(homNay, LocalTime.of(23, 59)), homNay));
        ds.themItem(new FixedEvent("Học môn A", LocalTime.of(10, 0), LocalTime.of(15, 0), PriorityLevel.BAT_BUOC));

        DeadlineFirstStrategy chienLuoc = new DeadlineFirstStrategy();
        KetQuaSoSanh kq = chienLuoc.apDung(ds, new DelayDisruption(5), LocalTime.of(9, 0));

        boolean khongDeXuatBoDeadline = kq.getDanhSachDeXuatBo().stream().noneMatch(i -> i.getTen().equals("Nộp báo cáo"));
        boolean coCanhBaoVeDeadline = kq.getDanhSachCanhBao().stream().anyMatch(s -> s.contains("Nộp báo cáo"));
        ok("BONUS-9", khongDeXuatBoDeadline && coCanhBaoVeDeadline,
            "DeadlineFirstStrategy không được đề xuất BỎ DeadlineTask dù hết chỗ (phải cảnh báo thay vì bỏ). "
            + "deXuatBo=" + kq.getDanhSachDeXuatBo() + ", canhBao=" + kq.getDanhSachCanhBao());
    }

    // ---------------- Bổ sung: template tuần khác nhau theo từng thứ ----------------
    @Test
    void bonus_LichMauTuan_KhacNhauTheoThu() {
        // Tìm 1 Thứ Hai và 1 Thứ Ba gần nhất kể từ hôm nay để so sánh (LichMauTuan
        // sinh lịch dựa theo DayOfWeek, không phụ thuộc ngày cụ thể là bao nhiêu).
        LocalDate thuHai = LocalDate.now();
        while (thuHai.getDayOfWeek() != java.time.DayOfWeek.MONDAY) thuHai = thuHai.plusDays(1);
        LocalDate thuBa = thuHai.plusDays(1);
        LocalDate thuTu = thuHai.plusDays(2);

        DaySchedule lichThuHai = LichMauTuan.taoLich(thuHai);
        DaySchedule lichThuBa = LichMauTuan.taoLich(thuBa);
        DaySchedule lichThuTu = LichMauTuan.taoLich(thuTu);

        boolean thuHaiVaThuBaKhacNhau = !cungTenHoatDong(lichThuHai, lichThuBa);
        boolean thuHaiVaThuTuKhacNhau = !cungTenHoatDong(lichThuHai, lichThuTu);
        boolean vanCoHoatDongCaNhanChung = lichThuHai.layLichHienTai().stream().anyMatch(i -> i.getTen().equals("Ăn sáng"))
            && lichThuBa.layLichHienTai().stream().anyMatch(i -> i.getTen().equals("Ăn sáng"));

        ok("BONUS-10", thuHaiVaThuBaKhacNhau && thuHaiVaThuTuKhacNhau && vanCoHoatDongCaNhanChung,
            "Thời khóa biểu Thứ 2/3/4 phải khác nhau (không phải ngày nào cũng học y hệt), "
            + "nhưng hoạt động cá nhân (vd Ăn sáng) vẫn phải có mặt ở mọi ngày.");
    }

    private static boolean cungTenHoatDong(DaySchedule a, DaySchedule b) {
        java.util.Set<String> tenA = a.layLichHienTai().stream().map(ScheduleItem::getTen).collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> tenB = b.layLichHienTai().stream().map(ScheduleItem::getTen).collect(java.util.stream.Collectors.toSet());
        return tenA.equals(tenB);
    }

    // ================= VALIDATION (theo yêu cầu review 12/09) =================

    /**
     * VAL-01 — Cascade thật sự: A bị trễ -> A dời -> B dời -> B đụng việc cố
     * định F -> B bị ép dời kèm cảnh báo -> C (sau đó) không còn đủ chỗ vì bị
     * F chặn -> C được đề xuất BỎ. Test 1 chuỗi cascade 3 tầng thay vì chỉ
     * test 1 xung đột đơn lẻ.
     */
    @Test
    void val01_CascadeXungDot_A_KeoTheoB_KeoTheoC() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("A", LocalTime.of(9, 0), LocalTime.of(9, 30), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("B", LocalTime.of(9, 30), LocalTime.of(10, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("C", LocalTime.of(10, 0), LocalTime.of(10, 30), PriorityLevel.CO_THE_BO));
        ds.themItem(new FixedEvent("F", LocalTime.of(11, 0), LocalTime.of(11, 15), PriorityLevel.BAT_BUOC));

        ReplanEngine engine = new ReplanEngine();
        KetQuaSoSanh kq = engine.reSapXep(ds, new DelayDisruption(45), LocalTime.of(9, 0));

        boolean aVaBConDo = kq.getLichSau().layLichHienTai().stream()
            .anyMatch(i -> i.getTen().equals("A") && i.getGioBatDau().equals(LocalTime.of(9, 45)))
            && kq.getLichSau().layLichHienTai().stream()
            .anyMatch(i -> i.getTen().equals("B") && i.getGioBatDau().equals(LocalTime.of(10, 15)));
        boolean cBiDeXuatBo = kq.getDanhSachDeXuatBo().stream().anyMatch(i -> i.getTen().equals("C"));

        ok("VAL-01", aVaBConDo && cBiDeXuatBo,
            "Cascade A->B->C: A/B phải dời đúng theo trễ, C phải bị đề xuất BỎ vì đụng việc cố định F phía sau. "
            + "deXuatBo=" + kq.getDanhSachDeXuatBo() + ", lichSau=" + kq.getLichSau().layLichHienTai());
    }

    /**
     * VAL-02 — Kiểm tra 3 ReplanStrategy thực sự cho ra 3 KẾT QUẢ khác nhau
     * (không chỉ khác TÊN gọi), dùng đúng tinh thần kịch bản trong tài liệu
     * review: 1 DeadlineTask (Làm project) đụng 1 việc cố định (Họp tối) sau
     * khi bị trễ. "Giảm tải" phải chủ động đề xuất bỏ Làm project (vì không
     * phải Bắt buộc), còn "Ít thay đổi" và "Ưu tiên deadline" đều PHẢI ép dời
     * kèm cảnh báo thay vì bỏ nó.
     */
    @Test
    void val02_KichBanTaiLieu_3PhuongAnThatSuKhacNhau() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        LocalDate homNay = LocalDate.now();
        // K-14: báo trễ KHÔNG còn dời hàng loạt — chỉ việc thật sự bị ảnh hưởng mới bị đẩy. Kịch bản: 08:00
        // "Làm project" đã tới giờ, báo trễ 150' -> bị đẩy tới 10:30-12:00 và đụng "Họp tối" 11:30.
        ds.themItem(new DeadlineTask("Làm project", LocalTime.of(8, 0), LocalTime.of(9, 30), PriorityLevel.NEN_LAM,
            LocalDateTime.of(homNay, LocalTime.of(23, 59)), homNay));
        ds.themItem(new FixedEvent("Họp tối", LocalTime.of(11, 30), LocalTime.of(12, 0), PriorityLevel.BAT_BUOC));

        KetQuaSoSanh minimize = new MinimizeDisruptionStrategy().apDung(ds.saoChep(), new DelayDisruption(150), LocalTime.of(8, 0));
        KetQuaSoSanh deadlineFirst = new DeadlineFirstStrategy().apDung(ds.saoChep(), new DelayDisruption(150), LocalTime.of(8, 0));
        KetQuaSoSanh reduceWorkload = new ReduceWorkloadStrategy().apDung(ds.saoChep(), new DelayDisruption(150), LocalTime.of(8, 0));

        boolean minimizeKhongBoProject = minimize.getDanhSachDeXuatBo().stream().noneMatch(i -> i.getTen().equals("Làm project"));
        boolean deadlineFirstKhongBoProject = deadlineFirst.getDanhSachDeXuatBo().stream().noneMatch(i -> i.getTen().equals("Làm project"));
        boolean reduceWorkloadCoBoProject = reduceWorkload.getDanhSachDeXuatBo().stream().anyMatch(i -> i.getTen().equals("Làm project"));

        ok("VAL-02", minimizeKhongBoProject && deadlineFirstKhongBoProject && reduceWorkloadCoBoProject,
            "3 chiến lược phải cho 3 KẾT QUẢ khác nhau với cùng 1 sự cố: Ít thay đổi/Ưu tiên deadline phải ép dời "
            + "'Làm project' (không bỏ), còn Giảm tải phải chủ động đề xuất BỎ nó. Minimize.deXuatBo=" + minimize.getDanhSachDeXuatBo()
            + ", DeadlineFirst.deXuatBo=" + deadlineFirst.getDanhSachDeXuatBo() + ", ReduceWorkload.deXuatBo=" + reduceWorkload.getDanhSachDeXuatBo());
    }

    /** VAL-03 — "Chuyển sang ngày mai" khi ngày mai CÒN slot: task phải xuất hiện ở lịch ngày mai, không bị mất. */
    @Test
    void val03_ChuyenSangNgayMai_ConSlot() {
        LocalDate ngayMai = LocalDate.now().plusDays(1);
        DaySchedule lichNgayMai = new DaySchedule(ngayMai);
        lichNgayMai.themItem(new FlexibleTask("Việc có sẵn ngày mai", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.NEN_LAM));

        ScheduleItem biDeXuatBoHomNay = new FlexibleTask("Đọc sách", LocalTime.of(20, 0), LocalTime.of(20, 30), PriorityLevel.CO_THE_BO);
        boolean coDuCho = lichNgayMai.coDuCho(biDeXuatBoHomNay.layThoiLuong());
        if (coDuCho) {
            lichNgayMai.themItem(DaySchedule.taoBanSaoSangNgayKhac(biDeXuatBoHomNay, ngayMai));
        }
        boolean coTrongLichNgayMai = lichNgayMai.layLichHienTai().stream().anyMatch(i -> i.getTen().equals("Đọc sách"));
        ok("VAL-03", coDuCho && coTrongLichNgayMai,
            "Ngày mai còn slot -> 'Đọc sách' phải được chuyển sang lịch ngày mai thành công, không được mất task.");
    }

    /** VAL-04 — "Chuyển sang ngày mai" khi ngày mai CŨNG kín: phải báo "Không thể sắp xếp", không được âm thầm nhét chồng giờ. */
    @Test
    void val04_ChuyenSangNgayMai_NgayMaiCungKin_KhongTheSapXep() {
        LocalDate ngayMai = LocalDate.now().plusDays(1);
        DaySchedule lichNgayMai = new DaySchedule(ngayMai);
        // Nhồi gần kín cả ngày mai (23h20 trong tổng 23h59 khả dụng).
        lichNgayMai.themItem(new FixedEvent("Kín cả ngày", LocalTime.of(0, 0), LocalTime.of(23, 20), PriorityLevel.BAT_BUOC));

        ScheduleItem biDeXuatBoHomNay = new FlexibleTask("Tập gym", LocalTime.of(18, 0), LocalTime.of(19, 0), PriorityLevel.CO_THE_BO);
        boolean coDuCho = lichNgayMai.coDuCho(biDeXuatBoHomNay.layThoiLuong());
        ok("VAL-04", !coDuCho && lichNgayMai.layLichHienTai().size() == 1 && biDeXuatBoHomNay.getTen().equals("Tập gym"),
            "Ngày mai cũng đã kín -> coDuCho() phải trả về false (trạng thái 'Không thể sắp xếp'), "
            + "và KHÔNG được nhét thêm vào lịch ngày mai (không được âm thầm mất/chồng task).");
    }

    /** VAL-05 — Nút "🗑️ Xóa/Hủy hoạt động": phải xóa thật khỏi lịch và tăng đúng bộ đếm soDaHuy cho Tổng kết ngày. */
    @Test
    void val05_XoaHuyHoatDong_KhongMatDauVetSoDaHuy() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ScheduleItem viec = new FlexibleTask("Việc sẽ bị hủy", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.NEN_LAM);
        ds.themItem(viec);
        boolean daXoa = ds.huyItem(viec);
        ok("VAL-05", daXoa && ds.layLichHienTai().isEmpty() && ds.getSoDaHuy() == 1,
            "huyItem() phải xóa item khỏi lịch và tăng soDaHuy lên 1, thực tế soDaHuy=" + ds.getSoDaHuy()
            + ", còn lại trong lịch=" + ds.layLichHienTai().size());
    }

    /** VAL-06 — TongKetNgay phải tính đúng % hoàn thành theo SỐ LƯỢNG lẫn theo KHỐI LƯỢNG (2 chỉ số khác nhau). */
    @Test
    void val06_TongKetNgay_TinhDungPhanTramSoLuongVaKhoiLuong() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ScheduleItem a = new FlexibleTask("A", LocalTime.of(8, 0), LocalTime.of(9, 0), PriorityLevel.BAT_BUOC); // 60 phút, xong
        a.danhDauHoanThanh();
        ScheduleItem b = new FlexibleTask("B", LocalTime.of(9, 0), LocalTime.of(9, 30), PriorityLevel.NEN_LAM); // 30 phút, chưa xong, đã trễ
        ds.themItem(a);
        ds.themItem(b);

        TongKetNgay tk = TongKetNgay.tinhToan(ds, LocalTime.of(9, 15));

        boolean soLuongDung = tk.getTongSoViec() == 2 && tk.getSoHoanThanh() == 1 && tk.getSoTre() == 1
            && Math.abs(tk.getPhanTramSoLuong() - 50.0) < 0.01;
        boolean khoiLuongDung = Math.abs(tk.getPhanTramKhoiLuong() - (100.0 * 60 / 90)) < 0.01;
        ok("VAL-06", soLuongDung && khoiLuongDung,
            "2 chỉ số phải khác nhau khi 2 việc có thời lượng khác nhau: %SoLuong=" + tk.getPhanTramSoLuong()
            + " (kỳ vọng 50.0), %KhoiLuong=" + tk.getPhanTramKhoiLuong() + " (kỳ vọng " + (100.0 * 60 / 90) + ")");
    }

    // ================= ReminderService (mục 4 tài liệu thiết kế mở rộng) =================

    /** VAL-07 — Còn <= 15 phút (mặc định) là tới giờ 1 việc chưa hoàn thành -> phải có nhắc "Sắp tới". */
    @Test
    void val07_ReminderService_NhacSapToiGio() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Họp nhóm đồ án", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.NEN_LAM));

        ReminderService rs = new ReminderService(15);
        List<String> nhacXa = rs.quet(ds, LocalDate.now(), LocalTime.of(9, 30)); // còn 30 phút -> CHƯA tới ngưỡng 15
        List<String> nhacGan = rs.quet(ds, LocalDate.now(), LocalTime.of(9, 50)); // còn 10 phút -> phải nhắc

        boolean chuaNhacKhiConXa = nhacXa.isEmpty();
        boolean coNhacKhiConGan = nhacGan.size() == 1 && nhacGan.get(0).contains("Họp nhóm đồ án");
        ok("VAL-07", chuaNhacKhiConXa && coNhacKhiConGan,
            "Còn 30 phút thì chưa nhắc (ngưỡng mặc định 15 phút), còn 10 phút thì phải nhắc đúng 1 lần. "
            + "nhacXa=" + nhacXa + ", nhacGan=" + nhacGan);
    }

    /** VAL-08 — Quét nhiều lần liên tiếp trong cùng khung "sắp tới" không được nhắc lặp lại (tránh spam). */
    @Test
    void val08_ReminderService_KhongNhacTrungLap() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Nộp bài", LocalTime.of(10, 0), LocalTime.of(10, 30), PriorityLevel.BAT_BUOC));

        ReminderService rs = new ReminderService(15);
        List<String> lan1 = rs.quet(ds, LocalDate.now(), LocalTime.of(9, 50));
        List<String> lan2 = rs.quet(ds, LocalDate.now(), LocalTime.of(9, 51)); // 1 phút sau, vẫn trong ngưỡng
        ok("VAL-08", lan1.size() == 1 && lan2.isEmpty(),
            "Lần quét đầu phải nhắc, lần quét sau (vẫn việc đó, chưa qua giờ) không được nhắc lại. "
            + "lan1=" + lan1 + ", lan2=" + lan2);
    }

    /** VAL-09 — DeadlineTask còn đúng 3 tiếng trước hạn chót -> phải nhắc mốc "3 tiếng", không nhắc nhầm mốc khác. */
    @Test
    void val09_ReminderService_NhacDeadlineGanKe() {
        LocalDate homNay = LocalDate.now();
        LocalDateTime hanChot = LocalDateTime.of(homNay, LocalTime.of(23, 0));
        DaySchedule ds = new DaySchedule(homNay);
        ds.themItem(new DeadlineTask("Nộp báo cáo cuối kỳ", LocalTime.of(19, 0), LocalTime.of(20, 0),
            PriorityLevel.NEN_LAM, hanChot, homNay));

        ReminderService rs = new ReminderService(15);
        // 20:00 -> còn đúng 3 tiếng tới hạn 23:00.
        List<String> nhac = rs.quet(ds, homNay, LocalTime.of(20, 0));
        boolean coNhac3Tieng = nhac.stream().anyMatch(s -> s.contains("3 tiếng") && s.contains("Nộp báo cáo cuối kỳ"));
        boolean khongNhac1Tieng = nhac.stream().noneMatch(s -> s.contains("1 tiếng"));
        ok("VAL-09", coNhac3Tieng && khongNhac1Tieng,
            "Còn đúng 3 tiếng trước hạn phải nhắc mốc '3 tiếng', chưa tới mốc '1 tiếng'. nhac=" + nhac);
    }

    // ---------------- VAL-10..14: vòng sửa cuối (persistence/gap/deadline/midnight/null) ----------------

    /** VAL-10 — SAVE -> LOAD phải giữ nguyên id, hoanThanh, soLanDaDoi, streak/boQuaHomNay, deadline state, soDaHuy. */
    @Test
    void val10_Persistence_GiuNguyenToanBoTrangThai() {
        try {
            java.nio.file.Path tam = java.nio.file.Files.createTempFile("oops-lich-val10-", ".json");
            LocalDate ngay = LocalDate.of(2026, 9, 15);
            DaySchedule ds = new DaySchedule(ngay);

            FlexibleTask ft = new FlexibleTask("Làm bài", LocalTime.of(14, 0), LocalTime.of(15, 0), PriorityLevel.NEN_LAM);
            ft.xuLyKhiBiTre(10); // soLanDaDoi = 1
            ft.danhDauHoanThanh();
            ds.themItem(ft);

            RecurringEvent re = new RecurringEvent("Tập thể dục", LocalTime.of(6, 0), LocalTime.of(6, 30), PriorityLevel.CO_THE_BO, 5);
            re.boQuaHomNay(); // boQuaHomNay = true, streak reset về 0
            ds.themItem(re);

            LocalDateTime hanChot = LocalDateTime.of(ngay, LocalTime.of(23, 0));
            DeadlineTask dt = new DeadlineTask("Nộp báo cáo", LocalTime.of(21, 0), LocalTime.of(21, 30), PriorityLevel.BAT_BUOC, hanChot, ngay);
            dt.xuLyKhiBiTre(150); // -> 23:30, trễ hạn -> canhBaoTreHan = true
            ds.themItem(dt);

            ScheduleItem bDaHuy = new FlexibleTask("Việc sẽ hủy", LocalTime.of(8, 0), LocalTime.of(8, 30), PriorityLevel.CO_THE_BO);
            ds.themItem(bDaHuy);
            ds.huyItem(bDaHuy); // soDaHuy = 1

            LuuLich.luu(ds, tam);
            DaySchedule docLai = LuuLich.doc(tam);
            java.nio.file.Files.deleteIfExists(tam);

            List<ScheduleItem> ketQua = docLai.layLichHienTai();
            FlexibleTask ftLai = (FlexibleTask) timTheoTen(ketQua, "Làm bài");
            RecurringEvent reLai = (RecurringEvent) timTheoTen(ketQua, "Tập thể dục");
            DeadlineTask dtLai = (DeadlineTask) timTheoTen(ketQua, "Nộp báo cáo");

            boolean ok = ftLai != null && ftLai.isHoanThanh() && ftLai.getSoLanDaDoi() == 1
                && ftLai.getId().equals(ft.getId())
                && reLai != null && reLai.isBoQuaHomNay() && reLai.getSoNgayLienTiep() == 0
                && dtLai != null && dtLai.isCanhBaoTreHan()
                && docLai.getSoDaHuy() == 1;
            ok("VAL-10", ok, "Sau SAVE->LOAD phải giữ nguyên id/hoanThanh/soLanDaDoi/streak/boQuaHomNay/"
                + "canhBaoTreHan/soDaHuy. ftLai=" + ftLai + ", reLai=" + reLai + ", dtLai=" + dtLai
                + ", soDaHuy=" + docLai.getSoDaHuy());
        } catch (Exception e) {
            bad("VAL-10", "Lỗi khi lưu/đọc JSON: " + e);
        }
    }

    private static ScheduleItem timTheoTen(List<ScheduleItem> ds, String ten) {
        return ds.stream().filter(it -> it.getTen().equals(ten)).findFirst().orElse(null);
    }

    /** VAL-11 — 3 việc cố định 08-12, 13-17, 18-22: tổng thời gian trống còn nhiều, nhưng không có khe 3 tiếng LIÊN TỤC. */
    @Test
    void val11_GapFinding_TongTrongNhieuNhungKhongCoKheLienTuc() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        // Chặn luôn đầu ngày (00:00-08:00) để không tạo ra 1 "khe giả" 8 tiếng
        // ở đầu ngày làm sai lệch mục đích của test (chỉ còn khe 12-13h/17-18h/22h-23:59).
        ds.themItem(new FixedEvent("Đầu ngày", LocalTime.of(0, 0), LocalTime.of(8, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FixedEvent("A", LocalTime.of(8, 0), LocalTime.of(12, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FixedEvent("B", LocalTime.of(13, 0), LocalTime.of(17, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FixedEvent("C", LocalTime.of(18, 0), LocalTime.of(22, 0), PriorityLevel.BAT_BUOC));

        boolean khongCoKhe3Tieng = !ds.coDuCho(Duration.ofHours(3));
        boolean coKhe1Tieng = ds.coDuCho(Duration.ofMinutes(60)); // khe 12h-13h hoặc 17h-18h
        ok("VAL-11", khongCoKhe3Tieng && coKhe1Tieng,
            "Tổng thời gian trống còn nhiều nhưng không có khe LIÊN TỤC 3 tiếng nào (chỉ có khe 1 tiếng 12-13h/17-18h). "
            + "coDuCho(3h)=" + !khongCoKhe3Tieng + ", coDuCho(1h)=" + coKhe1Tieng);
    }

    /** VAL-12 — Khi Replan Engine ép/dời DeadlineTask qua hạn chót, canhBaoTreHan phải được đồng bộ = true. */
    @Test
    void val12_DeadlineWarning_DongBoKhiBiEpDoiQuaHan() {
        LocalDate ngay = LocalDate.now();
        DaySchedule ds = new DaySchedule(ngay);
        ds.themItem(new FixedEvent("Họp", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.BAT_BUOC));
        LocalDateTime hanChot = LocalDateTime.of(ngay, LocalTime.of(10, 0));
        ds.themItem(new DeadlineTask("Nộp bài", LocalTime.of(9, 30), LocalTime.of(9, 45), PriorityLevel.NEN_LAM, hanChot, ngay));

        ReplanEngine engine = new ReplanEngine();
        KetQuaSoSanh kq = engine.reSapXep(ds, new DelayDisruption(5), LocalTime.of(8, 0));
        DeadlineTask sauKhi = (DeadlineTask) timTheoTen(kq.getLichSau().layLichHienTai(), "Nộp bài");

        ok("VAL-12", sauKhi != null && sauKhi.isCanhBaoTreHan() && sauKhi.getGioKetThuc().isAfter(LocalTime.of(10, 0)),
            "DeadlineTask bị đẩy qua hạn chót (do việc cố định chiếm chỗ) phải có canhBaoTreHan=true đồng bộ theo giờ MỚI. "
            + "sauKhi=" + sauKhi);
    }

    /**
     * VAL-13 — Task cuối ngày không đủ chỗ trước 23:59: KHÔNG được ép/cắt bớt
     * thời lượng thật (cập nhật theo fix mới nhất — trước đây engine ép đặt
     * cứng vào 23:59, ÂM THẦM CẮT MẤT thời lượng task, vd 60 phút chỉ còn 29
     * phút). Giờ item phải bị loại khỏi lịch hôm nay (đưa vào danh sách "đề
     * xuất bỏ" để MainFrame thử chuyển sang ngày mai), không xuất hiện trong
     * lịch sau với thời lượng bị cắt xén, và thời lượng gốc phải được giữ
     * nguyên nếu sau đó được chuyển sang ngày khác.
     */
    @Test
    void val13_CrossMidnight_KhongCatBotThoiLuong() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FixedEvent("Việc gấp", LocalTime.of(23, 0), LocalTime.of(23, 45), PriorityLevel.BAT_BUOC));
        // "Xem phim" dài 40 phút (23:10-23:50); bị "Việc gấp" chặn nên phải dời
        // sau 23:45 -> 24:25, vượt quá nửa đêm.
        ds.themItem(new FlexibleTask("Xem phim", LocalTime.of(23, 10), LocalTime.of(23, 50), PriorityLevel.NEN_LAM));

        ReplanEngine engine = new ReplanEngine();
        KetQuaSoSanh kq = engine.reSapXep(ds, new DelayDisruption(1), LocalTime.of(22, 0));
        ScheduleItem conTrongLichSau = timTheoTen(kq.getLichSau().layLichHienTai(), "Xem phim");
        boolean coTrongDeXuatBo = kq.getDanhSachDeXuatBo().stream().anyMatch(i -> i.getTen().equals("Xem phim"));
        ScheduleItem deXuat = kq.getDanhSachDeXuatBo().stream()
            .filter(i -> i.getTen().equals("Xem phim")).findFirst().orElse(null);
        boolean giuNguyenThoiLuong = deXuat != null && deXuat.layThoiLuong().toMinutes() == 40;
        boolean coThongBaoChuyenNgayMai = kq.getDanhSachThayDoi().stream().anyMatch(s -> s.contains("chuyển sang ngày mai"));

        ok("VAL-13", conTrongLichSau == null && coTrongDeXuatBo && giuNguyenThoiLuong && coThongBaoChuyenNgayMai,
            "Việc không đủ chỗ trước nửa đêm phải bị loại khỏi lịch hôm nay (không ép/cắt vào 23:59) và giữ nguyên "
            + "thời lượng gốc trong danh sách đề xuất. conTrongLichSau=" + conTrongLichSau
            + ", coTrongDeXuatBo=" + coTrongDeXuatBo + ", deXuat=" + deXuat + ", thayDoi=" + kq.getDanhSachThayDoi());
    }

    /** VAL-14 — Input null (SkipDisruption/ReplanEngine/DaySchedule) phải reject sớm bằng NullPointerException, không âm thầm bỏ qua. */
    @Test
    void val14_NullInvalid_SkipDisruptionVaReplanEngineVaDaySchedule() {
        boolean skipRejectNull = false;
        try {
            new SkipDisruption(null);
        } catch (NullPointerException e) {
            skipRejectNull = true;
        } catch (Exception e) {
            // sai loại exception -> vẫn tính fail bên dưới
        }

        boolean engineRejectNull = false;
        try {
            new ReplanEngine().taoCacPhuongAn(null, new DelayDisruption(5), LocalTime.now());
        } catch (NullPointerException e) {
            engineRejectNull = true;
        } catch (Exception e) {
            // sai loại exception
        }

        boolean dsRejectNull = false;
        try {
            new DaySchedule(null);
        } catch (NullPointerException e) {
            dsRejectNull = true;
        } catch (Exception e) {
            // sai loại exception
        }

        ok("VAL-14", skipRejectNull && engineRejectNull && dsRejectNull,
            "SkipDisruption(null)/ReplanEngine.taoCacPhuongAn(null,...)/new DaySchedule(null) đều phải ném "
            + "NullPointerException ngay lập tức, không được âm thầm bỏ qua. skip=" + skipRejectNull
            + ", engine=" + engineRejectNull + ", ds=" + dsRejectNull);
    }

    /**
     * VAL-15 — Daily Summary: việc đã hủy vẫn phải nằm trong "khối lượng kế
     * hoạch" của ngày (mẫu số), không được biến mất khỏi phép tính % hoàn
     * thành chỉ vì đã bị xóa khỏi danh sách hiện tại.
     */
    @Test
    void val15_TongKetNgay_ViecDaHuyVanTinhVaoMauSo() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ScheduleItem v1 = new FlexibleTask("Việc 1", LocalTime.of(8, 0), LocalTime.of(9, 0), PriorityLevel.NEN_LAM);
        ScheduleItem v2 = new FlexibleTask("Việc 2", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.NEN_LAM);
        v1.danhDauHoanThanh();
        v2.danhDauHoanThanh();
        ds.themItem(v1);
        ds.themItem(v2);
        ScheduleItem bHuy1 = new FlexibleTask("Bị hủy 1", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.CO_THE_BO);
        ScheduleItem bHuy2 = new FlexibleTask("Bị hủy 2", LocalTime.of(11, 0), LocalTime.of(12, 0), PriorityLevel.CO_THE_BO);
        ds.themItem(bHuy1);
        ds.themItem(bHuy2);
        ds.huyItem(bHuy1);
        ds.huyItem(bHuy2);

        TongKetNgay tk = TongKetNgay.tinhToan(ds, LocalTime.of(13, 0));
        // 2 hoàn thành / (2 còn lại + 2 đã hủy) = 2/4 = 50%, KHÔNG phải 2/2 = 100%.
        boolean tongDung = tk.getTongSoViec() == 4;
        boolean phanTramDung = Math.abs(tk.getPhanTramSoLuong() - 50.0) < 0.001;
        ok("VAL-15", tongDung && phanTramDung,
            "Việc đã hủy phải cộng vào tongSoViec làm mẫu số (2 xong + 2 hủy = 4 tổng, 50%), thực tế tongSoViec="
            + tk.getTongSoViec() + ", phanTramSoLuong=" + tk.getPhanTramSoLuong());
    }

    private static void ok(String ma, boolean dieuKien, String moTaNeuSai) {
        assertTrue(dieuKien, ma + ": " + moTaNeuSai);
    }

    private static void pass(String ma) {
        // PASS ngầm: không có assertion nào thất bại
    }

    private static void bad(String ma, String moTa) {
        fail(ma + ": " + moTa);
    }
}
