# Nhật ký dùng AI – Ngân (NG)

> Chỉ sửa file của mình. Ghi **ngay sau khi dùng AI** (khoảng 2 phút), chép **nguyên văn prompt**. Mục tiêu: ≥ 5 prompt trong nhật ký đến Thứ 2 12/10 22h, trong đó có ≥ 1 prompt AI trả sai và nhóm phải sửa. Thứ 3 13/10 12h: chọn 3 prompt tiêu biểu để đưa vào mục "Báo cáo quá trình sử dụng AI". Hạn nộp mục AI: Thứ 3 13/10 20h. Trong mô tả Pull Request thêm dòng `AI: có (mã prompt)` hoặc `AI: không`.

## Mục lục

| Mã | Ngày | Task | Công cụ | Mức dùng được (nguyên / sửa nhẹ / sửa nhiều / bỏ) | Chọn vào báo cáo |
|---|---|---|---|---|---|
| AI-NG-01 | 09/10 | NG-05a (D1 Use Case) | Gemini | Sửa nhẹ | Có |
| AI-NG-02 | 10/10 | NG-02b (Test Matrix) | ChatGPT | Sửa nhẹ | Có |
| AI-NG-03 | 12/10 | NG-06 (Báo cáo, rà văn phong) | Claude | Sửa nhiều | Có |

---

## AI-NG-01 · 09/10 · NG-05a (D1 Use Case) · Gemini

**Prompt (nguyên văn):**
> Đây là bảng Functional Requirement từ FR-01 đến FR-10 của đề tài nhóm mình: Xây dựng ứng dụng quản lý lịch trình cá nhân nếu xảy ra sự cố [dán bảng FR mà nhóm đã thống nhất]. Hãy giúp mình tạo ra sơ đồ Use Case bằng PlantUML cho đối tượng sử dụng duy nhất là "Người dùng cá nhân", mỗi UC mỗi chức năng ghi FR tương ứng. Tuyệt đối chỉ được dùng FR có trong bảng, không thêm chức năng ngoài bảng. Sau đó hãy xuất cho mình output là bảng UC ↔ FR.

1. **Dùng để làm gì:** Dựng bản nháp D1 Use Case và bảng UC↔FR cho `01_Requirements.md`.
2. **AI đề xuất gì:** 8 UC (tạo việc, sửa/xóa, xem lịch, hoàn thành, báo sự cố, điều chỉnh lịch, chuyển ngày mai, lưu/khôi phục) + quan hệ `<<include>>`/`<<extend>>`. Tuy nhiên AI tự thêm 2 UC không có trong bảng FR: "Đăng nhập" và "Hoàn tác (Undo)", và gắn "Báo sự cố" `<<include>>` vào "Lưu lịch".
3. **Nhóm dùng/sửa gì:** Giữ 8 UC khớp FR-01…FR-10. **Bỏ** UC "Đăng nhập" (D-20: 1 actor, không đăng nhập) và "Undo" (mục 9: không làm thêm). **Sửa** quan hệ: "Điều chỉnh lịch" là phần mở rộng (`<<extend>>`) của "Báo sự cố" thay vì include vào "Lưu lịch"; "Chuyển việc sang ngày mai" là `<<include>>` của "Điều chỉnh lịch".
4. **Kiểm chứng thế nào:** Đối chiếu từng UC với bảng FR mục 4 (mỗi UC phải trỏ được tới ≥1 FR, không UC nào thừa); đối chiếu D-20 và mục 9 bản phân tích để loại UC ngoài phạm vi.
5. **Nhận xét, đánh giá:** Đúng một phần, cần phải sửa nhẹ (≈3 UC/quan hệ). Do AI đã bịa UC "Đăng nhập" và "Undo" dù đã dặn "tuyệt đối chỉ được dùng FR có trong bảng" (do ứng dụng thường có đăng nhập). Nhóm đã phải hỏi lại 2 lần (lần 2 thêm câu "KHÔNG có đăng nhập, không có Undo"). Tiết kiệm tầm 10 phút để nhóm đối chiếu UC và FR, sau tình huống này, nhóm rút kinh nghiệm rằng phải liệt kê rõ cả những việc AI KHÔNG được làm (D-20, mục 9) ngay trong prompt.

---

## AI-NG-02 · 10/10 · NG-02b (Test Matrix) · ChatGPT

**Prompt (nguyên văn):**
> Với business rule sau đây: BR-01 "Không nén thời lượng: việc chỉ được đặt vào khoảng trống LIÊN TỤC dài hơn hoặc bằng thời lượng; những khoảng trống bị FixedEvent chia cắt thì không được cộng lại; khoảng trống đúng bằng thì mới được" và hàm `DaySchedule.timKhoangTrongDauTien(Duration, LocalTime tuGio)` trả `LocalTime` hoặc null, hãy giúp mình liệt kê các ca test kiểm thử happy / invalid / boundary dưới dạng bảng: Test ID, Input, Expected mà bạn cho là hợp lý. Chưa được viết code. Yêu cầu tối thiểu như sau: giờ kết thúc tối đa phải là 23:59, ngày mai thì dò từ khung 06:00.

