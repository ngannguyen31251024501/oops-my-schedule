# Oops! My Schedule 😭

Ứng dụng lịch trình cá nhân (Java Swing) **tự tái sắp xếp khi phát sinh sự cố**. Bạn lập lịch buổi sáng, ngủ quên 90 phút, lịch vỡ: chỉ cần báo "tôi trễ X phút" hoặc "tôi không làm được việc này", hệ thống tính lại phần còn lại và đưa **3 phương án** để bạn so sánh, chọn.

Đây là đồ án môn **Lập trình ứng dụng** của sinh viên. Toàn bộ quá trình tính lịch được tính toán theo luật (rule-based), **không dùng AI lúc chạy** để kết quả giải thích được với người dùng.

> Tài liệu gốc của nhóm: *GĐ1 – Task 1: Phân tích yêu cầu – Bản thống nhất (v2)*, *Tài liệu thiết kế*, *Kế hoạch phân công chi tiết 7 ngày*. README này dùng đúng mã FR / BR / INV / CT / K / Q / T trong Task 1.

---

## 1. Ý tưởng cốt lõi

**Ba luật chính của app**

1. `FixedEvent` (giờ học, họp, xe bus) **không bao giờ bị dời**.
2. **Không nén thời lượng**: việc 60 phút vẫn phải là 60 phút, chỉ được đặt vào khe trống **liên tục** đủ dài (BR-01).
3. **Không tự xóa việc**: hệ thống chỉ đề xuất, người dùng quyết định (BR-02, BR-03).

**Bốn loại việc × ba mức ưu tiên** (`BAT_BUOC` / `NEN_LAM` / `CO_THE_BO` chỉ quyết định việc nào được *đề xuất bỏ* khi hết chỗ, xem ma trận BR-05).

| Loại việc | Khi bị trễ (đa hình `xuLyKhiBiTre`) |
|---|---|
| `FixedEvent` | Không dời; gọi trực tiếp → `IllegalStateException` |
| `FlexibleTask` | Dời cả giờ bắt đầu/kết thúc, tăng `soLanDaDoi` |
| `DeadlineTask` | Dời nếu còn kịp hạn chót; không kịp thì **không dời**, bật `canhBaoTreHan` |
| `RecurringEvent` | Dời bình thường; hết chỗ thì bỏ qua hôm nay, reset streak |

**Luồng "Báo sự cố"**: nhập sự cố → ghi nhận (`DelayDisruption` / `SkipDisruption`) → `ReplanEngine` chạy 3 `ReplanStrategy` (**Ít thay đổi / Ưu tiên deadline / Giảm tải**) trên **bản sao** lịch → hiện 3 thẻ + so sánh trước/sau → người dùng chọn 1 phương án → áp dụng (đóng hộp thoại = lịch không đổi). Việc bị đề xuất bỏ được thử chuyển sang ngày mai; nếu ngày mai cũng kín thì vào danh sách **"Chưa xếp lịch"** (không bao giờ mất việc âm thầm).

---

## 2. Trạng thái hiện tại và phạm vi công việc hiện tại

**Code demo (`oopsmyschedule_da_sua_15`)**: biên dịch được; `KiemThu.java` chạy **33 PASS, 0 FAIL**. Chưa có package `controller/`; `MainFrame.java` (756 dòng) còn chứa logic "áp dụng phương án + chuyển ngày mai" (khoảng dòng 578–630) và nút "Bỏ qua" (dòng 511–512) cần tách.

**Phạm vi (theo Task 1 và Kế hoạch Mục 0.1)**

| Nhóm | Nội dung |
|---|---|
| **Làm ngay (P0)** | Chặn dời qua 23:59 (INV-03, K-01/02) · tách `ReplanApplyService` + `controller/` khỏi `MainFrame` · guard trùng id (INV-05) và `hanChot ≥ giờ kết thúc` (INV-06) · việc không chuyển được sang ngày mai → vào **Chưa xếp lịch** (`chuaXepLich`, lưu JSON) · nối UI với 3 phương án thật, kiểm Save → Load |
| **Nếu còn giờ (P1)** | Việc đã hoàn thành không bị dời (K-03) · nút "Bỏ qua" không đánh dấu hoàn thành (K-04) · NPE → `IllegalArgumentException` · công thức Giảm tải (K-10) · phút trễ 1–1440 (K-11) · chuyển test cũ sang JUnit |
| **Hoãn (ghi vào báo cáo, mục "hạn chế và hướng phát triển")** | Xử lý đầy đủ danh sách "Chưa xếp lịch" · dòng ước lượng ở bảng so sánh · chọn nhiều việc / bỏ chọn · Undo · Replan nhiều ngày · INV-14…17, 19…23 · mở rộng Reminder / Tổng kết / Mascot |

