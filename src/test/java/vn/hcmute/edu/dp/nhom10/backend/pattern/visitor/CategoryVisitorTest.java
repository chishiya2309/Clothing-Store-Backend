package vn.hcmute.edu.dp.nhom10.backend.pattern.visitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryVisitorTest {

    private Category root;
    private Category sub1;
    private Category sub2;
    private Category leaf;

    @BeforeEach
    public void setUp() {
        root = Category.builder().id(1L).name("Root").children(new ArrayList<>()).products(new ArrayList<>()).build();
        sub1 = Category.builder().id(2L).name("Sub 1").parent(root).children(new ArrayList<>()).products(new ArrayList<>()).build();
        sub2 = Category.builder().id(3L).name("Sub 2").parent(root).children(new ArrayList<>()).products(new ArrayList<>()).build();
        leaf = Category.builder().id(4L).name("Leaf").parent(sub1).children(new ArrayList<>()).products(new ArrayList<>()).build();

        root.getChildren().addAll(List.of(sub1, sub2));
        sub1.getChildren().add(leaf);
    }

    @Test
    public void testDepthVisitor() {
        CategoryDepthVisitor depthVisitor = new CategoryDepthVisitor();
        
        assertEquals(3, depthVisitor.visit(root));
        assertEquals(2, depthVisitor.visit(sub1));
        assertEquals(1, depthVisitor.visit(sub2));
        assertEquals(1, depthVisitor.visit(leaf));
    }

    @Test
    public void testProductCountVisitor() {
        CategoryProductCountVisitor countVisitor = new CategoryProductCountVisitor();

        // Initially 0 products
        assertEquals(0, countVisitor.visit(root));

        // Add products to leaf and sub2
        Product p1 = Product.builder().id(10L).name("Product 1").build();
        Product p2 = Product.builder().id(11L).name("Product 2").build();
        
        leaf.getProducts().add(p1);
        sub2.getProducts().add(p2);

        assertEquals(2, countVisitor.visit(root));
        assertEquals(1, countVisitor.visit(sub1));
        assertEquals(1, countVisitor.visit(sub2));
        assertEquals(1, countVisitor.visit(leaf));
    }

    @Test
    public void testCycleDetectionVisitor() {
        // Setting leaf (4L) as parent of root (1L) creates a cycle because leaf is descendant of root
        CategoryCycleDetectionVisitor cycleVisitor = new CategoryCycleDetectionVisitor(4L);
        assertTrue(cycleVisitor.visit(root)); // root's subtree contains leaf, so cycle detected

        // Candidate parent is sub2 (3L) which is not a descendant of leaf (4L)
        CategoryCycleDetectionVisitor noCycleVisitor = new CategoryCycleDetectionVisitor(3L);
        assertFalse(noCycleVisitor.visit(leaf));
    }
}
