function switchTab(tabId) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
    document.getElementById('page-' + tabId).classList.add('active');
    event.currentTarget.classList.add('active');
}

function toggleApi(el) {
    const body = el.nextElementSibling;
    body.classList.toggle('open');
}

function loadTransactions() {
    const tbody = document.getElementById('tx-body');
    if(!tbody) return;
    tbody.innerHTML = '';
    const adquirentes = ['Cielo', 'Stone', 'Rede', 'PagSeguro'];
    
    for(let i=0; i<6; i++) {
        const val = (Math.random() * 5000 + 50).toFixed(2);
        const ibs = (val * 0.005).toFixed(2);
        const cbs = (val * 0.005).toFixed(2);
        const adq = adquirentes[Math.floor(Math.random() * adquirentes.length)];
        const key = '35260312...' + Math.floor(Math.random() * 9000 + 1000);
        
        let statusHtml = `<span style="color:#34d399; background:rgba(52,211,153,0.1); padding:2px 6px; border-radius:4px; font-size:12px;">ROC Concluído</span>`;
        if (Math.random() > 0.8) {
            statusHtml = `<span style="color:#fbbf24; background:rgba(251,191,36,0.1); padding:2px 6px; border-radius:4px; font-size:12px;">Pendente</span>`;
        }
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="color:#60a5fa">${key}</td>
            <td>R$ ${val}</td>
            <td>R$ ${ibs}</td>
            <td>R$ ${cbs}</td>
            <td>${adq}</td>
            <td>${statusHtml}</td>
            <td style="color:#64748b; font-size: 11px;">Agora</td>
        `;
        tbody.appendChild(tr);
    }
}

function fmtBRL(v) {
    return 'R$ ' + v.toFixed(2).replace('.',',');
}

function calcMargem() {
    const priceField = document.getElementById('sim-price');
    if(!priceField) return;

    const price = parseFloat(document.getElementById('sim-price').value) || 0;
    const cost  = parseFloat(document.getElementById('sim-cost').value)  || 0;
    const creditPct = parseFloat(document.getElementById('sim-credit-pct')?.value || 80) / 100 || 0;
    const rate  = parseFloat(document.getElementById('sim-phase').value) || 0;
    const oldTaxRate = parseFloat(document.getElementById('sim-old-tax')?.value || 18) / 100 || 0;

    // Cenário Atual: Receita - Tributos - Custo
    const oldTax = price * oldTaxRate;
    const oldMargin = price - cost - oldTax; 

    // Cenário IVA Dual (Não-cumulativo)
    const ivaTax = price * rate;
    const baseCredit = cost * creditPct;
    const ivaCredit = baseCredit * rate;
    
    // Impacto do custo no DRE: Custo Pago - Crédito que pode compensar IVA das Vendas
    const dreCostIva = cost - ivaCredit;

    const newMargin = price - dreCostIva - ivaTax; 
    
    // Preço Ideal para manter a margem antiga:
    // price_new - (cost - cost*creditPct*rate) - price_new*rate = oldMargin
    // price_new * (1 - rate) = oldMargin + cost - baseCredit*rate
    const idealPrice = (oldMargin + cost - ivaCredit) / (1 - rate);
    
    // Split Payment Retention (Adquirente retém taxa cheia sobre faturamento)
    const splitRetention = ivaTax;
    const repasseLiquido = price - splitRetention;

    // Render Table DRE
    const tbody = document.getElementById('sim-table-body');
    if(!tbody) return;
    
    const deltaMargem = newMargin - oldMargin;
    
    tbody.innerHTML = `
      <tr>
        <td style="padding:16px 20px; font-weight:600; font-size:12px; color:var(--text1)">Receita Bruta (Faturamento)</td>
        <td style="padding:16px 20px; text-align:right" class="mono">${fmtBRL(price)}</td>
        <td style="padding:16px 20px; text-align:right; font-weight:600; color:var(--text1)" class="mono">${fmtBRL(price)}</td>
        <td style="padding:16px 20px; text-align:right; color:var(--text3)" class="mono">-</td>
      </tr>
      <tr>
        <td style="padding:16px 20px; font-size:12px; color:var(--red2)">(-) Tributos s/ Vendas<div style="font-size:10px;opacity:0.6;margin-top:4px">ICMS/ISS/PIS/COFINS vs IBS/CBS</div></td>
        <td style="padding:16px 20px; text-align:right; color:var(--red2)" class="mono">${fmtBRL(-oldTax)} <br><span style="font-size:10px; color:var(--text3)">(${(oldTaxRate*100).toFixed(1)}%)</span></td>
        <td style="padding:16px 20px; text-align:right; color:var(--red2); font-weight:600;" class="mono">${fmtBRL(-ivaTax)} <br><span style="font-size:10px; color:var(--accent2)">(${(rate*100).toFixed(1)}%)</span></td>
        <td style="padding:16px 20px; text-align:right; color:${(-ivaTax + oldTax) > 0 ? 'var(--green2)' : 'var(--red2)'}" class="mono">${(-ivaTax + oldTax) > 0 ? '+' : ''}${fmtBRL(-ivaTax + oldTax)}</td>
      </tr>
      <tr style="border-bottom: 1px solid var(--border)">
        <td style="padding:16px 20px; font-size:12px; color:var(--text2); font-weight: 500;">(=) Receita Líquida</td>
        <td style="padding:16px 20px; text-align:right" class="mono">${fmtBRL(price - oldTax)}</td>
        <td style="padding:16px 20px; text-align:right; color:var(--text1)" class="mono">${fmtBRL(price - ivaTax)}</td>
        <td style="padding:16px 20px; text-align:right" class="mono">${((price - ivaTax) - (price - oldTax)) > 0 ? '+' : ''}${fmtBRL((price - ivaTax) - (price - oldTax))}</td>
      </tr>
      <tr>
        <td style="padding:16px 20px; font-size:12px; color:var(--amber2)">(-) Custo de Aquisição<div style="font-size:10px;opacity:0.6;margin-top:4px">Insumos, Serviços e Despesas</div></td>
        <td style="padding:16px 20px; text-align:right; color:var(--amber2)" class="mono">${fmtBRL(-cost)}</td>
        <td style="padding:16px 20px; text-align:right; color:var(--amber2); font-weight:600" class="mono">${fmtBRL(-cost)}</td>
        <td style="padding:16px 20px; text-align:right; color:var(--text3)" class="mono">-</td>
      </tr>
      <tr style="border-bottom: 1px solid var(--border)">
        <td style="padding:16px 20px; font-size:12px; color:var(--green2)">(+) Recuperação de Tributos<div style="font-size:10px;opacity:0.6;margin-top:4px">Créditos IBS/CBS s/ Entradas (Não Cumu.)</div></td>
        <td style="padding:16px 20px; text-align:right; color:var(--text3)" class="mono">${fmtBRL(0)} <br><span style="font-size:10px">(Modelo Cumulativo)</span></td>
        <td style="padding:16px 20px; text-align:right; color:var(--green2); font-weight:600" class="mono">+${fmtBRL(ivaCredit)} <br><span style="font-size:10px">${(creditPct*100).toFixed(0)}% base creditável</span></td>
        <td style="padding:16px 20px; text-align:right; color:var(--green2)" class="mono">+${fmtBRL(ivaCredit)}</td>
      </tr>
      <tr style="background: rgba(255,255,255,0.02)">
        <td style="padding:16px 20px; font-weight:700; font-size:13px;">Margem Operacional DRE</td>
        <td style="padding:16px 20px; text-align:right; font-weight:700" class="mono">${fmtBRL(oldMargin)}</td>
        <td style="padding:16px 20px; text-align:right; font-weight:700; color:${newMargin >= oldMargin ? 'var(--green2)' : 'var(--red2)'}" class="mono">${fmtBRL(newMargin)}</td>
        <td style="padding:16px 20px; text-align:right; font-weight:700; color:${deltaMargem >= 0 ? 'var(--green2)' : 'var(--red2)'}" class="mono">${deltaMargem >= 0 ? '+' : ''}${fmtBRL(deltaMargem)}</td>
      </tr>
    `;

    document.getElementById('r-margem-atual').textContent = fmtBRL(oldMargin);
    document.getElementById('r-margem-atual-pct').textContent = (price > 0 ? (oldMargin/price)*100 : 0).toFixed(2) + '% de margem';
    
    document.getElementById('r-margem-nova').textContent = fmtBRL(newMargin);
    const newPctEl = document.getElementById('r-margem-nova-pct');
    newPctEl.textContent = (price > 0 ? (newMargin/price)*100 : 0).toFixed(2) + '% de margem';
    if(newMargin >= oldMargin) { newPctEl.className = 'metric-sub up'; newPctEl.style.color='var(--green2)'; }
    else { newPctEl.className = 'metric-sub down'; newPctEl.style.color='var(--red2)'; }
    
    document.getElementById('r-preco-ideal').textContent = fmtBRL(idealPrice);
    const diffPrice = idealPrice - price;
    const diffEl = document.getElementById('r-preco-diff');
    diffEl.textContent = (diffPrice > 0 ? 'Aumento de ' : diffPrice < 0 ? 'Redução de ' : 'Sem ajuste ') + fmtBRL(Math.abs(diffPrice)) + ' (' + (Math.abs(diffPrice)/price*100).toFixed(1) + '%)';
    if(diffPrice > 0) diffEl.style.color = 'var(--red2)';
    else if(diffPrice < 0) diffEl.style.color = 'var(--green2)';
    else diffEl.style.color = 'var(--text2)';

    document.getElementById('r-split-retido').textContent = '-' + fmtBRL(splitRetention);
    document.getElementById('r-split-liquido').textContent = fmtBRL(repasseLiquido);
}

function loadMatches() {
    const ml = document.getElementById('match-list');
    if(!ml) return;
    ml.innerHTML = '';
    for(let i=1; i<=3; i++) {
        ml.innerHTML += `
            <div style="background:rgba(0,0,0,0.2); border:1px solid rgba(255,255,255,0.08); padding:1rem; border-radius:8px; margin-bottom:1rem; display:flex; justify-content:space-between; align-items:center;">
                <div>
                    <div style="font-family:var(--mono); color:#60a5fa; margin-bottom:4px">NF-e 352603120000${Math.floor(Math.random()*9000+1000)}</div>
                    <div style="font-size:12px; color:#94a3b8">Valor: R$ 850.00 | Retenção calculada: R$ 8.50</div>
                </div>
                <div style="text-align:right">
                    <span style="color:#34d399; font-size:12px; border:1px solid #34d399; padding:2px 6px; border-radius:4px">Match Exato</span>
                    <div style="font-size:10px; color:#64748b; margin-top:4px">Recebido da Adquirente há 2s</div>
                </div>
            </div>
        `;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadTransactions();
    loadMatches();
    calcMargem();
    
    const re = document.getElementById('retained');
    if(re) {
        setInterval(() => {
            let val = parseFloat(re.innerText.replace('.','').replace(',','.'));
            val += (Math.random() * 8.50);
            re.innerText = val.toLocaleString('pt-BR', {minimumFractionDigits:2, maximumFractionDigits:2});
        }, 4000);
    }
});

// --- WIZARD LOGIC ---
let currentStep = 1;
const totalSteps = 5;
let creditsApplied = false;
let wizardData = {
    faturamento: '1250000.00',
    creditoAI: '0.00'
};

function formatCurrency(val) {
    if(!val) return 'R$ 0,00';
    return 'R$ ' + parseFloat(val).toLocaleString('pt-BR', {minimumFractionDigits: 2, maximumFractionDigits: 2});
}

function applySmartCredits() {
    creditsApplied = true;
    const btn = document.getElementById('btn-apply-credits');
    btn.innerHTML = `<span>✓ Desconto Aplicado na Memória</span>`;
    btn.style.background = 'var(--green)';
    btn.style.borderColor = 'var(--green)';
}

async function nextStep() {
    const btnNext = document.getElementById('btn-next');

    // Mover do Passo 1 para o 2: Buscar Dados auto-preenchidos do Backend (Java)
    if (currentStep === 1) {
        btnNext.disabled = true;
        btnNext.innerHTML = 'Carregando...';
        try {
            const resp = await fetch('http://localhost:8081/v1/declaracao/init');
            const data = await resp.json();
            wizardData.faturamento = data.faturamentoBruto;
            document.getElementById('wdc-faturamento').value = parseFloat(data.faturamentoBruto).toLocaleString('pt-BR', {minimumFractionDigits: 2});
            document.getElementById('wdc-insumos').value = parseFloat(data.baseCredito).toLocaleString('pt-BR', {minimumFractionDigits: 2});
        } catch (e) {
            console.error("Erro ao comunicar com backend Java", e);
        }
        btnNext.disabled = false;
        btnNext.innerHTML = 'Avançar';
    }

    // Mover do Passo 2 para o 3: Validar na API em tempo real
    if (currentStep === 2) {
        const loader = document.getElementById('validation-loader');
        btnNext.disabled = true;
        btnNext.innerHTML = 'Validando e Auditando API...';
        btnNext.style.opacity = '0.7';
        loader.style.display = 'block';
        
        try {
            const resp = await fetch('http://localhost:8081/v1/declaracao/validar', { method: 'POST' });
            const data = await resp.json();
            wizardData.creditoAI = data.creditosRecomendados;
            document.getElementById('ai-sug-val').innerText = 'Economia de ' + formatCurrency(data.creditosRecomendados);
        } catch (e) {
            console.error(e);
        }

        // Delay extra apenas para efeito de UX progressiva
        setTimeout(() => {
            btnNext.disabled = false;
            btnNext.innerHTML = 'Avançar';
            btnNext.style.opacity = '1';
            loader.style.display = 'none';
            currentStep++;
            updateWizardUI();
        }, 1200);
        return;
    }

    // Mover do Passo 3 para o 4: Gerar Resumo via Backend Java
    if (currentStep === 3) {
        btnNext.disabled = true;
        btnNext.innerHTML = 'Gerando Resumo...';
        try {
            const resp = await fetch('http://localhost:8081/v1/declaracao/resumo', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    faturamentoBruto: wizardData.faturamento,
                    aplicarCreditos: creditsApplied,
                    creditosExteriores: wizardData.creditoAI
                })
            });
            const data = await resp.json();
            
            document.getElementById('res-fat').innerText = formatCurrency(wizardData.faturamento);
            document.getElementById('res-ibs').innerText = formatCurrency(data.ibsApurado);
            document.getElementById('res-cbs').innerText = formatCurrency(data.cbsApurado);
            
            if (creditsApplied) {
                document.getElementById('row-credits').style.display = 'flex';
                document.getElementById('res-cred').innerText = '- ' + formatCurrency(data.creditosAplicados);
            } else {
                document.getElementById('row-credits').style.display = 'none';
            }
            document.getElementById('final-tax-value').innerText = formatCurrency(data.valorLiquidoAPagar);
        } catch(e) { console.error(e); }
        btnNext.disabled = false;
        btnNext.innerHTML = 'Revisar & Gerar Guia';
    }

    if (currentStep < totalSteps) {
        currentStep++;
        updateWizardUI();
    }
}

function prevStep() {
    if (currentStep > 1) {
        currentStep--;
        updateWizardUI();
    }
}

function updateWizardUI() {
    for (let i = 1; i <= totalSteps; i++) {
        document.getElementById(`step-${i}`).classList.remove('active');
        const indicator = document.getElementById(`indicator-${i}`);
        
        if (i < currentStep) {
            indicator.className = 'step-bubble completed';
            indicator.innerHTML = '✓';
        } else if (i === currentStep) {
            indicator.className = 'step-bubble active';
            indicator.innerHTML = i;
        } else {
            indicator.className = 'step-bubble';
            indicator.innerHTML = i;
        }
    }
    
    document.getElementById(`step-${currentStep}`).classList.add('active');
    
    const btnPrev = document.getElementById('btn-prev');
    const btnNext = document.getElementById('btn-next');
    
    btnPrev.style.visibility = currentStep === 1 ? 'hidden' : 'visible';
    
    if (currentStep === totalSteps) {
        btnNext.style.display = 'none';
    } else {
        btnNext.style.display = 'inline-block';
        btnNext.innerHTML = currentStep === totalSteps - 1 ? 'Revisar & Gerar Guia' : 'Avançar';
    }
}

function selectWizardOption(element) {
    const cards = element.parentElement.querySelectorAll('.option-card');
    cards.forEach(c => c.classList.remove('selected'));
    element.classList.add('selected');
}

// Add listener to reset wizard when opening the tab
document.querySelectorAll('.nav-tab').forEach(tab => {
    tab.addEventListener('click', (e) => {
        if(e.target.innerText.includes('Declaração')) {
            currentStep = 1;
            updateWizardUI();
        }
    });
});