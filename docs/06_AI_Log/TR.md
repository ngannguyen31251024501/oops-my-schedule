# Nhật ký dùng AI – Trúc (TR)

> Chỉ sửa file của mình. Ghi **ngay sau khi dùng AI** (khoảng 2 phút), chép **nguyên văn prompt**.
> Mục tiêu: ≥ 5 prompt trong nhật ký đến Thứ 2 12/10 22h, trong đó có ≥ 1 prompt AI trả sai và nhóm phải sửa.
> Thứ 3 13/10 12h: chọn 3 prompt tiêu biểu để đưa vào mục "Báo cáo quá trình sử dụng AI". Hạn nộp mục AI: Thứ 3 13/10 20h.
> Trong mô tả Pull Request thêm dòng `AI: có (mã prompt)` hoặc `AI: không`.

## Mục lục

| Mã | Ngày | Task | Công cụ | Mức dùng được (nguyên / sửa nhẹ / sửa nhiều / bỏ) | Chọn vào báo cáo |
|---|---|---|---|---|---|
| AI-TR-01 | | | | | |

## Mẫu một mục (sao chép phần này cho mỗi prompt, đổi số thứ tự)

### AI-TR-01 · 15/10 · <TR-02b (K-03) + test T-20> · <Claude>

**Prompt (nguyên văn):**

```
Còn lỗi Replan dời việc ở K-03 nên hãy sửa lại lỗi này, đồng thời viết test T-20
```

1. **Dùng để làm gì:** <Sửa lỗi Replan dời việc đã hoàn thành (K-03) và viết test T-20>
2. **AI đề xuất gì:** <Tách việc đã xong khỏi danh sách trước khi dời, rồi cho chiếm khung giờ như việc cố định; sửa hàm tìm việc cố định kế tiếp.>
3. **Nhóm dùng/sửa gì:** <Dùng cả ba chỗ sửa trong AbstractReplanStrategy; đổi tên getter và constructor cho khớp code thật.>
4. **Kiểm chứng thế nào:** <Chạy T-20 và toàn bộ test, BUILD SUCCESS.>
5. **Nhận xét, đánh giá:** <Dùng được sau khi sửa nhẹ. AI đoán sai tên getter vì chưa thấy code.>


### AI-TR-02 · 15/10 · <TR-03> · <Claude>

**Prompt (nguyên văn):**

```
Phần Manh chưa push nên là bây giờ chuyển qua làm TR-03 trước, kiểm tra ReplanEngine và làm theo yêu cầu TR-03
```

1. **Dùng để làm gì:** <Kiểm tra ReplanEngine ném IllegalArgumentException khi tham số null.>
2. **AI đề xuất gì:** <Test dùng assertThrows cho từng tham số null.>
3. **Nhóm dùng/sửa gì:** <Dùng nguyên test. Test đỏ vì engine ném NullPointerException, nên sửa engine.>
4. **Kiểm chứng thế nào:** <Chạy lại test và toàn bộ test, đều xanh.>
5. **Nhận xét, đánh giá:** <Dùng nguyên. Test phát hiện đúng một lỗi thật của engine.>

---
### AI-TR-03 · 15/10 · <TR-03 + test T10-T11> · <Claude>

**Prompt (nguyên văn):**

```
Kiểm biên hai test tương ứng với 1 việc bị dời hoặc bỏ và 1 việc giữ nguyên
```

1. **Dùng để làm gì:** <Kiểm biên: việc bắt đầu đúng giờ hiện tại và việc bắt đầu trước giờ hiện tại.>
2. **AI đề xuất gì:** <Hai test, một việc bị dời hoặc bỏ, một việc giữ nguyên giờ.>
3. **Nhóm dùng/sửa gì:** <Dùng nguyên, chỉ sửa cách tạo FlexibleTask theo code.>
4. **Kiểm chứng thế nào:** <Chạy từng test, cả hai xanh.>
5. **Nhận xét, đánh giá:** <Dùng được, ít phải sửa vì quy tắc biên rõ ràng.>
---
### AI-TR-04 · 15/10 · <TR-03 + T14> · <Claude>

**Prompt (nguyên văn):**

```
Viết tiếp phần T-14: chứng minh 3 chiến lược khác nhau
```

1. **Dùng để làm gì:** <Chứng minh ba chiến lược cho ba kết quả khác nhau trên cùng sự cố.>
2. **AI đề xuất gì:** <Một lịch mẫu rồi assert ba phương án có kết quả khác nhau; sau mỗi lần test đỏ, AI đề xuất lịch khác.>
3. **Nhóm dùng/sửa gì:** <Phải đổi lịch mẫu nhiều lần; cuối cùng hạ điều kiện thành "ít nhất hai kết quả khác nhau" sau khi đọc code coTheHySinh.>
4. **Kiểm chứng thế nào:** <Chạy test, in kết quả từng phương án để so sánh.>
5. **Nhận xét, đánh giá:** <AI đoán sai điều kiện khác biệt vì chưa đọc ba chiến lược, và có lần lịch mẫu vô tình chạm lỗi qua nửa đêm. Bài học: dán code các chiến lược trước.>

---
### AI-TR-05 · 15/10 · <> · <Claude>

**Prompt (nguyên văn):**

```
tôi gửi bạn các phần ảnh liên quan đến vẽ diagram, bạn hãy vẽ giúp tôi và các mũi tên không được đè lên nhau và phải phân biệt màu giữa các lớp
```

1. **Dùng để làm gì:** <Sinh file draw.io và PNG cho sơ đồ tuần tự Điều chỉnh lịch>
2. **AI đề xuất gì:** <Sơ đồ chín lifeline, khối alt/loop và ghi chú "lịch thật chưa đổi">
3. **Nhóm dùng/sửa gì:** <Giữ cấu trúc; đối chiếu tên phương thức với code và Method Contract.>
4. **Kiểm chứng thế nào:** <So từng tên lớp và phương thức với mã nguồn trên bản freeze.>
5. **Nhận xét, đánh giá:** <Dùng được sau sửa nhẹ. AI dễ bịa tên phương thức nên phải đối chiếu từng dòng.>
---
## Nhật ký



## Nhận xét cá nhân (3–5 dòng, viết Thứ 3 13/10)
AI giúp tốt nhất ở việc viết test và dựng khung sơ đồ, vì tiết kiệm thời gian ở phần lặp lại. AI sai nhiều nhất ở prompt AI-TR-04, vì nó không biết quy tắc riêng của từng chiến lược. Mình kiểm chứng bằng cách chạy JUnit và đối chiếu với mã nguồn. Nếu làm lại, mình sẽ dán code liên quan và quy tắc nghiệp vụ ngay từ prompt đầu tiên.
