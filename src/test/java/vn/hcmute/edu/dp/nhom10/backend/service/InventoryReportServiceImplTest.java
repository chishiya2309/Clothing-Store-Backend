package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ReportExportDescriptor;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;
import vn.hcmute.edu.dp.nhom10.backend.pattern.factory.report.ReportExporterFactory;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.report.ReportExportStrategy;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.InventoryReportServiceImpl;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryReportServiceImplTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ReportExporterFactory reportExporterFactory;

    @Mock
    private ReportExportStrategy<InventoryReportResponse> reportExportStrategy;

    @InjectMocks
    private InventoryReportServiceImpl inventoryReportService;

    @Test
    void getInventoryReport_stockZero_mapsOutOfStockAndPageResponse() {
        ProductVariant variant = variant(10L, 1L, "Áo Polo Nam Cotton", "Áo Polo", "L", "Trắng", 0);
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant));

        PageResponse<InventoryReportResponse> response = inventoryReportService.getInventoryReport(
                null, null, null, 0, 20, null);

        InventoryReportResponse item = response.getContent().get(0);
        assertEquals(0, response.getPageNumber());
        assertEquals(20, response.getPageSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals("SP001", item.productCode());
        assertEquals("Áo Polo Nam Cotton", item.productName());
        assertEquals("L / Trắng", item.variantInfo());
        assertEquals(InventoryReportStatus.OUT_OF_STOCK, item.status());
        assertEquals("Hết hàng", item.statusLabel());
    }

    @Test
    void getInventoryReport_lowStockRange_isAppliedInRepository() {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant(10L, 1L, "Áo Polo", "Áo Polo", "M", "Đen", 5)));

        inventoryReportService.getInventoryReport(InventoryReportStatus.LOW_STOCK, 3L, " polo ", 0, 10, "stockAsc");

        verify(productVariantRepository).findInventoryReport(
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq("polo"),
                org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq(9),
                any(Pageable.class));
    }

    @Test
    void getInventoryReport_outOfStockRange_isAppliedInRepository() {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant(10L, 1L, "Áo Polo", "Áo Polo", "M", "Đen", 0)));

        inventoryReportService.getInventoryReport(InventoryReportStatus.OUT_OF_STOCK, null, null, 0, 10, "stockAsc");

        verify(productVariantRepository).findInventoryReport(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(0),
                org.mockito.ArgumentMatchers.eq(0),
                any(Pageable.class));
    }

    @Test
    void getInventoryReport_inStockRange_isAppliedInRepository() {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant(10L, 1L, "Áo Polo", "Áo Polo", "M", "Đen", 10)));

        inventoryReportService.getInventoryReport(InventoryReportStatus.IN_STOCK, null, null, 0, 10, "stockAsc");

        verify(productVariantRepository).findInventoryReport(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(10),
                org.mockito.ArgumentMatchers.isNull(),
                any(Pageable.class));
    }

    @Test
    void getInventoryReport_blankKeyword_normalizesToNull() {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant(10L, 1L, "Áo Polo", "Áo Polo", "M", "Đen", 10)));

        inventoryReportService.getInventoryReport(null, null, "   ", 0, 10, "stockAsc");

        verify(productVariantRepository).findInventoryReport(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                any(Pageable.class));
    }

    @Test
    void getInventoryReport_sortByStockDesc_usesWhitelistedSort() {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant(10L, 1L, "Áo Polo", "Áo Polo", "M", "Đen", 10)));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        inventoryReportService.getInventoryReport(null, null, null, 0, 10, "stockDesc");

        verify(productVariantRepository).findInventoryReport(any(), any(), any(), any(), pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("stockQuantity").getDirection());
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("id").getDirection());
    }

    @Test
    void getInventoryReport_variantInfo_omitsNullParts() {
        ProductVariant variant = variant(10L, 1L, "Áo Polo", "Áo Polo", null, "Đen", 10);
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant));

        PageResponse<InventoryReportResponse> response = inventoryReportService.getInventoryReport(
                null, null, null, 0, 10, "stockAsc");

        assertEquals("Đen", response.getContent().get(0).variantInfo());
    }

    @Test
    void getInventoryReport_negativeStock_throws() {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant(10L, 1L, "Áo Polo", "Áo Polo", "M", "Đen", -1)));

        assertThrows(IllegalStateException.class,
                () -> inventoryReportService.getInventoryReport(null, null, null, 0, 10, "stockAsc"));
    }

    @Test
    void getInventoryReport_nullStock_throws() {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageOf(variant(10L, 1L, "Áo Polo", "Áo Polo", "M", "Đen", null)));

        assertThrows(IllegalStateException.class,
                () -> inventoryReportService.getInventoryReport(null, null, null, 0, 10, "stockAsc"));
    }

    @Test
    void getInventoryReport_invalidPage_throwsBeforeRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReportService.getInventoryReport(null, null, null, -1, 10, "stockAsc"));

        verify(productVariantRepository, never()).findInventoryReport(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getInventoryReport_invalidSize_throwsBeforeRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReportService.getInventoryReport(null, null, null, 0, 101, "stockAsc"));

        verify(productVariantRepository, never()).findInventoryReport(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getInventoryReport_invalidSort_throwsBeforeRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReportService.getInventoryReport(null, null, null, 0, 10, "stockQuantity"));

        verify(productVariantRepository, never()).findInventoryReport(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void describeInventoryExport_usesFactoryMetadata() {
        when(reportExporterFactory.getInventoryReportExporter(ReportExportFormat.CSV))
                .thenReturn(reportExportStrategy);
        when(reportExportStrategy.contentType()).thenReturn("text/csv");
        when(reportExportStrategy.fileName()).thenReturn("inventory_report.csv");

        ReportExportDescriptor descriptor = inventoryReportService.describeInventoryExport(ReportExportFormat.CSV);

        assertEquals("text/csv", descriptor.contentType());
        assertEquals("inventory_report.csv", descriptor.fileName());
        verify(reportExporterFactory).getInventoryReportExporter(ReportExportFormat.CSV);
    }

    @Test
    void exportInventoryReport_lowStock_usesFiltersLimitAndFactoryStrategy() throws Exception {
        ProductVariant variant = variant(10L, 1L, "Áo Polo Nam Cotton", "Áo Polo", "L", "Trắng", 5);
        Page<ProductVariant> page = new PageImpl<>(List.of(variant), PageRequest.of(0, 10_000), 10_001);
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(reportExporterFactory.getInventoryReportExporter(ReportExportFormat.CSV))
                .thenReturn(reportExportStrategy);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<List<InventoryReportResponse>> dataCaptor = ArgumentCaptor.forClass(List.class);

        inventoryReportService.exportInventoryReport(
                new ByteArrayOutputStream(),
                InventoryReportStatus.LOW_STOCK,
                3L,
                " polo ",
                "stockAsc",
                ReportExportFormat.CSV
        );

        verify(productVariantRepository).findInventoryReport(
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq("polo"),
                org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq(9),
                pageableCaptor.capture());
        assertEquals(10_000, pageableCaptor.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC, pageableCaptor.getValue().getSort().getOrderFor("stockQuantity").getDirection());
        verify(reportExporterFactory).getInventoryReportExporter(ReportExportFormat.CSV);
        verify(reportExportStrategy).export(any(ByteArrayOutputStream.class), dataCaptor.capture());
        assertEquals(1, dataCaptor.getValue().size());
        assertEquals("SP001", dataCaptor.getValue().get(0).productCode());
        assertEquals(InventoryReportStatus.LOW_STOCK, dataCaptor.getValue().get(0).status());
    }

    @Test
    void exportInventoryReport_outOfStockRange_isAppliedInRepository() throws Exception {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10_000), 0));
        when(reportExporterFactory.getInventoryReportExporter(ReportExportFormat.CSV))
                .thenReturn(reportExportStrategy);

        inventoryReportService.exportInventoryReport(
                new ByteArrayOutputStream(),
                InventoryReportStatus.OUT_OF_STOCK,
                null,
                null,
                "stockAsc",
                ReportExportFormat.CSV
        );

        verify(productVariantRepository).findInventoryReport(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(0),
                org.mockito.ArgumentMatchers.eq(0),
                any(Pageable.class));
        verify(reportExportStrategy).export(any(ByteArrayOutputStream.class), org.mockito.ArgumentMatchers.eq(List.of()));
    }

    @Test
    void exportInventoryReport_inStockRange_isAppliedInRepository() throws Exception {
        when(productVariantRepository.findInventoryReport(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10_000), 0));
        when(reportExporterFactory.getInventoryReportExporter(ReportExportFormat.CSV))
                .thenReturn(reportExportStrategy);

        inventoryReportService.exportInventoryReport(
                new ByteArrayOutputStream(),
                InventoryReportStatus.IN_STOCK,
                null,
                null,
                "stockAsc",
                ReportExportFormat.CSV
        );

        verify(productVariantRepository).findInventoryReport(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(10),
                org.mockito.ArgumentMatchers.isNull(),
                any(Pageable.class));
        verify(reportExportStrategy).export(any(ByteArrayOutputStream.class), org.mockito.ArgumentMatchers.eq(List.of()));
    }

    @Test
    void exportInventoryReport_invalidSort_throwsBeforeRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryReportService.exportInventoryReport(
                        new ByteArrayOutputStream(),
                        null,
                        null,
                        null,
                        "stockQuantity",
                        ReportExportFormat.CSV
                ));

        verify(productVariantRepository, never()).findInventoryReport(any(), any(), any(), any(), any(Pageable.class));
        verify(reportExporterFactory, never()).getInventoryReportExporter(any());
    }

    private Page<ProductVariant> pageOf(ProductVariant variant) {
        return new PageImpl<>(List.of(variant), PageRequest.of(0, 20), 1);
    }

    private ProductVariant variant(
            Long variantId,
            Long productId,
            String productName,
            String categoryName,
            String size,
            String color,
            Integer stockQuantity
    ) {
        Category category = new Category();
        category.setId(5L);
        category.setName(categoryName);

        Product product = new Product();
        product.setId(productId);
        product.setName(productName);
        product.setSlug("ao-polo");
        product.setCategory(category);
        product.setIsActive(true);

        ProductVariant variant = new ProductVariant();
        variant.setId(variantId);
        variant.setProduct(product);
        variant.setSku("SKU-" + variantId);
        variant.setSize(size);
        variant.setColor(color);
        variant.setStockQuantity(stockQuantity);
        variant.setIsActive(true);
        return variant;
    }
}
