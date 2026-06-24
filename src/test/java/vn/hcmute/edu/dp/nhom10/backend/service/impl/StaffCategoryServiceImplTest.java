package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCategoryRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffCategoryResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog.CatalogCommandExecutor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.category.CategoryDeletionPolicy;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StaffCategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryDeletionPolicy categoryDeletionPolicy;

    @Mock
    private CatalogCommandExecutor commandExecutor;

    @InjectMocks
    private StaffCategoryServiceImpl staffCategoryService;

    @BeforeEach
    public void setUp() {
        // Stub the command executor to run the command directly
        lenient().when(commandExecutor.execute(any(CatalogCommand.class), anyString())).thenAnswer(invocation -> {
            CatalogCommand<?> command = invocation.getArgument(0);
            return command.execute();
        });
    }

    @Test
    public void testGetCategoryHierarchy() {
        Category root = Category.builder().id(1L).name("Root").displayOrder(1).isActive(true).children(new ArrayList<>()).build();
        when(categoryRepository.findByIsActiveTrueAndParentIsNullOrderByDisplayOrderAsc()).thenReturn(List.of(root));

        List<StaffCategoryResponse> result = staffCategoryService.getCategoryHierarchy();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getName());
    }

    @Test
    public void testCreateRootCategory() {
        StaffCategoryRequest request = StaffCategoryRequest.builder()
                .name("Root Category")
                .isActive(true)
                .displayOrder(1)
                .build();

        Category saved = Category.builder()
                .id(1L)
                .name(request.getName())
                .slug("root-category")
                .isActive(true)
                .displayOrder(1)
                .children(new ArrayList<>())
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        StaffCategoryResponse response = staffCategoryService.createCategory(request, "staff@store.com");

        assertNotNull(response);
        assertEquals("root-category", response.getSlug());
        assertEquals(1, response.getDepth());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void testCreateChildCategoryExceedingMaxDepth() {
        // Root (depth 1) -> Sub (depth 2) -> SubSub (depth 3)
        Category root = Category.builder().id(1L).name("Root").build();
        Category sub = Category.builder().id(2L).name("Sub").parent(root).build();
        Category subSub = Category.builder().id(3L).name("SubSub").parent(sub).build();

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(subSub));

        StaffCategoryRequest request = StaffCategoryRequest.builder()
                .name("Too Deep Leaf")
                .parentId(3L)
                .build();

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> 
            staffCategoryService.createCategory(request, "staff@store.com")
        );
        assertTrue(ex.getMessage().contains("Vượt quá độ sâu"));
    }

    @Test
    public void testUpdateCategoryCycleDetected() {
        // Root (1L) -> Child (2L)
        Category root = Category.builder().id(1L).name("Root").children(new ArrayList<>()).build();
        Category child = Category.builder().id(2L).name("Child").parent(root).children(new ArrayList<>()).build();
        root.getChildren().add(child);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(child));

        // Attempting to set parent of Root (1L) to Child (2L)
        StaffCategoryRequest request = StaffCategoryRequest.builder()
                .name("Root New")
                .parentId(2L)
                .build();

        InvalidDataException ex = assertThrows(InvalidDataException.class, () ->
            staffCategoryService.updateCategory(1L, request, "staff@store.com")
        );
        assertTrue(ex.getMessage().contains("Phát hiện chu trình vòng lặp"));
    }

    @Test
    public void testDeleteCategoryEnforcesPolicy() {
        Category category = Category.builder().id(1L).name("Category").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doThrow(new InvalidDataException("Linked Products Exist")).when(categoryDeletionPolicy).checkCanDelete(category);

        assertThrows(InvalidDataException.class, () ->
            staffCategoryService.deleteCategory(1L, "staff@store.com")
        );
        verify(categoryRepository, never()).delete(any());
    }
}
