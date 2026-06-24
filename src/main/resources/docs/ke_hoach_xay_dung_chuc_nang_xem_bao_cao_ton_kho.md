# Ke hoach xay dung chuc nang xem bao cao ton kho

Tai lieu nay mo ta ke hoach trien khai chuc nang **nhan vien xem bao cao ton kho** theo UC-32 va bieu mau **BM3 - Bao cao ton kho**.

Nguyen tac trien khai:

- Bam sat dac ta hien co trong `Nhom10_FinalProject1.md`, `usecase_detail_khac.md`, `usecase_detail_sanpham.md` va bieu mau BM3.
- Phat trien tren code hien co, chi bo sung thanh phan moi khi can.
- Khong thay the cac phan da xay dung nhu `AdminReportController`, `AdminReportService`, cac bao cao BM1/BM2/BM5, luong dat hang/thanh toan va reservation ton kho.
- Khong thay doi nghiep vu tru ton kho. Chuc nang nay chi doc du lieu ton kho de lap bao cao.

## 1. Can cu tai lieu

### 1.1. UC-32 - Xem bao cao ton kho

Theo `usecase_detail_khac.md`:

| Thuoc tinh | Mo ta |
|---|---|
| Ma UC | UC-32 |
| Ten UC | Xem bao cao ton kho |
| Tac nhan chinh | Nhan vien |
| Mo ta | Nhan vien xem bao cao ton kho, canh bao san pham sap het hang |
| Tien dieu kien | Nhan vien da dang nhap |
| Hau dieu kien | Khong thay doi du lieu, chi doc |
| Quy dinh lien quan | QD6 |
| Bieu mau | BM3 |

Luong chinh:

```text
Nhan vien truy cap "Bao cao" -> "Ton kho"
-> He thong hien thi BM3
-> Highlight bien the co ton kho < 10
-> Nhan vien co the loc theo trang thai va danh muc
```

### 1.2. QD6 - Quy dinh ton kho

Theo `Nhom10_FinalProject1.md` va `usecase_detail_sanpham.md`:

- Ton kho tu dong giam khi dat hang/thanh toan thanh cong.
- Ton kho duoc hoan khi huy don theo quy dinh.
- Canh bao khi ton kho `< 10`.
- Staff co chuc nang cap nhat so luong ton kho va xem canh bao san pham sap het hang.

### 1.3. BM3 - Bao cao ton kho

Bieu mau BM3 trong tai lieu/anh mau:

| Ma SP | Ten san pham | Bien the (Size/Mau) | Ton kho | Trang thai |
|---|---|---|---:|---|
| SP001 | Ao Polo Nam Cotton | L / Trang | 5 | Sap het |
| SP001 | Ao Polo Nam Cotton | M / Den | 50 | Con hang |

## 2. Muc tieu

Xay dung API cho nhan vien xem bao cao ton kho theo BM3:

```http
GET /api/staff/reports/inventory
```

Ket qua tra ve danh sach bien the san pham voi cac cot:

- Ma SP.
- Ten san pham.
- Bien the `(Size/Mau)`.
- Ton kho.
- Trang thai.

Trang thai can tinh theo QD6:

| Dieu kien | Trang thai hien thi |
|---|---|
| `stockQuantity = 0` | Het hang |
| `0 < stockQuantity < 10` | Sap het |
| `stockQuantity >= 10` | Con hang |

`stockQuantity < 0` khong phai trang thai nghiep vu hop le. Neu xuat hien du lieu am, can xem la loi du lieu/loi cap nhat ton kho va xu ly bang validation hoac data integrity check, khong dua vao bao cao nhu mot trang thai binh thuong.

## 3. Pham vi chuc nang

Chuc nang nay bao gom:

- Lay danh sach ton kho theo tung `ProductVariant`.
- Join voi `Product` va `Category` de hien thi ten san pham, ma san pham va ho tro loc danh muc.
- Tinh trang thai ton kho theo QD6.
- Loc theo trang thai: `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`.
- Loc theo danh muc san pham.
- Phan trang va sap xep.
- Xuat CSV theo BM3 trong phase 1 de dap ung yeu cau "Tra cuu, Ket xuat" cua Staff.

Khong nam trong pham vi:

- Khong cap nhat ton kho.
- Khong tru/hoan ton kho.
- Khong thay doi `InventoryReservationServiceImpl`.
- Khong thay doi luong checkout, payment callback, order cancellation.
- Khong sua `AdminReportController` hien co de tranh anh huong BM1/BM2/BM5.

## 4. Hien trang code lien quan

### 4.1. Entity co san

`ProductVariant` da co du lieu can thiet:

```java
Product product;
String sku;
String size;
String color;
Integer stockQuantity;
Boolean isActive;
```

`Product` da co:

```java
Long id;
String name;
String slug;
Category category;
Boolean isActive;
OffsetDateTime deletedAt;
List<ProductVariant> variants;
```

`Category` da co:

```java
Long id;
String name;
String slug;
Category parent;
Boolean isActive;
```

### 4.2. Repository co san

`ProductVariantRepository` hien co:

