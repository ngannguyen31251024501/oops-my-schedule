# Nhật ký dùng AI – Manh (MA)

> Chỉ sửa file của mình. Ghi **ngay sau khi dùng AI** (khoảng 2 phút), chép **nguyên văn prompt**.
> Mục tiêu: ≥ 5 prompt trong nhật ký đến Thứ 2 12/10 22h, trong đó có ≥ 1 prompt AI trả sai và nhóm phải sửa.
> Thứ 3 13/10 12h: chọn 3 prompt tiêu biểu để đưa vào mục "Báo cáo quá trình sử dụng AI". Hạn nộp mục AI: Thứ 3 13/10 20h.
> Trong mô tả Pull Request thêm dòng `AI: có (mã prompt)` hoặc `AI: không`.

## Mục lục

| Mã | Ngày | Task | Công cụ | Mức dùng được (nguyên / sửa nhẹ / sửa nhiều / bỏ) | Chọn vào báo cáo |
| AI-MA-01 | [dd/mm] | MA01 | ChatGPT | sửa nhiều | Có |
| AI-MA-02 | [dd/mm] | MA02 | ChatGPT | sửa nhẹ | Có |
| AI-MA-03 | [dd/mm] | MA03 | ChatGPT | sửa nhẹ (bỏ đề xuất sai về cộng khoảng trống) | Có |

## Mẫu một mục (sao chép phần này cho mỗi prompt, đổi số thứ tự)

### AI-MA-01 · dd/mm · <ID task, ví dụ DR-01a> · <ChatGPT / Gemini / Claude / Copilot>

**Prompt (nguyên văn):**

```
<dán prompt ở đây>
```

1. **Dùng để làm gì:** <việc cụ thể, gắn với task>
2. **AI đề xuất gì:** <tóm tắt kết quả; dán đoạn code chính nếu cần, không dán cả file>
3. **Nhóm dùng/sửa gì:** <phần giữ nguyên, phần sửa, phần bỏ và lý do>
4. **Kiểm chứng thế nào:** <test nào đã chạy (T-xx, VAL-xx), đối chiếu với Method Contract / BR / INV nào>
5. **Nhận xét, đánh giá:** <AI đúng/sai ở đâu, tốn bao nhiêu lần chỉnh prompt, lần sau thêm gì vào prompt để ra đúng ngay>

---

## Nhật ký

### AI-MA-01 · [13/10] · MA01 · ChatGPT

**Prompt (nguyên văn):**

```
Hãy hướng dẫn chi tiết nhiệm vụ MA01 của tôi, hướng dẫn tôi làm và sửa để ra kết quả logic nhất
```

1. **Dùng để làm gì:** Hỗ trợ xây dựng phương thức `timKhoangTrongDauTien()` trong lớp `DaySchedule` để sắp xếp công việc và ngăn các công việc trùng ID.
2. **AI đề xuất gì:** Phương thức `timKhoangTrongDauTien(Duration thoiLuong, LocalTime tuGio)`, trả về `LocalTime` nếu tìm thấy khoảng trống đủ dài, trả về `null` nếu không tìm thấy.
3. **Nhóm dùng/sửa gì:** Giữ ý tưởng duyệt lần lượt các khoảng thời gian trống. Tự kiểm tra và bổ sung điều kiện ngăn trùng ID, vì phần này AI chưa xử lý đầy đủ.
4. **Kiểm chứng thế nào:** Chạy thử và kiểm chéo giữa các thành viên.
5. **Nhận xét, đánh giá:** AI hỗ trợ tốt ở việc định hướng cách làm, nhưng kết quả vẫn phải kiểm tra lại. Số lần chỉnh prompt: [điền]. Lần sau nên nêu rõ ngay trong prompt yêu cầu "không cho phép hai công việc trùng ID" để AI xử lý luôn.

---

### AI-MA-02 · [13/10] · MA02 · ChatGPT

**Prompt (nguyên văn):**

```
Hãy giúp tôi làm MA02, hãy xây dựng và hỗ trợ kiểm tra code đúng logic
```

1. **Dùng để làm gì:** Hỗ trợ rà soát chức năng lưu và đọc dữ liệu trong `LuuLich`.
2. **AI đề xuất gì:** Kiểm tra các thuộc tính `hoanThanh`, `soLanDaDoi`, thông tin deadline, `soDaHuy` và `suCoNhatKy` sau khi lưu rồi đọc lại.
3. **Nhóm dùng/sửa gì:** Dùng danh sách thuộc tính cần kiểm tra. Bổ sung thêm các trường hợp lịch rỗng, chưa có tệp dữ liệu và tệp JSON bị lỗi. Khi việc đọc dữ liệu thất bại thì giữ nguyên dữ liệu hiện có.
4. **Kiểm chứng thế nào:** Chạy thử và kiểm chéo giữa các thành viên.
5. **Nhận xét, đánh giá:** AI giúp hệ thống hóa các trường hợp cần kiểm tra, nhưng các trường hợp biên (lịch rỗng, file thiếu, JSON lỗi) là do nhóm bổ sung. Số lần chỉnh prompt: [điền]. Lần sau nên liệt kê sẵn các trường hợp lỗi trong prompt.

---

### AI-MA-03 · [15/10] · MA03 · ChatGPT

**Prompt (nguyên văn):**

```
Giúp tôi làm MA03, phần này kiểm tra như thế nào, giúp tôi viết và sửa các dòng còn thiếu, [dán một số đoạn code MA01, MA02]
```

1. **Dùng để làm gì:** Hỗ trợ xây dựng các ca kiểm thử JUnit cho `DaySchedule`.
2. **AI đề xuất gì:** Nếu có hai khoảng trống liên tiếp, mỗi khoảng 30 phút, thì có thể cộng lại thành 60 phút để xếp một công việc kéo dài 60 phút. **(Đề xuất này sai.)**
3. **Nhóm dùng/sửa gì:** Không dùng đề xuất cộng hai khoảng trống vì sai quy tắc nghiệp vụ: một công việc phải nằm trọn trong một khoảng trống liên tục, không thể bắc qua `FixedEvent` ở giữa. Các phần còn lại của bộ test được giữ lại.
4. **Kiểm chứng thế nào:** Chạy test T-09 với ba trường hợp: khoảng trống 59 phút không đủ, 60 phút đủ, và hai khoảng 30 phút bị `FixedEvent` chia cách (không được xếp). Đối chiếu kết quả với yêu cầu của `DaySchedule`.
5. **Nhận xét, đánh giá:** AI đưa ra kết luận sai khi chưa xét đầy đủ ràng buộc thời gian. Số lần chỉnh prompt: [điền]. Lần sau cần nêu rõ trong prompt quy tắc "khoảng trống phải liên tục, không được cộng dồn qua sự kiện cố định" và đưa sẵn các ca biên 59/60 phút.

## Nhận xét cá nhân (3–5 dòng, viết Thứ 3 13/10)
Trong quá trình làm dự án, AI hỗ trợ rất tốt trong đề xuất thuật toán tìm khoảng trống lịch và xây dựng các test case cho những tình huống biên ( AI-MA-01, AI-MA-03). Tuy nhiên, đôi khi AI hiểu sai và nếu không có sự kiểm tra cũng như rà soát cẩn thận sẽ gây ra lỗi lớn khi merge. Nếu thực hiện tại khi dùng AI sẽ yêu cầu rõ những lớp nào thuộc quyền sở hữu, và cung cấp rõ 



