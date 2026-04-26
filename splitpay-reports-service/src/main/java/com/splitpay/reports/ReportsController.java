package com.splitpay.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ReportsController {

    private final JdbcTemplate jdbcTemplate;

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
}
