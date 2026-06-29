package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.*;
import vn.hcmute.edu.dp.nhom10.backend.entity.*;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product.*;
import vn.hcmute.edu.dp.nhom10.backend.pattern.factory.product.ProductIdentityFactory;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.ProductPriceManager;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.product.ProductDeletionDecision;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.product.ProductDeletionPolicy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.specification.StaffProductSpecification;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.product.ProductStockStateResolver;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.StaffProductServiceImpl;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StaffProductServiceImplTest {

    private ProductRepository productRepository;
    private ProductVariantRepository productVariantRepository;
    private CategoryRepository categoryRepository;
    private ProductIdentityFactory productIdentityFactory;
    private ProductStockStateResolver stockStateResolver;
    private ProductDeletionPolicy productDeletionPolicy;
    private ProductPriceManager productPriceManager;
    private CacheManager cacheManager;

    private ProductPriceValidationHandler priceHandler;
    private ProductImageValidationHandler imageHandler;
    private ProductVariantValidationHandler variantHandler;
    private CategoryExistsValidationHandler categoryHandler;
    private ProductNameUniqueValidationHandler nameHandler;

    private StaffProductServiceImpl service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productVariantRepository = mock(ProductVariantRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        productIdentityFactory = mock(ProductIdentityFactory.class);
        stockStateResolver = mock(ProductStockStateResolver.class);
        productDeletionPolicy = mock(ProductDeletionPolicy.class);
        productPriceManager = mock(ProductPriceManager.class);
        cacheManager = mock(CacheManager.class);

        priceHandler = mock(ProductPriceValidationHandler.class);
        imageHandler = mock(ProductImageValidationHandler.class);
        variantHandler = mock(ProductVariantValidationHandler.class);
        categoryHandler = mock(CategoryExistsValidationHandler.class);
        nameHandler = mock(ProductNameUniqueValidationHandler.class);

        // Stub setNext chain setup
        when(priceHandler.setNext(any())).thenReturn(imageHandler);
        when(imageHandler.setNext(any())).thenReturn(variantHandler);
        when(variantHandler.setNext(any())).thenReturn(categoryHandler);
        when(categoryHandler.setNext(any())).thenReturn(nameHandler);

        service = new StaffProductServiceImpl(
                productRepository,
                productVariantRepository,
                categoryRepository,
                productIdentityFactory,
                stockStateResolver,
                productDeletionPolicy,
                productPriceManager,
                cacheManager,
                priceHandler,
                imageHandler,
                variantHandler,
                categoryHandler,
                nameHandler
        );
    }

    @Test
    void getProducts_shouldReturnPageResponse() {
        StaffProductSearchCriteria criteria = new StaffProductSearchCriteria("test", 1L, StaffProductStatus.ACTIVE);
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .slug("test-product")
                .category(Category.builder().id(1L).name("Shirts").build())
                .basePrice(BigDecimal.TEN)
                .isActive(true)
                .build();

        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(productPage);
        when(stockStateResolver.resolve(any())).thenReturn(StaffProductStatus.ACTIVE);
        when(stockStateResolver.totalStock(any())).thenReturn(10);

        PageResponse<StaffProductListItemResponse> response = service.getProducts(criteria, 0, 10, "name", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Product", response.getContent().get(0).name());
        verify(productRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void getProductDetail_productExists_shouldReturnDetail() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .slug("test-product")
                .category(Category.builder().id(1L).name("Shirts").build())
                .basePrice(BigDecimal.TEN)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockStateResolver.resolve(product)).thenReturn(StaffProductStatus.ACTIVE);
        when(stockStateResolver.totalStock(product)).thenReturn(15);

        StaffProductDetailResponse response = service.getProductDetail(1L);

        assertNotNull(response);
        assertEquals("Test Product", response.name());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductDetail_productNotFound_shouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getProductDetail(1L));
    }

    @Test
    void createProduct_validRequest_shouldSaveAndReturnDetail() {
        StaffProductImageRequest imageReq = new StaffProductImageRequest(null, "http://image", ImageType.main, 1, "alt");
        StaffProductVariantRequest variantReq = new StaffProductVariantRequest(null, "M", "Red", 10, BigDecimal.ZERO, true);
        StaffCreateProductRequest request = new StaffCreateProductRequest(
                "New Product", "Desc", "Cotton", "Wash cold", 1L,
                BigDecimal.TEN, BigDecimal.ONE, true, List.of(imageReq), List.of(variantReq)
        );

        Category category = Category.builder().id(1L).isActive(true).name("Shirts").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productIdentityFactory.createSlug(anyString(), any())).thenReturn("new-product");
        when(productIdentityFactory.createSku(anyString(), anyString(), anyString(), any())).thenReturn("SKU-NEW");

        Product savedProduct = Product.builder()
                .id(100L)
                .name("New Product")
                .slug("new-product")
                .category(category)
                .basePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.ONE)
                .isActive(true)
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(stockStateResolver.resolve(any())).thenReturn(StaffProductStatus.ACTIVE);
        when(stockStateResolver.totalStock(any())).thenReturn(10);

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        StaffProductDetailResponse response = service.createProduct(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        verify(priceHandler).handle(any(ProductValidationContext.class));
        verify(productRepository).save(any(Product.class));
        verify(productPriceManager).notifyObservers(any(), any(), any());
    }

    @Test
    void updateVisibility_shouldUpdateAndSave() {
        Product product = Product.builder()
                .id(1L)
                .isActive(true)
                .category(Category.builder().id(1L).name("Shirts").build())
                .images(new ArrayList<>())
                .variants(new ArrayList<>())
                .build();

        when(productRepository.findByIdIgnoringSoftDelete(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        StaffUpdateProductVisibilityRequest request = new StaffUpdateProductVisibilityRequest(false);
        StaffProductDetailResponse response = service.updateVisibility(1L, request);

        assertNotNull(response);
        assertFalse(response.isActive());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_shouldTriggerPolicyAndEvictCache() {
        Product product = Product.builder()
                .id(1L)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productDeletionPolicy.decide(1L)).thenReturn(ProductDeletionDecision.HARD_DELETE);

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        service.deleteProduct(1L);

        verify(productRepository).delete(product);
        verify(cacheManager, atLeastOnce()).getCache(anyString());
    }

    @Test
    void updateStock_shouldLockAndSaveAndWarnIfLow() {
        ProductVariant variant = ProductVariant.builder()
                .id(5L)
                .sku("SKU-1")
                .stockQuantity(20)
                .build();

        when(productVariantRepository.findByIdAndProductIdForUpdate(5L, 1L)).thenReturn(Optional.of(variant));
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockStateResolver.isLowStock(5)).thenReturn(true);

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        StaffUpdateStockRequest request = new StaffUpdateStockRequest(5);
        StaffStockUpdateResponse response = service.updateStock(1L, 5L, request);

        assertNotNull(response);
        assertEquals(5, response.newStockQuantity());
        assertTrue(response.lowStock());
        assertEquals("Sản phẩm sắp hết hàng", response.warningMessage());
        verify(productVariantRepository).findByIdAndProductIdForUpdate(5L, 1L);
        verify(productVariantRepository).save(variant);
    }
}
