# Khung báo cáo (Ngân – NG-06). Gửi outline cho cả nhóm trước 10h Thứ 2.

| Chương (theo đề bài) | Người viết | Độ dài | Lấy từ |
|---|---|---|---|
| 1. Mô tả đề tài, mục tiêu, phạm vi | Ngân | 1–1,5 tr | Task 1 mục 1, 4, 9 (giữ/gộp/không làm) |
| 2. Sơ đồ UML (D1 Use Case, D2 Class tổng, D3 ERD, D4 Sequence, D7 Flowchart) | từng người vẽ; Ngân chèn | mỗi hình 3–5 dòng giải thích | `docs/04_UML/*.png` (bản cuối SAU freeze) |
| 3. Thiết kế hệ thống / giao diện | Trân (4 tầng), Tú (UI + screenshot) | 1–2 tr | Class_Tong, Seq_DieuChinhLich |
| 4. Phân công & quá trình làm việc | Ngân | 1 tr | Bảng phân công, Git log, PR review |
| 5. Áp dụng OOP | Trân (Domain), Manh (Data), Trúc (Service) | 1–2 tr/người | đoạn code thật của module |
| 6. Kiểm thử | Ngân (owner cấp số liệu) | 1 tr | Test Matrix, bug đã sửa, kết quả E2E |
| 7. Báo cáo quá trình sử dụng AI | mỗi người 3 prompt; Ngân đánh giá chung | 3 tr | `docs/06_AI_Log/*.md` |
| 8. Hướng dẫn cài đặt & sử dụng | Ngân viết, Trân kiểm bằng clone sạch | 1 tr | README + screenshot |
| 9. Hạn chế & hướng phát triển | Ngân | ½ tr | Chưa xếp lịch đầy đủ, chọn nhiều việc, Undo, INV-14…23, K-12 nếu hoãn |

## Phần Ngân tự viết (gợi ý nội dung)
- **Chương 1:** bài toán (lịch vỡ → tính lại, không lập lại từ đầu), actor, 3 luật cứng (Fixed không dời / không nén thời lượng / không tự xóa việc), rule-based vì giải thích được.
- **Chương 4:** 5 người, nhánh `feature/*`, PR ≥1 review, cửa merge 3 mốc, Method Contract làm chuẩn.
- **Chương 6:** 33 case KiemThu + JUnit; bảng K-01…K-12 (lỗi → ai sửa → test nào chứng minh).
- **Chương 8:** Clone → mở thư mục có `pom.xml` → JDK 17 → Run `Main.java`; test: Run All Tests.
- **Mục Reminder cho chương 5 (nếu thầy hỏi):** `ReminderService` chỉ tính nội dung nhắc (SRP), `MainFrame` quyết định bắn; `BoTheoDoiLich` chỉ báo hiệu, không tự Replan (BR-03).

## Slide (Ngân + Tú): 10 slide
Đề tài → Vấn đề → Business Rule → Kiến trúc 4 tầng → Điểm OOP (đa hình 3 trục, Strategy+Template) → Demo (E2E) → Kiểm thử → 1 slide AI (mỗi người 1 prompt + bài học) → Hạn chế → Kết luận.
