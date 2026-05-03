package com.splitpay.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ReportsController {

    private final JdbcTemplate jdbcTemplate;
    private final ReportExportService reportExportService;

    /**
     * Retorna a tendência de retenção por segmento.
     */
    @GetMapping("/trends/segment")
    public List<Map<String, Object>> getTrendsBySegment() {
        try {
            String sql = "SELECT segmento, SUM(ibs_retido) as total_ibs, SUM(cbs_retido) as total_cbs " +
                         "FROM transactions GROUP BY segmento";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Error fetching trends by segment: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Retorna a tendência de retenção por período (dia).
     */
    @GetMapping("/trends/period")
    public List<Map<String, Object>> getTrendsByPeriod() {
        try {
            String sql = "SELECT DATE(created_at) as data, SUM(ibs_retido) as total_ibs, SUM(cbs_retido) as total_cbs " +
                         "FROM transactions GROUP BY DATE(created_at) ORDER BY data";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Error fetching trends by period: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Exporta as transações para Excel.
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            byte[] excelData = reportExportService.generateExcelReport(startDate, endDate);
            
            String filename = "relatorio_retencao";
            if (startDate != null && endDate != null) {
                filename += "_" + startDate + "_ate_" + endDate;
            } else if (startDate != null) {
                filename += "_a_partir_de_" + startDate;
            } else if (endDate != null) {
                filename += "_ate_" + endDate;
            }
            filename += ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            log.error("Error exporting to Excel: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
