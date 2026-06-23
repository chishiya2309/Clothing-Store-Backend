package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;

public record ReportExportDescriptor(
        String contentType,
        String fileName
) implements Serializable {
}
