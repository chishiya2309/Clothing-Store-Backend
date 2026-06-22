package vn.hcmute.edu.dp.nhom10.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(String folder, MultipartFile file);
    void deleteFile(String fileUrl);
}
