# 05_TestCases – Test Matrix + Bug log (Ngân)

Quy tắc: chỉ tick ✅ khi **owner đã chạy test và có bằng chứng**. ☐ = chưa.
Bằng chứng hiện có: `KiemThu.java` = 33 PASS / 0 FAIL (chạy ngày 14/10 trên code demo).

## Test Matrix
| Module | Owner | Happy | Invalid | Boundary | Integration | Ghi chú |
|---|---|---|---|---|---|---|
| Domain (ScheduleItem…) | Trân | ✅ (TC-02..08) | ✅ (BONUS-3, TC-04) | ☐ T-02, T-04, T-05 | — | JUnit: `domain/ScheduleItemTest` (Ngân viết sẵn khung, chưa chạy IntelliJ) |
| Data (DaySchedule, LuuLich) | Manh | ✅ (TC-01, BONUS-4) | ☐ T-19 trùng id | ☐ T-09 khe 59'/60' | ☐ T-18 Save→Load, file hỏng | |
| Replan (Engine/Strategy/Apply) | Trúc | ✅ (VAL-01..04) | ☐ null reject | ☐ T-04 23:59, T-11 | ☐ T-12/13, T-14, T-20, T-21 | |
| Controller | Tú | ☐ | ☐ ô trống / không phải số | ☐ T-24 trễ >600' | ☐ | |
| Reminder / BoTheoDoiLich | Ngân | ✅ (BONUS-5/6, VAL-07..09) | ✅ (`BoTheoDoiLichTest` null) | ✅ T-22 (0,4,5,19,20 – chạy tay 14/10) | ☐ chạy JUnit | overload `idBoQua` (K-04) đã thêm |

## Bug log
| Bug ID | Test ID | Nội dung | Expected | Actual | Status | Owner |
|---|---|---|---|---|---|---|
| K-01/02 | T-04 | Dời qua 23:59 ra giờ sai | Từ chối/đề xuất sang mai | 00:30–01:00 | Open | Trân, Trúc |
| K-03 | T-20 | Replan dời việc đã hoàn thành | Giữ nguyên giờ | Bị dời | Open | Trân, Trúc |
| K-04 | — | "Bỏ qua" đánh dấu hoàn thành | Chỉ ngừng nhắc | Hoàn thành | Ngân xong phần service; Tú nối UI (TU-03) | Ngân/Tú |
| K-05 | T-05 | Deadline trước giờ kết thúc | Từ chối | Nhận | Open | Trân |
| K-06 | T-19 | Trùng id | Từ chối | Nhận | Open | Manh |
| K-07 | T-12/13 | Mất việc khi ngày mai kín | Vào Chưa xếp lịch | Mất | Open | Trúc, Tú |
| K-08 | T-09/12 | Dò ngày mai từ 00:00 | Từ 06:00 | 00:00 | Open | Manh, Trúc |
| K-10 | T-21 | Giảm tải đếm đôi | Không cắt khi chưa quá tải | Cắt t1 | Open | Trúc |
| K-11 | T-24 | Banner >600' văng lỗi | Dialog mở | IllegalArgumentException | Open | Trân, Tú |
