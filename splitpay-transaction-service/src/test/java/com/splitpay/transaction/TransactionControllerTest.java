package com.splitpay.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.splitpay.transaction.dto.ProcessTransactionRequest;
import com.splitpay.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processTransaction_shouldReturnAcceptedWhenValidPayload() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
                "12345678901234567890123456789012345678901234",
                new BigDecimal("100.00"),
                "Cielo",
                "alimentacao",
                "2026_teste"
        );

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "processing");
        mockResponse.put("nfe_key", request.nfeKey());

        when(transactionService.processTransaction(any(ProcessTransactionRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/split/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("processing"))
                .andExpect(jsonPath("$.nfe_key").value(request.nfeKey()));
    }

    @Test
    void processTransaction_shouldReturnBadRequestWhenInvalidPayload() throws Exception {
        // Missing nfe_key and invalid valorBruto
        ProcessTransactionRequest invalidRequest = new ProcessTransactionRequest(
                "",
                new BigDecimal("-10.00"),
                "",
                "alimentacao",
                "2026_teste"
        );

        mockMvc.perform(post("/v1/split/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecentTransactions_shouldReturnList() throws Exception {
        Transaction t = new Transaction();
        t.setNfeKey("key");
        t.setValorBruto(new BigDecimal("50.0"));
        t.setAdquirente("Stone");

        when(transactionService.getRecentTransactions()).thenReturn(Collections.singletonList(t));

        mockMvc.perform(get("/v1/split/recent")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nfeKey").value("key"))
                .andExpect(jsonPath("$[0].adquirente").value("Stone"));
    }
}
