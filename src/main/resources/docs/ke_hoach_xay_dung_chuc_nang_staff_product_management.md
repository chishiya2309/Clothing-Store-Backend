# Ke hoach xay dung chuc nang Staff Product Management

## 1. Thong tin nhanh ve branch

- Branch: `feature/staff-product-management`
- Pham vi chinh: UC-15, UC-16, UC-16d, UC-16e
- Tac nhan: Nhan vien (`STAFF`)
- Tai lieu can cu:
  - `Nhom10_Project2.docx`
  - `usecase_detail_sanpham.md`
  - `project_structure.md`
  - Source hien co: `Product`, `ProductVariant`, `ProductImage`, `ProductRepository`, `ProductSpecification`, `StaffInventoryReportController`

## 2. Pham vi trong branch nay

Branch nay chi nen tap trung vao nhom chuc nang quan ly san pham cua nhan vien:

1. UC-15 - Them san pham moi
2. UC-16 - Cap nhat / xoa san pham
3. UC-16d - Tra cuu san pham trong trang quan tri
4. UC-16e - Cap nhat so luong ton kho

Khong nen gom vao branch nay:

- UC-16b - Quan ly danh muc san pham: nen tach `feature/staff-category-collection-management`
- UC-16c - Quan ly bo suu tap: nen tach `feature/staff-category-collection-management`
- UC-26 - Duyet / Xoa danh gia san pham: nen tach `feature/staff-review-moderation`

Ly do tach: danh muc, bo suu tap va review co domain, rule va endpoint rieng. Branch product management da du lon vi dung den `Product`, `ProductVariant`, `ProductImage`, ton kho, soft delete, validation va search.

## 3. Hien trang code lien quan

### 3.1. Entity co san

- `Product`
  - Co `name`, `slug`, `description`, `material`, `careInstructions`
  - Co `category`, `basePrice`, `salePrice`
  - Co `isActive`, `isFeatured`, `totalSold`, `averageRating`
  - Co `deletedAt` va `@SQLRestriction("deleted_at IS NULL")`
  - Quan he cascade voi `variants` va `images`

- `ProductVariant`
  - Co `sku`, `size`, `color`, `stockQuantity`, `additionalPrice`, `isActive`
  - Unique theo `sku`
  - Unique theo bo `product_id`, `size`, `color`

- `ProductImage`
  - Co `imageUrl`, `imageType`, `displayOrder`, `altText`
  - `imageType` dung enum `ImageType`: `main`, `thumbnail`, `gallery`

### 3.2. Service va API co san

- `ProductController`
  - Public read only: xem chi tiet, hang moi ve
- `GuestProductController`
  - Search, autocomplete, best sellers
- `ProductServiceImpl`
  - Dang phuc vu storefront, chi nen giu read-only/public behavior
- `StaffInventoryReportController`
  - Da co pattern va style cho endpoint staff

### 3.3. Repository co san

- `ProductRepository`
  - Da extend `JpaSpecificationExecutor<Product>`
  - Co query public cho active product
- `ProductVariantRepository`
  - Co query report ton kho
  - Co `findAllByIdInForUpdate` dung pessimistic lock
- `ProductSpecification`
  - Dang phuc vu search public, mac dinh chi lay `isActive`

## 4. Yeu cau nghiep vu rut ra tu `Nhom10_Project2.docx`

### 4.1. UC-15 - Them san pham moi

Nhan vien them san pham moi voi day du thong tin va it nhat 1 bien the.

Bat buoc:

- Nhan vien da dang nhap trang quan tri
- Nhap ten san pham, mo ta, chat lieu, danh muc, gia goc, gia ban
- Upload toi thieu 1 anh
- Them toi thieu 1 bien the: size, mau, so luong ton
- SKU tu sinh va khong trung
- Ten san pham khong trung trong cung danh muc
- Tao ma/slug san pham tu dong
- Luu CSDL va tra ve thong tin san pham vua tao

Loi can xu ly:

