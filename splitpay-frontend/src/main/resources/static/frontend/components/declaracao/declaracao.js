/**
 * Declaração Component Logic
 */
import { state, updateState } from '../../state.js';
import { initDeclaracao, validarDeclaracao, getResumoDeclaracao } from '../../api.js';

const totalSteps = 5;

function init() {
    updateState('ui.wizardStep', 1);
    updateWizardUI();

    const btnNext = document.getElementById('btn-next');
    const btnPrev = document.getElementById('btn-prev');
    const btnApply = document.getElementById('btn-apply-credits');
    const optMensal = document.getElementById('opt-mensal');
    const optAnual = document.getElementById('opt-anual');

    if (btnNext) btnNext.addEventListener('click', nextStep);
    if (btnPrev) btnPrev.addEventListener('click', prevStep);
    if (btnApply) btnApply.addEventListener('click', applySmartCredits);
    
    if (optMensal) optMensal.addEventListener('click', () => selectWizardOption(optMensal));
    if (optAnual) optAnual.addEventListener('click', () => selectWizardOption(optAnual));
}

function selectWizardOption(element) {
    const cards = element.parentElement.querySelectorAll('.option-card');
    cards.forEach(c => c.classList.remove('selected'));
    element.classList.add('selected');
}

async function nextStep() {
    const btnNext = document.getElementById('btn-next');
    const { ui, user, data } = state;

    const getAuthHeaders = () => ({
        'Authorization': `Bearer ${user.jwt}`,
        'Content-Type': 'application/json'
    });

    if (ui.wizardStep === 1) {
        btnNext.disabled = true;
        btnNext.innerHTML = 'Carregando...';
        try {
            const result = await initDeclaracao(getAuthHeaders());
            updateState('data.wizard.faturamento', result.faturamentoBruto);
            updateState('data.wizard.baseCredito', result.baseCredito);
            
            document.getElementById('wdc-faturamento').value = parseFloat(result.faturamentoBruto).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
            document.getElementById('wdc-insumos').value = parseFloat(result.baseCredito).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
        } catch (e) {
            console.error(e);
        }
        btnNext.disabled = false;
        btnNext.innerHTML = 'Avançar';
    }

    if (ui.wizardStep === 2) {
        const loader = document.getElementById('validation-loader');
        btnNext.disabled = true;
        btnNext.innerHTML = 'Validando...';
        loader.style.display = 'block';
        
        try {
            const result = await validarDeclaracao(getAuthHeaders());
            updateState('data.wizard.creditoAI', result.creditosRecomendados);
            document.getElementById('ai-sug-val').innerText = 'Economia de ' + formatCurrency(result.creditosRecomendados);
        } catch (e) {
            console.error(e);
        }

        setTimeout(() => {
            btnNext.disabled = false;
            btnNext.innerHTML = 'Avançar';
            loader.style.display = 'none';
            updateState('ui.wizardStep', ui.wizardStep + 1);
            updateWizardUI();
        }, 1200);
        return;
    }

    if (ui.wizardStep === 3) {
        btnNext.disabled = true;
        btnNext.innerHTML = 'Gerando Resumo...';
        try {
            const result = await getResumoDeclaracao({
                faturamentoBruto: data.wizard.faturamento,
                aplicarCreditos: ui.creditsApplied,
                creditosExteriores: data.wizard.creditoAI
            }, getAuthHeaders());
            
            document.getElementById('res-fat').innerText = formatCurrency(data.wizard.faturamento);
            document.getElementById('res-ibs').innerText = formatCurrency(result.ibsApurado);
            document.getElementById('res-cbs').innerText = formatCurrency(result.cbsApurado);
            
            if (ui.creditsApplied) {
                document.getElementById('row-credits').style.display = 'flex';
                document.getElementById('res-cred').innerText = '- ' + formatCurrency(result.creditosAplicados);
            } else {
                document.getElementById('row-credits').style.display = 'none';
            }
            document.getElementById('final-tax-value').innerText = formatCurrency(result.valorLiquidoAPagar);
        } catch (e) {
            console.error(e);
        }
        btnNext.disabled = false;
        btnNext.innerHTML = 'Revisar & Gerar Guia';
    }

    if (ui.wizardStep < totalSteps) {
        updateState('ui.wizardStep', ui.wizardStep + 1);
        updateWizardUI();
    }
}

function prevStep() {
    if (state.ui.wizardStep > 1) {
        updateState('ui.wizardStep', state.ui.wizardStep - 1);
        updateWizardUI();
    }
}

function updateWizardUI() {
    const { ui } = state;
    for (let i = 1; i <= totalSteps; i++) {
        const stepEl = document.getElementById(`step-${i}`);
        if(stepEl) stepEl.classList.remove('active');
        const indicator = document.getElementById(`indicator-${i}`);
        
        if (i < ui.wizardStep) {
            if(indicator) {
                indicator.className = 'step-bubble completed';
                indicator.innerHTML = '✓';
            }
        } else if (i === ui.wizardStep) {
            if(indicator) {
                indicator.className = 'step-bubble active';
                indicator.innerHTML = i;
            }
        } else {
            if(indicator) {
                indicator.className = 'step-bubble';
                indicator.innerHTML = i;
            }
        }
    }
    
    const currStep = document.getElementById(`step-${ui.wizardStep}`);
    if(currStep) currStep.classList.add('active');
    
    const btnPrev = document.getElementById('btn-prev');
    const btnNext = document.getElementById('btn-next');
    
    if(btnPrev) btnPrev.style.visibility = ui.wizardStep === 1 ? 'hidden' : 'visible';
    
    if(btnNext) {
        if (ui.wizardStep === totalSteps) {
            btnNext.style.display = 'none';
        } else {
            btnNext.style.display = 'inline-block';
            btnNext.innerHTML = ui.wizardStep === totalSteps - 1 ? 'Revisar & Gerar Guia' : 'Avançar';
        }
    }
}

function applySmartCredits() {
    updateState('ui.creditsApplied', true);
    const btn = document.getElementById('btn-apply-credits');
    if(btn) {
        btn.innerHTML = `<span>✓ Desconto Aplicado</span>`;
        btn.style.background = 'var(--green)';
    }
}

function formatCurrency(val) {
    if (!val) return 'R$ 0,00';
    return 'R$ ' + parseFloat(val).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
init();
