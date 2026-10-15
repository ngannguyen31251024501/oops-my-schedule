package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lịch trình của 1 ngày cụ thể — chứa danh sách {@link ScheduleItem}.
 * Đây là lớp "aggregate root" đơn giản, chịu trách nhiệm giữ tính hợp lệ của
 * toàn bộ lịch (không cho 2 việc cố định trùng giờ).
 */
public class DaySchedule {

    private final LocalDate ngay;
    private final List<ScheduleItem> danhSachItem = new ArrayList<>();
    // Các công việc chưa được xếp vào lịch trong ngày.
    private final List<ScheduleItem> danhSachChuaXepLich = new ArrayList<>();
    /**
     * Đếm số việc người dùng chủ động XÓA/HỦY trong ngày (khác với việc bị
     * ReplanEngine "đề xuất bỏ" rồi chuyển sang ngày mai) — dùng cho
     * "Tổng kết ngày" ({@link TongKetNgay}) ở mục "Đã hủy". Không reset lại vì
     * mỗi ngày vốn đã có 1 {@link DaySchedule} riêng.
     */
    private int soDaHuy = 0;

    /**
     * Nhật ký các sự cố đã xảy ra trong ngày + cách Replan Engine đã xử lý —
     * dùng cho mục "🔎 Vì sao hôm nay chưa đạt" trong Tổng kết ngày. Không
     * reset vì mỗi ngày vốn đã có 1 {@link DaySchedule} riêng.
     */
    private final List<SuCoNhatKy> nhatKySuCo = new ArrayList<>();

    public DaySchedule(LocalDate ngay) {
        // BR-12: DaySchedule không thể tồn tại mà không gắn với 1 ngày cụ thể
        // — mọi so sánh/khôi phục (LuuLich, LichMauTuan, ngày kế tiếp trong
        // "chuyển sang ngày mai") đều dựa vào ngay != null.
        this.ngay = java.util.Objects.requireNonNull(ngay, "Ngày của DaySchedule không được null.");
    }

    /**
     * Thêm 1 item vào lịch.
     * Guard clause: nếu item là {@link FixedEvent} và trùng giờ với 1
     * FixedEvent đã có trong lịch → ném {@link IllegalArgumentException},
     * không lưu item mới (đúng TC-01 trong tài liệu thiết kế).
     */

        public void themItem(ScheduleItem item) {
            if (item == null) {
                throw new IllegalArgumentException("Không thể thêm item null vào lịch.");
            }
            // K-06: không cho 2 item cùng id tồn tại trong 1 lịch
            for (ScheduleItem cu : danhSachItem) {
                if (cu.getId().equals(item.getId())) {
                    throw new IllegalArgumentException("Trùng id: " + item.getId());
                }
            }
        if (item instanceof FixedEvent) {
            for (ScheduleItem cu : danhSachItem) {
                if (cu instanceof FixedEvent && cu.trung(item)) {
                    throw new IllegalArgumentException(
                        "Việc cố định \"" + item.getTen() + "\" (" + item.getGioBatDau() + "-" + item.getGioKetThuc()
                        + ") bị trùng giờ với việc cố định đã có \"" + cu.getTen()
                        + "\" (" + cu.getGioBatDau() + "-" + cu.getGioKetThuc() + ").");
                }
            }
        }
        danhSachItem.add(item);
        sapXepTheoGio();
    }

    public boolean xoaItem(ScheduleItem item) {
        return danhSachItem.remove(item);
    }

    /**
     * Người dùng chủ động bấm "🗑️ Xóa/Hủy hoạt động" trên 1 thẻ việc — khác
     * với {@link #xoaItem(ScheduleItem)} đơn thuần (vốn cũng được dùng nội bộ
     * khi "Sửa" = xóa ở ngày cũ + thêm ở ngày mới), method này còn tăng bộ đếm
     * {@code soDaHuy} để "Tổng kết ngày" biết hôm nay đã hủy bao nhiêu việc.
     */
    public boolean huyItem(ScheduleItem item) {
        boolean daXoa = danhSachItem.remove(item);
        if (daXoa) {
            soDaHuy++;
        }
        return daXoa;
    }

    public int getSoDaHuy() {
        return soDaHuy;
    }

    /** Ghi lại 1 sự cố vừa được xử lý (gọi khi người dùng áp dụng 1 phương án Replan Engine). */
    public void ghiNhatKySuCo(SuCoNhatKy nhatKy) {
        nhatKySuCo.add(nhatKy);
    }

    /** Nhật ký sự cố trong ngày, theo đúng thứ tự xảy ra. */
    public List<SuCoNhatKy> getNhatKySuCo() {
        return Collections.unmodifiableList(nhatKySuCo);
    }

    /** Dùng bởi ReplanEngine để ghi lại toàn bộ danh sách sau khi đã tái sắp xếp. */
    public void thayTheToanBo(List<ScheduleItem> danhSachMoi) {
        danhSachItem.clear();
        danhSachItem.addAll(danhSachMoi);
        sapXepTheoGio();
    }

