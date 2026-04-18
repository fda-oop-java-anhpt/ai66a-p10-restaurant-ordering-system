# Design Document

## 1. Danh sách các lớp và vai trò (Class List & Responsibilities)

Danh sách dưới đây được cập nhật theo code hiện tại trong `src/com/oop/project`.

### 1.1 Core, DB, Exception

| Class | Package | Role |
|------|--------|------|
| Main | com.oop.project | Điểm khởi động ứng dụng, mở `LoginFrame`. |
| DBConnection | com.oop.project.db | Tạo JDBC connection từ config env/system property/default. |
| UnauthorizedException | com.oop.project.exception | Runtime exception cho trường hợp không đủ quyền quản trị. |

### 1.2 Domain Model

| Class | Package | Role |
|------|--------|------|
| Role | com.oop.project.model | Enum role (`MANAGER`, `STAFF`), parser `fromString(...)` và helper `isManager()/isStaff()`. |
| User | com.oop.project.model | Thông tin user đăng nhập (id, username, role). |
| MenuCategory | com.oop.project.model | Danh mục món ăn. |
| MenuItem | com.oop.project.model | Món ăn (id, tên, mô tả, giá, category, createdAt). |
| CustomizationOption | com.oop.project.model | Tùy chọn bổ sung cho món (`priceDelta`, `menuItemId`). |
| OrderDraft | com.oop.project.model | Giỏ đơn tạm trong phiên làm việc, chưa submit DB. |
| OrderItem | com.oop.project.model | 1 dòng món trong draft/order, tính `unitPrice` và `lineTotal`. |
| OrderItemCustomization | com.oop.project.model | Model liên kết customization với dòng món (mapping DB). |
| Order | com.oop.project.model | Đơn hàng đã persisted/phục vụ analytics (subtotal, tax, serviceFee, total, createdAt, items). |

### 1.3 Repository Layer

| Class | Package | Role |
|------|--------|------|
| UserRepository | com.oop.project.repository | Login query và map user + role. |
| LoginLogRepository | com.oop.project.repository | Ghi nhật ký `LOGIN`/`LOGOUT`. |
| MenuCategoryRepository | com.oop.project.repository | Truy vấn danh mục món. |
| MenuItemRepository | com.oop.project.repository | CRUD cơ bản cho món ăn (find/add/update price). |
| CustomizationOptionRepository | com.oop.project.repository | Lấy customization theo menu item và order item. |
| OrderRepository | com.oop.project.repository | Truy vấn order cho dashboard + submit order transaction (header/items/customizations). |
| AuditLogRepository | com.oop.project.repository | Lưu audit khi manager đổi giá. |

### 1.4 Service Layer

| Class | Package | Role |
|------|--------|------|
| AuthService | com.oop.project.service | Authenticate và logout, kết hợp `UserRepository` + `LoginLogRepository`. |
| MenuService | com.oop.project.service | Use case menu cơ bản (đọc category/item, thêm món). |
| MenuAdminService | com.oop.project.service | Kế thừa MenuService, bổ sung check quyền manager và audit update giá. |
| OrderService | com.oop.project.service | Tạo/thao tác `OrderDraft`, validate quantity, tính tax/fee/total. |
| DashboardService | com.oop.project.service | Tổng hợp analytics, search/filter/sort order và nạp order items. |

### 1.5 UI Layer

| Class | Package | Role |
|------|--------|------|
| LoginFrame | com.oop.project.ui | Cửa sổ login tonal style, có role pill UI (MANAGER/STAFF), eye toggle password. |
| MainFrame | com.oop.project.ui | App shell chính: sidebar + topbar + `CardLayout` content (`Menu`, `Cart`, `Orders`, `Dashboard` theo role). |
| MenuPanel | com.oop.project.ui.panels | Quản lý menu cho manager/staff theo quyền được phép. |
| OrdersPanel | com.oop.project.ui.panels | Tạo/chỉnh order draft: chọn món, customizations, qty, add/update/remove. |
| CartPanel | com.oop.project.ui.panels | Review order theo card, quantity stepper, edit/remove, payment toggle UI, empty state, checkout flow. |
| EditItemDialog | com.oop.project.ui.panels | Dialog sửa qty cho 1 dòng item trong cart. |
| OrderConfirmationDialog | com.oop.project.ui.panels | Dialog submit order, hiển thị summary, in receipt. |
| DashboardPanel | com.oop.project.ui.panels | KPI + chart + order history + search/filter/export theo role manager. |
| ReceiptPrinter | com.oop.project.ui.util | In hóa đơn từ order đã submit. |

