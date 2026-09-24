# Hệ thống đặt sân pickleball

Dự án được tổ chức theo mô hình **Modular Monolith**: một ứng dụng Spring Boot gồm các phần `auth`, `court`, `booking`, `apikey` và `common`. Giao diện React nằm trong `frontend/`. Các phần dùng chung một cơ sở dữ liệu và một máy chủ API.

## Chạy dự án

Yêu cầu Java 17 trở lên, Maven và Node.js.

Mở hai cửa sổ terminal tại thư mục này:

```powershell
# Terminal 1: máy chủ API tại http://localhost:8090
mvn spring-boot:run
```

```powershell
# Terminal 2: giao diện tại http://localhost:5173
cd frontend
npm install
npm run dev
```

Khi mới chạy, dữ liệu H2 được tạo trong `data/`, có hai danh mục và hai sân mẫu. Tài khoản quản trị mẫu: `admin` / `admin123`. Khách hàng tự tạo tài khoản tại trang **Đăng ký**. Tên đăng nhập chỉ gồm chữ và số; mật khẩu không được để trống; số điện thoại phải đúng 10 chữ số.

## Các chức năng

- Khách hàng: đăng ký, đăng nhập, xem và tìm kiếm sân, lọc danh mục, sắp xếp, phân trang, xem thông tin chi tiết cùng lịch trống/đã đặt theo ngày, chọn giờ đặt sân, xem và hủy lượt đặt.
- Quản trị viên có thể nhập địa chỉ, tiện ích, giờ mở cửa và giờ đóng cửa cho từng sân. Khi chưa nhập giờ hoạt động, lịch hiển thị cả ngày và nêu rõ thông tin này chưa được cập nhật. Lượt đặt mới ở sân đã có giờ hoạt động phải nằm trong khoảng giờ đó.
- Tiền sân được tính theo số phút thuê và giá mỗi giờ của sân (làm tròn lên đến đồng). Tiền cọc là 10% tổng tiền (làm tròn lên đến đồng). Khách thấy số tiền, tài khoản Techcombank và mã đơn để ghi nội dung chuyển khoản; sau khi báo đã chuyển, admin kiểm tra giao dịch và xác nhận thủ công.
- Lượt chờ cọc hết hạn sau 15 phút nếu khách chưa báo chuyển khoản. Khi khách báo đã chuyển, sân được giữ tối đa 2 giờ hoặc đến giờ bắt đầu (mốc nào đến trước). Hệ thống kiểm tra hạn mỗi phút. Đơn hết hạn được mở lại khung giờ và đưa vào danh sách cần đối chiếu tiền.
- Quản trị: quản lý danh mục và sân, tải ảnh sân, khóa giờ nghỉ/bảo trì, tìm và lọc đơn theo sân, ngày và trạng thái, xem đơn theo trang. Admin nhập mã giao dịch ngân hàng khi xác nhận cọc/thanh toán; hệ thống lưu người và thời điểm xác nhận.
- Khi admin từ chối hoặc hủy đơn đã thu tiền, hệ thống ghi số tiền cần hoàn. Admin đối chiếu khoản khách báo chuyển ở đơn hết hạn hoặc bị từ chối, ghi nhận tiền cần hoàn hay không nhận được tiền, rồi xác nhận đã hoàn bằng mã giao dịch. Khách thấy trạng thái hoàn tiền trong lịch sử đơn.
- Khách hàng và admin đổi mật khẩu trong trang Tài khoản. Sau 5 lần nhập sai, đăng nhập bị khóa 15 phút. Khách quên mật khẩu liên hệ admin để xác minh; admin cấp mã dùng một lần có hiệu lực 15 phút trong mục Tài khoản khách hàng. Đổi hoặc đặt lại mật khẩu sẽ vô hiệu các phiên đăng nhập cũ.
- Quản trị viên cũng có thể từ chối hoặc hủy đơn kèm lý do cho khách, cấp và thu hồi API Key.
- API đối tác: `GET /api/public/courts` với header `X-API-KEY`. Key có phạm vi `courts:read`, được hiển thị một lần khi cấp.
- Hệ thống từ chối hai lượt đặt trùng thời gian trên cùng sân.
- Những lượt chờ cọc cũ đã quá hạn được đánh dấu hết hạn khi khởi động lại; thông tin lịch sử của chúng được giữ nguyên và không còn chiếm khung giờ trống.

Ứng dụng chưa kết nối tự động với ngân hàng. Trạng thái thanh toán và hoàn tiền do admin xác nhận sau khi đối chiếu giao dịch theo mã đơn. Lượt đặt cũ chưa có giá lưu tại thời điểm đặt sẽ hiển thị dấu `—` ở phần tiền.

## MySQL

Để dùng MySQL, đặt biến môi trường `PICKLEBALL_DB_USER`, `PICKLEBALL_DB_PASSWORD` và tùy chọn `PICKLEBALL_DB_URL`, sau đó chạy:

```powershell
mvn spring-boot:run '-Dspring-boot.run.profiles=mysql'
```

URL mặc định là `jdbc:mysql://localhost:3306/pickleball_db?createDatabaseIfNotExist=true`. Cần thay mật khẩu tài khoản quản trị mẫu và biến `PICKLEBALL_JWT_SECRET` trước khi triển khai công khai.

## Kiểm tra

```powershell
mvn test
cd frontend
npm run build
```
