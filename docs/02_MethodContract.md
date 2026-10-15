# 02_MethodContract – đổi signature phải sửa file này TRƯỚC khi sửa code, rồi báo nhóm
Dòng (MỚI) = chưa có trong code, cả nhóm đã đồng ý ở tin nhắn chốt.
Quy ước lỗi chung: **IllegalArgumentException** cho đối số sai; **IllegalStateException** cho trạng thái không cho phép. Controller/UI không chứa business rule.

| Method | Chủ | Input → Output | Exception / ghi chú |
|---|---|---|---|
| ScheduleItem.coTheDoiLich() | Trân | — → boolean | false với FixedEvent; RecurringEvent false khi đã bỏ qua hôm nay (INV-19). Không xét hoàn thành: nơi gọi tự kiểm tra isHoanThanh() (INV-18) |
| ScheduleItem.xuLyKhiBiTre(int) | Trân | số phút → void | IllegalStateException (Fixed; đã hoàn thành – INV-18; Recurring đã bỏ qua – INV-19); IllegalArgumentException nếu dời vượt 23:59 (INV-03) |
| ScheduleItem.vuotNuaDemNeuDoi(int) (MỚI) | Trân | số phút → boolean | true nếu giờ kết thúc + số phút > 23:59; không đổi trạng thái |
| ScheduleItem.doiDuocTrongNgay(int) (có sẵn từ K-14) | Trân | số phút → boolean | true nếu dời xong vẫn nằm trong 00:00–23:59, không cuộn vòng; không đổi trạng thái. Với số phút ≥ 0 bằng !vuotNuaDemNeuDoi |
| ScheduleItem.ganLaiKhungGioBoiEngine(LocalTime, LocalTime) | Trân | giờ mới → void | IllegalStateException: đã hoàn thành (INV-18), FixedEvent (INV-20); IllegalArgumentException: bắt đầu không trước kết thúc; (DR-01b) khác thời lượng cũ – INV-08 |
| FixedEvent.setDoUuTien(PriorityLevel) | Trân | mức → void | IllegalArgumentException nếu khác BAT_BUOC; constructor luôn ép BAT_BUOC (INV-17) |
| DelayDisruption(int) | Trân | số phút → đối tượng | IllegalArgumentException nếu ≤ 0 hoặc > 1440 (INV-16) |
| DelayDisruption.apDung(List) + getViecVuotNgay() (MỚI) | Trân | → void; → List<ScheduleItem> (chỉ đọc) | Bỏ qua việc đã hoàn thành, việc không dời được; việc vượt 23:59 không dời mà ghi vào danh sách (xóa ở mỗi lần gọi). Lưu ý K-14: engine hiện không gọi apDung để dời hàng loạt, nên đây là API mức item (BR-06) |
| SkipDisruption(ScheduleItem) | Trân | item → đối tượng | null → hiện ném NPE; K-09 (chưa làm) sẽ đổi thành IllegalArgumentException |
| ScheduleItem (constructor các lớp con: FlexibleTask, FixedEvent, DeadlineTask, RecurringEvent) | Trân | tên, giờ bắt đầu, giờ kết thúc, độ ưu tiên (+ tham số riêng) → đối tượng | IllegalArgumentException: tên null/rỗng, giờ null, giờ bắt đầu không trước giờ kết thúc (INV-01/02). Độ ưu tiên null hiện ném NPE (K-09 chưa làm) |
| ScheduleItem copy constructor (id, ..., hoanThanh) | Trân | giữ nguyên id + trạng thái → đối tượng | Dùng cho DaySchedule.saoChep(); giữ id để đối chiếu cùng một việc |
| ScheduleItem.danhDauHoanThanh() / boDanhDauHoanThanh() / isHoanThanh() | Trân | — → void / boolean | Đã hoàn thành thì không bị dời hay đặt lại giờ (INV-18) |
| ScheduleItem.layThoiLuong() / trung(ScheduleItem) | Trân | → Duration; item → boolean | Chỉ đọc. trung(): nối tiếp (kết thúc = bắt đầu) không tính là trùng |
| DeadlineTask.xuLyKhiBiTre(int) | Trân | số phút → void | Dời nếu giờ kết thúc mới không quá hạn chót; quá hạn thì KHÔNG dời, bật canhBaoTreHan |
| DeadlineTask.dongBoCanhBaoTheoKhungGioHienTai() | Trân | — → void | Tính lại cờ cảnh báo theo giờ kết thúc hiện tại; gọi sau khi engine ép đổi giờ |
| DeadlineTask constructor | Trân | + hạn chót (LocalDateTime), ngày thực hiện → đối tượng | IllegalArgumentException: hạn chót null; (DR-01b) hạn chót trước giờ kết thúc – INV-06, ngày thực hiện null – INV-21 |
| RecurringEvent.boQuaHomNay() / isBoQuaHomNay() | Trân | — → void / boolean | Bỏ qua hôm nay, reset streak về 0; không xóa việc khỏi lịch |
| FlexibleTask.xuLyKhiBiTre(int) / getSoLanDaDoi() | Trân | số phút → void; → int | Dời giữ nguyên thời lượng, tăng số lần đã dời; không nén thời lượng |
| SkipDisruption.apDung(List) | Trân | → void | RecurringEvent: chỉ bỏ qua hôm nay, không xóa. Loại khác: xóa khỏi danh sách còn lại |
| DaySchedule.themItem(ScheduleItem) | Manh | item → void | IllegalArgumentException: null, trùng id, Fixed trùng Fixed |
| DaySchedule.timKhoangTrongDauTien(Duration) và (Duration, LocalTime tuGio) (MỚI) | Manh | → LocalTime | null nếu không có khe LIÊN TỤC đủ dài; ngày mai dùng tuGio = 06:00 |
| DaySchedule.saoChep() | Manh | → DaySchedule | Deep copy, giữ nguyên id |
| ReplanEngine.taoCacPhuongAn(DaySchedule, Disruption, LocalTime) | Trúc | → List<PhuongAn> (đủ 3) | IAE nếu null; tính trên bản sao |
| ReplanApplyService.apDung(DaySchedule homNay, DaySchedule ngayMai, PhuongAn) (MỚI) | Trúc | → KetQuaApDung (chuyenNgayMai, chuaXepLich) | Chỉ phương án được chọn đổi lịch; không chuyển được → Chưa xếp lịch, không xóa (BR-02 tạm); kết quả đúng như xem trước (BR-04) |
| ReplanController.taoPhuongAn(Disruption, LocalTime) / apDungPhuongAn(PhuongAn) (MỚI) | Tú | → List<PhuongAn>; → KetQuaApDung | Chỉ chuyển tiếp, không chứa business rule |
| ScheduleController: themViec / suaViec / xoaViec / hoanThanh (MỚI) | Tú | dữ liệu đã parse → void | Input validation ở đây; business validation do Domain ném |
| KhoDuLieu.luuNgay(DaySchedule) / docNeuCo(LocalDate) | Manh | → void / → DaySchedule hoặc null | IOException; DuLieuHongException khi file hỏng (không ghi đè) |
| BoTheoDoiLich.quet(DaySchedule, LocalTime, Set<String> idBoQua) (MỚI – Ngân đã code) | Ngân | → List<CanhBaoViec> | Bỏ qua việc có id trong idBoQua; chỉ đọc, không tự Replan |

## Việc Domain còn mở (chưa có trong code)
- K-09: SkipDisruption(null) và độ ưu tiên null đổi từ NPE sang IllegalArgumentException (không đổi chữ ký).
- INV-14, INV-15: chưa làm.
- T-21: chờ Trúc.

## Dòng của người khác – chủ file tự xác nhận khớp code
Các dòng (MỚI) của Manh, Trúc, Tú, Ngân là kế hoạch; ở main hiện chưa có ReplanApplyService, ReplanController, ScheduleController và overload timKhoangTrongDauTien(Duration, LocalTime). Hỏi từng người khi họ code xong.