- Ten san pham da ton tai trong danh muc
- Chua co anh
- Chua co bien the
- SKU trung
- Danh muc khong ton tai hoac dang inactive
- Gia ban lon hon gia goc neu quy dinh gia sale khong cho phep
- Ton kho am

### 4.2. UC-16 - Cap nhat / Xoa san pham

Cap nhat:

- Nhan vien chon san pham tu danh sach va sua thong tin
- Ma/slug san pham nen readonly hoac thay doi co kiem soat
- Cho phep cap nhat ten, mo ta, gia, hinh anh, bien the
- Gia moi chi ap dung cho don hang moi

Xoa/An:

- Neu san pham chua co don hang: co the xoa vat ly
- Neu san pham da co don hang: chi xoa mem/an san pham
- San pham bi an khong hien thi tren storefront nhung van giu du lieu lich su

Quyet dinh de an toan voi code hien co:

- Mac dinh nen thuc hien soft delete/an san pham bang `isActive = false` va `deletedAt = now`.
- Chi hard delete khi chac chan khong co `OrderItem`, `CartItem`, `Wishlist`, `Review`, `CollectionProduct` lien quan. Neu chua co du repository de kiem tra toan bo quan he, khong nen hard delete trong phase dau.

### 4.3. UC-16d - Tra cuu san pham

Nhan vien tim san pham trong trang quan tri.

Can ho tro:

- Danh sach phan trang
- Loc theo ma/slug, ten, danh muc, trang thai
- Trang thai gom: hien thi, an, het hang
- Xem chi tiet gom thong tin san pham, bien the, ton kho, anh
- Tai lieu co nhac "lich su thay doi gia"; hien code chua co entity lich su gia, nen phase dau nen ghi nhan la chua co va khong fake du lieu

### 4.4. UC-16e - Cap nhat ton kho

Nhan vien cap nhat ton kho cho bien the san pham.

Can ho tro:

- Xem danh sach bien the voi so ton hien tai
- Nhap so ton moi
- Luu so ton
- Neu ton < 10, response tra canh bao "San pham sap het hang"
- Dung transaction va lock de tranh de len cap nhat khi co checkout/order dang xu ly

## 5. Endpoint de xuat

Dung prefix theo convention staff hien co:

```text
GET    /api/staff/products
GET    /api/staff/products/{id}
POST   /api/staff/products
PUT    /api/staff/products/{id}
DELETE /api/staff/products/{id}
PATCH  /api/staff/products/{id}/visibility
PATCH  /api/staff/products/{productId}/variants/{variantId}/stock
```

Ghi chu:

- `DELETE /api/staff/products/{id}` nen goi logic safe delete. Ten API la delete, nhung service co quyen soft delete neu san pham da co lien ket nghiep vu.
- `PATCH /visibility` huu ich khi nhan vien chi muon an/hien san pham ma khong xoa.
- Stock update nen dung `variantId` de tranh nhap nhang size/mau.

## 6. DTO de xuat

### 6.1. Request DTO

- `StaffProductSearchCriteria`
  - `keyword`
  - `categoryId`
  - `status`: `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`
  - `page`, `size`, `sortBy`, `sortDir` o controller request param, khong nhat thiet nam trong DTO

- `StaffCreateProductRequest`
  - `name`
  - `description`
  - `material`
  - `careInstructions`
  - `categoryId`
  - `basePrice`
  - `salePrice`
  - `isFeatured`
  - `images: List<StaffProductImageRequest>`
  - `variants: List<StaffProductVariantRequest>`

- `StaffUpdateProductRequest`
  - Giong create, nhung cho phep update toan bo snapshot product
  - Can quy uoc ro: danh sach `images` va `variants` la replace-all hay patch tung item
  - De don gian va nhat quan cho frontend, phase dau nen dung replace-all co id tuy chon

- `StaffProductImageRequest`
  - `id` optional khi update
  - `imageUrl`
  - `imageType`
  - `displayOrder`
  - `altText`

- `StaffProductVariantRequest`
  - `id` optional khi update
  - `size`
  - `color`
  - `stockQuantity`
  - `additionalPrice`
  - `isActive`

- `StaffUpdateStockRequest`
  - `stockQuantity`

