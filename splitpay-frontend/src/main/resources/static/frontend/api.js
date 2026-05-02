/**
 * API Client for SplitPay IVA
 */

const API_BASE = '/api';

/**
 * Helper to get headers with Authorization token
 */
export function getAuthHeaders() {
    const token = localStorage.getItem('splitpay_token');
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}

/**
 * Login — routed through Gateway's /auth endpoint.
 */
export async function login(email, password) {
    const resp = await fetch(`${API_BASE}/v1/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (!resp.ok) {
        if (resp.status === 403) throw new Error('Acesso negado ou credenciais inválidas');
        throw new Error('Falha na autenticação');
    }
    
    const data = await resp.json();
    if (data.token) {
        localStorage.setItem('splitpay_token', data.token);
        localStorage.setItem('splitpay_user', JSON.stringify(data));
    }
    return data;
}

export function logout() {
    localStorage.removeItem('splitpay_token');
    localStorage.removeItem('splitpay_user');
    window.location.href = '/login';
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated() {
    return !!localStorage.getItem('splitpay_token');
}

/**
 * Register a new user
 */
export async function register(username, email, password, role = 'ROLE_CLIENTE') {
    const resp = await fetch(`${API_BASE}/v1/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email, password, role })
    });

    if (!resp.ok) {
        const error = await resp.text();
        throw new Error(error || 'Falha no registro');
    }
    return await resp.text();
}

/**
 * Simulador de Margem e Projeção — via Gateway
 */
export async function getMargemProjecao(params) {
    const resp = await fetch(`${API_BASE}/v1/simulador/margem`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(params)
    });
    if (!resp.ok) throw new Error('API Offline');
    return await resp.json();
}

/**
 * Declaração — Inicialização
 */
export async function initDeclaracao() {
    const resp = await fetch(`${API_BASE}/v1/declaracao/init`, { 
        headers: getAuthHeaders() 
    });
    if (!resp.ok) throw new Error('Erro ao inicializar declaração');
    return await resp.json();
}

/**
 * Declaração — Validação
 */
export async function validarDeclaracao() {
    const resp = await fetch(`${API_BASE}/v1/declaracao/validar`, {
        method: 'POST',
        headers: getAuthHeaders()
    });
    if (!resp.ok) throw new Error('Erro ao validar declaração');
    return await resp.json();
}

/**
 * Declaração — Resumo
 */
export async function getResumoDeclaracao(data) {
    const resp = await fetch(`${API_BASE}/v1/declaracao/resumo`, {
        method: 'POST',
        headers: getAuthHeaders(),
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
        headers: getAuthHeaders(),
        body: JSON.stringify(data)
    });
    
    if (!resp.ok) {
        const errorText = await resp.text();
        throw new Error(`Erro ao processar split (${resp.status}): ${errorText}`);
    }
    
    return await resp.json();
}

/**
 * Buscar transações recentes
 */
export async function getRecentTransactions() {
    const resp = await fetch(`${API_BASE}/v1/split/recent`, {
        headers: getAuthHeaders()
    });
    if (!resp.ok) throw new Error('Erro ao buscar transações');
    return await resp.json();
}

/**
 * Download da Guia PDF
 */
export async function downloadGuiaPDF() {
    const resp = await fetch(`${API_BASE}/v1/declaracao/download`, {
        method: 'GET',
        headers: getAuthHeaders()
    });
    if (!resp.ok) throw new Error('Erro ao baixar PDF');
    return await resp.blob();
}

/**
 * Cria um EventSource (SSE) para receber atualizações em tempo real
 */
export function createSplitStream(nfeKey) {
    const sseUrl = `${API_BASE}/v1/split/stream/${nfeKey}`;
    return new EventSource(sseUrl);
}

/**
 * Cria um EventSource (SSE) para o stream global do dashboard.
 */
export function createDashboardStream() {
    const sseUrl = `${API_BASE}/v1/split/stream/dashboard`;
    return new EventSource(sseUrl);
}

/**
 * Upload de arquivo para processamento em lote (Bulk)
 */
export async function uploadBulkFile(file) {
    const formData = new FormData();
    formData.append('file', file);

    const headers = getAuthHeaders();
    delete headers['Content-Type']; // Let browser set boundary

    const resp = await fetch(`${API_BASE}/v1/bulk/upload`, {
        method: 'POST',
        headers: headers,
        body: formData
    });

    if (!resp.ok) throw new Error('Erro no upload em lote: ' + resp.status);
    return await resp.json();
}

/**
 * Reports - Tendência por período
 */
export async function getTrendsByPeriod() {
    const resp = await fetch(`${API_BASE}/v1/reports/trends/period`, {
        headers: getAuthHeaders()
    });
    if (!resp.ok) throw new Error('Erro ao buscar tendências por período');
    return await resp.json();
}

/**
 * Reports - Tendência por segmento
 */
export async function getTrendsBySegment() {
    const resp = await fetch(`${API_BASE}/v1/reports/trends/segment`, {
        headers: getAuthHeaders()
    });
    if (!resp.ok) throw new Error('Erro ao buscar tendências por segmento');
    return await resp.json();
}

/**
 * Handle OAuth2 Redirect tokens
 */
(function handleOAuth2Redirect() {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const email = params.get('email');
    const username = params.get('username');

    if (token) {
        localStorage.setItem('splitpay_token', token);
        if (email) {
            localStorage.setItem('splitpay_user', JSON.stringify({ token, email, username }));
        }
        
        // Limpa a URL e redireciona para o dashboard limpo
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    }
})();
