/**
 * Conciliação Component Logic
 */
function init() {
    loadMatches();
}

function loadMatches() {
    const ml = document.getElementById('match-list');
    if (!ml) return;
    ml.innerHTML = '';
    for (let i = 1; i <= 3; i++) {
        ml.innerHTML += `
            <div class="match-item">
                <div>
                    <div class="match-nfe">NF-e 352603120000${Math.floor(Math.random() * 9000 + 1000)}</div>
                    <div class="match-details">Valor: R$ 850.00 | Retenção calculada: R$ 8.50</div>
                </div>
                <div class="match-status">
                    <span class="badge badge-success">Match Exato</span>
                    <div class="match-time">Recebido da Adquirente há 2s</div>
                </div>
            </div>
        `;
    }
}