- `StaffUpdateProductVisibilityRequest`
  - `isActive`

### 6.2. Response DTO

- `StaffProductListItemResponse`
  - `id`, `name`, `slug`
  - `categoryId`, `categoryName`
  - `basePrice`, `salePrice`
  - `isActive`, `isFeatured`
  - `totalStock`
  - `variantCount`
  - `thumbnailUrl`
  - `createdAt`, `updatedAt`

- `StaffProductDetailResponse`
  - Thong tin day du cua product
  - Danh sach anh
  - Danh sach bien the
  - `stockWarning` neu co bien the < 10

- `StaffProductVariantResponse`
  - `id`, `sku`, `size`, `color`, `stockQuantity`, `additionalPrice`, `isActive`
  - `lowStock`

- `StaffStockUpdateResponse`
  - `productId`, `variantId`, `sku`
  - `oldStockQuantity`, `newStockQuantity`
  - `lowStock`
  - `warningMessage`

## 7. Service de xuat

### 7.1. Interface

Tao `StaffProductService`:

```text
PageResponse<StaffProductListItemResponse> getProducts(...)
StaffProductDetailResponse getProductDetail(Long productId)
StaffProductDetailResponse createProduct(StaffCreateProductRequest request)
StaffProductDetailResponse updateProduct(Long productId, StaffUpdateProductRequest request)
StaffProductDetailResponse updateVisibility(Long productId, boolean active)
void deleteProduct(Long productId)
StaffStockUpdateResponse updateStock(Long productId, Long variantId, StaffUpdateStockRequest request)
```

### 7.2. Implementation

Tao `StaffProductServiceImpl`.

Quy tac:

- Tach khoi `ProductServiceImpl` vi `ProductServiceImpl` dang la storefront read service.
- Dung `@Transactional` cho create/update/delete/stock.
- Dung `@Transactional(readOnly = true)` cho list/detail.
- Khi update ton kho, lock variant bang query `PESSIMISTIC_WRITE`.
- Khi tao/cap nhat, validate truoc khi mutate entity.

## 8. Repository va query

### 8.1. ProductRepository can bo sung

De phuc vu staff:

- `Optional<Product> findById(...)` da co tu JPA, nhung `@SQLRestriction` se loai deleted.
- Can query kiem tra trung ten trong cung danh muc:

```text
existsByCategoryIdAndNameIgnoreCaseAndDeletedAtIsNull(...)
existsByCategoryIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(...)
```

Luu y: vi entity co `@SQLRestriction`, method Spring Data co the du, nhung nen viet query ro neu can bao gom/exclude soft-deleted product.

### 8.2. ProductVariantRepository can bo sung

- `existsBySkuIgnoreCase(String sku)`
- `Optional<ProductVariant> findByIdAndProductId(Long variantId, Long productId)`
- `Optional<ProductVariant> findByIdAndProductIdForUpdate(...)` neu can lock tung variant

### 8.3. ProductImageRepository

Hien chua thay repository rieng cho image. Neu replace images qua cascade tu `Product.images` thi co the chua can repository. Neu can query rieng thi tao sau.

### 8.4. Kiem tra san pham da co don hang

Can dung `OrderItemRepository` de dem lien ket:

- `boolean existsByProductId(Long productId)` hoac
- `boolean existsByVariantProductId(Long productId)`

Neu repository hien tai chua co, bo sung method query.

## 9. Design pattern nen ap dung

Tai lieu pattern chi tiet cua branch:

- `src/main/resources/docs/design-patterns/design_patterns_staff_product_management.md`

### 9.1. Nhom pattern bat buoc

Nhung pattern nen ap dung ngay trong phase implement:

1. Service Layer
   - Ap dung: `StaffProductService`
   - Vai tro: dinh nghia boundary nghiep vu cho Staff Product Management.

2. Facade
   - Ap dung: `StaffProductServiceImpl` hoac tach `StaffProductManagementFacade`
   - Vai tro: dieu phoi luong tao/cap nhat/xoa/tim kiem/cap nhat ton kho.

