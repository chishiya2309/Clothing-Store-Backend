package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.report;

import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ReportExportStrategy<T> {
    ReportExportFormat supportFormat();

    String contentType();

    String fileName();

    void export(OutputStream outputStream, List<T> data) throws IOException;
}
