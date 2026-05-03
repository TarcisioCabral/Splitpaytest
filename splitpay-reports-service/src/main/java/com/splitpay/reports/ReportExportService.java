package com.splitpay.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportExportService {

    private final JdbcTemplate jdbcTemplate;

    public byte[] generateExcelReport(String startDate, String endDate) {
        log.info("Generating Excel report for period: {} to {}", startDate, endDate);
        
        List<Map<String, Object>> transactions = fetchTransactions(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Retenção de Impostos");

            // Define styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("R$ #,##0.00"));

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(dataStyle);
            dateStyle.setDataFormat(format.getFormat("dd/MM/yyyy HH:mm:ss"));

            // Create Header
            String[] headers = {
                    "Data", "Chave NFe", "Adquirente", "Segmento", "Fase", 
                    "Valor Bruto", "IBS Retido", "CBS Retido", "Valor Líquido"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create Data Rows
            int rowNum = 1;
            
            double sumBruto = 0.0;
            double sumIbs = 0.0;
            double sumCbs = 0.0;
            double sumLiquido = 0.0;
            for (Map<String, Object> tx : transactions) {
                Row row = sheet.createRow(rowNum++);

                // Data
                Cell dateCell = row.createCell(0);
                Object createdAtObj = tx.get("created_at");
                if (createdAtObj instanceof Timestamp) {
                    dateCell.setCellValue(((Timestamp) createdAtObj).toLocalDateTime());
                } else if (createdAtObj != null) {
                    dateCell.setCellValue(createdAtObj.toString());
                }
                dateCell.setCellStyle(dateStyle);

                // NFe Key
                Cell nfeCell = row.createCell(1);
                nfeCell.setCellValue(getStringSafe(tx, "nfe_key"));
                nfeCell.setCellStyle(dataStyle);

                // Adquirente
                Cell adquirenteCell = row.createCell(2);
                adquirenteCell.setCellValue(getStringSafe(tx, "adquirente"));
                adquirenteCell.setCellStyle(dataStyle);

                // Segmento
                Cell segmentoCell = row.createCell(3);
                segmentoCell.setCellValue(getStringSafe(tx, "segmento"));
                segmentoCell.setCellStyle(dataStyle);

                // Fase
                Cell faseCell = row.createCell(4);
                faseCell.setCellValue(getStringSafe(tx, "fase"));
                faseCell.setCellStyle(dataStyle);

                // Valor Bruto
                double valorBruto = getDoubleSafe(tx, "valor_bruto");
                Cell valorBrutoCell = row.createCell(5);
                valorBrutoCell.setCellValue(valorBruto);
                valorBrutoCell.setCellStyle(currencyStyle);
                sumBruto += valorBruto;

                // IBS Retido
                double ibsRetido = getDoubleSafe(tx, "ibs_retido");
                Cell ibsCell = row.createCell(6);
                ibsCell.setCellValue(ibsRetido);
                ibsCell.setCellStyle(currencyStyle);
                sumIbs += ibsRetido;

                // CBS Retido
                double cbsRetido = getDoubleSafe(tx, "cbs_retido");
                Cell cbsCell = row.createCell(7);
                cbsCell.setCellValue(cbsRetido);
                cbsCell.setCellStyle(currencyStyle);
                sumCbs += cbsRetido;

                // Valor Liquido
                double liquido = getDoubleSafe(tx, "liquido");
                Cell liquidoCell = row.createCell(8);
                liquidoCell.setCellValue(liquido);
                liquidoCell.setCellStyle(currencyStyle);
                sumLiquido += liquido;
            }

            // Create Total Row
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(4);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalBrutoCell = totalRow.createCell(5);
            totalBrutoCell.setCellValue(sumBruto);
            totalBrutoCell.setCellStyle(headerStyle);

            Cell totalIbsCell = totalRow.createCell(6);
            totalIbsCell.setCellValue(sumIbs);
            totalIbsCell.setCellStyle(headerStyle);

            Cell totalCbsCell = totalRow.createCell(7);
            totalCbsCell.setCellValue(sumCbs);
            totalCbsCell.setCellStyle(headerStyle);

            Cell totalLiquidoCell = totalRow.createCell(8);
            totalLiquidoCell.setCellValue(sumLiquido);
            totalLiquidoCell.setCellStyle(headerStyle);

            // Autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }

        } catch (IOException e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private List<Map<String, Object>> fetchTransactions(String startDate, String endDate) {
        String baseSql = "SELECT nfe_key, valor_bruto, ibs_retido, cbs_retido, liquido, adquirente, segmento, fase, created_at " +
                         "FROM transactions ";
        
        boolean hasStart = (startDate != null && !startDate.isEmpty());
        boolean hasEnd = (endDate != null && !endDate.isEmpty());
        
        if (hasStart && hasEnd) {
            String sql = baseSql + "WHERE DATE(created_at) >= DATE(?) AND DATE(created_at) <= DATE(?) ORDER BY created_at DESC";
            return jdbcTemplate.queryForList(sql, startDate, endDate);
        } else if (hasStart) {
            String sql = baseSql + "WHERE DATE(created_at) >= DATE(?) ORDER BY created_at DESC";
            return jdbcTemplate.queryForList(sql, startDate);
        } else if (hasEnd) {
            String sql = baseSql + "WHERE DATE(created_at) <= DATE(?) ORDER BY created_at DESC";
            return jdbcTemplate.queryForList(sql, endDate);
        } else {
            String sql = baseSql + "ORDER BY created_at DESC";
            return jdbcTemplate.queryForList(sql);
        }
    }

    private String getStringSafe(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private double getDoubleSafe(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else if (val != null) {
            try {
                return Double.parseDouble(val.toString());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
