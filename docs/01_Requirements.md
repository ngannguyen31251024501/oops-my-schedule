# 01_Requirements – Oops! My Schedule

Nguồn: Bản phân tích thống nhất GĐ1 Task 1 (v2). Actor duy nhất: Người dùng cá nhân.

## Functional Requirement
| ID | Chức năng | Điều kiện / kết quả |
|---|---|---|
| FR-01 | Tạo việc (4 loại: Fixed, Flexible, Deadline, Recurring): tên, ngày, giờ, ưu tiên (+ hạn chót) | Sai dữ liệu → từ chối + lỗi cụ thể; Fixed trùng Fixed → từ chối |
| FR-02 | Sửa và xóa việc | Sửa có thể đổi ngày/loại; xóa có xác nhận, tăng soDaHuy |
| FR-03 | Xem lịch theo ngày và chọn ngày | Hiện trạng thái từng việc |
| FR-04 | Đánh dấu việc hoàn thành | Việc xong không bị Replan dời |
| FR-05 | Báo sự cố: trễ X phút / không làm được một việc / có việc phát sinh | X > 0 (≤ 1440) |
| FR-06 | Sinh 3 phương án (Ít thay đổi, Ưu tiên deadline, Giảm tải) | Tính trên bản sao; lịch thật chưa đổi |
| FR-07 | Xem so sánh trước/sau từng phương án | Việc dời, việc đề xuất bỏ, cảnh báo |
| FR-08 | Chọn 1 phương án để áp dụng | Chỉ phương án được chọn đổi lịch; đóng dialog = không đổi |
| FR-09 | Việc bị đề xuất bỏ: thử chuyển sang ngày mai; không được thì vào "Chưa xếp lịch" | Không bao giờ mất việc (BR-02) |
| FR-10 | Tự lưu và khôi phục lịch (JSON, mỗi ngày 1 file) | File hỏng → báo lỗi, không ghi đè |

## Bảng UC ↔ FR (sơ đồ: `04_UML/src/UseCase.drawio`)
| UC | Tên | FR | Quan hệ |
|---|---|---|---|
| UC-01 | Tạo việc | FR-01 | |
| UC-02 | Sửa / Xóa việc | FR-02 | |
| UC-03 | Xem lịch theo ngày | FR-03 | |
| UC-04 | Đánh dấu hoàn thành | FR-04 | |
| UC-05 | Báo sự cố | FR-05 | «include» UC-06 |
| UC-06 | Điều chỉnh lịch: xem 3 phương án, so sánh, chọn | FR-06, 07, 08 | «include» UC-07 |
| UC-07 | Chuyển việc bị đề xuất bỏ sang ngày mai / Chưa xếp lịch | FR-09 | |
| UC-08 | Lưu và khôi phục lịch | FR-10 | |

## Phạm vi tuần này
- Làm ngay (P0): chặn dời qua 23:59 (INV-03); tách `ReplanApplyService` + `controller/` khỏi MainFrame; INV-05, INV-06; nối UI với 3 phương án; Save→Load.
- Nếu còn giờ (P1): INV-18 (việc xong không bị dời), nút "Bỏ qua" không hoàn thành, NPE→IAE, ngày mai dò từ 06:00, JUnit.
- Hoãn (ghi vào báo cáo "hạn chế"): xử lý đầy đủ Chưa xếp lịch, dòng ước lượng, chọn nhiều việc, INV-14…17/19…23, Undo, Replan nhiều ngày.
