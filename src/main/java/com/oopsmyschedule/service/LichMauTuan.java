package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.RecurringEvent;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Sinh ra 1 {@link DaySchedule} MẶC ĐỊNH cho 1 ngày cụ thể — chỉ dùng làm
 * gợi ý ban đầu khi người dùng lần đầu mở xem 1 ngày chưa từng chỉnh sửa;
 * sau khi người dùng sửa/thêm/xóa việc trong ngày đó,
 * {@link com.oopsmyschedule.ui.MainFrame} sẽ giữ lại bản đã chỉnh (không
 * sinh lại từ template nữa).
 * <p>
 * QUAN TRỌNG: "không phải ngày nào lịch cũng giống nhau" — thực tế 1 tuần đi
 * học không phải hôm nào cũng học y hệt giờ đó (có hôm không học, có hôm học
 * ca khác). Trước đây cả 5 ngày Thứ 2 - Thứ 6 dùng chung 1 khối "Học ở
 * trường 7:10-11:10" — giờ tách riêng theo TỪNG thứ trong tuần, còn cuối
 * tuần (Thứ 7, Chủ nhật) vẫn giữ style "không đi học, việc cá nhân/nghỉ
 * ngơi" như cũ.
 * <p>
 * Đây vẫn chỉ là DỮ LIỆU MẪU minh họa việc thời khóa biểu có thể khác nhau
 * giữa các ngày — bạn nên sửa lại đúng giờ/môn học/CLB thật của mình trong
 * từng case bên dưới (hoặc đơn giản hơn: mở app rồi "+ Thêm việc"/"Sửa" theo
 * đúng lịch thật — template chỉ áp dụng cho ngày CHƯA từng được chỉnh).
 */
public final class LichMauTuan {

    private LichMauTuan() {
    }

    public static DaySchedule taoLich(LocalDate ngay) {
        DayOfWeek thu = ngay.getDayOfWeek();
        return switch (thu) {
            case MONDAY -> taoThuHai(ngay);
            case TUESDAY -> taoThuBa(ngay);
            case WEDNESDAY -> taoThuTu(ngay);
            case THURSDAY -> taoThuNam(ngay);
            case FRIDAY -> taoThuSau(ngay);
            case SATURDAY, SUNDAY -> taoLichCuoiTuan(ngay);
        };
    }

