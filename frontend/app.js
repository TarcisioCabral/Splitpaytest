/**
 * Main Application Entry Point
 * Refactored for Component-Based Architecture (SoC)
 */
import { state, updateState } from './state.js';
import { login } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

function initApp() {
    // Event Listeners for Navigation
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            const tabId = e.target.getAttribute('data-tab');
            switchTab(tabId, e.target);
        });
    });

    // Initial load
    switchTab('dashboard', document.querySelector('[data-tab="dashboard"]'));

    // Global Event Listeners
    window.simularLogin = async () => {
        const btn = document.getElementById('btn-login');
        btn.innerText = 'Autenticando...';
        try {
            const data = await login('admin', 'admin');
            updateState('user.jwt', data.access_token);
            updateState('user.isAuthenticated', true);
            
            btn.innerText = '✓ Autenticado';
            btn.style.background = 'var(--green)';
            document.getElementById('status-text').innerText = 'ROC: SECURE';
            document.getElementById('status-dot').style.background = 'var(--green)';
        } catch (e) {
            console.error(e);
            alert('Erro ao conectar com Keycloak');
            btn.innerText = 'Simular Login Keycloak';
        }
    };
}

// Global SPA Router (Dynamic Component Loader)
window.switchTab = async (tabId, element) => {
    // 1. Update active states on tabs
    document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
    if (element) element.classList.add('active');

    const appContent = document.getElementById('app-content');
    
    // Add simple fade animation
    appContent.style.opacity = '0';
    appContent.style.transform = 'translateY(10px)';

    try {
        // 2. Fetch component HTML
        const res = await fetch(`frontend/components/${tabId}/${tabId}.html`);
        if (!res.ok) throw new Error(`HTML for component ${tabId} not found`);
        const html = await res.text();
        
        appContent.innerHTML = html;

        // 3. Inject CSS dynamically if not already loaded
        const cssId = `css-${tabId}`;
        if (!document.getElementById(cssId)) {
            const link = document.createElement('link');
            link.id = cssId;
            link.rel = 'stylesheet';
            link.href = `frontend/components/${tabId}/${tabId}.css`;
            document.head.appendChild(link);
        }

        // 4. Load JS dynamically and initialize
        const module = await import(`./components/${tabId}/${tabId}.js`);
        if (module && module.init) {
            module.init();
        }

    } catch (e) {
        console.error("Error loading component:", e);
        appContent.innerHTML = `<h2>Erro ao carregar módulo ${tabId}</h2>`;
    } finally {
        // Restore opacity for fade-in effect
        appContent.style.transition = 'all 0.3s ease';
        appContent.style.opacity = '1';
        appContent.style.transform = 'translateY(0)';
    }
};
