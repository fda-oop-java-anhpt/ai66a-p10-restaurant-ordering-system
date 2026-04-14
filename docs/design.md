# Design Document

## 1. Danh sách các lớp và vai trò (Class List & Responsibilities)

Liệt kê các class chính trong hệ thống và mô tả ngắn gọn vai trò của từng class.

| Class | Package | Role |
|------|--------|--------|
| Main | com.oop.project | Điểm khởi động của ứng dụng, tạo và hiển thị `LoginFrame`. |
| DBConnection | com.oop.project.db | Đọc cấu hình kết nối DB từ system property/env hoặc giá trị mặc định, sau đó tạo JDBC connection. |
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
| OrderRepository | com.oop.project.repository | Truy vấn và phân tích đơn hàng: lấy danh sách, lọc theo ngày/giá/nhân viên, load item, tính doanh thu và best-seller. |
| UserRepository | com.oop.project.repository | Kiểm tra thông tin đăng nhập và ánh xạ kết quả từ DB sang `User`. |
| AuthService | com.oop.project.service | Xử lý đăng nhập/đăng xuất, gọi `UserRepository` và ghi log qua `LoginLogRepository`. |
| DashboardService | com.oop.project.service | Cung cấp dữ liệu dashboard: analytics, tìm kiếm, lọc, sắp xếp và nạp chi tiết đơn hàng. |
| MenuService | com.oop.project.service | Cung cấp nghiệp vụ menu cơ bản: lấy category, lấy món theo category và thêm món mới. |
| MenuAdminService | com.oop.project.service | Mở rộng `MenuService` cho quản trị: cập nhật giá, kiểm tra quyền manager và ghi audit log. |
| OrderService | com.oop.project.service | Tạo và thao tác `OrderDraft`, lấy menu/tùy chọn, validate số lượng và hỗ trợ cập nhật dòng món. |
| LoginFrame | com.oop.project.ui | Cửa sổ đăng nhập Swing, nhận username/password và điều hướng sang `MainFrame` khi hợp lệ. |
| MainFrame | com.oop.project.ui | Cửa sổ chính sau đăng nhập, hiển thị các tab Menu, Cart, Orders và Dashboard theo quyền. |
| CartPanel | com.oop.project.ui.panels | Panel giỏ hàng tạm thời, hiện mới là phần giao diện placeholder chưa triển khai nghiệp vụ đầy đủ. |
| DashboardPanel | com.oop.project.ui.panels | Panel dashboard hiển thị KPI, bộ lọc, bảng đơn hàng và phần best-selling items/categories. |
| MenuPanel | com.oop.project.ui.panels | Panel quản lý menu: xem danh mục, xem món theo danh mục, thêm món và sửa giá theo quyền. |
| OrdersPanel | com.oop.project.ui.panels | Panel tạo order tại quầy: chọn món, chọn customization, chỉnh số lượng, cập nhật draft và checkout. |

---

## 2. Áp dụng các nguyên lý OOP

Mô tả rõ **từng nguyên lý OOP được áp dụng ở đâu trong hệ thống**.

### 2.1. Encapsulation
- Các thuộc tính nào được khai báo `private`?
  Các model đều dùng `private` cho dữ liệu lõi, ví dụ: `User` (id, username, role), `MenuItem` (id, name, description, basePrice, categoryId, createdAt), `Order` (id, staffId, subtotal, total, items), `CustomizationOption`, `OrderItemCustomization`.
- Truy cập thông qua getter/setter nào?
  Truy cập qua getter/setter tương ứng như `getId()`, `getUsername()`, `getBasePrice()`, `setBasePrice()`, `setSubtotal()`, `setTax()`, `setServiceFee()`, `setMenuItemId()`. Một số class chỉ mở getter hoặc kiểm soát setter để đảm bảo tính hợp lệ.
- Lý do áp dụng encapsulation?
  Để bảo vệ trạng thái object, gom logic validate vào setter/constructor (ví dụ kiểm tra số lượng > 0, giá trị tiền không âm), tránh sửa dữ liệu trực tiếp từ UI/repository và giảm lỗi dữ liệu không hợp lệ.