3. Repository
   - Ap dung: `ProductRepository`, `ProductVariantRepository`, `OrderItemRepository`
   - Vai tro: gom query persistence, khong de service chua query phuc tap.

4. Unit of Work
   - Ap dung: `@Transactional`
   - Vai tro: dam bao product, image, variant va stock duoc commit/rollback cung nhau.

5. Specification
   - Ap dung: `StaffProductSpecification`
   - Vai tro: loc staff product theo keyword, category, active/inactive, out-of-stock.

6. Builder
   - Ap dung: entity va response DTO builders.
   - Vai tro: tao object phuc tap ro rang, tranh constructor dai.

7. Factory Method / Simple Factory
   - Ap dung: `ProductIdentityFactory`
   - Vai tro: sinh slug, ma san pham, SKU unique.

### 9.2. Nhom pattern nen ap dung khi implement chi tiet

1. Chain of Responsibility
   - Ap dung: validation chain cho create/update product.
   - Vai tro: tach rule validate thanh tung handler rieng.

2. Strategy / Policy
   - Ap dung: `ProductDeletionPolicy`, `ProductVariantMergePolicy`, `ProductImageMergePolicy`.
   - Vai tro: tach chinh sach xoa mem/xoa that va merge image/variant.

3. State hoac State Resolver
   - Ap dung: `StaffProductStatus`, `ProductStockStateResolver`.
   - Vai tro: tinh trang thai `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`, `LOW_STOCK`.

4. Observer / Domain Event
   - Ap dung: `ProductPriceDroppedEvent` neu update sale price giam.
   - Vai tro: thong bao wishlist ma khong lam product service phinh to.

5. Cache-Aside Eviction
   - Ap dung: clear cache `newArrivals`, `bestSellers`, product listing/detail sau khi mutate.
   - Vai tro: tranh storefront hien du lieu cu.

### 9.3. Nhom pattern co dieu kien hoac phase sau

1. Adapter
   - Dung khi backend xu ly upload anh truc tiep len S3.

2. Retry / Circuit Breaker
   - Dung cho remote dependency nhu S3/email, khong retry tuy tien thao tac DB khong idempotent.

3. Command
   - Dung neu can audit/undo thao tac staff.

4. Memento
   - Dung neu trien khai lich su thay doi gia. Tai lieu UC-16d co nhac, nhung code hien chua co entity.

## 10. Validation rule chi tiet

### 10.1. Product

- `name`: not blank, gioi han do dai hop ly
- `categoryId`: bat buoc, category ton tai va active
- `basePrice`: bat buoc, > 0
- `salePrice`: optional, neu co thi >= 0 va nen <= `basePrice`
- `images`: it nhat 1
- `variants`: it nhat 1

### 10.2. Image

- `imageUrl`: not blank
- Phai co it nhat 1 anh `main` hoac neu khong co thi lay anh dau tien lam main/thumbnail theo quy uoc
- `displayOrder`: >= 0

### 10.3. Variant

- `size`: not blank, max 10
- `color`: not blank, max 50
- `stockQuantity`: >= 0
- `additionalPrice`: >= 0
- Trong cung request khong trung cap `size + color`
- SKU sinh ra phai unique

### 10.4. Delete

- Neu co lien ket voi order: soft delete/an
- Neu khong co lien ket: co the hard delete, nhung phase dau nen uu tien soft delete de an toan

## 11. Transaction va concurrency

### 11.1. Create product

- Transaction boundary: toan bo create product + images + variants
- Neu bat ky validation nao fail, rollback
- SKU generation can kiem tra unique truoc khi save

### 11.2. Update product

- Load product trong transaction
- Validate duplicate name trong category tru id hien tai
- Cap nhat scalar fields
- Reconcile images/variants
- Khong xoa variant da tung co order neu co lien ket; nen set `isActive = false`

### 11.3. Stock update

- Lock variant bang pessimistic write
- Validate variant thuoc product
- Cap nhat stock
- Tra warning neu stock < 10
- Can tranh conflict voi checkout/order reservation dang tru ton kho

## 12. API response convention

Controller nen tra `ApiResponse` nhu cac controller hien co:

