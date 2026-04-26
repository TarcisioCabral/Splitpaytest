/**
 * API Client for SplitPay IVA
 *
 * All requests use relative paths routed through the Thymeleaf frontend's
 * GatewayProxyController (/api/**), which transparently forwards them to the
 * API Gateway. No backend port is ever hardcoded here.
 *
 * Path mapping:
 *   /api/v1/...  →  GatewayProxyController  →  Gateway (gateway.url in application.properties)
 *                                            →  Backend microservice
 */

const API_BASE = '/api';

/**
 * Login via Keycloak — routed through Gateway's /auth endpoint.
 * The Gateway is responsible for communicating with Keycloak.
 */
export async function login(username, password) {
    const body = new URLSearchParams({
        username: username,
        password: password,
        grant_type: 'password'
    });

    const resp = await fetch(`${API_BASE}/v1/auth/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body
    });

    if (!resp.ok) throw new Error('Falha na autenticação');
    return await resp.json();
}

/**
 * Simulador de Margem e Projeção — via Gateway
 */
export async function getMargemProjecao(params) {
    try {
        const resp = await fetch(`${API_BASE}/v1/simulador/margem`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(params)
        });
        if (!resp.ok) throw new Error('API Offline');
        return await resp.json();
    } catch (e) {
        console.warn('Simulador API offline, retornando fallback local');
        throw e; // Let the caller handle fallback
    }
}

/**
 * Declaração — Inicialização
 */
export async function initDeclaracao(headers) {
    const resp = await fetch(`${API_BASE}/v1/declaracao/init`, { headers });
    if (!resp.ok) throw new Error('Erro ao inicializar declaração');
    return await resp.json();
}

/**
 * Declaração — Validação
 */
export async function validarDeclaracao(headers) {
    const resp = await fetch(`${API_BASE}/v1/declaracao/validar`, {
        method: 'POST',
        headers
    });
    if (!resp.ok) throw new Error('Erro ao validar declaração');
    return await resp.json();
}

/**
 * Declaração — Resumo
 */
export async function getResumoDeclaracao(data, headers) {
    const resp = await fetch(`${API_BASE}/v1/declaracao/resumo`, {
        method: 'POST',
        headers,
        body: JSON.stringify(data)
    });
    if (!resp.ok) throw new Error('Erro ao gerar resumo');
    return await resp.json();
}

/**
 * Processar Split de Transação
 */
export async function processSplit(data) {
    const resp = await fetch(`${API_BASE}/v1/split/process`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    return await resp.json();
}

/**
 * Buscar transações recentes
 */
export async function getRecentTransactions() {
    const resp = await fetch(`${API_BASE}/v1/split/recent`);
    if (!resp.ok) throw new Error('Erro ao buscar transações');
    return await resp.json();
}

/**
 * Download da Guia PDF
 */
export async function downloadGuiaPDF(headers) {
    const resp = await fetch(`${API_BASE}/v1/declaracao/download`, {
        method: 'GET',
        headers
    });
    if (!resp.ok) throw new Error('Erro ao baixar PDF');
    return await resp.blob();
}

/**
 * Cria um EventSource (SSE) para receber atualizações em tempo real
 * de uma transação específica — roteado via proxy, sem expor porta do backend.
 *
 * @param {string} nfeKey - Chave da NF-e
 * @returns {EventSource}
 */
export function createSplitStream(nfeKey) {
    const sseUrl = `${API_BASE}/v1/split/stream/${nfeKey}`;
    console.log('Connecting to SSE (via proxy):', sseUrl);
    return new EventSource(sseUrl);
}

/**
 * Cria um EventSource (SSE) para o stream global do dashboard.
 */
export function createDashboardStream() {
    const sseUrl = `${API_BASE}/v1/split/stream/dashboard`;
    console.log('Connecting to Dashboard SSE (via proxy):', sseUrl);
    return new EventSource(sseUrl);
}
