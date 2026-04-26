/**
 * Dashboard Component Logic
 */
let retainedInterval = null;
let dashboardStream = null;

async function init() {
    await loadTransactions();
    startRetainedSimulation();
    startDashboardStream();
    
    const btnSimulate = document.getElementById('btn-simulate-nfe');
    if (btnSimulate) {
        btnSimulate.addEventListener('click', simulateNfeTransaction);
    }
}

async function simulateNfeTransaction() {
    const val = (Math.random() * 5000 + 50).toFixed(2);
    const nfeKey = '35260312...' + Math.floor(Math.random() * 90000 + 10000);
    
    try {
        const { processSplit } = await import('../../api.js');
        // We don't need to manually add the row here anymore, 
        // the Dashboard SSE stream will receive the broadcast and add it.
        await processSplit({
            nfe_key: nfeKey,
            valor_bruto: parseFloat(val),
            adquirente: 'Teste Adq.',
            segmento: 'VAREJO',
            fase: '2026'
        });
        console.log("Transaction processed, waiting for SSE broadcast...");
    } catch (e) {
        console.error("Failed to simulate NFe:", e);
    }
}

async function loadTransactions() {
    const tbody = document.getElementById('tx-body');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center">Carregando transações...</td></tr>';
    
    try {
        const { getRecentTransactions } = await import('../../api.js');
        const transactions = await getRecentTransactions();
        
        tbody.innerHTML = '';
        if (transactions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center">Nenhuma transação encontrada.</td></tr>';
            return;
        }

        // Sort by id descending to show newest first
        transactions.sort((a, b) => b.id - a.id).slice(0, 10).forEach(tx => {
            updateOrAddTransaction(tx, false);
        });
    } catch (e) {
        console.error("Error loading transactions:", e);
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:var(--error)">Erro ao carregar transações.</td></tr>';
    }
}

async function startDashboardStream() {
    try {
        const { createDashboardStream } = await import('../../api.js');
        dashboardStream = createDashboardStream();

        dashboardStream.addEventListener('TRANSACTION_UPDATE', (event) => {
            const data = JSON.parse(event.data);
            console.log("Dashboard Stream Received Update:", data);
            updateOrAddTransaction(data, true);
            
            // If it's a completion, update the total retained (IBS+CBS)
            if (data.ibs_retido || data.ibsRetido) {
                updateTotalRetained(parseFloat(data.ibs_retido || data.ibsRetido) + parseFloat(data.cbs_retido || data.cbsRetido));
            }
        });

        dashboardStream.onerror = (err) => {
            console.warn("Dashboard SSE connection lost. Reconnecting in 5s...");
            dashboardStream.close();
            setTimeout(startDashboardStream, 5000);
        };
    } catch (e) {
        console.error("Failed to start dashboard stream:", e);
    }
}

function updateOrAddTransaction(tx, animate) {
    const tbody = document.getElementById('tx-body');
    if (!tbody) return;

    const nfeKey = tx.nfeKey || tx.nfe_key;
    let tr = document.getElementById(`tx-${nfeKey}`);
    const isNew = !tr;

    if (isNew) {
        tr = document.createElement('tr');
        tr.id = `tx-${nfeKey}`;
        tbody.prepend(tr);
        
        // Remove oldest if more than 10
        if (tbody.children.length > 10) {
            tbody.removeChild(tbody.lastChild);
        }
    }

    const val = tx.valorBruto || tx.valor_bruto || 0;
    const ibs = tx.ibsRetido || tx.ibs_retido || 0;
    const cbs = tx.cbsRetido || tx.cbs_retido || 0;
    const status = (tx.status === 'COMPLETED' || tx.ibsRetido !== undefined) ? 'COMPLETED' : 'PENDING';

    let statusHtml = status === 'COMPLETED' 
        ? `<span class="badge badge-success">ROC Concluído</span>`
        : `<span class="badge badge-warning"><i class="fa-solid fa-circle-notch fa-spin" style="margin-right:4px;"></i> Processando...</span>`;

    tr.innerHTML = `
        <td style="color:var(--primary-light)">${nfeKey}</td>
        <td>R$ ${val.toLocaleString('pt-BR', {minimumFractionDigits: 2})}</td>
        <td>${ibs > 0 ? 'R$ ' + ibs.toLocaleString('pt-BR', {minimumFractionDigits: 2}) : '-'}</td>
        <td>${cbs > 0 ? 'R$ ' + cbs.toLocaleString('pt-BR', {minimumFractionDigits: 2}) : '-'}</td>
        <td>${tx.adquirente || '-'}</td>
        <td id="status-${nfeKey}">${statusHtml}</td>
        <td style="color:var(--text3); font-size: 11px;">${tx.createdAt ? new Date(tx.createdAt).toLocaleTimeString() : 'Agora'}</td>
    `;
    
    if (isNew && animate) {
        tr.classList.add('row-highlight');
        setTimeout(() => tr.classList.remove('row-highlight'), 3000);
    }
}

function updateTotalRetained(amount) {
    const re = document.getElementById('retained');
    if (!re) return;

    let text = re.innerText;
    let cleanText = text.replace(/\./g, '').replace(',', '.');
    let val = parseFloat(cleanText) || 0;
    
    val += amount;
    re.innerText = val.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    
    re.classList.add('value-pop');
    setTimeout(() => re.classList.remove('value-pop'), 1000);
}

function startRetainedSimulation() {
    if (retainedInterval) clearInterval(retainedInterval);

    const re = document.getElementById('retained');
    if (re) {
        retainedInterval = setInterval(() => {
            const currentRe = document.getElementById('retained');
            if (!currentRe) {
                clearInterval(retainedInterval);
                return;
            }
            // Small background increase to simulate other transactions
            updateTotalRetained(Math.random() * 2.50);
        }, 10000);
    }
}

init();
