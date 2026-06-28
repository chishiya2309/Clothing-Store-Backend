package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.CollectionResponse;

public interface CollectionService {
    CollectionResponse getCollectionBySlug(String slug);
    java.util.List<CollectionResponse> getActiveCollections();
}
