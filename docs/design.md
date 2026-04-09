# Design Document

## 1. Danh sách các lớp và vai trò (Class List & Responsibilities)

Liệt kê các class chính trong hệ thống và mô tả ngắn gọn vai trò của từng class.

| Class | Package | Role |
|------|--------|--------|
| Main | com.oop.project | Entry point của ứng dụng, khởi tạo và hiển thị `LoginFrame`. |
| DBConnection | com.oop.project.db | Cung cấp cấu hình DB (URL/user/password) và tạo kết nối JDBC dùng chung. |
| UnauthorizedException | com.oop.project.exception | Ngoại lệ runtime cho các thao tác không đủ quyền (authorization). |
| CustomizationOption | com.oop.project.model | Mô hình tùy chọn thêm cho món ăn (name, priceDelta, menuItemId). |
| MenuCategory | com.oop.project.model | Mô hình danh mục món ăn. |
| MenuItem | com.oop.project.model | Mô hình món ăn trong menu (thông tin cơ bản, giá, danh mục). |
| Order | com.oop.project.model | Mô hình đơn hàng hoàn chỉnh: tổng tiền, thuế/phí, danh sách item, metadata đơn. |
| OrderDraft | com.oop.project.model | Mô hình đơn hàng đang thao tác trên UI trước khi checkout. |
| OrderItem | com.oop.project.model | Mô hình 1 dòng món trong đơn, gồm món, tùy chọn, số lượng, tính line total. |
| OrderItemCustomization | com.oop.project.model | Mô hình liên kết tùy chọn với dòng món trong đơn hàng. |
| Role | com.oop.project.model | Enum vai trò người dùng (quản lý/nhân viên), parse và kiểm tra quyền cơ bản. |
| User | com.oop.project.model | Mô hình người dùng đăng nhập (id, username, role) và tiện ích kiểm tra quyền. |
| AuditLogRepository | com.oop.project.repository | Ghi log audit cho các hành động quản trị (ví dụ đổi giá món). |
| CustomizationOptionRepository | com.oop.project.repository | Truy vấn dữ liệu customization option theo món và theo order item. |
| LoginLogRepository | com.oop.project.repository | Ghi log các sự kiện đăng nhập/đăng xuất của người dùng. |
| MenuCategoryRepository | com.oop.project.repository | Truy vấn danh sách danh mục món ăn từ DB. |
| MenuItemRepository | com.oop.project.repository | Truy vấn/thêm/cập nhật dữ liệu món ăn (findAll, theo category, theo id, update price). |
| OrderRepository | com.oop.project.repository | Truy vấn và phân tích đơn hàng: lọc, chi tiết item, doanh thu, best-seller, thống kê. |
| UserRepository | com.oop.project.repository | Xác thực người dùng (login) và ánh xạ dữ liệu user từ DB sang model. |
| AuthService | com.oop.project.service | Nghiệp vụ đăng nhập/đăng xuất: gọi `UserRepository`, ghi `LoginLogRepository`. |
| DashboardService | com.oop.project.service | Nghiệp vụ dashboard: tổng hợp analytics, tìm kiếm/lọc/sắp xếp đơn hàng. |
| MenuService | com.oop.project.service | Nghiệp vụ menu cho người dùng thường: lấy category, lấy món theo category, thêm món. |
| MenuAdminService | com.oop.project.service | Mở rộng `MenuService` cho quản trị: cập nhật giá và kiểm tra quyền manager. |
| OrderService | com.oop.project.service | Nghiệp vụ tạo/chỉnh sửa đơn nháp, validate số lượng, lấy menu và customization. |
| LoginFrame | com.oop.project.ui | Cửa sổ đăng nhập (Swing), nhận credential và điều hướng sang `MainFrame` khi thành công. |
| MainFrame | com.oop.project.ui | Cửa sổ chính sau login, điều phối các tab chức năng Menu/Cart/Orders/Dashboard. |
| CartPanel | com.oop.project.ui.panels | Panel giỏ hàng (comming soon). |
| DashboardPanel | com.oop.project.ui.panels | Panel dashboard hiển thị KPI, bảng đơn hàng, bộ lọc, best-selling items/categories. |
| MenuPanel | com.oop.project.ui.panels | Panel quản lý menu trên UI: danh mục, danh sách món, thêm món, sửa giá theo quyền. |
| OrdersPanel | com.oop.project.ui.panels | Panel thao tác order: chọn món/tùy chọn/số lượng, cập nhật draft, checkout. |

---

## 2. Áp dụng các nguyên lý OOP

Mô tả rõ **từng nguyên lý OOP được áp dụng ở đâu trong hệ thống**.

### 2.1. Encapsulation
- Các thuộc tính nào được khai báo `private`?
- Truy cập thông qua getter/setter nào?
- Lý do áp dụng encapsulation?

**Mô tả:**
> …

---

### 2.2. Inheritance
- Class cha là gì?
- Các class con kế thừa từ đâu?
- Lý do sử dụng kế thừa?

**Mô tả:**
> …

---

### 2.3. Polymorphism
- Phương thức nào được override?
- Được gọi thông qua reference kiểu cha ở đâu?

**Mô tả:**
> …

---

### 2.4. Interface
- Interface nào được sử dụng?
- Vai trò của interface trong thiết kế?

**Mô tả:**
> …

---

### 2.5. Abstraction
- Abstract class / method nào được sử dụng?
- Phần chi tiết nào được ẩn đi?

**Mô tả:**
> …

---

## 3. Design Patterns được sử dụng

Liệt kê các design pattern (nếu có) và giải thích ngắn gọn cách áp dụng.

| Design Pattern | Áp dụng ở đâu | Mục đích |
|---------------|-------------|---------|
| | | |
| | | |

> Nếu không sử dụng design pattern nào, hãy giải thích lý do.

---

## 4. Luồng hoạt động chính (Main Application Flows)

Mô tả các luồng xử lý chính của hệ thống theo dạng từng bước.

### 4.1. Login
1. Người dùng nhập username và password.
2. LoginView gửi thông tin đăng nhập đến AuthService.
3. AuthService kiểm tra thông tin người dùng.
4. Nếu hợp lệ, hệ thống chuyển sang MenuView.

---

### 4.2. [Tên luồng chức năng khác]
1. …
2. …
3. …

---

## 5. Class Diagram

- Vẽ **class diagram** cho hệ thống bằng **draw.io**.
- Sơ đồ phải thể hiện:
  - Quan hệ kế thừa
  - Quan hệ association / composition (nếu có)
  - Interface và class implement

📌 **Yêu cầu:**
- Xuất sơ đồ thành file ảnh (PNG hoặc JPG).
- Lưu tại: `docs/class-diagram.png`

---

## 6. Thiết kế lưu trữ dữ liệu (Database / File Design)

Mô tả cách hệ thống lưu trữ dữ liệu.

### 6.1. Hình thức lưu trữ
- [ ] In-memory
- [ ] File (txt / csv / json)
- [ ] Database (MySQL, SQLite, ...)

**Mô tả lý do lựa chọn:**
> …

---

### 6.2. Cấu trúc dữ liệu lưu trữ

Mô tả các bảng / file chính và dữ liệu được lưu trữ.

| Tên bảng / file | Mô tả | Dữ liệu chính |
|----------------|------|--------------|
| | | |
| | | |

---

## 7. Nhận xét về thiết kế (Optional)

- Ưu điểm của thiết kế hiện tại
- Hạn chế
- Hướng cải tiến trong tương lai (nếu có)

---

## 8. Kết luận

Tóm tắt ngắn gọn cách thiết kế hệ thống và cách áp dụng OOP trong project.