```java
List<ProductVariant> findByProductId(Long productId);
Optional<ProductVariant> findByIdAndIsActiveTrue(Long id);
Optional<ProductVariant> findByProductIdAndSizeIgnoreCaseAndColorIgnoreCaseAndIsActiveTrue(...);
List<ProductVariant> findAllByIdInForUpdate(...);
```

Chuc nang bao cao chi can doc, nen khong dung lock `FOR UPDATE`.

### 4.3. Phan bao cao hien co

`AdminReportController` hien co:

```java
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
```

Controller nay dang phuc vu:

- BM1: doanh thu.
- BM2: san pham ban chay.
- BM5: khach hang than thiet.

Do UC-32 la chuc nang cua Staff, khong nen bo sung BM3 vao class nay neu muc tieu la cho nhan vien truy cap. Neu bo sung vao class nay thi se bi chan boi `hasRole('ADMIN')`, hoac phai sua security class-level, co nguy co anh huong cac bao cao admin da xay.

## 5. Huong thiet ke de khong thay the code hien co

Thay vi sua lon `AdminReportController`, bo sung cac thanh phan rieng cho BM3:

```text
controller.staff.StaffInventoryReportController
service.InventoryReportService
service.impl.InventoryReportServiceImpl
dto.response.InventoryReportResponse
dto.response.ReportExportDescriptor
dto.request.InventoryReportStatus
enums.ReportExportFormat
pattern.strategy.report.ReportExportStrategy
pattern.strategy.report.InventoryCsvExportStrategy
pattern.factory.report.ReportExporterFactory
pattern.template.report.InventoryCsvExporter
```

Ly do:

- Giu nguyen controller va service bao cao admin hien tai.
- Giu dung phan quyen Staff theo UC-32.
- Tai su dung Template Method dang co cho CSV va bo sung Strategy + Factory cho lop chon dinh dang export.
- Khong can tao bang moi.
- Khong tac dong luong ton kho cua don hang/thanh toan.

## 6. Pattern ap dung

| Pattern | Thanh phan ap dung | Vai tro |
|---|---|---|
| Proxy Pattern | `@PreAuthorize("hasRole('STAFF')")` tren controller Staff | Kiem soat quyen truy cap UC-32 |
| DTO Pattern | `InventoryReportResponse` | Dong goi du lieu BM3 tra ve client |
| Template Method Pattern | `InventoryCsvExporter extends CsvReportExporterTemplate` | Tai su dung khung xuat CSV hien co cho chuc nang export BM3 |
| Strategy Pattern | `ReportExportStrategy<T>` va `InventoryCsvExportStrategy` | Dong goi cach xuat tung dinh dang file, giup them Excel/PDF ma khong sua service chinh |
| Factory Pattern | `ReportExporterFactory` | Chon strategy export theo `ReportExportFormat` |
| Repository Query | `ProductVariantRepository` query read-only | Lay du lieu ton kho tu DB |

Ghi chu: neu muon Admin cung xem duoc BM3, co the them endpoint rieng cho Admin sau. Khong nen ha quyen hoac doi `@PreAuthorize` cua `AdminReportController` hien co trong giai doan nay.

## 7. API de xuat

### 7.1. Xem bao cao ton kho

```http
GET /api/staff/reports/inventory
Authorization: Bearer <staff_access_token>
```

Query params:

| Param | Bat buoc | Mo ta |
|---|---:|---|
| `status` | Khong | `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK` |
| `categoryId` | Khong | Loc theo danh muc |
| `keyword` | Khong | Tim theo ten san pham, ma san pham hien thi hoac SKU bien the |
| `page` | Khong | Mac dinh `0` |
| `size` | Khong | Mac dinh `20` |
| `sortBy` | Khong | Mac dinh `stockAsc`, service map sang `Sort` noi bo |

Vi du:

```http
GET /api/staff/reports/inventory?status=LOW_STOCK&categoryId=3&page=0&size=20
```

Response:

```json
{
  "status": 200,
  "message": "Lay bao cao ton kho thanh cong",
  "data": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "content": [
      {
        "productCode": "SP001",
        "productName": "Ao Polo Nam Cotton",
        "variantInfo": "L / Trang",
        "stockQuantity": 5,
        "status": "LOW_STOCK",
        "statusLabel": "Sap het",
        "categoryId": 1,
        "categoryName": "Ao Polo",
        "variantId": 10,
        "sku": "SP001-L-TRANG"
      }
    ]
  }
}
```

Response phan trang phai dung DTO chung hien co:

```text
dto.response.PageResponse<T>
-> ke thua PageResponseAbstract
-> gom pageNumber, pageSize, totalPages, totalElements, content
```

Khong tao DTO phan trang moi cho BM3.

### 7.2. Xuat bao cao CSV

Quyet dinh: **co lam CSV trong pham vi trien khai dau tien**.

Ly do:

- Bang chuc nang Staff ghi "Xem bao cao ton kho" la cong viec "Tra cuu, Ket xuat".
- Cac bao cao hien co da co huong Template Method cho CSV.
- CSV du gon cho BM3, khong mo rong sang Excel/PDF trong giai doan nay.