### 1.6 Theme/Component Foundation

| Class | Package | Role |
|------|--------|------|
| AppTheme | com.oop.project.ui.theme | Design tokens: color, spacing, radius, typography. |
| ThemeFonts | com.oop.project.ui.theme | Load/fallback font và scale typography utility. |
| ThemeInsets | com.oop.project.ui.theme | Helper `Insets` cho section/card/tight spacing. |
| ThemeHelper | com.oop.project.ui.theme | Style helper cho button/table (primary/ghost/zebra table). |
| TonalCard | com.oop.project.ui.components | Rounded card custom paint theo tonal layering. |
| OrderLineCard | com.oop.project.ui.components | Card hiển thị 1 order line trong cart (qty stepper + edit/remove). |

---

## 2. Áp dụng các nguyên lý OOP

### 2.1 Encapsulation
- Domain data được đóng gói trong `private` fields (`User`, `Order`, `MenuItem`, `OrderDraft`, `OrderItem`...).
- Truy cập thông qua getter/setter có kiểm soát; ví dụ `Order#setSubtotal/setTax/setServiceFee/setTotal` validate không âm.
- `OrderDraft#getItems()` trả về danh sách unmodifiable để UI không sửa trực tiếp vào collection bên trong.

Mô tả:
> Encapsulation được áp dụng đồng bộ ở model layer, giúp bảo vệ state, tránh leak mutable state và hạn chế sai dữ liệu khi chạy qua UI/service/repository.

### 2.2 Inheritance
- `MenuAdminService extends MenuService` để tái sử dụng nghiệp vụ menu cơ bản và mở rộng cho manager.
- `UnauthorizedException extends RuntimeException`.
- `LoginFrame/MainFrame extends JFrame`; các panel (`OrdersPanel`, `CartPanel`, `MenuPanel`, `DashboardPanel`) `extends JPanel`.
- `OrderLineCard extends TonalCard` để tái sử dụng card painting + bổ sung behavior item row.

Mô tả:
> Inheritance được dùng đúng mục đích: kế thừa năng lực chung, mở rộng theo context (UI component/service role-specific) thay vì copy code.

### 2.3 Polymorphism
- Override `paintComponent(...)` trong `TonalCard` để custom render rounded surface.
- Override renderer/listener methods trong Swing (`DefaultListCellRenderer`, `DocumentListener`, `AbstractAction`, `DocumentFilter`, table renderer trong `ThemeHelper`).
- Runtime dispatch thông qua parent type (`Component`, listener interfaces, Swing model abstractions).

Mô tả:
> Polymorphism xuất hiện rõ ở tầng UI event-driven và custom rendering, cho phép thay đổi hành vi theo context mà không đổi call site.

### 2.4 Interface
- Sử dụng interface/contract của Java: `List`, `Map`, `Runnable`, listener interfaces (`ActionListener`, `DocumentListener`, `ListSelectionListener`).
- Các callback UI (`Runnable onUpdate`) giúp tách panel khỏi coupling trực tiếp với panel khác.

Mô tả:
> Dù chưa có custom interface nghiệp vụ riêng, hệ thống vẫn tận dụng interface chuẩn để giảm phụ thuộc implementation và giao tiếp giữa các module linh hoạt hơn.

### 2.5 Abstraction
- UI chỉ gọi service (`AuthService`, `OrderService`, `DashboardService`) thay vì thao tác SQL trực tiếp.
- Service gọi repository; repository ẩn chi tiết JDBC/query mapping.
- `DBConnection` ẩn chi tiết cấu hình kết nối DB.

Mô tả:
> Abstraction được thể hiện rõ qua phân tầng UI -> Service -> Repository -> DB, giúp code dễ test, dễ bảo trì và dễ thay đổi implementation ở tầng dưới.

---

## 3. Design Patterns được sử dụng

