# 02_MethodContract – đổi signature phải sửa file này TRƯỚC khi sửa code, rồi báo nhóm
Dòng (MỚI) = chưa có trong code, cả nhóm đã đồng ý ở tin nhắn chốt.

| Method | Chủ | Input → Output | Exception / ghi chú |
|---|---|---|---|
| ScheduleItem.coTheDoiLich() | Trân | — → boolean | false với FixedEvent; (đề xuất) false khi đã hoàn thành |
| ScheduleItem.xuLyKhiBiTre(int) | Trân | số phút → void | IllegalStateException (Fixed, đã hoàn thành, Recurring đã bỏ qua); IllegalArgumentException nếu dời vượt 23:59 (INV-03) |
| ScheduleItem.vuotNuaDemNeuDoi(int) (MỚI) | Trân | số phút → boolean | true nếu giờ kết thúc + số phút > 23:59; không đổi trạng thái |
| DelayDisruption.apDung(List) + getViecVuotNgay() (MỚI) | Trân | → void; → List<ScheduleItem> | Việc vượt 23:59 không dời mà ghi vào danh sách; Strategy đọc để xử lý "hết chỗ vì vượt ngày" (BR-06) |
| DaySchedule.themItem(ScheduleItem) | Manh | item → void | IllegalArgumentException: null, trùng id, Fixed trùng Fixed |
| DaySchedule.timKhoangTrongDauTien(Duration) và (Duration, LocalTime tuGio) (MỚI) | Manh | → LocalTime | null nếu không có khe LIÊN TỤC đủ dài; ngày mai dùng tuGio = 06:00 |
| DaySchedule.saoChep() | Manh | → DaySchedule | Deep copy, giữ nguyên id |
| ReplanEngine.taoCacPhuongAn(DaySchedule, Disruption, LocalTime) | Trúc | → List<PhuongAn> (đủ 3) | IAE nếu null; tính trên bản sao |
| ReplanApplyService.apDung(DaySchedule homNay, DaySchedule ngayMai, PhuongAn) (MỚI) | Trúc | → KetQuaApDung (chuyenNgayMai, chuaXepLich) | Chỉ phương án được chọn đổi lịch; không chuyển được → Chưa xếp lịch, không xóa (BR-02 tạm); kết quả đúng như xem trước (BR-04) |
| ReplanController.taoPhuongAn(Disruption, LocalTime) / apDungPhuongAn(PhuongAn) (MỚI) | Tú | → List<PhuongAn>; → KetQuaApDung | Chỉ chuyển tiếp, không chứa business rule |
| ScheduleController: themViec / suaViec / xoaViec / hoanThanh (MỚI) | Tú | dữ liệu đã parse → void | Input validation ở đây; business validation do Domain ném |
| KhoDuLieu.luuNgay(DaySchedule) / docNeuCo(LocalDate) | Manh | → void / → DaySchedule hoặc null | IOException; DuLieuHongException khi file hỏng (không ghi đè) |
| BoTheoDoiLich.quet(DaySchedule, LocalTime, Set<String> idBoQua) (MỚI – Ngân đã code) | Ngân | → List<CanhBaoViec> | Bỏ qua việc có id trong idBoQua; chỉ đọc, không tự Replan |
