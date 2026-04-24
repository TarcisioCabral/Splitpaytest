/**
 * API Client for SplitPay IVA
 */

const CONFIG = {
    TRANSACTION_SVC: 'http://localhost:8081',
    CONCILIATION_SVC: 'http://localhost:8082',
    GATEWAY: 'http://localhost:8085',
    KEYCLOAK: 'http://localhost:8180/realms/splitpay/protocol/openid-connect/token'
};

export async function login(username, password) {
    const body = new URLSearchParams({
        client_id: 'splitpay-app',
        username: username,
        password: password,
        grant_type: 'password'
    });

    const resp = await fetch(CONFIG.KEYCLOAK, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body
    });

    if (!resp.ok) throw new Error('Falha na autenticação');
    return await resp.json();
}

export async function getMargemProjecao(params) {
    try {
        const resp = await fetch(`${CONFIG.TRANSACTION_SVC}/v1/simulador/margem`, {
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

export async function initDeclaracao(headers) {
    const resp = await fetch(`${CONFIG.GATEWAY}/v1/declaracao/init`, { headers });
    if (!resp.ok) throw new Error('Erro ao inicializar declaração');
    return await resp.json();
}

export async function validarDeclaracao(headers) {
    const resp = await fetch(`${CONFIG.GATEWAY}/v1/declaracao/validar`, { 
        method: 'POST',
        headers 
    });
    if (!resp.ok) throw new Error('Erro ao validar declaração');
    return await resp.json();
}

export async function getResumoDeclaracao(data, headers) {
    const resp = await fetch(`${CONFIG.GATEWAY}/v1/declaracao/resumo`, {
        method: 'POST',
        headers,
        body: JSON.stringify(data)
    });
    if (!resp.ok) throw new Error('Erro ao gerar resumo');
    return await resp.json();
}

export async function processSplit(data) {
    const resp = await fetch(`${CONFIG.TRANSACTION_SVC}/v1/split/process`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    return await resp.json();
}
