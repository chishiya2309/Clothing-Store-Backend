package vn.hcmute.edu.dp.nhom10.backend.pattern.template.report;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;

import java.io.IOException;
import java.io.Writer;

@Component
public class InventoryCsvExporter extends CsvReportExporterTemplate<InventoryReportResponse> {

    @Override
    protected void writeHeader(Writer writer) throws IOException {
        writer.write("Mã SP,Tên sản phẩm,Biến thể (Size/Màu),Tồn kho,Trạng thái\n");
    }

    @Override
    protected void writeRow(Writer writer, InventoryReportResponse item, int index) throws IOException {
        writer.write(String.format("%s,%s,%s,%s,%s\n",
                escapeCsvField(item.productCode()),
                escapeCsvField(item.productName()),
                escapeCsvField(item.variantInfo()),
                escapeCsvField(item.stockQuantity()),
                escapeCsvField(item.statusLabel())
        ));
    }
}