**Mô tả:**
> Encapsulation được áp dụng xuyên suốt ở tầng model: dữ liệu được đóng gói trong field private, còn việc đọc/ghi đi qua API rõ ràng (getter/setter). Cách làm này giúp kiểm soát ràng buộc dữ liệu và giữ domain object nhất quán khi đi qua các tầng service, repository, UI.

---

### 2.2. Inheritance
- Class cha là gì?
  `MenuService`, `RuntimeException`, `JFrame`, `JPanel` là các class cha chính trong hệ thống.
- Các class con kế thừa từ đâu?
  `MenuAdminService extends MenuService`; `UnauthorizedException extends RuntimeException`; `LoginFrame extends JFrame`; `MainFrame extends JFrame`; `CartPanel`, `DashboardPanel`, `MenuPanel`, `OrdersPanel` đều `extends JPanel`.
- Lý do sử dụng kế thừa?
  Tái sử dụng hành vi chung và mở rộng tính năng theo ngữ cảnh. Ví dụ `MenuAdminService` kế thừa nghiệp vụ menu cơ bản rồi bổ sung kiểm tra quyền và audit khi cập nhật giá.

**Mô tả:**
> Inheritance được dùng đúng mục tiêu: kế thừa năng lực chung từ class nền (Swing/Service) và thêm hành vi đặc thù ở lớp con. Điều này giảm lặp code và làm rõ phân tầng trách nhiệm giữa chức năng cơ bản và chức năng nâng cao theo quyền.

---

### 2.3. Polymorphism
- Phương thức nào được override?
  Override xuất hiện ở `toString()` trong nhiều model (`CustomizationOption`, `MenuCategory`, `Order`, `OrderItemCustomization`), `isCellEditable()` trong `DefaultTableModel` ẩn danh tại `DashboardPanel`, các hàm listener như `insertUpdate/removeUpdate/changedUpdate` (DocumentListener), `actionPerformed` (Action), `focusLost`, và các hàm của `DocumentFilter`.
- Được gọi thông qua reference kiểu cha ở đâu?
  Các callback UI được gọi thông qua reference kiểu cha/interface của Swing: `DocumentListener`, `FocusAdapter`, `AbstractAction`, `DocumentFilter`, `DefaultTableModel`. Runtime sẽ dispatch đến implementation override tương ứng.

**Mô tả:**
> Polymorphism được dùng mạnh ở tầng UI event-driven. Cùng một contract cha (listener/action/model), hệ thống có thể thay đổi hành vi theo context cụ thể mà không đổi luồng gọi từ Swing framework.

---

### 2.4. Interface
- Interface nào được sử dụng?
  Hệ thống không định nghĩa interface nghiệp vụ riêng trong domain hiện tại. Tuy nhiên có dùng interface của Java/Swing như `DocumentListener`, cùng với interface collection như `List` và `Map` ở model/service/repository.
- Vai trò của interface trong thiết kế?
  Interface giúp tách phần sử dụng khỏi phần cài đặt cụ thể: code thao tác trên `List`/`Map` thay vì class cụ thể; cơ chế listener của Swing tách event source khỏi logic xử lý.

**Mô tả:**
> Dù chưa có custom interface ở tầng nghiệp vụ, thiết kế vẫn tận dụng interface chuẩn của Java để tăng linh hoạt, giảm phụ thuộc cứng vào implementation, và phù hợp với cơ chế callback của UI framework.

---

### 2.5. Abstraction
- Abstract class / method nào được sử dụng?
  Không có abstract class/method tự định nghĩa trong domain. Ở UI có dùng abstract class thư viện như `AbstractAction` thông qua anonymous class để cài đặt hành vi cụ thể.
- Phần chi tiết nào được ẩn đi?
  Chi tiết truy vấn SQL được ẩn trong repository; chi tiết kết nối DB được ẩn trong `DBConnection`; UI gọi service ở mức nghiệp vụ (`AuthService`, `OrderService`, `DashboardService`) mà không cần biết cách thao tác dữ liệu bên dưới.