**Không làm** (đã chốt ở Task 1, mục 9): Replan khi thêm/sửa/xóa việc, Replan nhiều ngày, Undo, chọn nhiều việc, xác nhận 2 bước khi áp dụng, đăng nhập/Android, What-if Slider / Quick Template / Time Capacity Bar.

### Lỗi đã biết (K-xx) và nơi sửa

| Lỗi | Mô tả ngắn | Mức | Người sửa |
|---|---|---|---|
| K-01, K-02 | Dời qua nửa đêm ra giờ sai; engine kéo việc lùi về trước | P0 | Trân (Domain), Trúc (Strategy) |
| K-05 | `DeadlineTask` tạo được với hạn chót trước giờ kết thúc | P0 | Trân |
| K-06 | Thêm cùng một id hai lần vào `DaySchedule` | P0 | Manh |
| K-07 | Việc bị đề xuất bỏ mà ngày mai kín thì mất; logic nằm trong `MainFrame` | P0 | Trúc (Service), Tú (nối UI) |
| K-08 | Dò khe ngày mai từ 00:00 (đổi sang 06:00) | P0, làm cùng K-07 | Manh, Trúc |
| K-03 | Replan vẫn dời việc đã hoàn thành | P1 | Trân, Trúc |
| K-04 | Nút "Bỏ qua" đánh dấu hoàn thành | P1 | Ngân, Tú |
| K-09 | `NullPointerException` thay vì `IllegalArgumentException` | P1, cắt đầu tiên nếu trễ | Trân |
| K-10 | "Giảm tải" tính nhu cầu thời gian đếm đôi | P1 | Trúc |
| K-11 | Banner trễ > 600 phút làm dialog báo sự cố văng lỗi | P1 | Trân (Domain), Tú (UI) |
| K-12 | "Không làm được" không tăng `soDaHuy` | P2, hoãn nếu trễ | Trúc, Manh |

---

## 3. Cài đặt và chạy

**Yêu cầu**: JDK 17 trở lên, IntelliJ IDEA (Community đủ dùng). Code chính không cần thư viện ngoài (chỉ JDK: Swing, `java.time`, JSON tự viết).

1. IntelliJ → `File > Open` → chọn **thư mục repo** (thư mục chứa `pom.xml`). Đợi Maven nạp xong.
2. Nếu chưa nhận JDK: `File > Project Structure > SDK` → chọn JDK 17/21.
3. Chạy app: mở `src/main/java/com/oopsmyschedule/Main.java` → bấm ▶ cạnh `main`.
4. Chạy test cũ: mở `src/test/java/com/oopsmyschedule/KiemThu.java` → ▶. Kết quả đúng phải là **`33 PASS, 0 FAIL`**.
5. Chạy JUnit: chuột phải `src/test/java` → *Run 'All Tests'*, hoặc Maven tool window → *Lifecycle → test* (lệnh `mvn test`). Sau khi sửa `pom.xml`, bấm *Load Maven Changes* để IntelliJ nạp JUnit.

**Demo Mode**: đồng hồ có chế độ giả lập, bấm ⏩ +30/+60/+90 phút để tua nhanh khi thuyết trình, không phải chờ giờ thật.

