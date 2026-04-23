/**
 * Simulador Component Logic
 */
import { getMargemProjecao } from '../../api.js';

export function init() {
    // Attach event listeners
    const priceField = document.getElementById('sim-price');
    const costField = document.getElementById('sim-cost');
    const creditPctField = document.getElementById('sim-credit-pct');
    const phaseField = document.getElementById('sim-phase');
    const btnSimCalc = document.getElementById('btn-sim-calc');

    if (priceField) priceField.addEventListener('input', calcMargem);
    if (costField) costField.addEventListener('input', calcMargem);
    if (creditPctField) creditPctField.addEventListener('input', calcMargem);
    if (phaseField) phaseField.addEventListener('change', calcMargem);
    
    if (btnSimCalc) {
        btnSimCalc.addEventListener('click', async () => {
            const originalText = btnSimCalc.innerText;
            btnSimCalc.innerText = 'Calculando...';
            btnSimCalc.disabled = true;
            
            await calcMargem();
            
            setTimeout(() => {
                btnSimCalc.innerText = originalText;
                btnSimCalc.disabled = false;
            }, 500);
        });
    }

    // Initial calculation
    setTimeout(calcMargem, 100);
}

function parseBRL(value) {
    if (typeof value !== 'string') return value;
    // Remove dots (thousand separators) and replace comma with dot (decimal separator)
    const clean = value.replace(/\./g, '').replace(',', '.');
    return parseFloat(clean) || 0;
}

async function calcMargem() {
    const priceField = document.getElementById('sim-price');
    if (!priceField) return;

    // Use robust parsing for PT-BR format
    const price = parseBRL(priceField.value);
    const cost = parseBRL(document.getElementById('sim-cost').value);
    const creditPct = parseFloat(document.getElementById('sim-credit-pct')?.value || 80) / 100 || 0;
    const faseAlvo = document.getElementById('sim-phase').value || '2029_pleno';
    
    // Default fallback tax rate (old system)
    const oldTaxRate = 0.18; 

    let rate = 0.27;
    try {
        const data = await getMargemProjecao({
            preco_atual: price,
            custo: cost,
            fase_alvo: faseAlvo,
            segmento: "geral"
        });
        if (data.aliquota !== undefined) rate = data.aliquota;
    } catch (e) {
        console.warn("Fallback to local calculation due to API error/CORS");
        if (faseAlvo === "2026_teste") rate = 0.01;
        else if (faseAlvo === "2027_cbs") rate = 0.135;
        else if (faseAlvo === "2028_transicao") rate = 0.22;
        else rate = 0.27;
    }

    const oldTax = price * oldTaxRate;
    const oldMargin = price - cost - oldTax;

    const ivaTax = price * rate;
    const baseCredit = cost * creditPct;
    const ivaCredit = baseCredit * rate;
    const dreCostIva = cost - ivaCredit;

    const newMargin = price - dreCostIva - ivaTax;
    const idealPrice = (oldMargin + cost - ivaCredit) / (1 - rate);
    const splitRetention = ivaTax;
    const repasseLiquido = price - splitRetention;

    renderSimResults({
        price, cost, oldTax, oldTaxRate, rate, oldMargin, ivaTax, ivaCredit, newMargin, idealPrice, splitRetention, repasseLiquido, creditPct
    });
}

function renderSimResults(data) {
    const { price, cost, oldTax, oldTaxRate, rate, oldMargin, ivaTax, ivaCredit, newMargin, idealPrice, splitRetention, repasseLiquido, creditPct } = data;
    const tbody = document.getElementById('sim-table-body');
    if (!tbody) return;

    const deltaMargem = newMargin - oldMargin;
    const fmtBRL = (v) => 'R$ ' + v.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    tbody.innerHTML = `
      <tr>
        <td>Receita Bruta (Faturamento)</td>
        <td style="text-align:right" class="mono">${fmtBRL(price)}</td>
        <td style="text-align:right; font-weight:600; color:var(--text)" class="mono">${fmtBRL(price)}</td>
        <td style="text-align:right; color:var(--text3)" class="mono">-</td>
      </tr>
      <tr>
        <td style="color:var(--red)">(-) Tributos s/ Vendas</td>
        <td style="text-align:right; color:var(--red)" class="mono">${fmtBRL(-oldTax)} <br>(${(oldTaxRate * 100).toFixed(1)}%)</td>
        <td style="text-align:right; color:var(--red); font-weight:600;" class="mono">${fmtBRL(-ivaTax)} <br>(${(rate * 100).toFixed(1)}%)</td>
        <td style="text-align:right; color:${(-ivaTax + oldTax) > 0 ? 'var(--green)' : 'var(--red)'}" class="mono">${(-ivaTax + oldTax) > 0 ? '+' : ''}${fmtBRL(-ivaTax + oldTax)}</td>
      </tr>
      <tr>
        <td>(-) Custo de Aquisição</td>
        <td style="text-align:right; color:var(--amber)" class="mono">${fmtBRL(-cost)}</td>
        <td style="text-align:right; color:var(--amber); font-weight:600" class="mono">${fmtBRL(-cost)}</td>
        <td style="text-align:right; color:var(--text3)" class="mono">-</td>
      </tr>
      <tr>
        <td style="color:var(--green)">(+) Recuperação de Tributos</td>
        <td style="text-align:right; color:var(--text3)" class="mono">${fmtBRL(0)}</td>
        <td style="text-align:right; color:var(--green); font-weight:600" class="mono">+${fmtBRL(ivaCredit)} <br>${(creditPct * 100).toFixed(0)}% base</td>
        <td style="text-align:right; color:var(--green)" class="mono">+${fmtBRL(ivaCredit)}</td>
      </tr>
      <tr style="background: rgba(255,255,255,0.02)">
        <td style="font-weight:700;">Margem Operacional DRE</td>
        <td style="text-align:right; font-weight:700" class="mono">${fmtBRL(oldMargin)}</td>
        <td style="text-align:right; font-weight:700; color:${newMargin >= oldMargin ? 'var(--green)' : 'var(--red)'}" class="mono">${fmtBRL(newMargin)}</td>
        <td style="text-align:right; font-weight:700; color:${deltaMargem >= 0 ? 'var(--green)' : 'var(--red)'}" class="mono">${deltaMargem >= 0 ? '+' : ''}${fmtBRL(deltaMargem)}</td>
      </tr>
    `;

    document.getElementById('r-margem-atual').textContent = fmtBRL(oldMargin);
    document.getElementById('r-margem-nova').textContent = fmtBRL(newMargin);
    document.getElementById('r-preco-ideal').textContent = fmtBRL(idealPrice);
    document.getElementById('r-split-retido').textContent = '-' + fmtBRL(splitRetention);
    document.getElementById('r-split-liquido').textContent = fmtBRL(repasseLiquido);
}
