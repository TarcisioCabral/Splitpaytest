/**
 * Dashboard Component Logic
 */
let retainedInterval = null;
let dashboardStream = null;
let trendChart = null;
let segmentChart = null;

async function init() {
    await loadTransactions();
    await initAnalytics();
    startRetainedSimulation();
    startDashboardStream();
    
    initSimulationModal();
    initBulkUpload();
}

function initSimulationModal() {
    const btnSimulate = document.getElementById('btn-simulate-nfe');
    const modal = document.getElementById('modal-simula-nfe');
    const closeBtns = document.querySelectorAll('.close-modal');
    const btnGenKey = document.getElementById('btn-gen-key');
    const form = document.getElementById('form-simulation');

    if (btnSimulate) btnSimulate.addEventListener('click', () => {
        modal.classList.add('active');
        generateRandomNfeKey();
        resetLifecycle();
    });

    closeBtns.forEach(btn => btn.addEventListener('click', () => {
        modal.classList.remove('active');
    }));

    if (btnGenKey) btnGenKey.addEventListener('click', generateRandomNfeKey);

    if (form) form.addEventListener('submit', handleSimulationSubmit);
}

function generateRandomNfeKey() {
    const input = document.getElementById('sim-nfe-key');
    if (!input) return;
    const key = Array.from({length: 44}, () => Math.floor(Math.random() * 10)).join('');
    input.value = key;
}

function resetLifecycle() {
    const lifecycle = document.getElementById('sim-lifecycle');
    lifecycle.style.display = 'none';
    document.querySelectorAll('.step').forEach(s => s.classList.remove('active', 'completed'));
    document.getElementById('btn-submit-sim').disabled = false;
    document.getElementById('btn-submit-sim').innerText = 'Iniciar Simulação';
}

async function handleSimulationSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('btn-submit-sim');
    const lifecycle = document.getElementById('sim-lifecycle');
    const nfeKey = document.getElementById('sim-nfe-key').value;
    const valor = parseFloat(document.getElementById('sim-valor').value);
    const segmento = document.getElementById('sim-segmento').value;
    const adquirente = document.getElementById('sim-adquirente').value;

    btn.disabled = true;
    btn.innerText = 'Processando...';
    lifecycle.style.display = 'flex';

    try {
        // Step 1: Send
        document.getElementById('step-1').classList.add('active');
        const { processSplit } = await import('../../api.js');
        
        await new Promise(r => setTimeout(r, 800)); // Visual delay
        document.getElementById('step-1').classList.replace('active', 'completed');
        document.getElementById('step-2').classList.add('active');

        // Step 2: Split Calculation
        await processSplit({
            nfe_key: nfeKey,
            valor_bruto: valor,
            adquirente: adquirente,
            segmento: segmento,
            fase: '2026_teste'
        });

        await new Promise(r => setTimeout(r, 1000)); // Visual delay
        document.getElementById('step-2').classList.replace('active', 'completed');
        document.getElementById('step-3').classList.add('active');

        // Step 3: Finished
        await new Promise(r => setTimeout(r, 600)); 
        document.getElementById('step-3').classList.replace('active', 'completed');
        
        btn.innerText = 'Concluído!';
        setTimeout(() => {
            document.getElementById('modal-simula-nfe').classList.remove('active');
            resetLifecycle();
        }, 1500);

    } catch (e) {
        console.error("Simulation failed:", e);
        btn.innerText = 'Erro na Simulação';
        btn.style.background = 'var(--error)';
        setTimeout(() => {
            btn.style.background = '';
            btn.disabled = false;
            btn.innerText = 'Tentar Novamente';
        }, 3000);
    }
}

function initBulkUpload() {
    const btnBulk = document.getElementById('btn-bulk-upload');
    const fileInput = document.getElementById('bulk-file-input');

    if (btnBulk && fileInput) {
        btnBulk.addEventListener('click', () => fileInput.click());
        fileInput.addEventListener('change', handleBulkFileSelect);
    }
}

async function handleBulkFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    const btnBulk = document.getElementById('btn-bulk-upload');
    const originalHtml = btnBulk ? btnBulk.innerHTML : '';

    if (btnBulk) {
        btnBulk.disabled = true;
        btnBulk.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processando...';
    }

    try {
        const { uploadBulkFile } = await import('../../api.js');
        const result = await uploadBulkFile(file);
        
        console.log("Bulk upload success:", result);
        if (btnBulk) {
            btnBulk.innerHTML = `<i class="fa-solid fa-check"></i> ${result.count} Criadas!`;
            setTimeout(() => { 
                btnBulk.innerHTML = originalHtml; 
                btnBulk.disabled = false;
                event.target.value = ''; // Reset input
            }, 3000);
        }
        
        // Refresh list after a short delay
        setTimeout(loadTransactions, 1000);
    } catch (e) {
        console.error("Bulk upload failed:", e);
        if (btnBulk) {
            btnBulk.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> Erro no upload';
            btnBulk.style.background = 'var(--error)';
            setTimeout(() => { 
                btnBulk.innerHTML = originalHtml; 
                btnBulk.disabled = false;
                btnBulk.style.background = '';
                event.target.value = '';
            }, 3000);
        }
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
            updateMainLifecycle(data);
            
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
        
        // Refresh analytics on new transactions
        initAnalytics();
    }
}

