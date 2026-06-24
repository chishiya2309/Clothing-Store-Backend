package vn.hcmute.edu.dp.nhom10.backend.pattern.specification;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class StaffProductSpecificationTest {

    private Root<Product> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
    }

    @Test
    void fromCriteria_nullCriteria_shouldReturnAlwaysTrue() {
        Specification<Product> spec = StaffProductSpecification.fromCriteria(null);
        assertNotNull(spec);

        spec.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void fromCriteria_withKeyword_shouldApplyLikePredicate() {
        Path<String> namePath = mock(Path.class);
        Path<String> slugPath = mock(Path.class);
        Expression<String> lowerName = mock(Expression.class);
        Expression<String> lowerSlug = mock(Expression.class);
        Predicate likeName = mock(Predicate.class);
        Predicate likeSlug = mock(Predicate.class);

        when(root.<String>get("name")).thenReturn(namePath);
        when(root.<String>get("slug")).thenReturn(slugPath);
        when(cb.lower(namePath)).thenReturn(lowerName);
        when(cb.lower(slugPath)).thenReturn(lowerSlug);
        when(cb.like(eq(lowerName), anyString())).thenReturn(likeName);
        when(cb.like(eq(lowerSlug), anyString())).thenReturn(likeSlug);

        StaffProductSearchCriteria criteria = new StaffProductSearchCriteria("test", null, null);
        Specification<Product> spec = StaffProductSpecification.fromCriteria(criteria);
        assertNotNull(spec);

        spec.toPredicate(root, query, cb);
        verify(cb).or(likeName, likeSlug);
    }

    @Test
    void fromCriteria_withCategoryId_shouldApplyEqualPredicate() {
        Path<Object> categoryPath = mock(Path.class);
        Path<Object> idPath = mock(Path.class);
        when(root.get("category")).thenReturn(categoryPath);
        when(categoryPath.get("id")).thenReturn(idPath);

        StaffProductSearchCriteria criteria = new StaffProductSearchCriteria(null, 10L, null);
        Specification<Product> spec = StaffProductSpecification.fromCriteria(criteria);
        assertNotNull(spec);

        spec.toPredicate(root, query, cb);
        verify(cb).equal(idPath, 10L);
    }

    @Test
    void fromCriteria_withStatusInactive_shouldApplyIsFalsePredicate() {
        Path<Boolean> isActivePath = mock(Path.class);
        when(root.<Boolean>get("isActive")).thenReturn(isActivePath);

        StaffProductSearchCriteria criteria = new StaffProductSearchCriteria(null, null, StaffProductStatus.INACTIVE);
        Specification<Product> spec = StaffProductSpecification.fromCriteria(criteria);
        assertNotNull(spec);

        spec.toPredicate(root, query, cb);
        verify(cb).isFalse(isActivePath);
    }
}