**Mô tả:**
> Abstraction trong project thể hiện chủ yếu ở kiến trúc phân tầng: UI chỉ làm việc với service, service phối hợp repository, repository làm việc với DB. Mỗi tầng che giấu chi tiết kỹ thuật của tầng dưới, giúp code dễ bảo trì và mở rộng.

---

## 3. Design Patterns được sử dụng

Liệt kê các design pattern (nếu có) và giải thích ngắn gọn cách áp dụng.

| Design Pattern | Áp dụng ở đâu | Mục đích |
|---------------|-------------|---------|
| Repository Pattern | Các lớp `UserRepository`, `OrderRepository`, `MenuItemRepository`, `MenuCategoryRepository`, `CustomizationOptionRepository`, `LoginLogRepository`, `AuditLogRepository` | Tách logic truy cập dữ liệu (SQL/JDBC) khỏi business logic; giúp service/UI không phụ thuộc trực tiếp vào câu lệnh SQL. |
| Service Layer Pattern | Các lớp `AuthService`, `MenuService`, `MenuAdminService`, `OrderService`, `DashboardService` | Gom nghiệp vụ theo use-case, điều phối giữa UI và repository; giữ cho UI mỏng, dễ bảo trì. |
| MVC-like Separation (Model-Service/UI) | `model/*` (dữ liệu), `service/*` (nghiệp vụ), `ui/*` + `ui/panels/*` (giao diện) | Phân tách trách nhiệm theo tầng, giảm coupling giữa giao diện và dữ liệu, thuận tiện mở rộng tính năng. |
| Observer/Event Listener Pattern (qua Swing) | `OrdersPanel`, `DashboardPanel`, `LoginFrame` dùng `DocumentListener`, `ActionListener`, `ListSelectionListener`, `FocusAdapter` | Xử lý sự kiện UI theo cơ chế publish/subscribe của Swing; component phản ứng khi trạng thái người dùng thay đổi. |
| Data Mapper (mức đơn giản) | Các hàm map `ResultSet -> Model` như `MenuItemRepository.map(...)`, logic mapping trong `OrderRepository`, `UserRepository` | Chuyển đổi dữ liệu DB sang object domain, tránh để tầng trên thao tác trực tiếp `ResultSet`. |

> Nếu không sử dụng design pattern nào, hãy giải thích lý do.

**Nhận xét:**
> Tổng thể project được tổ chức khá rõ ràng giữa giao diện, nghiệp vụ và truy xuất dữ liệu, nhờ đó mã nguồn dễ đọc và thuận lợi hơn khi mở rộng. Các pattern được áp dụng chủ yếu là những pattern phù hợp với phạm vi đồ án như Repository và Service Layer, giúp các phần của hệ thống tách biệt và hạn chế phụ thuộc chéo. Tuy nhiên, hệ thống hiện tại vẫn còn ở mức đơn giản, chưa khai thác thêm các pattern nâng cao như Factory hay Strategy; nếu tiếp tục phát triển về sau, phần thiết kế có thể được hoàn thiện hơn để tăng tính linh hoạt.

---

## 4. Luồng hoạt động chính (Main Application Flows)

Mô tả các luồng xử lý chính của hệ thống theo dạng từng bước.

### 4.1. Login
1. Người dùng nhập username và password.
2. `LoginFrame` gọi `AuthService.authenticate(username, password)`.
3. `AuthService` gọi `UserRepository.login(...)` để kiểm tra thông tin trong DB.
4. Nếu hợp lệ, `AuthService` ghi log bằng `LoginLogRepository.log(userId, "LOGIN")`.
5. `LoginFrame` đóng cửa sổ đăng nhập và mở `MainFrame(user)`.
6. `MainFrame` nạp các tab chức năng (`Menu`, `Cart`, `Orders`) và chỉ hiển thị `Dashboard` khi user là manager.