1. **Dùng để làm gì:** Lấy danh sách ca test cho T-09 / T-12 để điền Test Matrix trong `05_TestCases.md` và đối chiếu với test Manh viết.
2. **AI đề xuất gì:** Bảng ~10 ca: khe 90' xếp việc 60' (happy); khe đúng 60' (biên, được); khe 59' (biên, null); hai khe 30' bị Fixed chia cắt (null); ngày trống từ 06:00 → 06:00; ngày có Fixed 06:00–07:00 → 07:00; khe cuối ngày đúng bằng thời lượng; tuGio = null. Riêng ca "khe cuối ngày": AI ghi Expected "khe 23:00–24:00 đủ cho việc 60'", sai so với quy tắc 23:59 (CT-09: ⌊23:59 − conTro⌋ ≥ dur → 1439' tìm được, 1440' thì không).
3. **Nhóm dùng/sửa gì:** Giữ các ca khe 90'/60'/59', hai khe 30', Fixed 06:00–07:00. **Sửa** Expected ca cuối ngày: việc 60' bắt đầu 23:00 → null (chỉ còn 59'), bắt đầu 22:59 → hợp lệ. **Bỏ** ca `tuGio = null` (chữ ký hiện tại không định nghĩa, Manh chưa có guard). **Thêm** ca ngày mai trống + tuGio 06:00 (K-08).
4. **Kiểm chứng thế nào:** Chạy thử các ca trên code `da_sua_15` và đối chiếu số 1439'/1440' ở CT-09; đối chiếu test `T-09` trong `MA-03` của Manh; chỉ tick Test Matrix khi test JUnit chạy xanh.
5. **Nhận xét, đánh giá:** Đúng một phần, cần phải sửa nhẹ. Do AI tính ngày dài đến 24:00 thay vì 23:59 (đúng loại lỗi biên K-01 mà nhóm đang sửa), đồng thời đề xuất thêm ca `tuGio = null` không có trong Method Contract. Nhóm đã phải hỏi lại 2 lần. Tiết kiệm tầm 30 phút nghĩ ca biên, nhưng nhóm vẫn phải chạy thật từng ca trước khi tick Test Matrix. Sau tình huống này, nhóm rút kinh nghiệm rằng phải dán sẵn công thức liên quan (CT-09) và mốc 23:59 vào prompt, không tick Expected theo lời AI.

---

## AI-NG-03 · 12/10 · NG-06 (Báo cáo, rà văn phong) · Claude

**Prompt (nguyên văn):**
> Giúp mình soạn lại đoạn báo cáo mình đã viết sao cho văn phong học thuật nhưng vẫn tự nhiên, giữ nguyên số liệu và tên lớp/method [dán đoạn mô tả "Đề tài, mục tiêu, phạm vi"]. Tuyệt đối không được thêm thông tin mới, không bịa, không đổi tên lớp, không thay đổi dữ liệu. Nếu câu nào mơ hồ thì liệt kê ra, không được tự bổ sung hay nói quá

1. **Dùng để làm gì:** Chuẩn hóa giọng văn chương "Mô tả đề tài, mục tiêu, phạm vi" (từ Task 1 mục 4 và 9) trước khi ghép báo cáo bản 1.
2. **AI đề xuất gì:** Đoạn văn mạch lạc, trang trọng hơn, gộp các gạch đầu dòng thành 3 đoạn. Nhưng AI (a) viết "ứng dụng sử dụng thuật toán học máy để tái sắp xếp lịch" trong khi hệ thống là rule-based; (b) đổi `ReplanApplyService` thành `ReplanService`; (c) thêm câu "ứng dụng hỗ trợ đồng bộ đa thiết bị" không có trong phạm vi.
3. **Nhóm dùng/sửa gì:** Giữ cấu trúc 3 đoạn và cách diễn đạt mục tiêu. **Sửa** lại "học máy" → "dựa trên luật (rule-based), không dùng AI lúc chạy"; trả lại tên `ReplanApplyService`. **Bỏ** câu đa thiết bị. Thêm lại phần "giữ / gộp / không làm" bằng lời nhóm.
4. **Kiểm chứng thế nào:** Đọc lại từng câu, đối chiếu với tài liệu thiết kế (mục 1: rule-based, 1 actor, Swing desktop), `02_MethodContract.md` (tên lớp) và mục 9 Task 1 (phạm vi giữ/bỏ); dùng Ctrl+F kiểm tên lớp, số liệu.
5. **Nhận xét, đánh giá:** Đúng một phần, cần phải sửa nhiều ở phần nội dung, văn phong thì dùng được. Do AI chèn "học máy" làm sai bản chất đề tài (hệ thống là rule-based), đổi tên `ReplanApplyService` và thêm câu về đa thiết bị không có trong phạm vi. Nhóm đã phải hỏi lại 2 lần. Tiết kiệm tầm 25 phút rà văn phong, sau tình huống này, nhóm rút kinh nghiệm rằng phải ghi rõ ràng buộc "rule-based, không AI/ML, không đa thiết bị" trong prompt và luôn đọc lại bản AI trả về trước khi ghép vào báo cáo.

---

## Nhận xét cá nhân (3–5 dòng)

* AI hỗ trợ tốt nhất ở phần nháp Use Case, gợi ý ca kiểm thử biên cho Test Matrix cũng như rà văn phong báo cáo (AI-NG-01, 02, 03).
* Tuy nhiên, AI lại sai nhiều nhất ở: AI-NG-03 (chèn "học máy", đổi tên lớp) và AI-NG-02 (tính 24:00 thay vì 23:59).
* Nhóm đã phải tự kiểm chứng lại bằng việc đối chiếu bảng FR/BR/CT, rà soát lại những ca test kiểm thử, chạy thử trên code, review với `02_MethodContract.md`; chỉ tick Test Matrix khi test xanh.
* Nếu phải kêu AI chỉnh sửa, cần phải dán đủ ràng buộc (kể cả những gì KHÔNG làm) và công thức liên quan ngay từ prompt đầu, thay vì phải hỏi lại AI và tốn nhiều thời gian cho 1 việc.
