# 03_BusinessRules
| ID | Luật | Owner |
|---|---|---|
| BR-01 | Không nén thời lượng: việc chỉ vào khe LIÊN TỤC dài ≥ thời lượng; các khe bị Fixed chia cắt không cộng lại; khe đúng bằng thì được | AbstractReplanStrategy, DaySchedule |
| BR-02 (tạm) | Bảo toàn việc: không bao giờ xóa. Việc không xếp được hôm nay phải ở đúng 1 nơi: ngày mai (khe liên tục từ 06:00), "Chưa xếp lịch" (chuaXepLich), hoặc đã hủy bởi chính người dùng. Hiện Toast + Trung tâm thông báo tên các việc chưa xếp | ReplanApplyService + DaySchedule |
| BR-03 | Hệ thống chỉ đề xuất: Replan chỉ chạy khi người dùng báo sự cố; tính trên bản sao; lịch thật chỉ đổi khi chọn. Nhắc/theo dõi trễ không tự Replan | ReplanEngine, Controller |
| BR-04 | Kết quả sau áp dụng phải giống phương án đã xem trước (kể cả chuyển ngày mai / Chưa xếp lịch) | ReplanApplyService |
| BR-05 | Mỗi chiến lược có quy tắc riêng về việc được phép đề xuất bỏ (ma trận bên dưới) | từng ReplanStrategy |
| BR-06 | Hết chỗ hôm nay: được phép bỏ → đề xuất bỏ; Recurring không bỏ được → bỏ qua hôm nay, reset streak; vượt 23:59 → đề xuất sang ngày mai; còn lại bị Fixed chặn → vẫn xếp + cảnh báo đỏ | AbstractReplanStrategy |
| BR-07 | DeadlineTask: dời mà kết thúc > hạn chót → không dời, bật canhBaoTreHan; đúng bằng hạn vẫn kịp | DeadlineTask |
| BR-08 | Trễ: chỉ dời việc chưa bắt đầu. Không làm được một việc: bỏ được cả việc đã bắt đầu nhưng chưa xong | DelayDisruption, SkipDisruption |
| BR-09 | Việc phát sinh: thêm trực tiếp, không Replan; chồng giờ chỉ cảnh báo (trùng Fixed → từ chối) | MainFrame → DaySchedule.themItem |
| BR-10 | Lưu: ghi file tạm rồi thay file chính; file hỏng → báo lỗi, không ghi đè; chưa có file → lịch mới | LuuLich, KhoDuLieu |

## Ma trận BR-05 – việc được phép đề xuất bỏ khi hết chỗ
| Loại việc | Ít thay đổi | Ưu tiên deadline | Giảm tải |
|---|---|---|---|
| FixedEvent | Không bỏ | Không bỏ | Không bỏ |
| Bắt buộc | Không bỏ | Không bỏ | Không bỏ |
| Nên làm – không phải Deadline | Không bỏ | Có thể bỏ | Có thể bỏ |
| Nên làm – DeadlineTask | Không bỏ | Không bỏ | Có thể bỏ |
| Có thể bỏ – không phải Deadline | Có thể bỏ | Có thể bỏ | Có thể bỏ |
| Có thể bỏ – DeadlineTask | Có thể bỏ | Không bỏ | Có thể bỏ |

## Invariant trọng yếu
INV-02 (giờ bắt đầu < kết thúc) · INV-03 (trong 1 ngày ≤ 23:59) · INV-05 (id duy nhất) · INV-06 (hạn chót ≥ giờ kết thúc) · INV-07 (Fixed không dời) · INV-11 (không 2 Fixed trùng giờ)
