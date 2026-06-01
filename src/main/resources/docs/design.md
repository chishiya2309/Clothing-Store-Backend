# Design Brief — Website Bán Quần Áo (Clothing Store)

> **Mục đích**: Tài liệu mô tả giao diện & cảm nhận (Look and Feel) cho toàn bộ sản phẩm.
> Dùng làm đầu vào cho **Google Stitch Design with AI** để sinh UI prototype.
> **Nhóm 10** | Môn: Mẫu Thiết Kế Phần Mềm | 2026

---

## 1. Tổng quan sản phẩm

**Tên**: CLOTHY — Website bán quần áo thời trang Việt Nam
**Loại**: E-commerce SPA (Single Page Application)
**Đối tượng**: Nam, Nữ, Trẻ em — Gen Z & Millennials Việt Nam (18–35 tuổi)
**Thiết bị**: Desktop-first, Responsive (Mobile, Tablet)
**Tech**: React + Vite (Frontend), Spring Boot (Backend API)

### Tầm nhìn thiết kế

> "Mang đến trải nghiệm mua sắm thời trang trực tuyến **nhanh, trực quan, đáng tin cậy** — nơi mỗi sản phẩm được trình bày như trong một lookbook chuyên nghiệp."

---

## 2. Phong cách thiết kế (Design Direction)

### 2.1. Aesthetic: Modern Minimal với điểm nhấn Bold

- **Tối giản nhưng không nhàm chán** — lấy cảm hứng từ Coolmate.me & ZARA.com
- Khoảng trắng (whitespace) rộng rãi tạo cảm giác premium
- Hình ảnh sản phẩm là nhân vật chính, UI chỉ là khung đỡ
- Typography mạnh mẽ cho heading, nhẹ nhàng cho body

### 2.2. Mood Keywords

```
Clean · Confident · Trustworthy · Youthful · Vietnamese-friendly
```

### 2.3. Những gì KHÔNG làm

- ❌ Không dùng gradient mesh / aurora blob
- ❌ Không dùng dark theme + neon glow mặc định
- ❌ Không dùng glassmorphism quá mức
- ❌ Không dùng tông tím/violet làm màu chính
- ❌ Không dùng bố cục Bento Grid cho trang chính

---

## 3. Hệ thống màu sắc (Color System)

### 3.1. Bảng màu chính

| Token | Hex | Vai trò |
|-------|-----|---------|
| `--color-primary` | `#1A1A2E` | Nền header, text chính, nút CTA chính |
| `--color-secondary` | `#E8D5B7` | Accent ấm, nền highlight, badge |
| `--color-accent` | `#C1272D` | Nút mua hàng, giá sale, thông báo quan trọng |
| `--color-bg` | `#FAFAF8` | Nền trang chính (off-white ấm) |
| `--color-bg-alt` | `#F0EDE8` | Nền section xen kẽ, card background |
| `--color-text` | `#2D2D2D` | Text body chính |
| `--color-text-muted` | `#8C8C8C` | Text phụ, placeholder, caption |
| `--color-success` | `#2D8F4E` | Còn hàng, thanh toán thành công |
| `--color-warning` | `#E8A317` | Sắp hết hàng, voucher sắp hết hạn |
| `--color-border` | `#E5E5E0` | Viền card, divider |

### 3.2. Quy tắc 60-30-10

```
60% — Off-white background (#FAFAF8) + White cards
30% — Dark navy text & header (#1A1A2E)
10% — Warm accent (#E8D5B7) + Red CTA (#C1272D)
```

### 3.3. Lý do chọn bảng màu

