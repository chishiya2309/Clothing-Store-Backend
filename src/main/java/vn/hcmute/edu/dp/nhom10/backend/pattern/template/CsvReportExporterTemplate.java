package vn.hcmute.edu.dp.nhom10.backend.pattern.template;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Lớp cơ sở trừu tượng thiết lập khung xương xuất báo cáo định dạng CSV.
 * Áp dụng Template Method Pattern để định nghĩa quy trình xuất CSV bất biến:
 *          ghi UTF-8 BOM -> ghi tiêu đề -> lặp ghi các dòng dữ liệu -> flush.
 *          Các lớp con chỉ cần định nghĩa cách ghi tiêu đề và ghi dòng cụ thể.
 */
public abstract class CsvReportExporterTemplate<T> {

    public final void export(Writer writer, List<T> data) throws IOException {
        writer.write('\uFEFF');
        writeHeader(writer);

        if (data != null && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                writeRow(writer, data.get(i), i + 1);
            }
        }
        
        writer.flush();
    }

    protected abstract void writeHeader(Writer writer) throws IOException;

    protected abstract void writeRow(Writer writer, T item, int index) throws IOException;

    protected String escapeCsvField(Object value) {
        if (value == null) {
            return "";
        }
        String field = value.toString().replace("\"", "\"\"");
        if (field.contains(",") || field.contains("\n") || field.contains("\"")) {
            return "\"" + field + "\"";
        }
        return field;
    }
}