    private void sapXepTheoGio() {
        danhSachItem.sort((a, b) -> a.getGioBatDau().compareTo(b.getGioBatDau()));
    }

    /**
     * Kiểm tra xem lịch còn 1 khoảng trống LIÊN TỤC đủ cho {@code thoiLuong}
     * không — dùng khi cần chuyển 1 việc bị "đề xuất bỏ" hôm nay sang NGÀY
     * KHÁC (xem {@link #taoBanSaoSangNgayKhac}): nếu ngày đó cũng đã kín lịch
     * thì không nên cố nhét thêm (sẽ chồng giờ vô tội vạ), mà phải báo
     * "Không thể sắp xếp" để người dùng biết task chưa có chỗ, thay vì âm
     * thầm mất task hoặc âm thầm nhét chồng giờ.
     * <p>
     * FIX BUG "Gap Finding": bản cũ chỉ so TỔNG thời lượng đã dùng với tổng
     * thời gian trong ngày — điều đó không chứng minh có 1 khe hở LIÊN TỤC đủ
     * dài. Ví dụ 3 việc cố định 08–12h, 13–17h, 18–22h: tổng thời gian trống
     * còn rất nhiều, nhưng không có khe nào đủ 3 tiếng liên tục. Giờ dùng
     * {@link #timKhoangTrongDauTien} để dò khe hở thật sự.
     */
    public boolean coDuCho(Duration thoiLuong) {
        return timKhoangTrongDauTien(thoiLuong) != null;
    }

    /**
     * Tìm giờ bắt đầu của khoảng trống LIÊN TỤC đầu tiên trong ngày (00:00 -
     * 23:59) đủ dài cho {@code thoiLuong}, quét qua các item đã có theo thứ
     * tự thời gian. Trả về {@code null} nếu không có khoảng nào đủ.
     */
    public LocalTime timKhoangTrongDauTien(Duration thoiLuong) {
        LocalTime cuoiNgay = LocalTime.of(23, 59);
        List<ScheduleItem> daSapXep = new ArrayList<>(danhSachItem);
        daSapXep.sort((a, b) -> a.getGioBatDau().compareTo(b.getGioBatDau()));

        LocalTime conTro = LocalTime.MIDNIGHT;
        for (ScheduleItem item : daSapXep) {
            if (item.getGioBatDau().isAfter(conTro)) {
                long phutTrong = Duration.between(conTro, item.getGioBatDau()).toMinutes();
                if (phutTrong >= thoiLuong.toMinutes()) {
                    return conTro;
                }
            }
            if (item.getGioKetThuc().isAfter(conTro)) {
                conTro = item.getGioKetThuc();
            }
        }
        if (!conTro.isAfter(cuoiNgay)) {
            long phutConLai = Duration.between(conTro, cuoiNgay).toMinutes();
            if (phutConLai >= thoiLuong.toMinutes()) {
                return conTro;
            }
        }
        return null;
    }

    /**
     * Dùng bởi {@link LuuLich#doc} để khôi phục lại 2 bộ đếm/nhật ký không
     * gắn với riêng 1 ScheduleItem nào ({@code soDaHuy}, {@code nhatKySuCo})
     * sau khi đọc lại từ JSON — khác với luồng chạy bình thường (qua
     * {@link #huyItem} / {@link #ghiNhatKySuCo}), lúc NẠP LẠI dữ liệu cần ghi
     * thẳng state cũ vào mà không đi qua logic thao tác thông thường.
     */
    public void khoiPhucTuFile(int soDaHuyDaLuu, List<SuCoNhatKy> nhatKySuCoDaLuu) {
        this.soDaHuy = soDaHuyDaLuu;
        this.nhatKySuCo.clear();
        this.nhatKySuCo.addAll(nhatKySuCoDaLuu);
    }

    /** Trả về bản danh sách hiện tại (chỉ đọc, đã sắp theo giờ bắt đầu). */
    public List<ScheduleItem> layLichHienTai() {
        sapXepTheoGio();
        return Collections.unmodifiableList(new ArrayList<>(danhSachItem));
    }

    public LocalDate getNgay() {
        return ngay;
    }

    /**
     * Tạo 1 item MỚI (id mới, chưa hoàn thành) với cùng tên/giờ/ưu tiên như
     * {@code item}, dùng để "chuyển sang ngày mai" khi 1 việc bị đề xuất bỏ
     * hôm nay nhưng vẫn còn ý nghĩa để làm vào ngày khác — thay vì chỉ đơn
     * giản biến mất khỏi lịch (điều mà người dùng không kiểm soát được).
     * RecurringEvent không cần chuyển vì bản chất nó đã tự lặp lại mỗi ngày.
     */
    public static ScheduleItem taoBanSaoSangNgayKhac(ScheduleItem item, LocalDate ngayMoi) {
        return taoBanSaoSangNgayKhac(item, ngayMoi, item.getGioBatDau());
    }