- **Navy đậm (#1A1A2E)**: Tạo cảm giác đáng tin cậy, chuyên nghiệp, không gây mệt mắt như đen tuyệt đối
- **Off-white ấm (#FAFAF8)**: Nền sáng nhưng không chói, phù hợp với hình ảnh thời trang
- **Đỏ đậm (#C1272D)**: Tạo urgency cho nút mua hàng, phù hợp văn hóa Việt Nam (may mắn, năng lượng)
- **Vàng ấm (#E8D5B7)**: Điểm nhấn sang trọng, tạo cảm giác thời trang cao cấp

---

## 4. Typography

### 4.1. Font Stack

| Vai trò | Font | Fallback | Weight |
|---------|------|----------|--------|
| **Heading** | **Outfit** (Google Fonts) | sans-serif | 600, 700 |
| **Body** | **Inter** (Google Fonts) | system-ui, sans-serif | 400, 500 |
| **Price/Number** | **DM Mono** (Google Fonts) | monospace | 500 |

### 4.2. Type Scale (ratio 1.25 — Major Third)

| Token | Size | Line Height | Dùng cho |
|-------|------|-------------|----------|
| `--text-xs` | 12px | 1.4 | Caption, tag nhỏ |
| `--text-sm` | 14px | 1.5 | Label, helper text |
| `--text-base` | 16px | 1.6 | Body text, mô tả SP |
| `--text-lg` | 20px | 1.5 | Card title, sub-heading |
| `--text-xl` | 25px | 1.3 | Section heading |
| `--text-2xl` | 32px | 1.2 | Page title |
| `--text-3xl` | 40px | 1.1 | Hero heading |
| `--text-4xl` | 56px | 1.05 | Hero impact text |

### 4.3. Quy tắc

- Heading: Outfit Bold, letter-spacing -0.02em, uppercase cho navigation
- Body: Inter Regular 16px, max-width 65ch cho đoạn văn
- Giá: DM Mono Medium, giá gốc có line-through, giá sale dùng `--color-accent`

---

## 5. Layout & Grid

### 5.1. Container

```
Max-width: 1280px
Padding: 0 24px (mobile), 0 40px (tablet), 0 80px (desktop)
```

### 5.2. Product Grid

| Breakpoint | Columns | Gap |
|------------|---------|-----|
| Mobile (< 640px) | 2 cột | 12px |
| Tablet (640–1024px) | 3 cột | 16px |
| Desktop (> 1024px) | 4 cột | 24px |

### 5.3. Spacing Scale (8pt Grid)

```
4px · 8px · 12px · 16px · 24px · 32px · 48px · 64px · 80px · 120px
```

---

## 6. Components chính

### 6.1. Header / Navigation Bar

```
Mô tả cho Stitch:
"Sticky header with white background. Top bar shows free shipping notice 
and language toggle. Main nav has logo on left, category links in center 
(NAM, NỮ, TRẺ EM, BỘ SƯU TẬP, SALE), and utility icons on right 
(search, user account, wishlist heart, cart bag with item count badge). 
On scroll, header gets subtle bottom shadow. Search opens as full-width 
overlay with popular keywords and product suggestions. Mobile: hamburger 
menu on left, logo center, cart icon right."
```

**Chi tiết**:
- Chiều cao: 64px (desktop), 56px (mobile)
- Top announcement bar: 32px, nền `--color-primary`, text trắng
- Logo: Text-based "CLOTHY" bằng Outfit Bold 24px
- Nav links: Outfit Medium 14px, uppercase, letter-spacing 0.05em
- Hover: underline animation từ trái sang phải, 200ms ease-out
- Active state: underline cố định
- Cart badge: Hình tròn đỏ (`--color-accent`), text trắng, 18px

### 6.2. Hero Section (Homepage)

```
Mô tả cho Stitch:
"Full-width hero section with large lifestyle fashion photo on the right 
(70% width) and text content on the left (30% width) on a warm off-white 
background. Heading says 'Phong cách của bạn, câu chuyện của bạn' in 
large bold Outfit font. Subtext describes the new summer collection. 
Two buttons: 'Khám phá ngay' (primary dark button) and 'Xem bộ sưu tập' 
(outline button). Below hero, a horizontal strip shows 4 trust badges: 
free shipping, 15-day returns, genuine products, secure payment."
```

**Chi tiết**:
- Chiều cao: 80vh (desktop), auto (mobile)
- Heading: Outfit Bold 56px, color `--color-primary`
- CTA chính: Nền `--color-primary`, text trắng, padding 16px 32px, border-radius 4px
- CTA phụ: Border 1.5px `--color-primary`, text `--color-primary`, cùng kích thước
- Hover CTA: Scale 1.02, shadow nhẹ, transition 200ms
- Trust badges strip: Nền `--color-bg-alt`, 4 icon + text, flex row

### 6.3. Product Card

```
Mô tả cho Stitch:
"Product card with clean white background. Square product image on top 
with subtle rounded corners (8px). On image hover, show second product 
image with crossfade transition. Below image: product name in dark text 
(1 line, truncate), original price with strikethrough and sale price in 
red next to it, and small color dots showing available colors. 
A wishlist heart icon in top-right corner of image, and a 'SALE -30%' 
badge in top-left corner with red background."
```

**Chi tiết**:
- Border-radius: 8px
- Image: Aspect ratio 3:4, object-fit cover
- Hover: Đổi sang ảnh thứ 2 (crossfade 300ms), hiện nút "Thêm nhanh"
- Tên SP: Inter Medium 14px, 1 dòng, text-overflow ellipsis
- Giá gốc: Inter Regular 14px, line-through, color `--color-text-muted`
- Giá sale: Inter SemiBold 16px, color `--color-accent`
- Color dots: 16px tròn, border 1px `--color-border`, gap 6px
- Sale badge: Nền `--color-accent`, text trắng, font 12px bold, padding 4px 8px
- Quick-add overlay: Nền trắng, hiện selector size, slide-up 250ms

### 6.4. Product Detail Page

```
Mô tả cho Stitch:
"Two-column layout. Left column (55%) has product image gallery — main 
large image with thumbnail strip below it (5 thumbnails). Clicking 
thumbnail changes main image with fade transition. Right column (45%) 
has: breadcrumb navigation, product name (large heading), star rating 
with review count link, price display (original strikethrough + sale 
price), available vouchers shown as clickable tags ('Giảm 50K', 
'Giảm 10%'), color selector with circular swatches and selected color 
name, size selector as horizontal button group (XS S M L XL 2XL) with 
selected state highlighted, size guide link that opens modal, quantity 
selector with minus/plus buttons, 'THÊM VÀO GIỎ' primary button 
full-width, 'Thêm vào yêu thích' secondary outline button, and 
expandable accordion sections for product description, material info, 
care instructions, and shipping policy."
```

**Chi tiết**:
- Gallery: Main image 1:1 aspect, zoom on hover (scale 1.5 within container)
- Thumbnails: 64x64px, border 2px transparent → `--color-primary` khi active
- Star rating: 5 sao vàng (#F5A623), text "(128 đánh giá)" link
- Voucher tags: Nền `--color-bg-alt`, border dashed, font 13px
- Color swatch: 32px tròn, active = border 2px `--color-primary` + ring
- Size button: 48x48px, border 1px, active = nền `--color-primary` text trắng
- Size hết hàng: Opacity 0.3, strikethrough, cursor not-allowed
- Nút "THÊM VÀO GIỎ": Full width, height 52px, nền `--color-primary`, text trắng
- Sticky Buy Bar: Khi scroll qua nút mua → hiện bar cố định ở top

### 6.5. Shopping Cart (Giỏ hàng)

```
Mô tả cho Stitch:
"Cart page with two sections. Left section (65%) shows cart items list — 
each item has small product image, product name, selected size/color, 
unit price, quantity adjuster (minus/number/plus), line total, and remove 
button. Right section (35%) is order summary card with sticky position: 
subtotal, shipping fee, voucher code input with apply button, total 
amount in large bold text, and 'Thanh toán' checkout button. 
Mini-cart: a slide-in drawer from right side, 400px wide, with same 
item list and summary."
```

### 6.6. Checkout Page

```
Mô tả cho Stitch:
"Clean checkout page with two columns. Left (60%): step indicator 
(1. Địa chỉ → 2. Thanh toán → 3. Xác nhận), shipping address form 
with saved addresses as selectable cards, delivery options. 
Right (40%): sticky order summary showing items, subtotal, shipping, 
discount, total. Payment method selection as card options: COD with 
cash icon, VNPay with logo, MoMo with logo. Selected payment has 
highlighted border. Final 'Đặt hàng' button in red accent color."
```

### 6.7. User Account / Profile

```
Mô tả cho Stitch:
"Account page with left sidebar navigation (Thông tin cá nhân, Đơn hàng, 
Địa chỉ, Đổi mật khẩu, Yêu thích, Điểm thành viên) and right content 
area. Order history shows table/card list with order code, date, status 
badge (color-coded: yellow for pending, blue for processing, green for 
completed, red for cancelled), total amount, and detail link."
```

### 6.8. Admin Dashboard

```
Mô tả cho Stitch:
"Admin dashboard with dark sidebar on left (240px width, background 
#1A1A2E). Sidebar has logo on top, navigation items with icons 
(Dashboard, Sản phẩm, Đơn hàng, Khách hàng, Voucher, Banner, Blog, 
Thống kê), and user avatar at bottom. Main content area has light 
background with: top stats row (4 cards showing revenue, orders, 
customers, products with trend arrows), revenue chart (line/bar chart), 
recent orders table, and top selling products list."
```

---

## 7. Trạng thái & Tương tác (States & Interactions)

### 7.1. Button States

| State | Style |
|-------|-------|
| Default | Nền `--color-primary`, text trắng |
| Hover | Lighten 10%, scale 1.02, shadow `0 4px 12px rgba(26,26,46,0.15)` |
| Active/Pressed | Darken 5%, scale 0.98 |
| Disabled | Opacity 0.4, cursor not-allowed |
| Loading | Text replaced by spinner animation |

### 7.2. Input States

| State | Style |
|-------|-------|
| Default | Border 1px `--color-border`, border-radius 6px, height 44px |
| Focus | Border 2px `--color-primary`, subtle box-shadow |
| Error | Border 2px `--color-accent`, error message đỏ bên dưới |
| Filled | Border 1px `--color-text-muted` |

### 7.3. Micro-animations

| Element | Animation | Duration |
|---------|-----------|----------|
| Page transition | Fade in + slide up 8px | 300ms ease-out |
| Card hover | Lift shadow + image zoom | 200ms ease |
| Add to cart | Button pulse + cart icon bounce | 400ms |
| Toast notification | Slide in from top-right | 250ms ease-out |
| Skeleton loading | Shimmer gradient left→right | 1.5s infinite |
| Filter panel | Slide down accordion | 250ms ease |
| Image gallery | Crossfade | 300ms |
| Modal | Fade overlay + scale content 0.95→1 | 200ms |

---

## 8. Responsive Breakpoints

| Breakpoint | Width | Thay đổi chính |
|------------|-------|-----------------|
| **Mobile** | < 640px | 1-2 cột SP, hamburger menu, bottom tab bar, full-width buttons |
| **Tablet** | 640–1024px | 3 cột SP, sidebar ẩn, header thu gọn |
| **Desktop** | > 1024px | 4 cột SP, full header, sidebar visible (admin) |

---

## 9. Iconography

- **Icon set**: Lucide Icons (outline style, 24px default, stroke-width 1.5px)
- **Dùng cho**: Navigation (Search, User, Heart, ShoppingBag), Actions (Plus, Minus, Trash, Edit), Status (Check, X, AlertCircle, Clock)
- **Style**: Outline consistent, không mix filled và outline

---

## 10. Hình ảnh & Media

### 10.1. Product Photography Style

- Nền trắng hoặc nền sáng trung tính
- Ảnh chính: Sản phẩm flat-lay hoặc người mẫc mặc
- Ảnh phụ: Nhiều góc, chi tiết chất liệu, outfit phối đồ
- Aspect ratio: 3:4 cho card, 1:1 cho gallery detail
- Format: WebP, lazy loading

### 10.2. Banner / Hero

- Lifestyle photography, ánh sáng tự nhiên
- Overlay text phải đảm bảo contrast ratio ≥ 4.5:1
- Mỗi banner có CTA rõ ràng

---

## 11. Danh sách màn hình cần thiết kế (Screen List)

### 11.1. Public Screens (Khách vãng lai & KH)

| # | Màn hình | Mô tả ngắn |
|---|----------|-------------|
| 1 | **Homepage** | Hero banner, BST mới, SP nổi bật, SP bán chạy, blog preview |
| 2 | **Product Listing** | Grid SP + sidebar filter (size, màu, giá, chất liệu) + sort |
| 3 | **Product Detail** | Gallery ảnh, chọn size/màu, review, SP gợi ý |
| 4 | **Search Results** | Overlay search + trang kết quả |
| 5 | **Collection** | Trang bộ sưu tập với banner + danh sách SP |
| 6 | **Cart** | Danh sách SP trong giỏ + order summary |
| 7 | **Checkout** | Form địa chỉ + chọn thanh toán + xác nhận |
| 8 | **Login** | Form đăng nhập + nút Google OAuth |
| 9 | **Register** | Form đăng ký + xác thực email |
| 10 | **Blog List** | Danh sách bài viết dạng card grid |
| 11 | **Blog Post** | Nội dung bài viết, sidebar tag |

### 11.2. Authenticated Screens (KH đã đăng nhập)

| # | Màn hình | Mô tả ngắn |
|---|----------|-------------|
| 12 | **Profile** | Thông tin cá nhân, form chỉnh sửa |
| 13 | **Order History** | Danh sách đơn hàng + status badges |
| 14 | **Order Detail** | Chi tiết 1 đơn hàng, timeline trạng thái |
| 15 | **Wishlist** | Grid SP yêu thích |
| 16 | **Address Book** | Danh sách địa chỉ + CRUD |
| 17 | **Membership** | Hạng thành viên, điểm tích lũy, ưu đãi |

### 11.3. Admin Screens

| # | Màn hình | Mô tả ngắn |
|---|----------|-------------|
| 18 | **Dashboard** | Thống kê tổng quan, chart doanh thu |
| 19 | **Product Management** | CRUD SP, table + form modal |
| 20 | **Order Management** | Table đơn hàng, filter status, detail modal |
| 21 | **User Management** | Danh sách KH, khóa/mở, xem chi tiết |
| 22 | **Voucher Management** | CRUD voucher, trạng thái |
| 23 | **Banner Management** | Upload/sắp xếp banner |
| 24 | **Reports** | Charts doanh thu, SP bán chạy, tồn kho |

---

## 12. Prompts mẫu cho Google Stitch

### Prompt 1 — Homepage

```
Design a Vietnamese clothing e-commerce homepage. Clean, modern, minimal 
aesthetic with warm off-white background (#FAFAF8) and dark navy text 
(#1A1A2E). 

Header: white sticky navbar with text logo "CLOTHY" on left, navigation 
links "NAM NỮ TRẺ EM BỘ SƯU TẬP SALE" in center (uppercase, small), 
search/user/heart/cart icons on right.

Hero: large section with fashion lifestyle photo on right side and bold 
Vietnamese heading on left: "Phong cách của bạn" with dark CTA button 
"Khám phá ngay".

Below hero: horizontal trust badges strip (Free Ship, Đổi trả 15 ngày, 
Hàng chính hãng, Thanh toán bảo mật).

New arrivals section: heading "Hàng Mới Về" with 4-column product grid. 
Each card has product photo (3:4 ratio), product name, strikethrough 
original price and red sale price, small color dots.

Best sellers section with same grid layout.

Footer: dark navy background with 4 columns (about, customer support, 
policies, social media links).

Font: Outfit for headings, Inter for body. Accent color: warm red 
(#C1272D) for sale prices and CTA buttons.
```

### Prompt 2 — Product Detail Page

```
Design a product detail page for a clothing e-commerce site. Background 
#FAFAF8, text #1A1A2E, accent red #C1272D.

Two-column layout:
Left (55%): Large product image with 5 thumbnail images below.
Right (45%): Breadcrumb "Trang chủ > Nam > Áo Polo", product name 
"Áo Polo Nam Cotton Premium" in large bold text, 4.5 star rating with 
"(128 đánh giá)", original price "599.000₫" strikethrough and sale 
price "449.000₫" in red, voucher tags "Giảm 50K cho đơn 500K", 
color selector with 4 circular swatches, size selector buttons 
(S M L XL 2XL), quantity selector, full-width dark "THÊM VÀO GIỎ HÀNG" 
button, outline "Yêu thích" button with heart icon.

Below: accordion sections for description, material, care instructions.
Then: "Đánh giá sản phẩm" section with star filter and review cards.
Then: "Sản phẩm gợi ý" carousel with 4 product cards.

Font: Outfit headings, Inter body. Clean minimal style.
```

### Prompt 3 — Admin Dashboard

```
Design an admin dashboard for a clothing store management system. 

Dark sidebar (240px, background #1A1A2E) with white text logo "CLOTHY 
Admin" on top, navigation items with icons: Dashboard, Sản phẩm, 
Đơn hàng, Khách hàng, Voucher, Banner, Blog, Thống kê.

Main content area with light background (#FAFAF8):
Top row: 4 stat cards (Doanh thu tháng, Đơn hàng mới, Khách hàng, 
Sản phẩm) each with icon, number, and trend percentage.

Middle: Line chart showing monthly revenue trend.

Bottom row: Recent orders table (columns: Mã ĐH, Khách hàng, Ngày, 
Tổng tiền, Trạng thái with color badges, Actions) and Top selling 
products list with small thumbnails.

Font: Inter. Professional, data-focused design. Color accents: 
green for positive trends, red for negative.
```

---

## 13. Accessibility (Khả năng tiếp cận)

| Tiêu chí | Yêu cầu |
|----------|---------|
| Contrast ratio | ≥ 4.5:1 cho text, ≥ 3:1 cho UI lớn |
| Focus visible | Outline 2px `--color-primary` cho keyboard navigation |
| Alt text | Tất cả ảnh SP có alt text mô tả |
| Touch target | ≥ 44x44px cho mobile |
| Reduced motion | `prefers-reduced-motion` tắt animation |
| Semantic HTML | Dùng đúng `<nav>`, `<main>`, `<article>`, `<button>` |

---

## 14. Tham khảo thiết kế (Design References)

| Website | Học gì |
|---------|--------|
| [coolmate.me](https://coolmate.me) | Layout tối giản, product card, sticky buy bar |
| [yody.vn](https://yody.vn) | Navigation structure, category mega-menu |
| [zara.com](https://zara.com) | Hero photography, minimal product grid |
| [uniqlo.com](https://uniqlo.com) | Size selector UX, clean checkout flow |
| [cos.com](https://cos.com) | Typography hierarchy, whitespace usage |
