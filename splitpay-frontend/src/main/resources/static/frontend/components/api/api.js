/**
 * API Docs Component Logic
 */
function init() {
    const headers = document.querySelectorAll('.toggle-api');
    headers.forEach(header => {
        header.addEventListener('click', () => {
            const body = header.nextElementSibling;
            if(body) {
                body.classList.toggle('open');
            }
        });
    });
}
init();
