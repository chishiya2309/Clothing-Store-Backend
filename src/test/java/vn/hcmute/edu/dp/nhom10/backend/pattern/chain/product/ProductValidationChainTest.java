package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductImageRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductVariantRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductValidationChainTest {

    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;

    private ProductPriceValidationHandler priceHandler;
    private ProductImageValidationHandler imageHandler;
    private ProductVariantValidationHandler variantHandler;
    private CategoryExistsValidationHandler categoryHandler;
    private ProductNameUniqueValidationHandler nameHandler;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        productRepository = mock(ProductRepository.class);

        priceHandler = new ProductPriceValidationHandler();
        imageHandler = new ProductImageValidationHandler();
        variantHandler = new ProductVariantValidationHandler();
        categoryHandler = new CategoryExistsValidationHandler(categoryRepository);
        nameHandler = new ProductNameUniqueValidationHandler(productRepository);

        // Chain the handlers
        priceHandler
                .setNext(imageHandler)
                .setNext(variantHandler)
                .setNext(categoryHandler)
                .setNext(nameHandler);
    }

    @Test
    void validate_allValid_shouldPass() {
        StaffProductImageRequest image = new StaffProductImageRequest(null, "http://image.url", null, 1, "alt");
        StaffProductVariantRequest variant = new StaffProductVariantRequest(null, "M", "Red", 10, BigDecimal.ZERO, true);

        Category category = Category.builder().id(1L).isActive(true).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryIdAndNameIgnoreCase(1L, "New Product")).thenReturn(false);

        ProductValidationContext context = ProductValidationContext.builder()
                .productId(null)
                .name("New Product")
                .categoryId(1L)
                .basePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.ONE)
                .images(List.of(image))
                .variants(List.of(variant))
                .build();

        assertDoesNotThrow(() -> priceHandler.handle(context));
    }

    @Test
    void validate_invalidPrice_shouldThrowException() {
        ProductValidationContext context = ProductValidationContext.builder()
                .basePrice(BigDecimal.ONE)
                .salePrice(BigDecimal.TEN)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> priceHandler.handle(context));
        assertEquals("Giá bán khuyến mãi không được lớn hơn giá gốc", ex.getMessage());
    }

    @Test
    void validate_emptyImages_shouldThrowException() {
        ProductValidationContext context = ProductValidationContext.builder()
                .basePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.ONE)
                .images(Collections.emptyList())
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> priceHandler.handle(context));
        assertEquals("Vui lòng upload ít nhất 1 hình ảnh", ex.getMessage());
    }

    @Test
    void validate_emptyVariants_shouldThrowException() {
        StaffProductImageRequest image = new StaffProductImageRequest(null, "http://image.url", null, 1, "alt");

        ProductValidationContext context = ProductValidationContext.builder()
                .basePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.ONE)
                .images(List.of(image))
                .variants(Collections.emptyList())
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> priceHandler.handle(context));
        assertEquals("Vui lòng thêm ít nhất 1 biến thể (size + màu)", ex.getMessage());
    }

    @Test
    void validate_duplicateVariants_shouldThrowException() {
        StaffProductImageRequest image = new StaffProductImageRequest(null, "http://image.url", null, 1, "alt");
        StaffProductVariantRequest variant1 = new StaffProductVariantRequest(null, "M", "Red", 10, BigDecimal.ZERO, true);
        StaffProductVariantRequest variant2 = new StaffProductVariantRequest(null, "M", "Red", 5, BigDecimal.ZERO, true);

        ProductValidationContext context = ProductValidationContext.builder()
                .basePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.ONE)
                .images(List.of(image))
                .variants(List.of(variant1, variant2))
                .build();

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> priceHandler.handle(context));
        assertEquals("Biến thể size + màu bị trùng trong request", ex.getMessage());
    }

    @Test
    void validate_categoryInactiveOrNotFound_shouldThrowException() {
        StaffProductImageRequest image = new StaffProductImageRequest(null, "http://image.url", null, 1, "alt");
        StaffProductVariantRequest variant = new StaffProductVariantRequest(null, "M", "Red", 10, BigDecimal.ZERO, true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        ProductValidationContext context = ProductValidationContext.builder()
                .basePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.ONE)
                .images(List.of(image))
                .variants(List.of(variant))
                .categoryId(1L)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> priceHandler.handle(context));

        Category inactiveCategory = Category.builder().id(1L).isActive(false).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(inactiveCategory));

        assertThrows(ResourceNotFoundException.class, () -> priceHandler.handle(context));
    }

    @Test
    void validate_duplicateProductNameForCreate_shouldThrowException() {
        StaffProductImageRequest image = new StaffProductImageRequest(null, "http://image.url", null, 1, "alt");
        StaffProductVariantRequest variant = new StaffProductVariantRequest(null, "M", "Red", 10, BigDecimal.ZERO, true);

        Category category = Category.builder().id(1L).isActive(true).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryIdAndNameIgnoreCase(1L, "Duplicate Product")).thenReturn(true);

        ProductValidationContext context = ProductValidationContext.builder()
                .productId(null)
                .name("Duplicate Product")
                .categoryId(1L)
                .basePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.ONE)
                .images(List.of(image))
                .variants(List.of(variant))
                .build();

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> priceHandler.handle(context));
        assertEquals("Tên sản phẩm đã tồn tại trong danh mục này", ex.getMessage());
    }
}
