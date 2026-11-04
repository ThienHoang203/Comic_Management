# 📚 Quản Lý Truyện Tranh (Comic & Manga Collection Manager)

![Android](https://img.shields.io/badge/Android-34%2B-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-purple.svg)
![Room DB](https://img.shields.io/badge/Database-Room%20DB-orange.svg)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

Ứng dụng quản lý bộ sưu tập cá nhân dành cho tín đồ đọc **Manga, Manhwa, Manhua, Light Novel, Comic và Tiểu thuyết** trên nền tảng Android. Được xây dựng 100% ngoại tuyến (Offline-First), riêng tư, mượt mà và hiện đại theo ngôn ngữ thiết kế Material Design 3.

---

## 🌟 Tính Năng Nổi Bật

### 📖 1. Quản Lý Tác Phẩm & Bộ Sưu Tập
* **Thông tin toàn diện**: Lưu trữ Tên tác phẩm, Tác giả, Nhà xuất bản, Định dạng, Thể loại, Tổng số tập, Tóm tắt và Ảnh bìa.
* **Giá mặc định linh hoạt (`Work Default Price`)**: Cấu hình giá mặc định cho tác phẩm. Khi sinh tập mới, giá mặc định được áp dụng tự động mà không làm ảnh hưởng đến giá của các tập đã lưu trước đó.
* **Đổi ảnh bìa dễ dàng**: Tích hợp Android 14 Photo Picker và lưu trữ nội bộ riêng tư.

### 📚 2. Quản Lý Tiến Độ & Giá Tiền Chi Tiết Theo Tập (`VolumeEntity`)
* **Trạng thái sở hữu**: Đánh dấu tập *"Đã mua / Còn thiếu"* bằng 1 chạm.
* **Theo dõi giá từng tập**: Chỉnh sửa giá tiền riêng biệt cho tập đặc biệt/bản sưu tầm.
* **Thống kê tự động**: Tự động tính toán **Tổng tiền đã chi tiêu** và **Giá trung bình/tập** thời gian thực.
* **Thao tác nhanh**: Tích hợp bộ đôi nút `+` và `-` tăng/giảm số tập trực tiếp tại trang Chi tiết.

### 🔍 3. Tìm Kiếm & Bộ Lọc Đa Tiêu Chí
* **Tìm kiếm tức thì**: Lọc theo Tên tác phẩm, Tác giả, Nhà xuất bản.
* **Lọc nhiều Thể loại đồng thời (Multi-Select Genre Filter)**: Tích chọn kết hợp nhiều thể loại *(VD: Action + Romance + Isekai)* với Bảng trượt dạng lưới (`ModalBottomSheet`) kèm ô tìm kiếm nhanh.
* **Lọc Định dạng xuất bản (Format Filter)**: Lọc chính xác theo định dạng *(Light Novel, Webtoon, Artbook, Novel, One-shot, Doujinshi, Bìa cứng, Bản đặc biệt...)*.
* **Menu Sắp xếp đa dạng**:
  * Tên A-Z / Z-A
  * Mới thêm gần đây / Cũ nhất
  * Số tập nhiều nhất / Ít nhất
  * Giá tập cao nhất / Thấp nhất
  * Tổng chi tiêu cao nhất

### 🎨 4. Giao Diện & Animation Tối Ưu
* **Bảng màu Manga Crimson Red**: Tông màu đỏ Manga trẻ trung, hiện đại kết hợp Xanh Slate và Vàng Amber. Hỗ trợ đầy đủ Chế độ Sáng/Tối (Light & Dark Mode).
* **Trượt ngang Card tác phẩm (`Swipe-to-Reveal`)**: Vuốt card sang trái hoặc ấn giữ để mở nhanh lựa chọn Sửa & Xóa. Được gia tốc đồ họa GPU phần cứng (`graphicsLayer`) đạt 60 - 120 FPS.
* **Chuyển cảnh mượt mà**: Hiệu ứng chuyển màn hình Scale-Fade chuẩn Material 3.

### 🛡️ 5. An Toàn Dữ Liệu & Kiểm Thử Tự Động
* **Xác nhận an toàn**: Mọi thao tác Xóa Tác phẩm, Thể loại, Định dạng hay Giảm tập đều có Hộp thoại Xác nhận (`AlertDialog`) bảo vệ khỏi thao tác nhầm.
* **Bộ test tự động (Automated Unit Tests)**: Tích hợp sẵn suite test tự động kiểm thử logic sinh dữ liệu mẫu, theo dõi số tập và thuật toán sắp xếp (`4 passed, 0 skipped, 0 failed`).

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

* **Ngôn ngữ**: [Kotlin](https://kotlinlang.org/) (100%)
* **Giao diện (UI)**: [Jetpack Compose](https://developer.android.com/jetpack/compose) với [Material Design 3](https://m3.material.io/)
* **Kiến trúc**: MVVM (Model-View-ViewModel) + Reactive StateFlow
* **Cơ sở dữ liệu (Database)**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
* **Bất đồng bộ**: Kotlin Coroutines & Flow (`combine`, `stateIn`, `distinctUntilChanged`)
* **Tải ảnh**: [Coil Compose](https://coil-kt.github.io/coil/compose/)
* **Định tuyến (Navigation)**: Custom State-Driven Navigation với `AnimatedContent` & `BackHandler`

---

## 📂 Cấu Trúc Dự Án

```text
com.thien203.comicmanagement
├── data
│   ├── local
│   │   ├── dao          # WorkDao, VolumeDao, GenreDao, FormatDao
│   │   ├── entity       # WorkEntity, VolumeEntity, GenreEntity, FormatEntity, WorkGenreCrossRef
│   │   ├── relation     # WorkWithGenres, WorkDetail
│   │   └── AppDatabase  # Room Database Configuration (v9)
│   ├── model            # SortOrder, ComicFormat
│   └── repository       # WorkRepository, WorkRepositoryImpl
├── ui
│   ├── components       # WorkCard, VolumeItem, ImagePicker
│   ├── navigation       # Screen definitions
│   ├── screens
│   │   ├── add_edit_manga   # Form Thêm / Chỉnh sửa Tác phẩm
│   │   ├── genre_manager    # Quản lý Thể loại & Định dạng
│   │   ├── manga_detail     # Màn hình Chi tiết Tác phẩm & Danh sách Tập
│   │   └── manga_list       # Trang chính, Thống kê, Tìm kiếm & Bộ lọc
│   └── theme            # Color, Theme, Type (Manga Crimson M3 Palette)
└── util                 # ImageStorageHelper (Internal Photo Storage)
```

---

## 🚀 Hướng Dẫn Cài Đặt & Biên Dịch

### Yêu cầu hệ thống:
* **Android Studio**: Jellyfish / Koala hoặc mới hơn.
* **JDK**: 17
* **Android SDK**: Compile SDK 34, Min SDK 24+

### Các bước thực hiện:

1. **Clone repository**:
   ```bash
   git clone https://github.com/USERNAME/ComicManagement.git
   cd ComicManagement
   ```

2. **Mở dự án trong Android Studio**:
   Mở Android Studio ➔ chọn `Open` ➔ trỏ tới thư mục `ComicManagement`.

3. **Biên dịch & Chạy ứng dụng**:
   * Chọn thiết bị ảo (Emulator) hoặc thiết bị thật (USB Debugging).
   * Bấm nút **Run `app`** (`Shift + F10`).

4. **Chạy Unit Test**:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

---

## 📜 Giấy Phép (License)

Dự án được phân phối dưới giấy phép [MIT License](LICENSE).
