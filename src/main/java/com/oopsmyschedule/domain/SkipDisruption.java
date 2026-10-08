package com.oopsmyschedule.domain;

import java.util.List;
import java.util.Objects;

/** Sự cố "Tôi phải bỏ việc này" — loại 1 item khỏi lịch, giải phóng khung giờ của nó. */
public class SkipDisruption extends Disruption {

    private final ScheduleItem itemBiBo;

    /**
     * BR-13: trước đây {@code itemBiBo == null} được âm thầm chấp nhận ở
     * constructor rồi {@code apDung()} lặng lẽ không làm gì — input sai
     * trông như chương trình chạy "bình thường". Giờ reject ngay tại
     * constructor để lỗi lộ ra sớm, đúng chỗ gọi sai (UI phải đảm bảo có
     * item được chọn trước khi tạo SkipDisruption).
     */
    public SkipDisruption(ScheduleItem itemBiBo) {
        super("Bỏ việc: " + Objects.requireNonNull(itemBiBo, "Việc cần bỏ không được null.").getTen());
        this.itemBiBo = itemBiBo;
    }

    public ScheduleItem getItemBiBo() {
        return itemBiBo;
    }

    @Override
    public void apDung(List<ScheduleItem> danhSachConLai) {
        if (itemBiBo instanceof RecurringEvent recurring) {
            // Recurring event "bị bỏ" nghĩa là bỏ qua hôm nay, không xóa khỏi lịch vĩnh viễn.
            recurring.boQuaHomNay();
            return;
        }
        // Guard: FixedEvent về nguyên tắc không nên bị bỏ tùy tiện, nhưng nếu
        // người dùng chủ động chọn bỏ (ví dụ họp bị hủy) thì vẫn cho phép xóa
        // khỏi danh sách còn lại — khác với việc "dời giờ" (bị chặn cứng).
        danhSachConLai.remove(itemBiBo);
    }
}