| Design Pattern | Áp dụng ở đâu | Mục đích |
|---------------|---------------|---------|
| Repository Pattern | `UserRepository`, `OrderRepository`, `MenuItemRepository`, `MenuCategoryRepository`, `CustomizationOptionRepository`, `LoginLogRepository`, `AuditLogRepository` | Tách truy cập dữ liệu/JDBC khỏi nghiệp vụ và UI. |
| Service Layer Pattern | `AuthService`, `MenuService`, `MenuAdminService`, `OrderService`, `DashboardService` | Điều phối use case, giữ UI mỏng, tập trung business logic. |
| MVC-like Layered Separation | `model/*`, `service/*`, `repository/*`, `ui/*` | Phân tách trách nhiệm, giảm coupling, dễ mở rộng. |
| Observer/Event Listener Pattern | Toàn bộ Swing UI (`LoginFrame`, `MainFrame`, `OrdersPanel`, `CartPanel`, `DashboardPanel`) | Xử lý event người dùng theo cơ chế callback/listener. |
| Data Mapper (lightweight) | Các hàm map `ResultSet -> Model` trong repository | Tránh để tầng trên thao tác trực tiếp với `ResultSet`. |
| Transaction Script (submit order) | `OrderRepository.submitOrder(...)` | Gom thao tác tạo order header/items/customizations trong 1 transaction commit/rollback. |

Nhận xét:
> Bộ pattern hiện tại phù hợp quy mô đồ án và yêu cầu Java Swing + JDBC. Điểm mạnh là rõ tầng và dễ theo dõi luồng nghiệp vụ; điểm cần nâng cấp về sau là mở rộng strategy/factory nếu cần thêm payment flow/role workflow phức tạp hơn.

---

## 4. Luồng hoạt động chính (Main Application Flows)

### 4.1 Login
1. Người dùng nhập username/password trên `LoginFrame` (role pill hiện chỉ là UI state).
2. `LoginFrame` gọi `AuthService.authenticate(username, password)`.
3. `AuthService` gọi `UserRepository.login(...)`; nếu thành công thì `LoginLogRepository.log(userId, "LOGIN")`.
4. Nếu sai thông tin, UI hiện thông báo lỗi.
5. Nếu hợp lệ, `LoginFrame` đóng và mở `MainFrame(user)`.
6. `MainFrame` khởi tạo shared `OrderDraft` và các panel theo role (Dashboard chỉ dành cho manager).

### 4.2 Điều hướng app shell
1. `MainFrame` dùng sidebar bên trái + topbar + `CardLayout` content area.
2. Người dùng chuyển màn hình bằng nav button (`Menu`, `Cart`, `Orders`, `Dashboard` nếu manager).
3. Khi mở `Orders`, panel được refresh để đồng bộ draft.
4. Khi mở `Dashboard`, panel refresh lại analytics data.
5. `Logout` gọi `AuthService.logout(currentUser)` rồi quay về `LoginFrame`.

### 4.3 Tạo và chỉnh sửa order draft (OrdersPanel)
1. `OrdersPanel` nhận `OrderDraft` dùng chung từ `MainFrame`.
2. Tải menu items + customization options từ `OrderService`.
3. Người dùng chọn món, customizations, quantity; preview unit/line total cập nhật realtime.
4. Nút `Add Item` hoặc `Update Item` gọi `OrderService.addItem(...)` / `replaceItem(...)`.
5. `Remove Selected` gọi `OrderService.removeItem(...)`.
6. Mọi thay đổi draft sẽ gọi callback `onUpdate` để đồng bộ sang `CartPanel`.

### 4.4 Review cart và checkout (CartPanel)
1. `CartPanel` hiển thị item theo `OrderLineCard` (không còn placeholder/JTable cũ).
2. Mỗi dòng item có quantity stepper (+/-), `Edit` (mở `EditItemDialog`) và `Remove`.
3. Summary card hiển thị `Subtotal`, `Tax (10%)`, `Service Fee (5%)`, `Grand Total`.
4. Payment toggle UI (`CARD/CASH`) hiện tại lưu tạm ở UI state (`selectedPaymentMethod`).
5. `Checkout` mở `OrderConfirmationDialog`; dialog submit qua `OrderRepository.submitOrder(...)`.
6. Sau khi submit thành công: thông báo order id, clear `OrderDraft`, refresh lại UI và callback.

### 4.5 Dashboard và analytics (manager)
1. `DashboardPanel` gọi `DashboardService.getDailyAnalytics(...)`, `getTodaysOrders()` khi tải màn hình.
2. Cho phép filter theo ngày/khoảng giá và sort theo tiêu chí (`time`, `total`, `items`, `staff`).
3. Search order theo tên staff hoặc tên món trong order.
4. Mỗi order cần hiển thị chi tiết sẽ được nạp thêm items qua `getOrderWithItems(order)`.
5. Best selling items/categories được tổng hợp từ `OrderRepository`.