**Dữ liệu** được tự động lưu, mỗi ngày một file `lich_<ngày>.json`:
- Windows: `%APPDATA%\OhMySchedule\`
- Nơi khác: `~/.oopsmyschedule/`

File chưa có thì tạo lịch mới; file hỏng thì báo `DuLieuHongException` và **không ghi đè** (BR-10). Muốn demo từ đầu, xóa thư mục dữ liệu trên.

---

## 4. Cấu trúc project

```
src/main/java/com/oopsmyschedule/
├── domain/      ScheduleItem (abstract) + FixedEvent, FlexibleTask, DeadlineTask, RecurringEvent
│                Disruption (abstract) + DelayDisruption, SkipDisruption, PriorityLevel (enum)
├── service/     DaySchedule, ReplanEngine, ReplanStrategy (interface), AbstractReplanStrategy
│                + MinimizeDisruption / DeadlineFirst / ReduceWorkload Strategy, PhuongAn, KetQuaSoSanh,
│                LuuLich + KhoDuLieu (JSON), LichMauTuan, SuCoNhatKy, DongHo,
│                BoTheoDoiLich, ReminderService, TongKetNgay, TrungTamThongBao ...
├── ui/          MainFrame, TrangChuPanel, TimelinePanel, các Dialog, Mascot, Toast, VintageTheme ...
├── controller/  (MỚI, đang tách khỏi MainFrame trong tuần này)
└── Main.java
src/test/java/com/oopsmyschedule/   KiemThu.java (33 case, giữ nguyên) + JUnit của từng module
docs/                               tài liệu nhóm (mục 6)
```

**Kiến trúc 4 tầng**: `UI → Controller → Service → Domain`. UI/Controller chỉ nhận input và hiển thị (input validation); **business rule nằm ở Domain/Service** (Domain vẫn chặn dữ liệu sai dù UI lỡ cho qua).

**Quy ước: không di chuyển file giữa các package** (tránh vỡ import và conflict). Tuần này chỉ thêm `controller/` và vài class mới trong `service/` (`ReplanApplyService`, `KetQuaApDung`).

---

## 5. OOP

- **Trừu tượng**: `ScheduleItem.coTheDoiLich()/xuLyKhiBiTre()/moTaTrangThai()` và `Disruption.apDung()` là abstract; `ReplanStrategy` là interface thuần.
- **Kế thừa**: `ScheduleItem` → 4 lớp con; `Disruption` → 2 lớp con; `AbstractReplanStrategy` → 3 chiến lược.
- **Đa hình 3 trục**: (1) `xuLyKhiBiTre` theo loại việc; (2) `Disruption.apDung` (Delay/Skip); (3) `ReplanStrategy.apDung` (cùng sự cố, 3 kết quả khác nhau, kiểm bằng VAL-02).
- **Strategy + Template Method**: `AbstractReplanStrategy` giữ khung chung (quét bằng con trỏ thời gian, tách việc đã qua/còn lại, né `FixedEvent`); mỗi chiến lược chỉ khác ở `coTheHySinh(item)` (và `tienXuLyTruocKhiQuet` với Giảm tải).
- **Đóng gói / Tell-Don't-Ask**: field `private`; `DaySchedule.layLichHienTai()` trả danh sách không sửa được; `ReplanEngine` không hỏi loại việc, chỉ gửi message cho item.
- **Guard clause và Invariant**: constructor chặn tên rỗng, giờ sai, null; `IllegalArgumentException` cho đối số sai, `IllegalStateException` cho trạng thái không cho phép; dùng enum thay String.

---

## 6. Tài liệu (`docs/`)

| File | Nội dung | Lấy từ Task 1 |
|---|---|---|
| `01_Requirements.md` | FR-01…FR-10, Use Case, bảng loại/gộp | mục 4 |
| `02_MethodContract.md` | Chữ ký method mới và thay đổi (đổi signature thì **sửa file này trước**) | Kế hoạch Mục 6 |
| `03_BusinessRules.md` | BR-01…BR-10, ma trận BR-05, INV-01…23, công thức CT-01…15 | mục 5, 5.2, 6 |
| `04_UML/` | `src/*.drawio` (nguồn) và `*.png` (xuất cho báo cáo): D1 Use Case, D2 Class tổng, D3 ERD, D4 Sequence Điều chỉnh lịch, D7 Flowchart Replan | Kế hoạch Mục 4 |
| `05_TestCases.md` | Test Matrix T-01…T-24 (happy / biên / sai) và danh sách bug | mục 8 |
| `06_AI_Log/` | Nhật ký prompt AI của từng người (`NG.md`, `DR.md`, `MA.md`, `TR.md`, `TU.md`) | Kế hoạch Mục 11 |

Quy tắc tạm đã chốt: khi áp dụng phương án mà việc bị đề xuất bỏ **không chuyển được sang ngày mai** (không có khe liên tục đủ dài từ 06:00), việc đó được đưa vào **"Chưa xếp lịch"** của người dùng kèm Toast và ghi Trung tâm thông báo (ghi vào `03_BusinessRules.md` là *BR-02 (tạm)*).

---

## 7. Làm việc nhóm

**Thành viên và phần phụ trách**

| Mã | Người | Phụ trách chính |
|---|---|---|
| NG | Ngân | Repo, `pom.xml`, `.gitignore`, `README.md`; test tổng, Test Matrix, E2E; `BoTheoDoiLich`, `ReminderService`, `TongKetNgay`, `TrungTamThongBao`, `DongHo`; ghép báo cáo và gói nộp |
| DR | Trân | `domain/*`; `docs/02_MethodContract.md`; ghép Class Diagram tổng; build sạch cuối |
| MA | Manh | `DaySchedule`, `LuuLich`, `KhoDuLieu`, `LichMauTuan`, `SuCoNhatKy` (lưu/đọc, `chuaXepLich`); ERD |
| TR | Trúc | `ReplanEngine`, các `*Strategy`, `PhuongAn`, `KetQuaSoSanh`, `ReplanApplyService`, `KetQuaApDung`; Sequence và Flowchart Replan |
| TU | Tú | `controller/*`, `ui/*` (chỉ Tú sửa `MainFrame.java`); screenshot, slide |

**Mỗi file một chủ** (tránh conflict): chỉ sửa file thuộc phần mình; cần đổi file người khác thì nhắn chủ file hoặc gửi đoạn code qua chat. `src/test/`: mỗi người test của module mình, **không sửa `KiemThu.java`** (Ngân giữ), thêm file test mới (vd. `domain/ScheduleItemTest.java`).

**Quy trình git**

- Nhánh: `main` (luôn xanh) và `feature/domain`, `feature/dao`, `feature/replan`, `feature/ui`, `feature/reminder-qa`. **Không push thẳng `main`**.
- Commit nhỏ, thường xuyên, có mã task: `[DR-01a] Thêm vuotNuaDemNeuDoi`.
- Pull Request: tiêu đề có ID task; mô tả ghi làm gì, test nào đã chạy, có đổi signature không, và dòng **`AI: có (AI-xx-nn) hoặc không`**. Cần **≥ 1 review** trước khi merge.
- Cửa merge vào `main`: **Thứ 6 09/10 23h** · **Thứ 7 10/10 23h** · **Thứ 2 12/10 22h (freeze, gắn tag `v1.0-freeze`)**. Ngoài giờ này chỉ push nhánh của mình. Ngân chạy lại toàn bộ test trên `main` sau mỗi lần merge.
- Chỉ có 2 buổi call: **CN 11/10 20h** (E2E + đối chiếu diagram với code) và **Thứ 4 14/10 16h** (dợt + build cuối).

**Một task chỉ "xong" khi**: đúng FR/BR/INV liên quan · compile, có guard clause · JUnit đủ happy / invalid / boundary và test cũ vẫn pass · signature khớp `02_MethodContract.md` · PR có review, bug ghi vào `05_TestCases.md` · đã viết 5–10 dòng + hình cho mục báo cáo · diagram của module khớp code.

---

## 8. Dùng AI trong đồ án

Yêu cầu môn học khuyến khích dùng AI như trợ lý lập trình và yêu cầu báo cáo ghi lại prompt tiêu biểu kèm nhận xét, đánh giá. Mỗi người ghi **ngay khi dùng** vào `docs/06_AI_Log/<mã>.md` theo mẫu 5 dòng (dùng để làm gì → AI đề xuất gì → nhóm dùng/sửa gì → kiểm chứng thế nào → nhận xét, đánh giá). Code do AI sinh vẫn phải qua test, review PR và đối chiếu với `02_MethodContract.md`; AI hay bịa method hoặc quan hệ không có trong code nên luôn kiểm lại.

---

## 9. Kiểm thử và kịch bản E2E

Mỗi module cần JUnit đủ 3 nhóm: **happy path · invalid input · boundary**. Ca bắt buộc: T-04 (qua nửa đêm), T-05 (hạn chót), T-09 (khe vừa đủ), T-12/T-13 (chuyển ngày mai; ngày mai kín → vào Chưa xếp lịch; áp dụng đúng phương án), T-18 (Save → Load đủ state), T-19 (trùng id), T-20 (việc hoàn thành không bị dời), T-21 (Giảm tải không cắt khi chưa quá tải), T-24 (báo trễ trên 600 phút), cascade, null reject sớm.

**E2E (chạy ở call CN 11/10)**

1. Tạo lịch có đủ Fixed, Flexible, Deadline, Recurring với ưu tiên khác nhau.
2. Báo sự cố → "Tôi đang bị trễ" → 90 phút → Điều chỉnh lịch.
3. Có 3 phương án khác nhau thật sự; xem được việc dời, việc đề xuất bỏ, cảnh báo.
4. Chọn 1 phương án → áp dụng. Fixed không dời, không việc nào mất (không xếp được thì sang ngày mai; ngày mai kín thì vào Chưa xếp lịch kèm thông báo), không việc nào qua 23:59, deadline xử lý đúng, chỉ phương án được chọn đổi lịch.
5. Tự động lưu → đóng hẳn app → mở lại → lịch giống hệt trước khi đóng.
