# Oops! My Schedule

Đây là đồ án thuộc khuôn khổ môn  **Lập trình ứng dụng**. Tại đây, nhóm xây dựng ứng dụng lịch trình cá nhân (Java Swing) **tự tái sắp xếp khi phát sinh sự cố**. Người dùng sẽ tạo thời gian biểu trong ngày. Khi bị trễ, không làm được một việc hoặc có việc chen ngang, người dùng báo sự cố; hệ thống tính lại phần còn lại và đưa **ba phương án** (Ít thay đổi, Ưu tiên deadline, Giảm tải) để người dùng so sánh và chọn.

Việc tính lịch dựa trên luật (rule-based), **không dùng AI lúc chạy**, nên mọi đề xuất đều giải thích được.

## 1. Cách nhóm thực hiện

Nhóm **thống nhất yêu cầu và quy tắc trước, rồi mới viết code**, theo ba bước:

1. **Phân tích và chốt quy tắc** (thư mục `docs/`): 10 yêu cầu chức năng (FR), 10 quy tắc nghiệp vụ (BR), 23 ràng buộc dữ liệu (INV) và 15 công thức tính toán (CT).
2. **Thống nhất hợp đồng method** (`docs/02_MethodContract.md`): tên, chữ ký và ngoại lệ của từng method chính; kiến trúc bốn tầng; mỗi tệp có một người phụ trách.
3. **Lập trình, kiểm thử, review**: mỗi người code theo hợp đồng trên nhánh riêng, có JUnit và pull request được review. Code do AI gợi ý cũng phải theo hợp đồng và qua test như code thường. Prompt được ghi trong `docs/06_AI_Log/`.

## 2. Ý tưởng cốt lõi

**Ba luật chính**

1. `FixedEvent` (giờ học, họp) không bao giờ bị dời.
2. Không nén thời lượng: việc 60 phút chỉ được đặt vào khe trống liên tục đủ 60 phút (BR-01).
3. Không tự xóa việc: hệ thống chỉ đề xuất, người dùng quyết định (BR-02, BR-03).

| Loại việc | Khi bị trễ (đa hình `xuLyKhiBiTre`) |
|---|---|
| `FixedEvent` | Không dời; gọi trực tiếp thì ném `IllegalStateException` |
| `FlexibleTask` | Dời giờ bắt đầu và kết thúc, tăng `soLanDaDoi` |
| `DeadlineTask` | Dời nếu còn kịp hạn chót; không kịp thì không dời và bật `canhBaoTreHan` |
| `RecurringEvent` | Dời bình thường; hết chỗ thì bỏ qua hôm nay, reset streak |

**Luồng "Báo sự cố"**: nhập sự cố → ghi nhận (`DelayDisruption` hoặc `SkipDisruption`) → `ReplanEngine` chạy ba `ReplanStrategy` trên bản sao lịch → hiện ba thẻ và bảng so sánh → người dùng chọn một phương án → `ReplanApplyService` áp dụng (đóng hộp thoại thì lịch không đổi). Việc bị đề xuất bỏ được thử chuyển sang ngày mai (từ 06:00); nếu ngày mai kín thì đưa vào danh sách `chuaXepLich` và báo cho người dùng, không mất việc.

## 3. Cài đặt và chạy

**Yêu cầu**: JDK 17 trở lên, IntelliJ IDEA (bản Community đủ dùng). Mã chính chỉ dùng JDK (Swing, `java.time`, JSON tự viết).

1. IntelliJ → `File > Open` → chọn thư mục chứa `pom.xml`, đợi Maven nạp xong.
2. Nếu chưa nhận JDK: `File > Project Structure > SDK` → chọn JDK 17 hoặc 21.
3. Chạy ứng dụng: mở `src/main/java/com/oopsmyschedule/Main.java` → bấm ▶.
4. Chạy toàn bộ JUnit: chuột phải `src/test/java` → *Run 'All Tests'*, hoặc `mvn test`. Kết quả hiện tại: **134 test, 132 PASS, 0 FAIL, 2 ignored** (hai test K-14 được tạm hoãn, ghi trong mục hạn chế của báo cáo).
5. Chạy bộ kiểm thử cũ: mở `src/test/java/com/oopsmyschedule/KiemThu.java` → ▶. Kết quả đúng: **33 PASS, 0 FAIL**.

**Demo Mode**: đồng hồ có chế độ giả lập, bấm +30/+60/+90 phút để tua nhanh khi thuyết trình.

