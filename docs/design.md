# Design Document

## 1. Danh sách các lớp và vai trò (Class List & Responsibilities)

Liệt kê các class chính trong hệ thống và mô tả ngắn gọn vai trò của từng class.

| Class | Package | Role |
|------|--------|--------|
|Users  |model |Represents user-related information and is used to store and tranfer user information between application layers. |
|AuthService|service |Handles business logic related to user authentication, including verifying login credentials and coordinating data access. |
|LoginDAO |dao |Perform queries against the database to verify user login credentials, such as user ID, and retrieve user roles. |
|UserDAO|dao |Accessing and managing user information in the database includes adding users, reading, updating, and deleting user information,... . |

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