    /** Hoạt động cá nhân của NGÀY ĐI HỌC (giống nhau Thứ 2 - Thứ 6): ăn uống, ngủ. */
    private static void themCaNhanNgayThuong(DaySchedule ds) {
        ds.themItem(new FlexibleTask("Tắm rửa", LocalTime.of(6, 0), LocalTime.of(6, 20), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn sáng", LocalTime.of(6, 20), LocalTime.of(6, 50), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn trưa", LocalTime.of(11, 40), LocalTime.of(12, 10), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn tối", LocalTime.of(18, 0), LocalTime.of(18, 30), PriorityLevel.NEN_LAM));
        ds.themItem(new FixedEvent("Chuẩn bị đi ngủ", LocalTime.of(23, 0), LocalTime.of(23, 30), PriorityLevel.BAT_BUOC));
    }

    /** Thứ 2: học cả sáng, chiều làm bài tập (deadline hạn chót ngày mai), tối học tiếng Anh. */
    private static DaySchedule taoThuHai(LocalDate ngay) {
        DaySchedule ds = new DaySchedule(ngay);
        themCaNhanNgayThuong(ds);
        ds.themItem(new FixedEvent("Học ở trường (ca sáng)", LocalTime.of(7, 10), LocalTime.of(11, 10), PriorityLevel.BAT_BUOC));
        ds.themItem(new DeadlineTask("Làm bài tập về nhà", LocalTime.of(13, 30), LocalTime.of(15, 0),
            PriorityLevel.NEN_LAM, LocalDateTime.of(ngay.plusDays(1), LocalTime.of(23, 59))));
        ds.themItem(new RecurringEvent("Học tiếng Anh", LocalTime.of(17, 0), LocalTime.of(17, 30), PriorityLevel.NEN_LAM, 0));
        return ds;
    }

    /** Thứ 3: KHÔNG có lịch học buổi sáng hôm nay -> tự học ở nhà + sinh hoạt CLB buổi chiều. */
    private static DaySchedule taoThuBa(LocalDate ngay) {
        DaySchedule ds = new DaySchedule(ngay);
        themCaNhanNgayThuong(ds);
        ds.themItem(new FlexibleTask("Tự học ở nhà", LocalTime.of(9, 0), LocalTime.of(11, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FixedEvent("Sinh hoạt CLB", LocalTime.of(14, 0), LocalTime.of(16, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new RecurringEvent("Tập thể dục", LocalTime.of(17, 0), LocalTime.of(17, 45), PriorityLevel.NEN_LAM, 0));
        return ds;
    }

    /** Thứ 4: học CA CHIỀU (khác giờ Thứ 2), sáng rảnh, tối học tiếng Anh trễ hơn. */
    private static DaySchedule taoThuTu(LocalDate ngay) {
        DaySchedule ds = new DaySchedule(ngay);
        themCaNhanNgayThuong(ds);
        ds.themItem(new FixedEvent("Học ở trường (ca chiều)", LocalTime.of(13, 0), LocalTime.of(17, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new RecurringEvent("Học tiếng Anh", LocalTime.of(19, 30), LocalTime.of(20, 0), PriorityLevel.NEN_LAM, 0));
        return ds;
    }

    /** Thứ 5: học cả sáng như Thứ 2, nhưng chiều là hạn nộp báo cáo tuần thay vì bài tập thường. */
    private static DaySchedule taoThuNam(LocalDate ngay) {
        DaySchedule ds = new DaySchedule(ngay);
        themCaNhanNgayThuong(ds);
        ds.themItem(new FixedEvent("Học ở trường (ca sáng)", LocalTime.of(7, 10), LocalTime.of(11, 10), PriorityLevel.BAT_BUOC));
        ds.themItem(new DeadlineTask("Nộp báo cáo tuần", LocalTime.of(13, 30), LocalTime.of(15, 0),
            PriorityLevel.NEN_LAM, LocalDateTime.of(ngay, LocalTime.of(23, 59))));
        return ds;
    }

    /** Thứ 6: ca học NGẮN hơn các ngày khác (nghỉ sớm), chiều rảnh để ôn bài. */
    private static DaySchedule taoThuSau(LocalDate ngay) {
        DaySchedule ds = new DaySchedule(ngay);
        themCaNhanNgayThuong(ds);
        ds.themItem(new FixedEvent("Học ở trường (ca ngắn)", LocalTime.of(7, 10), LocalTime.of(9, 40), PriorityLevel.BAT_BUOC));
        ds.themItem(new FlexibleTask("Ôn bài / Đọc thêm", LocalTime.of(14, 0), LocalTime.of(16, 0), PriorityLevel.CO_THE_BO));
        ds.themItem(new RecurringEvent("Học tiếng Anh", LocalTime.of(17, 0), LocalTime.of(17, 30), PriorityLevel.NEN_LAM, 0));
        return ds;
    }

    /** Thứ 7 - Chủ nhật: không có lịch học cố định, thay bằng việc cá nhân/nghỉ ngơi (dậy trễ hơn). */
    private static DaySchedule taoLichCuoiTuan(LocalDate ngay) {
        DaySchedule ds = new DaySchedule(ngay);
        ds.themItem(new FlexibleTask("Ngủ nướng / Dọn phòng", LocalTime.of(8, 0), LocalTime.of(9, 0), PriorityLevel.CO_THE_BO));
        ds.themItem(new FlexibleTask("Ăn sáng", LocalTime.of(9, 0), LocalTime.of(9, 30), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Giặt đồ / Việc nhà", LocalTime.of(9, 30), LocalTime.of(10, 30), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ôn bài / Đọc thêm", LocalTime.of(14, 0), LocalTime.of(16, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new RecurringEvent("Tập thể dục", LocalTime.of(17, 0), LocalTime.of(17, 45), PriorityLevel.NEN_LAM, 0));
        ds.themItem(new FlexibleTask("Ăn tối", LocalTime.of(18, 30), LocalTime.of(19, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FixedEvent("Chuẩn bị đi ngủ", LocalTime.of(23, 30), LocalTime.of(23, 59), PriorityLevel.BAT_BUOC));
        return ds;
    }
}