**Dữ liệu** tự động lưu, mỗi ngày một tệp `lich_<ngày>.json`: Windows lưu ở `%APPDATA%\OhMySchedule\`, nơi khác lưu ở `~/.oopsmyschedule/`. Chưa có tệp thì tạo lịch mới; tệp hỏng thì báo `DuLieuHongException` và không ghi đè (BR-10). Muốn demo từ đầu, xóa thư mục dữ liệu.

## 4. Cấu trúc project

```
src/main/java/com/oopsmyschedule/
├── domain/      ScheduleItem (abstract) + FixedEvent, FlexibleTask, DeadlineTask, RecurringEvent
│                Disruption (abstract) + DelayDisruption, SkipDisruption; PriorityLevel (enum)
├── service/     DaySchedule, ReplanEngine, ReplanStrategy (interface), AbstractReplanStrategy
│                + 3 Strategy, PhuongAn, KetQuaSoSanh, ReplanApplyService, KetQuaApDung,
│                LuuLich + KhoDuLieu (JSON), BoTheoDoiLich, ReminderService, TongKetNgay, DongHo ...
├── controller/  ScheduleController, ReplanController
├── ui/          MainFrame, các Dialog, TrangChuPanel, TimelinePanel, Mascot, Toast ...
└── Main.java
src/test/java/com/oopsmyschedule/   16 tệp JUnit 5 (domain, service, controller) + KiemThu.java
docs/                               tài liệu của nhóm (mục 6)
```

**Kiến trúc bốn tầng**: `UI → Controller → Service → Domain`. UI và Controller chỉ nhận input và hiển thị; luật nghiệp vụ nằm ở Domain và Service, nên Domain vẫn chặn dữ liệu sai dù UI bỏ sót.

## 5. Áp dụng OOP

- **Trừu tượng**: `ScheduleItem.coTheDoiLich()`, `xuLyKhiBiTre()`, `moTaTrangThai()` và `Disruption.apDung()` là abstract; `ReplanStrategy` là interface.
- **Kế thừa**: `ScheduleItem` có 4 lớp con; `Disruption` có 2 lớp con; `AbstractReplanStrategy` có 3 chiến lược.
- **Đa hình ba trục**: `xuLyKhiBiTre` theo loại việc; `Disruption.apDung` (Delay/Skip); `ReplanStrategy.apDung` (cùng sự cố, ba kết quả khác nhau).
- **Strategy + Template Method**: `AbstractReplanStrategy` giữ khung chung (con trỏ thời gian, tách việc đã qua/còn lại, né `FixedEvent`); mỗi chiến lược chỉ khác ở `coTheHySinh(item)`.
- **Đóng gói, Tell-Don't-Ask**: field `private`; `DaySchedule.layLichHienTai()` trả danh sách không sửa được; `ReplanEngine` gửi message cho item thay vì hỏi loại việc.
- **Guard clause và Invariant**: constructor chặn tên rỗng, giờ sai, null; `IllegalArgumentException` cho đối số sai, `IllegalStateException` cho trạng thái không cho phép.

## 6. Tài liệu phục vụ báo cáo và code (`docs/`)

| Tệp | Nội dung |
|---|---|
| `01_Requirements.md` | FR-01 đến FR-10, Use Case |
| `02_MethodContract.md` | Hợp đồng method (đổi chữ ký thì sửa tệp này trước) |
| `03_BusinessRules.md` | BR-01 đến BR-10, ma trận BR-05, INV-01 đến INV-23, CT-01 đến CT-15 |
| `04_UML/` | Nguồn `.drawio` và ảnh `.png` các sơ đồ |
| `05_TestCases.md` | Ma trận kiểm thử và nhật ký lỗi |
| `E2E_checklist.md` | Kịch bản kiểm thử end-to-end và kết quả |
| `06_AI_Log/` | Nhật ký prompt AI của từng thành viên |
| `report/` | Khung báo cáo |

## 7. Phân chia công việc nhóm 

| Mã | Thành viên | Phụ trách chính |
|---|---|---|
| NG | Ngân | Repo, `pom.xml`, README; kiểm thử và E2E; `BoTheoDoiLich`, `ReminderService`, `TongKetNgay`; ghép báo cáo |
| DR | Trân | `domain/*`; Method Contract; sơ đồ lớp tổng |
| MA | Manh | `DaySchedule`, `LuuLich`, `KhoDuLieu`, `chuaXepLich`; ERD |
| TR | Trúc | `ReplanEngine`, các Strategy, `ReplanApplyService`; sơ đồ tuần tự và lưu đồ Replan |
| TU | Tú | `controller/*`, `ui/*`; ảnh chụp màn hình, slide |

Quy tắc làm việc: mỗi nhánh có một owner; không push thẳng `main`; commit nhỏ kèm mã task (ví dụ `[DR-01a] ...`); pull request cần ít nhất một review và ghi dòng `AI: có (mã) / không`; sau mỗi lần merge, `main` phải chạy test xanh. Merge vào `main` theo các mốc 09/10, 10/10 và đóng băng 12/10.

## 8. Hạn chế và hướng phát triển

- Danh sách `chuaXepLich` đã được lưu và báo cho người dùng, nhưng chưa có màn hình để xếp lại từng việc.
- Hai test K-14 (việc lỡ giờ, dời dây chuyền) đang `@Disabled`, chờ chốt hành vi.
- "Không làm được một việc" chưa tính vào `soDaHuy` của Tổng kết ngày.
- Chỉ tái sắp xếp trong ngày hiện tại; chưa có Undo và chưa chọn nhiều việc cùng lúc.
- JSON viết tay bằng regex; có thể nâng lên Gson hoặc Jackson.
