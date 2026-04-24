package com.splitpay.conciliation_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class ConciliationServiceApplicationTests {

	@org.springframework.boot.test.mock.mockito.MockBean
	private com.splitpay.conciliation_service.repository.ConciliationRepository conciliationRepository;

	@org.springframework.boot.test.mock.mockito.MockBean
	private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

	@Test
	void contextLoads() {
	}

}
