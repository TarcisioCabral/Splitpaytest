/**
 * Dashboard Component Logic
 */
let retainedInterval = null;

function init() {
    loadTransactions();
    startRetainedSimulation();
    
    // Attach event listener instead of inline onclick
    const btnSimulate = document.getElementById('btn-simulate-nfe');
    if (btnSimulate) {
        btnSimulate.addEventListener('click', simulateNfeTransaction);
    }
}

async function simulateNfeTransaction() {
    const tbody = document.getElementById('tx-body');
    if (!tbody) return;

    const val = (Math.random() * 5000 + 50).toFixed(2);
    const nfeKey = '35260312...' + Math.floor(Math.random() * 90000 + 10000);
    
    // Add pending row
    const tr = document.createElement('tr');
    tr.id = `tx-${nfeKey}`;
    tr.innerHTML = `
        <td style="color:var(--primary-light)">${nfeKey}</td>
        <td>R$ ${val}</td>
        <td>-</td>
        <td>-</td>
        <td>Teste Adq.</td>
        <td id="status-${nfeKey}"><span class="badge badge-warning"><i class="fa-solid fa-circle-notch fa-spin" style="margin-right:4px;"></i> Processando...</span></td>
        <td style="color:var(--text3); font-size: 11px;">Agora</td>
    `;
    tbody.prepend(tr);

    try {
        // Call API
        const { processSplit } = await import('../../api.js');
        await processSplit({
            nfe_key: nfeKey,
            valor_bruto: parseFloat(val),
            adquirente: 'Teste Adq.',
            segmento: 'VAREJO',
            fase: '2026'
        });

        // Start SSE Listener for real-time conciliation feedback
        const sseUrl = `http://localhost:8081/v1/split/stream/${nfeKey}`;
        console.log("Connecting to SSE:", sseUrl);
        const eventSource = new EventSource(sseUrl);

        eventSource.addEventListener('COMPLETED', (event) => {
            const response = JSON.parse(event.data);
            console.log("SSE Received COMPLETED:", response);
            
            // Update UI to success
            const statusCell = document.getElementById(`status-${nfeKey}`);
            if (statusCell) {
                statusCell.innerHTML = `<span class="badge badge-success">ROC Concluído</span>`;
            }
            
            // Close connection
            eventSource.close();
        });
        
        eventSource.addEventListener('INIT', (event) => {
            console.log("SSE Initialized:", event.data);
        });

        eventSource.onerror = (err) => {
            console.error("SSE Error:", err);
            eventSource.close();
        };

    } catch (e) {
        console.error("Failed to simulate NFe:", e);
        const statusCell = document.getElementById(`status-${nfeKey}`);
        if (statusCell) {
            statusCell.innerHTML = `<span class="badge badge-error">Erro</span>`;
        }
    }
}

function loadTransactions() {
    const tbody = document.getElementById('tx-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    
    const adquirentes = ['Cielo', 'Stone', 'Rede', 'PagSeguro'];
    
    for (let i = 0; i < 6; i++) {
        const val = (Math.random() * 5000 + 50).toFixed(2);
        const ibs = (val * 0.005).toFixed(2);
        const cbs = (val * 0.005).toFixed(2);
        const adq = adquirentes[Math.floor(Math.random() * adquirentes.length)];
        const key = '35260312...' + Math.floor(Math.random() * 9000 + 1000);
        
        let statusHtml = `<span class="badge badge-success">ROC Concluído</span>`;
        if (Math.random() > 0.8) {
            statusHtml = `<span class="badge badge-warning">Pendente</span>`;
        }
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="color:var(--primary-light)">${key}</td>
            <td>R$ ${val}</td>
            <td>R$ ${ibs}</td>
            <td>R$ ${cbs}</td>
            <td>${adq}</td>
            <td>${statusHtml}</td>
            <td style="color:var(--text3); font-size: 11px;">Agora</td>
        `;
        tbody.appendChild(tr);
    }
}

function startRetainedSimulation() {
    // Clear previous interval if any (to prevent leaks in SPA)
    if (retainedInterval) {
        clearInterval(retainedInterval);
    }

    const re = document.getElementById('retained');
    if (re) {
        retainedInterval = setInterval(() => {
            // Check if element still exists (tab might have changed)
            const currentRe = document.getElementById('retained');
            if (!currentRe) {
                clearInterval(retainedInterval);
                retainedInterval = null;
                return;
            }

            let text = currentRe.innerText;
            // Robust parsing for PT-BR format (e.g. 4.283,00)
            let cleanText = text.replace(/\./g, '').replace(',', '.');
            let val = parseFloat(cleanText) || 0;
            
            val += (Math.random() * 8.50);
            currentRe.innerText = val.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }, 4000);
    }
}
init();