```http
GET /api/staff/reports/inventory/export
Authorization: Bearer <staff_access_token>
```

Query params export:

```http
GET /api/staff/reports/inventory/export?status=LOW_STOCK&categoryId=3&sortBy=stockAsc&format=CSV
```

Export nhan `status`, `categoryId`, `keyword`, `sortBy`, `format`. Phase 1 chi ho tro `format=CSV`. Export khong dung `page`/`size`; service xuat toan bo du lieu khop filter. De tranh file qua lon, giai doan dau gioi han toi da 10.000 dong va ghi log neu vuot nguong.

Response:

```text
Content-Type: text/csv; charset=UTF-8
Content-Disposition: attachment; filename=inventory_report.csv
```

CSV header theo BM3:

```csv
Ma SP,Ten san pham,Bien the (Size/Mau),Ton kho,Trang thai
```

## 8. DTO de xuat

### 8.1. `InventoryReportStatus`

Nen dat trong `dto.request` hoac `enums` tuy convention cua project. Neu trang thai chi dung cho filter/response bao cao, co the dat trong `dto.request`.

```java
public enum InventoryReportStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}
```

Mapping label:

```text
IN_STOCK -> Con hang
LOW_STOCK -> Sap het
OUT_OF_STOCK -> Het hang
```

### 8.2. `InventoryReportResponse`

```java
public record InventoryReportResponse(
        String productCode,
        String productName,
        String variantInfo,
        Integer stockQuantity,
        InventoryReportStatus status,
        String statusLabel,
        Long categoryId,
        String categoryName,
        Long variantId,
        String sku
) implements Serializable {}
```

Ghi chu ve `productCode`:

- Entity `Product` hien chua co cot `productCode`.
- BM3 yeu cau cot "Ma SP", khong phai "SKU bien the".
- Giai doan dau khong nen sua schema chi de them ma SP.
- Co the tinh ma hien thi tu `product.id`, vi du `SP%03d`.
- Van tra them `sku` de frontend/debug co ma bien the that neu can.
- Neu sau nay bo sung `Product.productCode`, chi can sua mapper, khong doi API BM3.

### 8.3. `ReportExportFormat`

Phase 1 chi ho tro CSV, nhung van khai bao enum de endpoint va factory co duong mo rong ro rang.

```java
public enum ReportExportFormat {
    CSV
}
```

Neu client truyen format khac `CSV`, he thong tra HTTP 400 trong phase 1.

### 8.4. `ReportExportDescriptor`

DTO nay giup controller lay metadata response truoc khi ghi file vao output stream.

```java
public record ReportExportDescriptor(
        String contentType,
        String fileName
) {}
```

Voi CSV BM3:

```text
contentType = text/csv
fileName = inventory_report.csv
```

## 9. Controller de xuat

### 9.1. Class

```java
package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

@RestController
@RequestMapping("/api/staff/reports/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffInventoryReportController {
    private final InventoryReportService inventoryReportService;
}
```

### 9.2. Ham `getInventoryReport`

```java
@GetMapping
public ApiResponse getInventoryReport(
        @RequestParam(required = false) InventoryReportStatus status,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "stockAsc") String sortBy
) {
    PageResponse<InventoryReportResponse> response =
            inventoryReportService.getInventoryReport(status, categoryId, keyword, page, size, sortBy);

    return ApiResponse.builder()
            .status(HttpStatus.OK.value())
            .message("Lay bao cao ton kho thanh cong")
            .data(response)
            .timestamp(OffsetDateTime.now())
            .build();
}
```

### 9.3. Ham `exportInventoryReport`

```java
@GetMapping("/export")
public void exportInventoryReport(
        @RequestParam(required = false) InventoryReportStatus status,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "stockAsc") String sortBy,
        @RequestParam(defaultValue = "CSV") ReportExportFormat format,
        HttpServletResponse response
) throws IOException {
    ReportExportDescriptor descriptor = inventoryReportService.describeInventoryExport(format);
    response.setContentType(descriptor.contentType());
    response.setCharacterEncoding("UTF-8");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + descriptor.fileName());

    inventoryReportService.exportInventoryReport(
            response.getOutputStream(),
            status,
            categoryId,
            keyword,
            sortBy,
            format
    );
}
```

## 10. Service de xuat

### 10.1. Interface

```java
public interface InventoryReportService {
    PageResponse<InventoryReportResponse> getInventoryReport(
            InventoryReportStatus status,
            Long categoryId,
            String keyword,
            int page,
            int size,
            String sortBy
    );

    ReportExportDescriptor describeInventoryExport(ReportExportFormat format);

    void exportInventoryReport(
            OutputStream outputStream,
            InventoryReportStatus status,
            Long categoryId,
            String keyword,
            String sortBy,
            ReportExportFormat format
    );
}
```