---

### 4.2. Quản lý menu (xem danh mục, thêm món, sửa giá)
1. Khi mở `MenuPanel`, hệ thống gọi `MenuService.getAllCategories()` để tải danh mục món.
2. Người dùng chọn một category trong `JList`, `MenuPanel` gọi `MenuService.getMenuItemsByCategory(categoryId)` để hiển thị bảng món.
3. Khi thêm món, `MenuPanel` thu thập thông tin từ form, validate dữ liệu cơ bản, rồi gọi `MenuService.addFood(...)`.
4. `MenuService` chuyển yêu cầu xuống `MenuItemRepository.addFood(...)` để ghi DB.
5. Khi sửa giá, `MenuPanel` gọi `MenuAdminService.updatePrice(currentUser, menuItemId, newPrice)`.
6. `MenuAdminService` kiểm tra quyền manager, cập nhật giá qua `MenuItemRepository.updatePrice(...)`, đồng thời ghi audit log bằng `AuditLogRepository.logPriceChange(...)`.

---

### 4.3. Tạo order và checkout tại quầy
1. Khi mở `OrdersPanel`, hệ thống tạo `OrderDraft` mới bằng `OrderService.createOrder(staffId)`.
2. `OrdersPanel` tải danh sách món qua `OrderService.getAllMenuItems()` và tải tùy chọn theo món qua `OrderService.getCustomizationOptions(menuItemId)`.
3. Người dùng chọn món, chọn customization, nhập số lượng; UI cập nhật preview giá theo event listener.
4. Khi bấm Add/Update, `OrdersPanel` gọi `OrderService.addItem(...)` hoặc `OrderService.replaceItem(...)` để cập nhật `OrderDraft`.
5. Khi bấm Remove Selected, `OrdersPanel` gọi `OrderService.removeItem(...)` để xóa dòng món khỏi draft.
6. Khi bấm Checkout, nếu draft rỗng thì báo lỗi; nếu hợp lệ thì hiển thị thông báo hoàn tất và subtotal, sau đó reset về đơn mới (`startNewOrder()`).
7. Ở phiên bản hiện tại, bước checkout mới hoàn tất ở mức UI/draft, chưa gọi lưu order chính thức xuống DB.

---

### 4.4. Cart và chỉnh sửa món trong order
1. `MainFrame` luôn mở tab `Cart`, nhưng ở phiên bản hiện tại `CartPanel` chỉ là giao diện placeholder.
2. Chức năng thêm, xóa và cập nhật món thực tế đang nằm trong `OrdersPanel`, nơi người dùng thao tác với `OrderDraft`.
3. `OrdersPanel` cho phép thêm món vào draft bằng nút `Add Item`, xóa món bằng `Remove Selected`, và cập nhật lại dòng món khi đổi số lượng hoặc customization.
4. Vì vậy, tab Cart hiện chưa phải nơi xử lý nghiệp vụ chính; nó mới là phần giao diện dự phòng cho mở rộng sau này.

---

### 4.5. Dashboard và phân tích doanh thu
1. `DashboardPanel` khởi tạo và gọi `loadDashboardData()` để lấy dữ liệu ngày hiện tại.
2. Panel gọi `DashboardService.getDailyAnalytics(date)` để lấy tổng doanh thu, số đơn, giá trị trung bình.
3. Panel gọi `DashboardService.getTodaysOrders()` để hiển thị bảng đơn và `getBestSellingItems/getBestSellingCategories` để hiển thị top bán chạy.
4. Khi áp dụng bộ lọc, UI gửi điều kiện ngày + khoảng giá đến `DashboardService.filterOrders(...)`, sau đó `sortOrders(...)` trước khi hiển thị.
5. Khi search, UI gọi `DashboardService.searchOrders(keyword)` để lọc theo tên nhân viên hoặc tên món trong đơn.
6. Với từng order hiển thị, panel gọi `DashboardService.getOrderWithItems(order)` để nạp chi tiết item trước khi render bảng.

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

