# Splitpay Frontend Mastery Skill

Este arquivo define os padrões estéticos e técnicos para o frontend do Splitpay. O Antigravity deve consultar este arquivo antes de realizar qualquer alteração na interface.

## 1. Princípios de Animação (Ref: Animate.style & W3Schools)
As animações devem ser usadas para guiar o usuário e dar uma sensação de fluidez e "premiumness".

- **Biblioteca Base**: [Animate.style](https://animate.style/)
- **Padrão de Entrada**: Elementos de nível superior (cards, tabelas, seções) devem usar `animate__animated animate__fadeInUp` com duração suave.
- **Micro-interações (CSS Custom)**:
  - Botões: `transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1)`. No hover: `transform: translateY(-1px) scale(1.02)`.
  - Estados de Loading: Usar animações de pulso ou brilho (shimmer) inspiradas nos exemplos do [W3Schools CSS Animations](https://www.w3schools.com/css/css3_animations.asp).

## 2. Design System & Estética
- **Dark Mode Premium**:
  - Background: `--bg-dark` (#0a0e1a) com gradientes radiais sutis.
  - Superfícies: `--surface` (#111827) com bordas de baixa opacidade.
- **Glassmorphism**:
  - Usar `backdrop-filter: blur(12px)` e `background: rgba(255, 255, 255, 0.03)` para elementos flutuantes ou sidebars.
- **Tipografia**:
  - Principal: 'Space Grotesk' (para títulos e corpo).
  - Mono: 'JetBrains Mono' (para dados financeiros e códigos).

## 3. Fluxo de Trabalho do Antigravity
Sempre que o usuário pedir uma melhoria no frontend:
1. Verifique se as classes do **Animate.style** estão presentes.
2. Certifique-se de que os novos componentes sigam a hierarquia de cores e sombras definida no `style.css`.
3. Garanta que todas as interações tenham feedback visual imediato (Optimistic UI + CSS Transitions).

## 4. Referências Rápidas
- **Animate.style Classes Sugeridas**:
  - `animate__fadeIn`: Entradas sutis.
  - `animate__slideInRight`: Modais ou sidebars.
  - `animate__pulse`: Alertas ou itens importantes (discreto).

## 5. Psicologia das Cores (Ref: CRM PipeRun)
As cores devem ser aplicadas estrategicamente para evocar sentimentos e guiar decisões, conforme os princípios da psicologia das cores:

- **Azul (Confiança/Poder)**: Usar o azul escuro para transmitir segurança, autonomia e profissionalismo em dashboards e seções corporativas. O azul claro sugere produtividade e sucesso.
- **Verde (Harmonia/Saúde)**: Usar para estados de sucesso, conciliação concluída e equilíbrio financeiro. Transmite coerência no atendimento.
- **Amarelo/Amber (Atenção/Otimismo)**: Usar para alertas, notificações e estados pendentes. É ideal para CTAs que precisam de foco imediato sem transmitir perigo.
- **Vermelho (Urgência/Perigo)**: Usar estritamente para erros críticos, divergências graves ou promoções de tempo limitado. Desperta senso de urgência.
- **Cinza (Profissionalismo/Responsabilidade)**: Usar para dados neutros, metadados e elementos de suporte. Combina bem com outras cores para um visual moderno e tecnológico.
- **Roxo (Nobreza/Sabedoria)**: Usar para seções "Premium", insights estratégicos ou funcionalidades avançadas. Transmite paz de espírito e bem-estar.

**Regra de Aplicação**: Evite usar cores isoladas. Combine-as para criar contraste (ex: fundo escuro com badges translúcidas) e garantir que a interface "fale" sem precisar de texto.