### 10.2. Implementation

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReportServiceImpl implements InventoryReportService {
    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductVariantRepository productVariantRepository;
    private final ReportExporterFactory reportExporterFactory;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryReportResponse> getInventoryReport(...) {
        validatePageRequest(page, size);
        StockRange stockRange = resolveStockRange(status);
        Sort sort = resolveSort(sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ProductVariant> variantPage =
                productVariantRepository.findInventoryReport(
                        categoryId,
                        normalizeKeyword(keyword),
                        stockRange.minStock(),
                        stockRange.maxStock(),
                        pageRequest
                );

        List<InventoryReportResponse> content = variantPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<InventoryReportResponse>builder()
                .pageNumber(variantPage.getNumber())
                .pageSize(variantPage.getSize())
                .totalElements(variantPage.getTotalElements())
                .totalPages(variantPage.getTotalPages())
                .content(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportExportDescriptor describeInventoryExport(ReportExportFormat format) {
        ReportExportStrategy<InventoryReportResponse> exporter =
                reportExporterFactory.getInventoryReportExporter(format);
        return new ReportExportDescriptor(exporter.contentType(), exporter.fileName());
    }

    @Override
    @Transactional(readOnly = true)
    public void exportInventoryReport(...) {
        StockRange stockRange = resolveStockRange(status);
        Pageable exportLimit = PageRequest.of(0, 10_000, resolveSort(sortBy));
        List<InventoryReportResponse> data = productVariantRepository
                .findInventoryReport(
                        categoryId,
                        normalizeKeyword(keyword),
                        stockRange.minStock(),
                        stockRange.maxStock(),
                        exportLimit
                )
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        ReportExportStrategy<InventoryReportResponse> exporter =
                reportExporterFactory.getInventoryReportExporter(format);
        exporter.export(outputStream, data);
    }
}
```

### 10.3. Quy dinh phan trang bat buoc bam theo code hien co

Project da co DTO phan trang chung:

```java
public class PageResponse<T> extends PageResponseAbstract {
    private List<T> content;
}
```

`PageResponseAbstract` gom:

```java
private int pageNumber;
private int pageSize;
private long totalPages;
private long totalElements;
```

Vi vay chuc nang BM3 phai:

- Tra ve `PageResponse<InventoryReportResponse>`, khong tao response phan trang moi.
- Dung builder cua `PageResponse` nhu `ProductServiceImpl` va `WishlistServiceImpl`.
- Controller nhan `page`, `size`, `sortBy` bang `@RequestParam`, service tao `PageRequest`.
- Response JSON dung ten field `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `content`.
- Neu can them thong tin tong hop nhu tong so mat hang sap het, tao endpoint summary rieng hoac field rieng ngoai page sau nay; khong chen vao `PageResponse` hien co.

Validation:

| Tham so | Quy dinh | Loi neu vi pham |
|---|---|---|
| `page` | `page >= 0` | HTTP 400 |
| `size` | `1 <= size <= 100` | HTTP 400 |
| `sortBy` | chi nhan cac gia tri trong bang ben duoi | HTTP 400 |

`size` mac dinh la 20. Gioi han 100 giup API bao cao khong tra qua nhieu bien the trong mot request.

Danh sach `sortBy` duoc chap nhan:

| `sortBy` | Sort tuong ung | Ghi chu |
|---|---|---|
| `stockAsc` | `stockQuantity ASC`, `id ASC` | Mac dinh, de thay hang sap het truoc |
| `stockDesc` | `stockQuantity DESC`, `id ASC` | Xem hang ton nhieu |
| `productNameAsc` | `product.name ASC`, `size ASC`, `color ASC`, `id ASC` | Sap theo ten SP |
| `productNameDesc` | `product.name DESC`, `size ASC`, `color ASC`, `id ASC` | Sap theo ten SP giam dan |
| `updatedDesc` | `updatedAt DESC`, `id ASC` | Bien the moi cap nhat gan nhat |
| `skuAsc` | `sku ASC`, `id ASC` | Tim/doi chieu SKU |

Khong truyen truc tiep gia tri `sortBy` cua client vao `Sort.by(...)`. Service phai map bang `switch` de tranh sort theo field khong mong muon.

Pseudo code validation/sort:

```java
private void validatePageRequest(int page, int size) {
    if (page < 0) {
        throw new IllegalArgumentException("page must be greater than or equal to 0");
    }
    if (size < 1 || size > 100) {
        throw new IllegalArgumentException("size must be between 1 and 100");
    }
}

private Sort resolveSort(String sortBy) {
    String normalized = (sortBy == null || sortBy.isBlank()) ? "stockAsc" : sortBy.trim();

    return switch (normalized) {
        case "stockAsc" -> Sort.by(Sort.Direction.ASC, "stockQuantity").and(Sort.by("id"));
        case "stockDesc" -> Sort.by(Sort.Direction.DESC, "stockQuantity").and(Sort.by("id"));
        case "productNameAsc" -> Sort.by(Sort.Direction.ASC, "product.name")
                .and(Sort.by(Sort.Direction.ASC, "size"))
                .and(Sort.by(Sort.Direction.ASC, "color"))
                .and(Sort.by("id"));
        case "productNameDesc" -> Sort.by(Sort.Direction.DESC, "product.name")
                .and(Sort.by(Sort.Direction.ASC, "size"))
                .and(Sort.by(Sort.Direction.ASC, "color"))
                .and(Sort.by("id"));
        case "updatedDesc" -> Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by("id"));
        case "skuAsc" -> Sort.by(Sort.Direction.ASC, "sku").and(Sort.by("id"));
        default -> throw new IllegalArgumentException("Unsupported sortBy: " + sortBy);
    };
}
```

Ham tao response phan trang nen theo dung mau:

```java
return PageResponse.<InventoryReportResponse>builder()
        .pageNumber(variantPage.getNumber())
        .pageSize(variantPage.getSize())
        .totalElements(variantPage.getTotalElements())
        .totalPages(variantPage.getTotalPages())
        .content(content)
        .build();
```

## 11. Repository de xuat

Bo sung query read-only vao `ProductVariantRepository`.

Khong sua cac method lock/reservation hien co.

### 11.1. Quyet dinh cach viet repository

Repository phai viet cung phong cach voi cac repository hien co:

- Dung interface co san `ProductVariantRepository extends JpaRepository<ProductVariant, Long>`.
- Them method vao repository hien co, khong tao repository moi cho bao cao.
- Dung JPQL `@Query` text block nhu `ProductRepository` va `ProductVariantRepository`.
- Dung `@Param` ro rang cho tung tham so.
- Dung `Page<ProductVariant>` va `Pageable` nhu cac query phan trang hien co.
- Khong dung native SQL neu JPQL dap ung duoc.
- Khong dung query string tu ghep bang tay trong service.
- Khong dung `join fetch` voi query phan trang de tranh loi count query; thay vao do join binh thuong trong query va de mapper doc quan he `product/category`. Neu can toi uu N+1 sau nay, xem xet `@EntityGraph` rieng, khong thay doi nghiep vu.

Query chinh:

```java
@Query("""
        select pv
        from ProductVariant pv
        join pv.product p
        join p.category c
        where pv.isActive = true
          and p.isActive = true
          and (:categoryId is null or c.id = :categoryId)
          and (:minStock is null or pv.stockQuantity >= :minStock)
          and (:maxStock is null or pv.stockQuantity <= :maxStock)
          and (
              :keyword is null
              or lower(p.name) like lower(concat('%', :keyword, '%'))
              or lower(p.slug) like lower(concat('%', :keyword, '%'))
              or lower(pv.sku) like lower(concat('%', :keyword, '%'))
          )
        """)
Page<ProductVariant> findInventoryReport(
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword,
        @Param("minStock") Integer minStock,
        @Param("maxStock") Integer maxStock,
        Pageable pageable
);
```

### 11.2. Loc status trong database bang `minStock` / `maxStock`

Service khong loc status sau khi lay du lieu ve Java. Service chi chuyen `status` thanh khoang ton kho roi day vao repository:

```text
status = null       -> minStock = null, maxStock = null
OUT_OF_STOCK        -> minStock = 0,    maxStock = 0
LOW_STOCK           -> minStock = 1,    maxStock = 9
IN_STOCK            -> minStock = 10,   maxStock = null
```

Theo quy dinh nghiep vu, `OUT_OF_STOCK` nen la `stock_quantity = 0`. Dieu kien `stock_quantity < 0` chi nen dung cho test phat hien du lieu loi neu can, khong nam trong filter bao cao chuan.

Pseudo code:

```java
private StockRange resolveStockRange(InventoryReportStatus status) {
    if (status == null) {
        return new StockRange(null, null);
    }

    return switch (status) {
        case OUT_OF_STOCK -> new StockRange(0, 0);
        case LOW_STOCK -> new StockRange(1, LOW_STOCK_THRESHOLD - 1);
        case IN_STOCK -> new StockRange(LOW_STOCK_THRESHOLD, null);
    };
}
```

Voi cach nay database chi tra dung nhom can xem, phan trang tinh tren tap du lieu da loc.

## 12. Mapper va quy tac tinh trang thai

### 12.1. Tao `variantInfo`

```java
private String buildVariantInfo(ProductVariant variant) {
    return variant.getSize() + " / " + variant.getColor();
}
```

### 12.2. Tao `productCode`

```java
private String buildProductCode(Product product) {
    return "SP" + String.format("%03d", product.getId());
}
```

Ghi chu: day la ma hien thi tam thoi de dung BM3 trong khi `Product` chua co truong ma SP rieng.

### 12.3. Tinh `InventoryReportStatus`

```java
private InventoryReportStatus resolveStatus(Integer stockQuantity) {
    int stock = stockQuantity == null ? 0 : stockQuantity;

    if (stock < 0) {
        throw new IllegalStateException("Inventory stock quantity must not be negative");
    }

    if (stock == 0) {
        return InventoryReportStatus.OUT_OF_STOCK;
    }

    if (stock < LOW_STOCK_THRESHOLD) {
        return InventoryReportStatus.LOW_STOCK;
    }

    return InventoryReportStatus.IN_STOCK;
}
```

### 12.4. Label hien thi

```java
private String resolveStatusLabel(InventoryReportStatus status) {
    return switch (status) {
        case OUT_OF_STOCK -> "Het hang";
        case LOW_STOCK -> "Sap het";
        case IN_STOCK -> "Con hang";
    };
}
```

## 13. Export file: Strategy + Factory + Template Method

Quyet dinh: CSV la **bat buoc trong phase 1** cua UC-32.

Pham vi CSV:

- Chi xuat CSV, khong lam Excel/PDF.
- Dung cung filter voi API xem bao cao: `status`, `categoryId`, `keyword`, `sortBy`.
- Khong phan trang export; xuat toi da 10.000 dong dau tien theo `sortBy`.
- Header va cot dung BM3.
- Neu khong co du lieu, van tra file CSV co header.

### 13.1. Nguyen tac ap dung pattern

Khong goi truc tiep `new InventoryCsvExporter()` trong `InventoryReportServiceImpl`.

Thay vao do:

```text
InventoryReportServiceImpl
-> ReportExporterFactory
-> ReportExportStrategy<InventoryReportResponse>
-> InventoryCsvExportStrategy
-> InventoryCsvExporter extends CsvReportExporterTemplate
```

Vai tro:

| Pattern | File | Vai tro |
|---|---|---|
| Strategy | `ReportExportStrategy<T>` | Hop dong chung cho moi dinh dang export |
| Strategy | `InventoryCsvExportStrategy` | Strategy export BM3 dang CSV |
| Factory | `ReportExporterFactory` | Chon strategy theo `ReportExportFormat` |
| Template Method | `InventoryCsvExporter` | Ghi CSV theo khung `CsvReportExporterTemplate` da co |

### 13.2. Cay thu muc bo sung

Can bo sung cac file lien quan pattern theo cay sau:

```text
src/main/java/vn/hcmute/edu/dp/nhom10/backend
├── enums
│   └── ReportExportFormat.java
├── dto
│   └── response
│       └── ReportExportDescriptor.java
└── pattern
    ├── factory
    │   └── report
    │       └── ReportExporterFactory.java
    ├── strategy
    │   └── report
    │       ├── ReportExportStrategy.java
    │       └── InventoryCsvExportStrategy.java
    └── template
        └── report
            └── InventoryCsvExporter.java
```

`pattern.template.report.CsvReportExporterTemplate` da co san, chi bo sung `InventoryCsvExporter.java` vao cung package.

### 13.3. `ReportExportStrategy`

```java
public interface ReportExportStrategy<T> {
    ReportExportFormat supportFormat();
    String contentType();
    String fileName();
    void export(OutputStream outputStream, List<T> data);
}
```

### 13.4. `ReportExporterFactory`

Factory nhan danh sach strategy tu Spring, tao map theo format va tra strategy phu hop.

```java
@Component
public class ReportExporterFactory {
    private final Map<ReportExportFormat, ReportExportStrategy<InventoryReportResponse>> inventoryReportExporters;

    public ReportExporterFactory(List<ReportExportStrategy<InventoryReportResponse>> inventoryReportExporters) {
        this.inventoryReportExporters = inventoryReportExporters.stream()
                .collect(Collectors.toMap(ReportExportStrategy::supportFormat, Function.identity()));
    }

    public ReportExportStrategy<InventoryReportResponse> getInventoryReportExporter(ReportExportFormat format) {
        ReportExportFormat normalized = format == null ? ReportExportFormat.CSV : format;
        ReportExportStrategy<InventoryReportResponse> exporter = inventoryReportExporters.get(normalized);

        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export format: " + normalized);
        }

        return exporter;
    }
}
```

Phase 1 chi co `CSV`, nhung factory giup them Excel/PDF sau nay bang cach them strategy moi, khong sua service chinh.

### 13.5. `InventoryCsvExportStrategy`

Strategy nay la lop adapter nhe giua factory va template CSV.

```java
@Component
public class InventoryCsvExportStrategy implements ReportExportStrategy<InventoryReportResponse> {
    private final InventoryCsvExporter inventoryCsvExporter = new InventoryCsvExporter();

    @Override
    public ReportExportFormat supportFormat() {
        return ReportExportFormat.CSV;
    }

    @Override
    public String contentType() {
        return "text/csv";
    }

    @Override
    public String fileName() {
        return "inventory_report.csv";
    }

    @Override
    public void export(OutputStream outputStream, List<InventoryReportResponse> data) {
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            inventoryCsvExporter.export(writer, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export inventory CSV report", e);
        }
    }
}
```

### 13.6. `InventoryCsvExporter`

Tai su dung Template Method Pattern hien co:

```text
pattern.template.report.CsvReportExporterTemplate
```

```java
public class InventoryCsvExporter extends CsvReportExporterTemplate<InventoryReportResponse> {
    @Override
    protected void writeHeader(Writer writer) throws IOException {
        writer.write("Ma SP,Ten san pham,Bien the (Size/Mau),Ton kho,Trang thai");
        writer.write(System.lineSeparator());
    }

    @Override
    protected void writeRow(Writer writer, InventoryReportResponse item, int index) throws IOException {
        writeCsvRow(writer,
                item.productCode(),
                item.productName(),
                item.variantInfo(),
                item.stockQuantity(),
                item.statusLabel()
        );
    }
}
```

Neu `CsvReportExporterTemplate` hien tai chua expose helper `writeCsvRow`, co the lam theo cach cac exporter BM1/BM2/BM5 dang dung. Khong can sua template neu khong bat buoc.

### 13.7. Mo rong sau phase 1

Khi can them Excel/PDF:

- Bo sung enum value vao `ReportExportFormat`, vi du `XLSX`, `PDF`.
- Tao strategy moi trong `pattern.strategy.report`.
- Neu dinh dang moi can template rieng, bo sung vao `pattern.template.report` hoac package pattern phu hop.
- Khong sua `InventoryReportServiceImpl`, ngoai tru truong hop can quy tac nghiep vu moi cho du lieu export.

## 14. Bao mat va phan quyen

Theo UC-32, tac nhan la Staff:

```java
@PreAuthorize("hasRole('STAFF')")
```

Khong nen dung `AdminReportController` hien co vi class do dang:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Neu yeu cau nghiep vu sau nay muon Admin cung xem duoc BM3, co 2 cach an toan:

1. Doi controller Staff sang:

```java
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
```

2. Tao endpoint Admin rieng goi lai `InventoryReportService`.

Khong nen doi `AdminReportController` thanh `hasAnyRole('STAFF', 'ADMIN')`, vi nhu vay Staff co the truy cap BM1/BM2/BM5.

## 15. Luong xu ly

```text
Staff -> StaffInventoryReportController: GET /api/staff/reports/inventory
Controller -> InventoryReportService: getInventoryReport(status, categoryId, keyword, page, size, sortBy)
Service -> PageRequest: tao PageRequest.of(page, size, resolveSort(sortBy))
Service -> ProductVariantRepository: query product variants read-only
Repository -> Database: select product_variants join products join categories
Service -> Mapper: tinh productCode, variantInfo, status, statusLabel
Service -> PageResponse builder: pageNumber, pageSize, totalElements, totalPages, content
Service -> Controller: PageResponse<InventoryReportResponse>
Controller -> Staff: ApiResponse BM3
```

Luong export:

```text
Staff -> StaffInventoryReportController: GET /api/staff/reports/inventory/export
Controller -> InventoryReportService: describeInventoryExport(format)
Controller -> HttpServletResponse: set contentType va filename
Controller -> InventoryReportService: exportInventoryReport(outputStream, status, categoryId, keyword, sortBy, format)
Service -> PageRequest: tao PageRequest.of(0, 10000, resolveSort(sortBy))
Service -> ProductVariantRepository: query bang minStock/maxStock
Service -> ReportExporterFactory: getInventoryReportExporter(format)
Factory -> InventoryCsvExportStrategy: chon CSV strategy
InventoryCsvExportStrategy -> InventoryCsvExporter: write CSV theo BM3 bang Template Method
Controller -> Staff: inventory_report.csv
```

## 16. UI/Frontend goi y

Man hinh "Bao cao" -> "Ton kho" can hien thi bang BM3:

| Ma SP | Ten san pham | Bien the (Size/Mau) | Ton kho | Trang thai |
|---|---|---|---:|---|

Hanh vi:

- Dong `LOW_STOCK` nen highlight bang mau canh bao.
- Dong `OUT_OF_STOCK` nen highlight manh hon hoac badge "Het hang".
- Bo loc gom: danh muc, trang thai, tu khoa.
- Nut "Xuat bao cao" tai CSV.

## 17. Luong loi

### 17.1. Staff chua dang nhap

```text
Khong co token hoac token het han
-> HTTP 401
```

### 17.2. User khong phai Staff

```text
Role khong co STAFF
-> HTTP 403
```

### 17.3. Status filter khong hop le

```text
status=ABC
-> HTTP 400
-> Thong bao: Trang thai ton kho khong hop le
```

### 17.4. Category khong ton tai

Co 2 cach:

- Cach don gian: query tra danh sach rong.
- Cach chat che: kiem tra category truoc, neu khong ton tai tra HTTP 404.

De giam query phu va phu hop bao cao, giai doan dau nen tra danh sach rong neu `categoryId` khong khop du lieu nao.

### 17.5. Khong co du lieu

```text
Tra ve PageResponse rong
Khong xem la loi
```

### 17.6. Format export khong ho tro

```text
format=XLSX trong phase 1
-> HTTP 400
-> Thong bao: Dinh dang xuat bao cao khong duoc ho tro
```

## 18. Thu tu trien khai de xuat

1. Tao enum `InventoryReportStatus`.
2. Tao enum `ReportExportFormat`.
3. Tao DTO `InventoryReportResponse`.
4. Tao DTO `ReportExportDescriptor`.
5. Bo sung query read-only trong `ProductVariantRepository`.
6. Tao `ReportExportStrategy` trong `pattern.strategy.report`.
7. Tao `InventoryCsvExporter` trong `pattern.template.report`.
8. Tao `InventoryCsvExportStrategy` trong `pattern.strategy.report`.
9. Tao `ReportExporterFactory` trong `pattern.factory.report`.
10. Tao `InventoryReportService`.
11. Tao `InventoryReportServiceImpl`.
12. Tao mapper tinh `productCode`, `variantInfo`, `status`, `statusLabel`.
13. Tao `StaffInventoryReportController` trong package `controller.staff`.
14. Viet unit test cho service mapping va filter status.
15. Viet unit test cho `ReportExporterFactory` va `InventoryCsvExportStrategy`.
16. Viet controller test cho API Staff.
17. Viet test CSV export.
18. Chay `mvn clean test`.

## 19. Test case can co

### 19.1. Test tinh trang thai

- `stockQuantity = 0` -> `OUT_OF_STOCK`, label `Het hang`.
- `stockQuantity = 5` -> `LOW_STOCK`, label `Sap het`.
- `stockQuantity = 9` -> `LOW_STOCK`, label `Sap het`.
- `stockQuantity = 10` -> `IN_STOCK`, label `Con hang`.
- `stockQuantity = 50` -> `IN_STOCK`, label `Con hang`.

### 19.2. Test API xem bao cao

- Staff xem danh sach ton kho thanh cong.
- Loc `LOW_STOCK` chi tra bien the ton kho 1-9.
- Loc `OUT_OF_STOCK` chi tra bien the ton kho = 0.
- Loc `IN_STOCK` chi tra bien the ton kho >= 10.
- Loc theo `categoryId`.
- Tim theo keyword ten san pham.
- Tim theo SKU.
- Ket qua phan trang dung DTO chung: `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `content`.
- Service dung `PageResponse.<InventoryReportResponse>builder()` thay vi tao DTO moi.
- Loc status phai duoc day xuong database bang `minStock/maxStock`; khong loc bang stream sau khi query.
- `page < 0` tra HTTP 400.
- `size < 1` hoac `size > 100` tra HTTP 400.
- `sortBy` ngoai whitelist tra HTTP 400.

### 19.3. Test bao mat

- Khong co token -> 401.
- Role customer -> 403.
- Role staff -> 200.

Neu quyet dinh cho admin xem BM3:

- Role admin -> 200.

### 19.4. Test export

- CSV co BOM UTF-8 neu template hien co dang dung BOM.
- Header dung BM3.
- Dong `LOW_STOCK` co label `Sap het`.
- Gia tri co dau phay/ky tu dac biet duoc escape dung.
- `format=CSV` goi dung `InventoryCsvExportStrategy`.
- Format khong ho tro tra HTTP 400.
- `ReportExporterFactory` tra dung strategy theo `ReportExportFormat.CSV`.
- Service export khong goi truc tiep `InventoryCsvExporter`.

## 20. Anh huong den cac phan da co

Khong anh huong:

- `AdminReportController`: giu nguyen BM1/BM2/BM5 va quyen ADMIN.
- `AdminReportService`: giu nguyen.
- `OrderRepository`: khong can dung cho BM3.
- `InventoryReservationServiceImpl`: khong sua, vi bao cao chi doc `stockQuantity`.
- Luong checkout/thanh toan: khong sua.
- Luong cap nhat ton kho UC-16e: khong sua.

Thanh phan bo sung:

- Controller Staff moi.
- Service bao cao ton kho moi.
- DTO/enum moi.
- Query read-only moi.
- Strategy export moi cho BM3.
- Factory chon exporter theo format.
- CSV template exporter moi cho BM3.

## 21. Tieu chi chap nhan

Chuc nang dat yeu cau khi:

- Staff goi duoc API xem BM3.
- API tra dung cac cot: Ma SP, Ten san pham, Bien the `(Size/Mau)`, Ton kho, Trang thai.
- Bien the co ton kho `< 10` va `> 0` duoc danh dau `Sap het`.
- Bien the co ton kho `0` duoc danh dau `Het hang`.
- Bien the co ton kho `>= 10` duoc danh dau `Con hang`.
- Co the loc theo trang thai va danh muc.
- Co the export CSV qua Strategy + Factory.
- `format` khong ho tro bi tu choi ro rang.
- Chuc nang chi doc, khong lam thay doi DB.
- Cac bao cao admin hien co van hoat dong nhu cu.
- Test lien quan pass voi `mvn clean test`.

## 22. Ket luan

Chuc nang xem bao cao ton kho nen duoc trien khai nhu mot module bo sung cho Staff:

```text
StaffInventoryReportController
-> InventoryReportService
-> ProductVariantRepository read-only query
-> InventoryReportResponse theo BM3
-> ReportExporterFactory khi export file
-> ReportExportStrategy
-> InventoryCsvExporter theo Template Method
```

Cach lam nay bam dung UC-32, QD6 va BM3, dong thoi tranh thay the hay pha vo cac phan da xay dung trong phan he bao cao admin, dat hang, thanh toan va reservation ton kho.