async function initAnalytics() {
    try {
        const { getTrendsByPeriod, getTrendsBySegment } = await import('../../api.js');
        
        const periodData = await getTrendsByPeriod();
        const segmentData = await getTrendsBySegment();
        
        renderTrendChart(periodData);
        renderSegmentChart(segmentData);
        updateProjections(segmentData);
    } catch (e) {
        console.error("Failed to load analytics:", e);
    }
}

function renderTrendChart(data) {
    const ctx = document.getElementById('trendChart');
    if (!ctx) return;

    const labels = data.map(d => new Date(d.data).toLocaleDateString('pt-BR'));
    const totals = data.map(d => parseFloat(d.total_ibs) + parseFloat(d.total_cbs));

    if (trendChart) trendChart.destroy();

    trendChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Retenção Total (R$)',
                data: totals,
                borderColor: '#6366f1',
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { grid: { display: false } }
            }
        }
    });
}

function renderSegmentChart(data) {
    const ctx = document.getElementById('segmentChart');
    if (!ctx) return;

    const labels = data.map(d => d.segmento);
    const totals = data.map(d => parseFloat(d.total_ibs) + parseFloat(d.total_cbs));

    if (segmentChart) segmentChart.destroy();

    segmentChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: totals,
                backgroundColor: ['#6366f1', '#a855f7', '#ec4899', '#f59e0b', '#10b981'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right', labels: { color: '#94a3b8', font: { size: 10 } } }
            }
        }
    });
}

function updateProjections(data) {
    const currentEl = document.getElementById('proj-current');
    const futureEl = document.getElementById('proj-future');
    if (!currentEl || !futureEl) return;

    const totalRetained2026 = data.reduce((acc, d) => acc + parseFloat(d.total_ibs) + parseFloat(d.total_cbs), 0);
    
    // In 2026, total rate is 1% (0.5% IBS + 0.5% CBS)
    // In 2027+, total rate is projected at ~26.5%
    const totalFuture = (totalRetained2026 / 0.01) * 0.265;

    currentEl.innerText = totalRetained2026.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    futureEl.innerText = totalFuture.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
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

function updateMainLifecycle(data) {
    const nfeKey = data.nfeKey || data.nfe_key;
    const infoEl = document.getElementById('lifecycle-nfe-info');
    if (infoEl) infoEl.innerText = `NFe Ativa: ${nfeKey}`;

    const steps = {
        emissao: document.getElementById('step-emissao'),
        calculo: document.getElementById('step-calculo'),
        conciliacao: document.getElementById('step-conciliacao'),
        repasse: document.getElementById('step-repasse')
    };

    if (!steps.emissao) return;

    const resetSteps = () => {
        Object.values(steps).forEach(s => {
            s.classList.remove('active', 'completed');
            const statusText = s.querySelector('.step-status');
            if (s.id === 'step-emissao') statusText.innerText = 'Aguardando';
            else statusText.innerText = 'Pendente';
        });
    };

    // Check if it's a new transaction (from TransactionService) or a completion (from ConciliationEventListener)
    if (data.valorBruto || data.valor_bruto) {
        // Initial stages
        resetSteps();
        steps.emissao.classList.add('completed');
        steps.emissao.querySelector('.step-status').innerText = 'Concluído';
        
        steps.calculo.classList.add('active');
        steps.calculo.querySelector('.step-status').innerText = 'Em processamento';
        
        // After a small delay, complete calculation
        setTimeout(() => {
            steps.calculo.classList.replace('active', 'completed');
            steps.calculo.querySelector('.step-status').innerText = 'Calculado';
            
            steps.conciliacao.classList.add('active');
            steps.conciliacao.querySelector('.step-status').innerText = 'Cruzando dados';
        }, 1000);
    } else if (data.status === 'COMPLETED') {
        // Final stages
        if (steps.conciliacao.classList.contains('active') || steps.conciliacao.classList.contains('completed')) {
             steps.conciliacao.classList.replace('active', 'completed');
             steps.conciliacao.classList.add('completed');
             steps.conciliacao.querySelector('.step-status').innerText = 'Conciliado';
             
             steps.repasse.classList.add('active');
             steps.repasse.querySelector('.step-status').innerText = 'Provisionado';
             
             setTimeout(() => {
                 steps.repasse.classList.replace('active', 'completed');
                 steps.repasse.querySelector('.step-status').innerText = 'Finalizado';
             }, 1200);
        }
    }
}

init();
