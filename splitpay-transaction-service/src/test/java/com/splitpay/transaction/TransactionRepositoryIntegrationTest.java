package com.splitpay.transaction;

import com.splitpay.TransactionServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para o TransactionRepository.
 * Diferente de testes unitários com H2, aqui o Testcontainers garante
 * que rodaremos os testes em um banco de dados real do PostgreSQL.
 */
@SpringBootTest(classes = TransactionServiceApplication.class) // Aponta para a classe principal do sistema
@Testcontainers // Habilita o ciclo de vida do Testcontainers nesta classe de teste
public class TransactionRepositoryIntegrationTest {

    // Define o container Docker que será criado e gerado para os testes.
    // Usamos a mesma versão (15) que poderia ser do ambiente de produção.
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // Injeta as propriedades do banco de dados provisionado pelo Testcontainers
    // de forma dinâmica no contexto do Spring (application.properties virtual)
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Garante que a estrutura das tabelas será criada antes de rodar os testes
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldSaveAndFindTransactionByNfeKey() {
        // Arrange (Preparação): Criamos a entidade de teste simulando a recepção de dados reais
        Transaction transaction = new Transaction();
        transaction.setNfeKey("12345678901234567890123456789012345678901234");
        transaction.setValorBruto(new BigDecimal("100.00"));
        transaction.setAdquirente("Cielo");

        // Act (Ação): Realizamos a persistência na tabela do PostgreSQL
        transactionRepository.save(transaction);
        
        // E depois buscamos pelo NfeKey criado
        Optional<Transaction> found = transactionRepository.findByNfeKey("12345678901234567890123456789012345678901234");

        // Assert (Verificação): Confirmamos que a entidade foi salva e que
        // os dados buscados batem com os valores armazenados de forma precisa
        assertThat(found).isPresent();
        assertThat(found.get().getValorBruto()).isEqualByComparingTo("100.00"); // BigDecimal usa isEqualByComparingTo para não falhar com escalas diferentes
        assertThat(found.get().getAdquirente()).isEqualTo("Cielo");
    }

    @Test
    void shouldNotFindTransactionByNonExistentNfeKey() {
        // Act (Ação): Tenta procurar por uma NfeKey que sabemos que não existe
        Optional<Transaction> found = transactionRepository.findByNfeKey("NON_EXISTENT");

        // Assert (Verificação): O retorno do banco deve ser vazio (not present)
        assertThat(found).isNotPresent();
    }
}
