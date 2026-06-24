package vn.hcmute.edu.dp.nhom10.backend.pattern.policy;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.category.CategoryDeletionPolicy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryDeletionPolicyTest {

    @Test
    public void testCanDeleteEmptyCategory() {
        Category category = Category.builder().id(1L).name("Empty").products(new ArrayList<>()).children(new ArrayList<>()).build();
        CategoryDeletionPolicy policy = new CategoryDeletionPolicy();

        assertDoesNotThrow(() -> policy.checkCanDelete(category));
    }

    @Test
    public void testCannotDeleteCategoryWithProducts() {
        Product p = Product.builder().id(100L).name("P").build();
        Category category = Category.builder().id(1L).name("Non-Empty").products(new ArrayList<>(List.of(p))).children(new ArrayList<>()).build();
        CategoryDeletionPolicy policy = new CategoryDeletionPolicy();

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> policy.checkCanDelete(category));
        assertTrue(ex.getMessage().contains("Không thể xóa danh mục"));
    }

    @Test
    public void testCannotDeleteCategoryWithDescendantProducts() {
        Product p = Product.builder().id(100L).name("P").build();
        Category leaf = Category.builder().id(2L).name("Leaf").products(new ArrayList<>(List.of(p))).children(new ArrayList<>()).build();
        Category root = Category.builder().id(1L).name("Root").products(new ArrayList<>()).children(new ArrayList<>(List.of(leaf))).build();
        
        CategoryDeletionPolicy policy = new CategoryDeletionPolicy();

        InvalidDataException ex = assertThrows(InvalidDataException.class, () -> policy.checkCanDelete(root));
        assertTrue(ex.getMessage().contains("Không thể xóa danh mục vì vẫn còn 1 sản phẩm"));
    }
}
