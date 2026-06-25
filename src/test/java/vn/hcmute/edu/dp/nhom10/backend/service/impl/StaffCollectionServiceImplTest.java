package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionProductsRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.*;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.enums.CollectionStatusState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommandExecutor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.collection.CollectionStateResolver;
import vn.hcmute.edu.dp.nhom10.backend.repository.CollectionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StaffCollectionServiceImplTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CollectionStateResolver stateResolver;

    @Mock
    private CatalogCommandExecutor commandExecutor;

    @InjectMocks
    private StaffCollectionServiceImpl staffCollectionService;

    @BeforeEach
    public void setUp() {
        // Stub the command executor to run the command directly
        lenient().when(commandExecutor.execute(any(CatalogCommand.class), anyString())).thenAnswer(invocation -> {
            CatalogCommand<?> command = invocation.getArgument(0);
            return command.execute();
        });
    }

    @Test
    public void testGetCollections() {
        Collection col = Collection.builder().id(1L).name("Summer").isActive(true).collectionProducts(new ArrayList<>()).build();
        Page<Collection> page = new PageImpl<>(List.of(col));
        
        when(collectionRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(stateResolver.resolve(col)).thenReturn(CollectionStatusState.ACTIVE);

        PageResponse<StaffCollectionResponse> result = staffCollectionService.getCollections(0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Summer", result.getContent().get(0).getName());
        assertEquals("ACTIVE", result.getContent().get(0).getStatusState());
    }

    @Test
    public void testCreateCollection() {
        StaffCollectionRequest request = StaffCollectionRequest.builder()
                .name("Winter Collection")
                .isActive(true)
                .build();

        Collection saved = Collection.builder()
                .id(1L)
                .name(request.getName())
                .slug("winter-collection")
                .isActive(true)
                .collectionProducts(new ArrayList<>())
                .build();

        when(collectionRepository.save(any(Collection.class))).thenReturn(saved);
        when(stateResolver.resolve(saved)).thenReturn(CollectionStatusState.ACTIVE);

        StaffCollectionResponse response = staffCollectionService.createCollection(request, "staff@store.com");

        assertNotNull(response);
        assertEquals("winter-collection", response.getSlug());
        verify(collectionRepository, times(1)).save(any(Collection.class));
    }

    @Test
    public void testGetCollectionDetail() {
        Collection collection = Collection.builder().id(1L).name("Autumn").isActive(true).collectionProducts(new ArrayList<>()).build();
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(stateResolver.resolve(collection)).thenReturn(CollectionStatusState.ACTIVE);

        StaffCollectionDetailResponse detail = staffCollectionService.getCollectionDetail(1L);

        assertNotNull(detail);
        assertEquals("Autumn", detail.getCollection().getName());
        verify(collectionRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetCollectionDetailNotFound() {
        when(collectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                staffCollectionService.getCollectionDetail(1L)
        );
    }

    @Test
    public void testUpdateCollection() {
        Collection existing = Collection.builder().id(1L).name("Old Name").isActive(true).collectionProducts(new ArrayList<>()).build();
        StaffCollectionRequest request = StaffCollectionRequest.builder().name("New Name").isActive(true).build();
        Collection updated = Collection.builder().id(1L).name("New Name").slug("new-name").isActive(true).collectionProducts(new ArrayList<>()).build();

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(collectionRepository.save(any(Collection.class))).thenReturn(updated);
        when(stateResolver.resolve(any())).thenReturn(CollectionStatusState.ACTIVE);

        StaffCollectionResponse response = staffCollectionService.updateCollection(1L, request, "staff@store.com");

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertEquals("new-name", response.getSlug());
        verify(collectionRepository, times(1)).save(any(Collection.class));
    }

    @Test
    public void testUpdateCollectionNotFound() {
        StaffCollectionRequest request = StaffCollectionRequest.builder().name("New Name").build();
        when(collectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                staffCollectionService.updateCollection(1L, request, "staff@store.com")
        );
    }

    @Test
    public void testDeleteCollection() {
        Collection collection = Collection.builder().id(1L).name("Autumn").isActive(true).build();
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));

        staffCollectionService.deleteCollection(1L, "staff@store.com");

        verify(collectionRepository, times(1)).delete(collection);
    }

    @Test
    public void testDeleteCollectionNotFound() {
        when(collectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                staffCollectionService.deleteCollection(1L, "staff@store.com")
        );
        verify(collectionRepository, never()).delete(any());
    }

    @Test
    public void testAddProductsToCollection() {
        Collection collection = Collection.builder().id(1L).name("Autumn").isActive(true).collectionProducts(new ArrayList<>()).build();
        Product product = Product.builder().id(100L).name("Coat").images(new ArrayList<>()).variants(new ArrayList<>()).build();

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);
        when(stateResolver.resolve(collection)).thenReturn(CollectionStatusState.ACTIVE);

        StaffCollectionProductsRequest request = StaffCollectionProductsRequest.builder()
                .productIds(List.of(100L))
                .build();

        StaffCollectionDetailResponse response = staffCollectionService.addProductsToCollection(1L, request, "staff@store.com");

        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        assertEquals("Coat", response.getProducts().get(0).getName());
    }

    @Test
    public void testAddProductsToCollectionProductNotFound() {
        Collection collection = Collection.builder().id(1L).name("Autumn").isActive(true).collectionProducts(new ArrayList<>()).build();

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(productRepository.findById(100L)).thenReturn(Optional.empty());

        StaffCollectionProductsRequest request = StaffCollectionProductsRequest.builder()
                .productIds(List.of(100L))
                .build();

        assertThrows(ResourceNotFoundException.class, () ->
                staffCollectionService.addProductsToCollection(1L, request, "staff@store.com")
        );
    }

    @Test
    public void testRemoveProductsFromCollection() {
        Product product = Product.builder().id(100L).name("Coat").images(new ArrayList<>()).variants(new ArrayList<>()).build();
        Collection collection = Collection.builder().id(1L).name("Autumn").isActive(true).collectionProducts(new ArrayList<>()).build();
        vn.hcmute.edu.dp.nhom10.backend.entity.CollectionProduct cp = vn.hcmute.edu.dp.nhom10.backend.entity.CollectionProduct.builder()
                .collection(collection)
                .product(product)
                .build();
        collection.getCollectionProducts().add(cp);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);
        when(stateResolver.resolve(collection)).thenReturn(CollectionStatusState.ACTIVE);

        StaffCollectionProductsRequest request = StaffCollectionProductsRequest.builder()
                .productIds(List.of(100L))
                .build();

        StaffCollectionDetailResponse response = staffCollectionService.removeProductsFromCollection(1L, request, "staff@store.com");

        assertNotNull(response);
        assertTrue(response.getProducts().isEmpty());
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    public void testRemoveProductsFromCollectionNotFound() {
        StaffCollectionProductsRequest request = StaffCollectionProductsRequest.builder()
                .productIds(List.of(100L))
                .build();

        when(collectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                staffCollectionService.removeProductsFromCollection(1L, request, "staff@store.com")
        );
    }
}