### 4.6 Quản lý menu (MenuPanel)
1. Tải danh mục và danh sách món theo danh mục.
2. Thêm món mới qua service/repository.
3. Cập nhật giá món thông qua `MenuAdminService.updatePrice(...)` (có check role manager).
4. Mọi cập nhật giá được ghi audit log.

---

## 5. Class Diagram

- Vẽ class diagram bằng draw.io dựa trên code hiện tại.
- Sơ đồ nên thể hiện tối thiểu:
  - Kế thừa (`MenuAdminService -> MenuService`, `OrderLineCard -> TonalCard`, các UI `extends JFrame/JPanel`).
  - Association/composition (`MainFrame` giữ `OrdersPanel/CartPanel/MenuPanel/DashboardPanel`; `OrderDraft` chứa `OrderItem`; `Order` chứa `OrderItem`).
  - Phụ thuộc theo tầng (`UI -> Service -> Repository -> DB`).

Yêu cầu lưu:
- Xuất ảnh PNG/JPG.
- Lưu tại `docs/class-diagram.png`.

---

## 6. Thiết kế lưu trữ dữ liệu (Database / File Design)

### 6.1 Hình thức lưu trữ
- [ ] In-memory
- [ ] File (txt/csv/json)
- [x] Database (PostgreSQL)

Mô tả lý do lựa chọn:
> Hệ thống cần lưu trữ bền vững cho users/menu/orders/logs, có quan hệ ràng buộc giữa nhiều bảng và có nhu cầu truy vấn analytics. PostgreSQL phù hợp với mô hình này và đồng bộ với JDBC repository hiện tại.

### 6.2 Cấu trúc dữ liệu lưu trữ (theo `schema.sql`)

| Tên bảng | Mô tả | Dữ liệu chính |
|----------|-------|---------------|
| `roles` | Danh mục role | `id`, `name` (`MANAGER`, `STAFF`) |
| `users` | Tài khoản đăng nhập | `id`, `username`, `password`, `created_at`, `role_id` |
| `login_logs` | Lịch sử login/logout | `id`, `user_id`, `action`, `logged_at` |
| `menu_categories` | Danh mục món | `id`, `name` |
| `menu_items` | Món ăn | `id`, `name`, `description`, `image_path`, `base_price`, `category_id`, `created_at` |
| `customization_options` | Tùy chọn món | `id`, `name`, `price_delta`, `menu_item_id` |
| `dining_tables` | Bàn trong nhà hàng | `id`, `table_number`, `capacity`, `status` |
| `orders` | Header đơn hàng | `id`, `staff_id`, `table_number`, `payment_method`, `order_status`, `subtotal`, `tax`, `service_fee`, `total`, `created_at` |
| `order_items` | Dòng món của đơn | `id`, `order_id`, `menu_item_id`, `quantity`, `unit_price` |
| `order_item_customizations` | Mapping customizations cho từng order item | `id`, `order_id`, `order_item_id`, `menu_item_id`, `customization_id` |
| `audit_logs` | Audit thay đổi giá | `id`, `manager_id`, `menu_item_id`, `old_price`, `new_price`, `changed_at` |

Lưu ý đồng bộ model-schema hiện tại:
- Schema đã có `menu_items.image_path`, `orders.table_number`, `orders.payment_method`, `orders.order_status`.
- Model Java hiện tại (`MenuItem`, `Order`, `OrderDraft`) chưa map đầy đủ các cột mở rộng này.
- Luồng submit order hiện tại insert qua `OrderRepository.createOrderHeader(...)` với các cột có sẵn (staff/subtotal/tax/service_fee/total), các cột mở rộng đang dùng default DB.

Dữ liệu khởi tạo:
> `seed_static.sql` cung cấp dữ liệu mẫu cho roles, users, menu, customization và dữ liệu order/log cơ bản để test nhanh UI + dashboard.

---

## 7. Nhận xét về thiết kế (Optional)

- Ưu điểm của thiết kế hiện tại
- Hạn chế
- Hướng cải tiến trong tương lai (nếu có)

---

## 8. Kết luận

Tóm tắt ngắn gọn cách thiết kế hệ thống và cách áp dụng OOP trong project.
