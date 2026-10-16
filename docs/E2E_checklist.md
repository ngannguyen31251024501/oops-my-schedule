# E2E checklist (chạy CN 11/10, Thứ 2 22h, Thứ 4 build cuối) – Ngân (NG-04)

**Dữ liệu demo** (ngày hôm nay, Demo Mode, now ≈ 09:00):
- Fixed: Học 08:00–09:30 (Bắt buộc) · Họp CLB 11:00–12:00 (Bắt buộc)
- Deadline: Làm bài nhóm 09:30–10:30 (Nên làm, hạn tối nay 23:59)
- Flexible: Đọc tài liệu 10:30–11:30 (Có thể bỏ) · Ôn thi 13:00–15:00 (Nên làm) · Dọn phòng 15:00–15:30 (Có thể bỏ)
- Recurring: Tập thể dục 18:00–19:00 (Nên làm)
- Việc muộn tối: Làm slide 21:30–23:00 (Nên làm) → đủ để trễ 90' đẩy sát 23:59

| # | Bước | Kỳ vọng | Bằng chứng |
|---|---|---|---|
| 1 | Tạo đủ 4 loại việc ở trên | Vào timeline; Fixed trùng Fixed bị từ chối | | Tự động: ScheduleControllerTest, KiemThuJUnitTest (TC-01) PASS · Chạy tay (Ngân): ☐ |
| 2 | Báo sự cố → "Tôi đang bị trễ" → 90 phút | Hiện 3 thẻ | | Tự động: ReplanTreTest, KiemThuJUnitTest (VAL-02) PASS · Chạy tay (Ngân): ☐ |
| 3 | So 3 thẻ | 3 phương án khác nhau thật; thấy việc dời / đề xuất bỏ / cảnh báo | | Tự động: ReplanEngineTest t14 PASS · Chạy tay (Ngân): ☐ |
| 4 | Chọn 1 phương án | Fixed không dời; không việc nào qua 23:59; deadline xử lý đúng; chỉ PA chọn đổi lịch | | Tự động: ScheduleItemMidnightTest, NuaDemTest, ReplanApplyServiceTest PASS · Chạy tay (Ngân): ☐ |
| 5 | Việc bị bỏ | Sang ngày mai (từ 06:00) hoặc vào Chưa xếp lịch + Toast/thông báo; không mất việc | | Tự động: ReplanApplyServiceTest (k07, k08), ReplanControllerTest PASS · Chạy tay (Ngân): ☐ |
| 6 | Đóng dialog không chọn | Lịch không đổi | | Chưa có test; cần chạy tay · Chạy tay (Ngân): ☐ |
| 7 | Banner "Bỏ qua" | Ngừng nhắc, việc KHÔNG thành hoàn thành | | Tự động: BoTheoDoiLichTest (idBoQua) PASS; nút trên UI cần chạy tay · Chạy tay (Ngân): ☐ |
| 8 | Banner trễ > 600' → "Tôi đang trễ" | Dialog mở, không lỗi | | Tự động: DelayDisruption chặn >1440; dialog cần chạy tay · Chạy tay (Ngân): ☐ |
| 9 | Đóng hẳn app → mở lại | Lịch giống hệt (✓ Đã lưu) | | Tự động: KiemThuJUnitTest (VAL-10) PASS; mở lại app cần chạy tay · Chạy tay (Ngân): ☐ |

Bug ghi vào `05_TestCases.md` (Bug log).


Bản chạy lại 16/10/2026: JUnit 134 test, 132 PASS, 0 FAIL, 2 ignored; KiemThu.java 33 PASS, 0 FAIL. Cột "Chạy tay" chỉ tick sau khi Ngân chạy giao diện và có ảnh chụp.