- `status`
- `message`
- `data`
- `timestamp`

Message nen dung tieng Viet cho staff UI, vi cac controller staff hien co da co message tieng Viet o report.

## 13. Test plan

### 13.1. Unit test service

- Tao san pham thanh cong voi 1 image, 1 variant
- Loi khi trung ten trong cung danh muc
- Loi khi khong co image
- Loi khi khong co variant
- Loi khi stock am
- Update product thanh cong
- Soft delete product da co order
- Update stock thanh cong
- Update stock < 10 tra warning

### 13.2. Controller test

- Staff goi duoc endpoint
- Customer/anonymous bi chan
- Validate request body loi tra 400
- Duplicate/invalid business rule tra 409 hoac 400 theo exception hien co

### 13.3. Integration test neu kip

- Create product luu du product/images/variants
- Update product replace images/variants
- Stock update co lock, tranh lost update
- Search staff product loc active/inactive/out-of-stock

## 14. File du kien them moi

### 14.1. Cay thu muc file main source du kien

```text
src/main/java/vn/hcmute/edu/dp/nhom10/backend/
├── controller/
│   └── staff/
│       └── StaffProductController.java
├── dto/
│   ├── request/
│   │   ├── StaffCreateProductRequest.java
│   │   ├── StaffProductImageRequest.java
│   │   ├── StaffProductSearchCriteria.java
│   │   ├── StaffProductVariantRequest.java
│   │   ├── StaffUpdateProductRequest.java
│   │   ├── StaffUpdateProductVisibilityRequest.java
│   │   └── StaffUpdateStockRequest.java
│   └── response/
│       ├── StaffProductDetailResponse.java
│       ├── StaffProductImageResponse.java
│       ├── StaffProductListItemResponse.java
│       ├── StaffProductVariantResponse.java
│       └── StaffStockUpdateResponse.java
├── enums/
│   └── StaffProductStatus.java
├── event/
│   ├── ProductPriceDroppedEvent.java
│   └── listener/
│       └── ProductPriceDroppedEventListener.java
├── pattern/
│   ├── chain/
│   │   └── product/
│   │       ├── CategoryExistsValidationHandler.java
│   │       ├── ProductImageValidationHandler.java
│   │       ├── ProductNameUniqueValidationHandler.java
│   │       ├── ProductPriceValidationHandler.java
│   │       ├── ProductValidationContext.java
│   │       ├── ProductValidationHandler.java
│   │       └── ProductVariantValidationHandler.java
│   ├── factory/
│   │   └── product/
│   │       └── ProductIdentityFactory.java
│   ├── policy/
│   │   └── product/
│   │       ├── ProductDeletionDecision.java
│   │       ├── ProductDeletionPolicy.java
│   │       ├── ProductImageMergePolicy.java
│   │       └── ProductVariantMergePolicy.java
│   ├── specification/
│   │   └── StaffProductSpecification.java
│   └── state/
│       └── product/
│           └── ProductStockStateResolver.java
├── service/
│   ├── StaffProductService.java
│   └── impl/
│       └── StaffProductServiceImpl.java
└── repository/
    ├── ProductRepository.java          # chinh sua
    ├── ProductVariantRepository.java   # chinh sua
    └── OrderItemRepository.java        # chinh sua
```

Ghi chu:

- `ProductPriceDroppedEvent` va listener chi tao neu branch kich hoat thong bao wishlist khi gia giam.
- `ProductImageMergePolicy` va `ProductVariantMergePolicy` co the de phase update chi tiet neu phase dau chi replace-all don gian.
- `StaffProductStatus` co the dat trong `enums` de controller request param dung chung.

### 14.2. Cay thu muc file test du kien

