/**
 * Conciliação Component Logic
 */
export function init() {
    loadMatches();
}

function loadMatches() {
    const ml = document.getElementById('match-list');
    if (!ml) return;
    
    ml.innerHTML = '<div style="color:var(--text3); font-size:12px; padding:10px;">Aguardando novas transações...</div>';
    
    // Simulação de dados iniciais
    setTimeout(() => {
        ml.innerHTML = '';
        addMatch(ml, "3526031200001234", "R$ 1.250,00", "R$ 12,50", "Match Exato", "Há 5s", false);
        addMatch(ml, "3526031200005678", "R$ 450,00", "R$ 4,50", "Match Exato", "Há 12s", false);
        
        // Simular uma nova chegando
        setTimeout(() => {
            simulateNewMatch(ml);
        }, 2000);
    }, 800);
}

function addMatch(container, nfe, valor, retencao, status, time, isProcessing) {
    const item = document.createElement('div');
    item.className = `match-item animate__animated animate__fadeInLeft ${isProcessing ? 'processing-pulse' : ''}`;
    
    item.innerHTML = `
        <div>
            <div class="match-nfe">NF-e ${nfe}</div>
            <div class="match-details">Valor: ${valor} | Retenção calculada: ${retencao}</div>
        </div>
        <div class="match-status">
            <span class="badge ${isProcessing ? 'badge-warning' : 'badge-success'}">${status}</span>
            <div class="match-time">${time}</div>
        </div>
    `;
    
    container.prepend(item);
    return item;
}

function simulateNewMatch(container) {
    const nfe = "352603120000" + Math.floor(Math.random() * 9000 + 1000);
    const item = addMatch(container, nfe, "R$ 890,00", "R$ 8,90", "Processando...", "Agora", true);
    
    // Transição de processamento para conciliado
    setTimeout(() => {
        item.classList.remove('processing-pulse');
        const badge = item.querySelector('.badge');
        badge.className = 'badge badge-success';
        badge.innerText = 'Match Exato';
        item.querySelector('.match-time').innerText = 'Há poucos segundos';
    }, 3000);
}

// Inicialização automática
if (document.getElementById('match-list')) {
    init();
}