    /**
     * FIX: overload nhận thẳng {@code gioBatDauMoi} — dùng khi đã dò được 1
     * khe hở thật sự qua {@link #timKhoangTrongDauTien} thay vì mặc định giữ
     * nguyên giờ cũ (giờ cũ có thể trùng việc khác ở ngày mới).
     */
    public static ScheduleItem taoBanSaoSangNgayKhac(ScheduleItem item, LocalDate ngayMoi, LocalTime gioBatDauMoi) {
        Duration thoiLuong = item.layThoiLuong();
        LocalTime gioKetThucMoi = gioBatDauMoi.plus(thoiLuong);
        if (item instanceof FixedEvent) {
            return new FixedEvent(item.getTen(), gioBatDauMoi, gioKetThucMoi, item.getDoUuTien());
        }
        if (item instanceof DeadlineTask dt) {
            return new DeadlineTask(item.getTen(), gioBatDauMoi, gioKetThucMoi,
                item.getDoUuTien(), dt.getHanChot(), ngayMoi);
        }
        if (item instanceof RecurringEvent re) {
            return new RecurringEvent(item.getTen(), gioBatDauMoi, gioKetThucMoi,
                item.getDoUuTien(), re.getSoNgayLienTiep());
        }
        if (item instanceof FlexibleTask) {
            return new FlexibleTask(item.getTen(), gioBatDauMoi, gioKetThucMoi, item.getDoUuTien());
        }
        throw new IllegalStateException("Loại ScheduleItem chưa được hỗ trợ: " + item.getClass());
    }

    /**
     * Tạo 1 bản sao ĐỘC LẬP hoàn toàn (deep copy) — dùng để lưu trạng thái
     * "trước" trước khi ReplanEngine chỉnh sửa lịch "sau", nhờ vậy bảng so
     * sánh trước/sau không bị ảnh hưởng chéo do object bị mutate.
     */
    public DaySchedule saoChep() {
        DaySchedule ban = new DaySchedule(this.ngay);
        for (ScheduleItem item : this.danhSachItem) {
            ban.danhSachItem.add(saoChepItem(item));
        }

        for (ScheduleItem item : this.danhSachChuaXepLich) {
            ban.danhSachChuaXepLich.add(saoChepItem(item));
        }

        ban.soDaHuy = this.soDaHuy;
        ban.nhatKySuCo.addAll(this.nhatKySuCo);

        return ban;
    }

    /**
     * QUAN TRỌNG: phải giữ nguyên {@code item.getId()} khi sao chép — nếu
     * không, {@code ReplanEngine}/{@code SkipDisruption} thao tác trên bản
     * "sau" sẽ không bao giờ đối chiếu được với item người dùng chọn trên UI
     * (vốn tham chiếu tới bản gốc hoặc 1 bản sao khác), khiến "Bỏ việc" trông
     * như không có tác dụng gì (trước/sau giống hệt nhau).
     */
    private static ScheduleItem saoChepItem(ScheduleItem item) {
        if (item instanceof FixedEvent) {
            return new FixedEvent(item.getId(), item.getTen(), item.getGioBatDau(), item.getGioKetThuc(),
                item.getDoUuTien(), item.isHoanThanh());
        }
        if (item instanceof DeadlineTask dt) {
            return new DeadlineTask(item.getId(), item.getTen(), item.getGioBatDau(), item.getGioKetThuc(),
                item.getDoUuTien(), item.isHoanThanh(), dt.getHanChot(), dt.getNgayThucHien(), dt.isCanhBaoTreHan());
        }
        if (item instanceof RecurringEvent re) {
            return new RecurringEvent(item.getId(), item.getTen(), item.getGioBatDau(), item.getGioKetThuc(),
                item.getDoUuTien(), item.isHoanThanh(), re.getSoNgayLienTiep(), re.isBoQuaHomNay());
        }
        if (item instanceof FlexibleTask ft) {
            return new FlexibleTask(item.getId(), item.getTen(), item.getGioBatDau(), item.getGioKetThuc(),
                item.getDoUuTien(), item.isHoanThanh(), ft.getSoLanDaDoi());
        }
        throw new IllegalStateException("Loại ScheduleItem chưa được hỗ trợ sao chép: " + item.getClass());
    }
    /**
     * Trả về danh sách công việc chưa được xếp lịch.
     */
    public List<ScheduleItem> getChuaXepLich() {
        return Collections.unmodifiableList(
                new ArrayList<>(danhSachChuaXepLich)
        );
    }

    /**
     * Thêm một công việc vào danh sách chưa xếp lịch.
     */
    public void themChuaXepLich(ScheduleItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Không thể thêm item null vào danh sách chưa xếp lịch."
            );
        }

        for (ScheduleItem cu : danhSachChuaXepLich) {
            if (cu.getId().equals(item.getId())) {
                throw new IllegalArgumentException(
                        "Trùng id trong danh sách chưa xếp lịch: " + item.getId()
                );
            }
        }

        danhSachChuaXepLich.add(item);
    }
}
