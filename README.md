# 💳 SplitPay 

## 🌟 Visão Geral

O **SplitPay** é uma plataforma robusta de microserviços projetada para gerenciar o ciclo de vida completo de transações financeiras, com foco em divisão de pagamentos (splits), cálculos tributários automáticos (IVA) e relatórios fiscais.

Utilizando uma arquitetura moderna e escalável, o sistema garante alta disponibilidade e processamento assíncrono para operações complexas, oferecendo uma experiência de usuário premium através de dashboards interativos em tempo real.

---

## 🚀 Principais Funcionalidades

- **⚙️ Ciclo de Vida em Tempo Real:** Acompanhamento visual de transações via *Server-Sent Events (SSE)*.
- **📈 Split de Pagamentos:** Divisão inteligente de valores entre múltiplos recebedores.
- **📄 Simulação de NF-e:** Fluxo completo de teste para emissão de notas fiscais eletrônicas.
- **📊 Analítica Fiscal:** Dashboards detalhados e exportação de relatórios avançados em Excel.
- **🔐 Segurança Robusta:** Gestão de identidade e acessos integrada com Keycloak e RBAC.
- **🔔 Notificações Inteligentes:** Alertas automáticos via mensagens assíncronas (RabbitMQ).

---

## 🏗️ Arquitetura de Microserviços

O ecossistema é composto por 7 serviços independentes:

| Serviço | Descrição | Porta |
| :--- | :--- | :--- |
| **Gateway** | Ponto de entrada único e roteamento de requisições. | `9085` |
| **Auth** | Gerenciamento de usuários e persistência de perfis. | `9084` |
| **Transaction** | Core business: transações, splits e NF-e. | `9081` |
| **Conciliation** | Validação e conciliação de dados financeiros. | `9082` |
| **Reports** | Inteligência fiscal e geração de documentos Excel. | `9086` |
| **Notification** | Processamento de alertas e mensagens. | - |
| **Frontend** | Dashboard administrativo (Thymeleaf/JS). | `9080` |

---

## 🛠️ Stack Tecnológica

- **Backend:** Java 17+, Spring Boot 3, Spring Cloud Gateway.
- **Frontend:** HTML5, CSS3 Moderno, JavaScript, Thymeleaf.
- **Dados:** PostgreSQL (Bancos independentes por serviço).
- **Mensageria:** RabbitMQ.
- **Segurança:** Keycloak (OAuth2/OIDC).
- **DevOps:** Docker, Docker Compose, Makefile.

---

## 🚦 Como Iniciar

O projeto está totalmente conteinerizado. Siga os passos abaixo:

### Pré-requisitos
- Docker & Docker Compose
- Maven (para build inicial)

### Execução Rápida (via Makefile)

1. **Compilar o projeto:**
   ```bash
   make build
   ```

2. **Subir toda a infraestrutura:**
   ```bash
   make up
   ```

3. **Verificar o status:**
   ```bash
   make ps
   ```

### Acessos Locais
- **Dashboard Principal:** [http://localhost:9080](http://localhost:9080)
- **Gestão RabbitMQ:** [http://localhost:15674](http://localhost:15674)
- **Painel Keycloak:** [http://localhost:9182](http://localhost:9182)

---


## 🛠️ Comandos de Manutenção

Utilize o `Makefile` para gerenciar o ambiente:
- `make logs`: Visualiza os logs em tempo real.
- `make restart`: Reinicia todos os containers.
- `make clean`: Remove volumes e containers (limpeza total).
- `make down`: Para a execução do sistema.

---

> [!NOTE]
> Este projeto foi desenvolvido com foco em escalabilidade e padrões de design modernos. Para suporte ou dúvidas, consulte a equipe de desenvolvimento.
