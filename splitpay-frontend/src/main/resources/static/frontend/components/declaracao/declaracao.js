/**
 * Declaração Component Logic
 */
import { state, updateState } from '../../state.js';
import { initDeclaracao, validarDeclaracao, getResumoDeclaracao, downloadGuiaPDF } from '../../api.js';

const totalSteps = 5;

export function init() {
    console.log("Iniciando Wizard de Declaração...");
    // Reset wizard state on init
    updateState('ui.wizardStep', 1);
    updateWizardUI();

    const btnNext = document.getElementById('btn-next');
    const btnPrev = document.getElementById('btn-prev');
    const btnApply = document.getElementById('btn-apply-credits');
    const btnDownload = document.getElementById('btn-download-pdf');
    const optMensal = document.getElementById('opt-mensal');
    const optAnual = document.getElementById('opt-anual');

    if (btnNext) btnNext.onclick = nextStep;
    if (btnPrev) btnPrev.onclick = prevStep;
    if (btnApply) btnApply.onclick = applySmartCredits;
    if (btnDownload) btnDownload.onclick = handleDownload;
    
    if (optMensal) optMensal.onclick = () => selectWizardOption(optMensal);
    if (optAnual) optAnual.onclick = () => selectWizardOption(optAnual);
}

async function handleDownload() {
    const btnDownload = document.getElementById('btn-download-pdf');
    if (!btnDownload) return;

    btnDownload.disabled = true;
    const originalContent = btnDownload.innerHTML;
    btnDownload.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Gerando PDF...';

    const getAuthHeaders = () => {
        const headers = { 'Content-Type': 'application/json' };
        if (state.user.jwt) headers['Authorization'] = `Bearer ${state.user.jwt}`;
        return headers;
    };

    try {
        const blob = await downloadGuiaPDF(getAuthHeaders());
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = `Guia_SplitPay_IVA_${new Date().toISOString().split('T')[0]}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
    } catch (e) {
        console.warn("Backend offline, simulando download de PDF");
        setTimeout(() => {
            alert('Simulação: O download da Guia_SplitPay_IVA.pdf começaria agora se o backend estivesse online.');
        }, 800);
    } finally {
        setTimeout(() => {
            btnDownload.disabled = false;
            btnDownload.innerHTML = originalContent;
        }, 1200);
    }
}

function selectWizardOption(element) {
    const cards = element.parentElement.querySelectorAll('.option-card');
    cards.forEach(c => c.classList.remove('selected'));
    element.classList.add('selected');
}

async function nextStep() {
    const currentStep = state.ui.wizardStep;
    const btnNext = document.getElementById('btn-next');
    if (!btnNext) return;

    const getAuthHeaders = () => {
        const headers = { 'Content-Type': 'application/json' };
        if (state.user.jwt) headers['Authorization'] = `Bearer ${state.user.jwt}`;
        return headers;
    };

    switch(currentStep) {
        case 1:
            btnNext.disabled = true;
            btnNext.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processando...';
            try {
                const result = await initDeclaracao(getAuthHeaders());
                updateState('data.wizard.faturamento', result.faturamentoBruto || 1250000.00);
                updateState('data.wizard.baseCredito', result.baseCredito || 850000.00);
            } catch (e) {
                updateState('data.wizard.faturamento', 1250000.00);
                updateState('data.wizard.baseCredito', 850000.00);
            }
            document.getElementById('wdc-faturamento').value = formatCurrency(state.data.wizard.faturamento);
            document.getElementById('wdc-insumos').value = formatCurrency(state.data.wizard.baseCredito);
            goToStep(2);
            break;

        case 2:
            // Sincronizar valores editados manualmente para o estado
            const fatInput = document.getElementById('wdc-faturamento');
            const insInput = document.getElementById('wdc-insumos');
            if (fatInput) updateState('data.wizard.faturamento', parseCurrency(fatInput.value));
            if (insInput) updateState('data.wizard.baseCredito', parseCurrency(insInput.value));

            const loader = document.getElementById('validation-loader');
            btnNext.disabled = true;
            btnNext.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Validando...';
            if(loader) loader.style.display = 'block';
            try {
                const result = await validarDeclaracao(getAuthHeaders());
                updateState('data.wizard.creditoAI', result.creditosRecomendados || 15400.00);
            } catch (e) {
                updateState('data.wizard.creditoAI', 15400.00);
            }
            setTimeout(() => {
                if(loader) loader.style.display = 'none';
                const aiEl = document.getElementById('ai-sug-val');
                if(aiEl) aiEl.innerText = formatCurrency(state.data.wizard.creditoAI);
                goToStep(3);
            }, 1000);
            break;

        case 3:
            btnNext.disabled = true;
            btnNext.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Calculando...';
            try {
                const result = await getResumoDeclaracao({
                    faturamentoBruto: state.data.wizard.faturamento,
                    aplicarCreditos: state.ui.creditsApplied,
                    creditosExteriores: state.data.wizard.creditoAI
                }, getAuthHeaders());
                populateSummary(result);
            } catch (e) {
                populateSummary({
                    ibsApurado: state.data.wizard.faturamento * 0.177,
                    cbsApurado: state.data.wizard.faturamento * 0.088,
                    creditosAplicados: state.ui.creditsApplied ? state.data.wizard.creditoAI : 0,
                    valorLiquidoAPagar: (state.data.wizard.faturamento * 0.265) - (state.ui.creditsApplied ? state.data.wizard.creditoAI : 0)
                });
            }
            goToStep(4);
            break;

        case 4:
            goToStep(5);
            break;
    }
}

function populateSummary(result) {
    const resFat = document.getElementById('res-fat');
    const resIbs = document.getElementById('res-ibs');
    const resCbs = document.getElementById('res-cbs');
    const finalVal = document.getElementById('final-tax-value');
    
    if(resFat) resFat.innerText = formatCurrency(state.data.wizard.faturamento);
    if(resIbs) resIbs.innerText = formatCurrency(result.ibsApurado);
    if(resCbs) resCbs.innerText = formatCurrency(result.cbsApurado);
    
    const rowCredits = document.getElementById('row-credits');
    const resCred = document.getElementById('res-cred');
    if (state.ui.creditsApplied) {
        if(rowCredits) rowCredits.style.display = 'flex';
        if(resCred) resCred.innerText = '- ' + formatCurrency(result.creditosAplicados);
    } else {
        if(rowCredits) rowCredits.style.display = 'none';
    }
    if(finalVal) finalVal.innerText = formatCurrency(result.valorLiquidoAPagar);
}

function goToStep(step) {
    updateState('ui.wizardStep', step);
    updateWizardUI();
    const btnNext = document.getElementById('btn-next');
    if(btnNext) {
        btnNext.disabled = false;
        if (step === totalSteps - 1) {
            btnNext.innerHTML = 'Revisar & Gerar Guia <i class="fa-solid fa-check-double" style="margin-left:8px;"></i>';
        } else {
            btnNext.innerHTML = 'Próximo Passo <i class="fa-solid fa-arrow-right" style="margin-left:8px;"></i>';
        }
    }
}

function prevStep() {
    if (state.ui.wizardStep > 1) {
        goToStep(state.ui.wizardStep - 1);
    }
}

function updateWizardUI() {
    const step = state.ui.wizardStep;
    console.log("Atualizando UI para etapa:", step);

    for (let i = 1; i <= totalSteps; i++) {
        const content = document.getElementById(`step-${i}`);
        if(content) {
            if (i === step) {
                content.classList.add('active');
            } else {
                content.classList.remove('active');
            }
        }
        
        const bubble = document.getElementById(`indicator-${i}`);
        if(bubble) {
            if (i < step) {
                bubble.className = 'step-bubble completed';
                bubble.innerHTML = '<i class="fa-solid fa-check"></i>';
            } else if (i === step) {
                bubble.className = 'step-bubble active';
                bubble.innerHTML = i;
            } else {
                bubble.className = 'step-bubble';
                bubble.innerHTML = i;
            }
        }
    }
    
    const btnPrev = document.getElementById('btn-prev');
    const btnNext = document.getElementById('btn-next');
    
    if(btnPrev) btnPrev.style.visibility = (step === 1 || step === totalSteps) ? 'hidden' : 'visible';
    if(btnNext) btnNext.style.display = step === totalSteps ? 'none' : 'inline-flex';
}

function applySmartCredits() {
    updateState('ui.creditsApplied', true);
    const btn = document.getElementById('btn-apply-credits');
    if(btn) {
        btn.innerHTML = `<i class="fa-solid fa-circle-check"></i> <span>Desconto Aplicado</span>`;
        btn.style.background = 'var(--green)';
        btn.classList.add('btn-success');
    }
}

function formatCurrency(val) {
    if (val === null || val === undefined || isNaN(parseFloat(val))) return 'R$ 0,00';
    return 'R$ ' + parseFloat(val).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function parseCurrency(str) {
    if (!str) return 0;
    // Remove tudo que não é número, vírgula ou hífen, depois troca vírgula por ponto
    const cleaned = str.replace(/[^\d,-]/g, '').replace(',', '.');
    return parseFloat(cleaned) || 0;
}

// Auto-init based on element presence
if (document.getElementById('indicator-1')) {
    init();
}
