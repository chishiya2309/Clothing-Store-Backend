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
}
