# Nhật ký dùng AI – Trân (DR)

> Chỉ sửa file của mình. Ghi **ngay sau khi dùng AI** (khoảng 2 phút), chép **nguyên văn prompt**.
> Mục tiêu: ≥ 5 prompt trong nhật ký đến Thứ 2 12/10 22h, trong đó có ≥ 1 prompt AI trả sai và nhóm phải sửa.
> Thứ 3 13/10 12h: chọn 3 prompt tiêu biểu để đưa vào mục "Báo cáo quá trình sử dụng AI". Hạn nộp mục AI: Thứ 3 13/10 20h.
> Trong mô tả Pull Request thêm dòng `AI: có (mã prompt)` hoặc `AI: không`.

## Mục lục

| Mã | Ngày | Task | Công cụ | Mức dùng được (nguyên / sửa nhẹ / sửa nhiều / bỏ) | Chọn vào báo cáo |
|---|---|---|---|---|---|
| AI-DR-01 | 16/10 | DR-02 (gỡ xung đột merge) | Claude | Sửa nhẹ | ✔ |
| AI-DR-02 | 16/10 | DR-01b (guard DeadlineTask) | Claude | Nguyên | ✔ |
| AI-DR-03 | 16/10 | DR-02 (thứ tự sửa và merge) | Claude | Sửa nhẹ (AI trả sai một phần) | ✔ |
| AI-DR-04 | 16/10 | DR-01a (PR #6 còn đỏ) | Claude | Nguyên | |
| AI-DR-05 | 16/10 | DR-02 (báo Ngân cách merge) | Claude | Sửa nhẹ | |

## Mẫu một mục (sao chép phần này cho mỗi prompt, đổi số thứ tự)

### AI-DR-01 · dd/mm · <ID task, ví dụ DR-01a> · <ChatGPT / Gemini / Claude / Copilot>

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

### AI-DR-01 · 16/10 · DR-02 · Claude

**Prompt (nguyên văn):**

```
Bạn là trợ lý Git/Java giúp tôi giải quyết xung đột merge trên GitHub cho đồ án Java 17 + Maven.
Tôi gửi kèm: ai_pack.zip (đọc 01_CONTEXT.md TRƯỚC, rồi 02_MethodContract.md) và conflict_pack.zip (kết quả `git merge origin/main` thật trên nhánh feature/domain-DR-02: mỗi file conflict có bản .BASE, .NHANH_CUA_MINH, .MAIN, .CO_MARKER, cùng log_main.txt, status.txt, ket_qua_merge.txt).
Việc cần làm: (1) nói ngắn nguyên nhân từng conflict (ai sửa gì trên main); (2) với mỗi file thuộc package com.oopsmyschedule.domain, đưa FILE HOÀN CHỈNH để tôi dán đè (không đưa đoạn nhỏ); (3) file ngoài Domain thì không sửa, chỉ nói chủ file cần làm gì; (4) cho các lệnh git (PowerShell, Windows) để hoàn tất merge và push; (5) soạn mô tả PR gồm: làm gì, test nào, có đổi chữ ký không, dòng "AI: có (mã nhật ký)".
Ràng buộc: không đổi chữ ký public có sẵn trên main; FixedEvent không bao giờ dời; không nén thời lượng; không tự xóa việc; Domain tự guard invariant (IllegalArgumentException cho đối số sai, IllegalStateException cho trạng thái không cho phép); không sửa file ngoài Domain.
Yêu cầu trả lời: tiết kiệm token, không giải thích dài; chỉ nêu giả định khi thật sự cần; nếu thiếu thông tin thì hỏi MỘT câu trước khi viết code. Nếu bạn tự chạy được javac/Maven thì kiểm tra biên dịch và chạy test trước khi trả file.
```

1. **Dùng để làm gì:** Gỡ xung đột merge giữa `feature/domain-DR-02` và `origin/main`. GitHub web báo xung đột quá phức tạp, không sửa được trên web.
2. **AI đề xuất gì:** Từ kết quả `git merge` thật (conflict_pack.zip), AI chỉ ra chỉ có một xung đột, ở `ScheduleItemGuardTest.java`: `main` đã đổi tên file test của Ngân (`ScheduleItemTest.java` thành `ScheduleItemGuardTest.java`), còn nhánh của mình ghi đè cùng đường dẫn cũ bằng test T-04. AI đề xuất giữ bản của `main` và chuyển test T-04 sang file mới `ScheduleItemMidnightTest.java` (đổi tên class).
3. **Nhóm dùng/sửa gì:** Dùng nguyên cách gỡ. Tự chạy lệnh git ở máy mình (checkout --theirs file của Ngân, thêm file `ScheduleItemMidnightTest.java`, commit merge). Không bấm Merge PR.
4. **Kiểm chứng thế nào:** `git status` không còn `Unmerged paths`; Maven test xanh; push nhánh thành công (`6087724..8cc5ca3`); PR #9 hiện không còn xung đột.
5. **Nhận xét, đánh giá:** Đúng, sửa nhẹ. AI không đoán được xung đột từ repo private nên phải có dữ liệu thật: lần đầu AI từ chối viết code và yêu cầu `conflict_pack.zip`, đúng như prompt dặn "thiếu thông tin thì hỏi trước". Cần nhiều lượt hướng dẫn vì mình chưa quen chạy script và dùng Terminal. Bài học: luôn gửi kèm kết quả `git merge` thật, danh sách nhánh và hướng dẫn từng nút bấm trong IntelliJ.

---

### AI-DR-02 · 16/10 · DR-01b · Claude

**Prompt (nguyên văn):**

```
Hướng dẫn cách sửa lỗi chi tiết dễ hiểu, lưu ý tiết kiệm token
```

(Kèm đoạn mô tả đã dán trong cùng tin nhắn: 3 test trong DeadlineTaskGuardTest vẫn có thể đỏ vì nhánh DR-02 chưa có phần DR-01b: hạn chót không trước giờ kết thúc, ngayThucHien không null, thời lượng không đổi khi đặt lại giờ.)

1. **Dùng để làm gì:** Bổ sung phần guard DR-01b còn thiếu trên nhánh DR-02 để 3 test của `DeadlineTaskGuardTest` chuyển từ đỏ sang xanh.
2. **AI đề xuất gì:** Thêm hai kiểm tra vào constructor công khai (6 tham số) của `DeadlineTask`: `ngayThucHien` khác null (INV-21) và hạn chót không trước giờ kết thúc (INV-06). Thêm kiểm tra thời lượng mới bằng thời lượng cũ vào `ScheduleItem.datLaiKhungGio` (INV-08), ném `IllegalArgumentException`.
3. **Nhóm dùng/sửa gì:** Dùng nguyên. Chỉ sửa constructor công khai, không sửa constructor khôi phục từ file (9 tham số) để vẫn đọc được file lưu cũ. Commit `1f4ace8`: 2 file, 9 dòng thêm.
4. **Kiểm chứng thế nào:** Chạy Maven test: 3 test `DeadlineTaskGuardTest` chuyển từ đỏ sang xanh và toàn bộ test xanh. Đối chiếu INV-06, INV-08, INV-21 trong bản phân tích yêu cầu.
5. **Nhận xét, đánh giá:** Đúng, dùng nguyên, chỉ hỏi 1 lần. AI chỉ đúng chỗ cần sửa vì đã đọc code `DeadlineTask` và `ScheduleItem` (có trong gói tài liệu). AI cũng cảnh báo trước một rủi ro: kiểm tra mới có thể ảnh hưởng `taoBanSaoSangNgayKhac` (file của Manh); test hiện có không báo lỗi, nên chưa phải báo Manh. Bài học: dán code hiện tại của lớp cần sửa vào prompt và hỏi luôn rủi ro ảnh hưởng module khác.

---

### AI-DR-03 · 16/10 · DR-02 · Claude

**Prompt (nguyên văn):**

```
Hiện tại cần phải sửa trước rồi mới merge chứ không hướng dẫn theo kiểu merge xong rồi sửa. 
```

```
Merge sau khi test xanh nhưng merge là phần của bạn khác, tôi chỉ được chỉnh sửa file thôi.
```

1. **Dùng để làm gì:** Hướng dẫn từng bước cho người mới dùng Git: sửa DR-01b, chạy test, commit rồi mới merge `origin/main` vào nhánh DR-02, trong khi việc bấm Merge PR do Ngân làm.
2. **AI đề xuất gì:** Lần đầu AI hướng dẫn gỡ xung đột và merge trước, sửa DR-01b sau, sai thứ tự mình cần. Ở lần sau AI hiểu nhầm "merge" là bấm nút Merge PR trên GitHub, nên đưa cách khác (đưa file `ScheduleItemTest.java` về bản gốc để GitHub tự gộp) thay vì merge `origin/main` ở máy.
3. **Nhóm dùng/sửa gì:** Mình yêu cầu đổi thứ tự thành sửa → test → commit → merge; AI viết lại các bước đúng. Khi AI đưa cách khác, mình không dùng; làm theo kết quả thực tế là merge `origin/main` vào nhánh ở máy mình (đúng quy trình nhóm mục 3.3, người merge PR là Ngân). AI sau đó xác nhận lại rằng đây là cách đúng.
4. **Kiểm chứng thế nào:** `git log` cho thấy commit DR-01b (`1f4ace8`) nằm trước commit merge (`8cc5ca3`); Maven test xanh; đối chiếu kế hoạch phân công (mục 3.3, 3.4).
5. **Nhận xét, đánh giá:** Sai một phần: nhầm thứ tự các bước và lẫn giữa "bấm Merge PR" với "merge nhánh ở máy". Phải hỏi lại 2 lần mới ra đúng. Bài học: nói rõ trong prompt "tôi chỉ được sửa file, không bấm Merge PR" và nêu thứ tự mong muốn ngay từ đầu.

---

### AI-DR-04 · 16/10 · DR-01a · Claude

**Prompt (nguyên văn):**

```
Tại sao DR01 sao vẫn hiện đỏ :
```

```
Ý bạn là phải merge cái PR#9 vô rồi PR#6 mới xanh được đúng không 
```

(Ảnh chụp trang PR #6: nút Merge đỏ, báo xung đột ở `ScheduleItemTest.java`.)

1. **Dùng để làm gì:** Hiểu vì sao PR #6 (DR-01) vẫn đỏ sau khi đã gỡ xung đột ở nhánh DR-02, và biết cần làm gì với nó.
2. **AI đề xuất gì:** PR #6 là nhánh riêng `feature/domain-DR-01a`, vẫn chứa `ScheduleItemTest.java` cũ nên xung đột với `main` (đã đổi tên file). Nội dung #6 đã nằm trong #9, nên không sửa #6, không bấm "Resolve conflicts", chờ #9 được merge rồi đóng #6 nếu nó vẫn mở. AI cũng làm rõ #6 không "tự xanh lại": có thể tự thành "Merged" hoặc phải đóng tay, tùy cách merge.
3. **Nhóm dùng/sửa gì:** Dùng nguyên. Không động vào nhánh DR-01a; dùng giải thích này để báo Ngân.
4. **Kiểm chứng thế nào:** Xem trang PR #6: xung đột đúng ở `ScheduleItemTest.java`, danh sách commit gồm `64020f7` và `7838cf1`; hai commit này cũng có trong lịch sử nhánh DR-02 (`git log`).
5. **Nhận xét, đánh giá:** Đúng, nguyên. Phải hỏi lại 1 lần vì lời giải thích đầu chưa nói rõ #6 sẽ kết thúc thế nào sau khi #9 merge. Bài học: hỏi thẳng "PR này có cần tự sửa không, hay đóng được?" kèm ảnh chụp trang PR.

---

### AI-DR-05 · 16/10 · DR-02 · Claude

**Prompt (nguyên văn):**

```
Vậy giờ nên nói lại cho ngân như thế nào về tình trạng dr01 vẫn còn đỏ và Ngân nên làm gì khi merge để không bị xung đột
```

1. **Dùng để làm gì:** Soạn tin nhắn báo Ngân (người bấm Merge PR) về tình trạng PR #9 và #6, và thứ tự merge để không bị xung đột.
2. **AI đề xuất gì:** Một tin nhắn gồm 3 phần: #9 là PR chính, đã chứa DR-01a, DR-01b, DR-03; #6 đang đỏ, đừng merge và đừng bấm "Resolve conflicts"; thứ tự merge: kiểm #9 hiện "No conflicts", merge bằng "Create a merge commit", đóng #6 nếu còn mở, rồi xem #10 và #11, và chạy lại toàn bộ test trên `main`.
3. **Nhóm dùng/sửa gì:** Dùng làm tin nhắn gửi Ngân, chỉnh lời cho tự nhiên và thêm trạng thái thật của nhánh trước khi gửi.
4. **Kiểm chứng thế nào:** Đối chiếu với quy trình nhóm (mục 3.3, 10.4 của kế hoạch); kiểm trang PR #9 có "No conflicts" và test xanh trước khi gửi.
5. **Nhận xét, đánh giá:** Dùng được, sửa nhẹ. AI nói rõ mình chưa thấy nội dung nhánh `feature/domain-DR-03`, nên PR #10 có thể vẫn xung đột sau khi #9 merge; điểm này cần kiểm tra thật trên GitHub, không thể kết luận từ AI. Bài học: nhờ AI soạn tin nhắn nhưng phải tự kiểm trạng thái PR thật trước khi gửi.

