/**
 * Simple state management for SplitPay App
 */

export const state = {
    user: {
        jwt: null,
        isAuthenticated: false
    },
    ui: {
        currentPage: 'dashboard',
        wizardStep: 1,
        creditsApplied: false
    },
    data: {
        wizard: {
            faturamento: 0,
            baseCredito: 0,
            creditoAI: 0
        },
        transactions: []
    }
};

// Simple event-driven updates (optional, for reactivity)
export const listeners = [];

export function subscribe(fn) {
    listeners.push(fn);
}

export function updateState(path, value) {
    const keys = path.split('.');
    let current = state;
    for (let i = 0; i < keys.length - 1; i++) {
        current = current[keys[i]];
    }
    current[keys[keys.length - 1]] = value;
    
    // Notify listeners
    listeners.forEach(fn => fn(state));
}
