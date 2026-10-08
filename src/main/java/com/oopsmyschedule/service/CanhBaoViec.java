package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.ScheduleItem;

/** 1 cảnh báo: việc nào, trễ bao nhiêu phút, mức độ gì. */
public class CanhBaoViec {

    private final ScheduleItem item;
    private final long soPhutTre;
    private final MucDoCanhBao mucDo;

    public CanhBaoViec(ScheduleItem item, long soPhutTre, MucDoCanhBao mucDo) {
        this.item = item;
        this.soPhutTre = soPhutTre;
        this.mucDo = mucDo;
    }

    public ScheduleItem getItem() {
        return item;
    }

    public long getSoPhutTre() {
        return soPhutTre;
    }

    public MucDoCanhBao getMucDo() {
        return mucDo;
    }
}