```text
src/test/java/vn/hcmute/edu/dp/nhom10/backend/
├── controller/
│   └── staff/
│       └── StaffProductControllerTest.java
├── integration/
│   ├── StaffProductCreateIT.java
│   ├── StaffProductSearchIT.java
│   ├── StaffProductUpdateIT.java
│   └── StaffProductStockConcurrencyIT.java
├── pattern/
│   ├── chain/
│   │   └── product/
│   │       └── ProductValidationChainTest.java
│   ├── factory/
│   │   └── product/
│   │       └── ProductIdentityFactoryTest.java
│   ├── policy/
│   │   └── product/
│   │       └── ProductDeletionPolicyTest.java
│   ├── specification/
│   │   └── StaffProductSpecificationTest.java
│   └── state/
│       └── product/
│           └── ProductStockStateResolverTest.java
└── service/
    └── StaffProductServiceImplTest.java
```

### 14.3. Cay thu muc tai lieu du kien

```text
src/main/resources/docs/
├── ke_hoach_xay_dung_chuc_nang_staff_product_management.md
└── design-patterns/
    └── design_patterns_staff_product_management.md
```

## 15. File du kien chinh sua

- `ProductRepository.java`
- `ProductVariantRepository.java`
- `OrderItemRepository.java`
- `GlobalExceptionHandling.java` neu can exception moi
- `ProductSpecification.java` chi sua neu quyet dinh khong tao `StaffProductSpecification`

## 16. Checklist trien khai

### Phase 1 - Nen tang doc/plan

- [x] Tao branch `feature/staff-product-management`
- [x] Doc `Nhom10_Project2.docx`
- [x] Doi chieu `usecase_detail_sanpham.md`
- [x] Lap ke hoach chuc nang

### Phase 2 - Contract API

- [ ] Tao DTO request/response
- [ ] Tao `StaffProductController`
- [ ] Tao `StaffProductService`
- [ ] Viet controller test skeleton

### Phase 3 - Search/detail

- [ ] Tao staff search specification
- [ ] Bo sung repository query can thiet
- [ ] Implement `GET /api/staff/products`
- [ ] Implement `GET /api/staff/products/{id}`
- [ ] Test paging/filter/status

### Phase 4 - Create product

- [ ] Tao identity/SKU factory
- [ ] Implement validate create request
- [ ] Implement create product + images + variants
- [ ] Test success va cac loi UC-15

### Phase 5 - Update/delete

- [ ] Implement update product
- [ ] Implement visibility update
- [ ] Implement safe delete
- [ ] Test soft delete va validation UC-16

### Phase 6 - Stock update

- [ ] Bo sung query lock variant neu can
- [ ] Implement update stock
- [ ] Tra warning neu stock < 10
- [ ] Test concurrency co ban

### Phase 7 - Hoan thien

- [ ] Chay unit test lien quan
- [ ] Chay integration test neu co thay doi repository/transaction lon
- [ ] Cap nhat Swagger/OpenAPI annotation neu repo dang dung
- [ ] Cap nhat bao cao design pattern neu can

## 17. Rủi ro va quyet dinh can chot

1. Hard delete hay soft delete:
   - De xuat phase dau: soft delete/an de bao toan du lieu lich su.

2. Update variants la replace-all hay patch:
   - De xuat phase dau: replace-all co `id` optional, nhung khong hard delete variant da co lien ket order.

3. Lich su thay doi gia:
   - Tai lieu UC-16d co nhac lich su thay doi gia, code hien chua co entity. Khong nen fake. Neu bat buoc, tao branch/phase rieng them `ProductPriceHistory`.

4. Upload anh:
   - Backend hien co `S3Service`. Branch nay co the nhan `imageUrl` da upload san de giam scope, hoac bo sung endpoint upload rieng neu frontend can.

5. Price notification cho wishlist:
   - Da co Observer cho price/wishlist. Nen can nhac kich hoat khi sale price giam, nhung de phase sau neu branch qua lon.

## 18. Ket luan thiet ke

Branch `feature/staff-product-management` nen tap trung vao API quan ly san pham cua nhan vien, khong gom category/collection/review. Thiet ke nen dung service rieng lam Facade cho workflow staff, Builder khi tao entity, Factory cho slug/SKU, Specification cho tra cuu san pham, va lock transaction cho cap nhat ton kho. Cach nay bam dung `Nhom10_Project2.docx`, dong thoi ton trong kien truc va pattern hien co cua repository.
