# Hướng dẫn Theme Foundation cho các phase UI

Tài liệu này mô tả cách dùng bộ Theme Foundation đã được tạo ở Phase 1.
Mục tiêu là để tất cả thành viên dùng cùng một chuẩn style, không hardcode màu, font, khoảng cách trong từng panel.

## 1. Thành phần đã có sẵn

- `src/com/oop/project/ui/theme/AppTheme.java`
  - Chứa toàn bộ token màu, spacing, radius.
- `src/com/oop/project/ui/theme/ThemeFonts.java`
  - Khởi tạo font display/body, có fallback an toàn nếu thiếu file font.
- `src/com/oop/project/ui/theme/ThemeInsets.java`
  - Helper tạo Insets chuẩn theo token spacing.
- `src/com/oop/project/ui/theme/ThemeHelper.java`
  - Utility để style button và table.
- `src/com/oop/project/ui/components/TonalCard.java`
  - Card bo góc theo tonal layering bằng custom paint.

## 2. Khởi tạo font

Đã được gọi trong `Main.java` trước khi mở LoginFrame:

- `ThemeFonts.initialize();`

Hành vi:

- Nếu có font trong resources: dùng Manrope + Inter.
- Nếu thiếu font: fallback về Segoe UI, app vẫn chạy bình thường.

## 3. Quy tắc bắt buộc khi code panel mới

1. Không tạo màu trực tiếp bằng `new Color(...)` trong panel.
2. Không dùng `LineBorder` cho card/section.
3. Dùng `TonalCard` để tạo khối nền bo góc.
4. Dùng `ThemeInsets` thay cho việc lặp lại `new Insets(...)`.
5. Dùng `ThemeHelper.applyPrimaryButton(...)` cho action chính.
6. Dùng `ThemeHelper.applyGhostButton(...)` cho action phụ.
7. Dùng `ThemeHelper.applyTableStyle(...)` cho mọi JTable mới.

## 4. Cách dùng nhanh

### 4.1 Button

~~~java
JButton confirmBtn = new JButton("Confirm");
ThemeHelper.applyPrimaryButton(confirmBtn);

JButton cancelBtn = new JButton("Cancel");
ThemeHelper.applyGhostButton(cancelBtn);
~~~

### 4.2 Table

~~~java
JTable table = new JTable(model);
ThemeHelper.applyTableStyle(table);
~~~

### 4.3 Tonal card

~~~java
TonalCard card = new TonalCard(AppTheme.RADIUS_LG, AppTheme.SURFACE_CONTAINER_LOWEST);
card.setLayout(new BorderLayout());
~~~

## 5. Font resources

Thư mục font chuẩn:

- `src/main/resources/fonts/`

Khuyến nghị đặt các file:

- `Manrope-Regular.ttf`
- `Inter-Regular.ttf`

Lưu ý: Có thể bổ sung thêm bản Bold, nhưng code hiện tại vẫn chạy tốt chỉ với bản Regular.

## 6. Checklist review trước khi merge

1. Panel mới có dùng token trong `AppTheme` chưa.
2. Có hardcode màu/font/spacer trong class panel không.
3. Có dùng `ThemeHelper` cho button/table không.
4. Có dùng `TonalCard` cho các card chính không.
5. Chạy app khi thiếu font file để xác nhận fallback không crash.

## 7. Gợi ý áp dụng ở các phase kế tiếp

- Phase 2 (Shell): SidebarNav, TopBar dùng token màu + spacing ngay từ đầu.
- Phase 3 (Login): áp dụng typography scale từ `ThemeFonts`.
- Phase 4-6 (Cart, Orders, Dashboard): ưu tiên `TonalCard` + `ThemeHelper.applyTableStyle(...)`.

Nếu cần thêm token mới, chỉ thêm ở `AppTheme` rồi dùng xuyên suốt, không patch cục bộ ở từng panel.