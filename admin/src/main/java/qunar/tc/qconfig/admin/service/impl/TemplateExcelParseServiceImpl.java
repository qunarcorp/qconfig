package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import qunar.tc.qconfig.admin.service.TemplateExcelParseService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.poi.ss.usermodel.Cell.*;

/**
 * @author keli.wang
 * @since 2017/4/10
 */
@Service
public class TemplateExcelParseServiceImpl implements TemplateExcelParseService {
    private static final Logger LOG = LoggerFactory.getLogger(TemplateExcelParseServiceImpl.class);

    @Override
    public List<Map<String, String>> parse(final MultipartFile file) {
        try {
            final List<Map<String, String>> data = new ArrayList<>();

            final XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            final XSSFSheet sheet = workbook.getSheetAt(0);
            final List<Row> rows = Lists.newArrayList(sheet);
            final List<String> header = readHeader(rows.get(0));
            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                final Row row = rows.get(rowIndex);
                final Map<String, String> rowMap = new HashMap<>();
                for (int cellIndex = 0; cellIndex < header.size(); cellIndex++) {
                    rowMap.put(header.get(cellIndex), readCellAsString(row.getCell(cellIndex)));
                }
                data.add(rowMap);
            }

            return data;
        } catch (IOException e) {
            LOG.error("parse excel failed. name: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("parse excel failed");
        }
    }

    private List<String> readHeader(final Row row) {
        final List<String> header = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            header.add(readCellAsString(row.getCell(i)));
        }
        return header;
    }

    private String readCellAsString(final Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case CELL_TYPE_NUMERIC:
                final DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            default:
                throw new RuntimeException("unknown cell type " + cell.getCellType());
        }

    }
}
