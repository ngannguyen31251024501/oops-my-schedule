package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;

/**
 * Phương án 2 — "Ưu tiên deadline": CỐ GẮNG giữ nguyên các việc có hạn chót
 * riêng ({@link DeadlineTask}) và việc cố định — không chủ động đề xuất bỏ
 * DeadlineTask khi hết chỗ, mà "hy sinh" (đề xuất bỏ) các việc "Nên làm"
 * thông thường trước (khác với "Ít thay đổi" chỉ bỏ việc "Có thể bỏ"). Tuy
 * nhiên đây KHÔNG PHẢI bảo vệ tuyệt đối: nếu vẫn không đủ chỗ, DeadlineTask
 * có thể bị ép dời và trễ hạn — lúc đó strategy chỉ giữ task lại và bật cờ
 * cảnh báo trễ hạn (canhBaoTreHan = true) để người dùng biết, chứ không đảm
 * bảo đúng hạn 100%.
 */
public class DeadlineFirstStrategy extends AbstractReplanStrategy {

    @Override
    public String getTen() {
        return "Ưu tiên deadline";
    }

    @Override
    public String getBieuTuong() {
        return "⏰";
    }

    @Override
    public String getMoTaNgan() {
        return "Ưu tiên giữ các việc có hạn chót; nếu không còn đủ chỗ, vẫn giữ việc lại và cảnh báo nguy cơ trễ hạn.";
    }

    @Override
    protected boolean coTheHySinh(ScheduleItem item) {
        // DeadlineTask không bao giờ bị đề xuất bỏ — chấp nhận cảnh báo/ép dời
        // còn hơn là để lỡ hạn chót.
        if (item instanceof DeadlineTask) {
            return false;
        }
        return item.getDoUuTien() == PriorityLevel.CO_THE_BO || item.getDoUuTien() == PriorityLevel.NEN_LAM;
    }
}
