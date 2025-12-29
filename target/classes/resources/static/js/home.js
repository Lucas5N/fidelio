// Micro: evidenzia link attivo in base al path (se vuoi)
(() => {
    const path = window.location.pathname;
    document.querySelectorAll(".nav-link").forEach(a => {
        const href = a.getAttribute("href");
        if (href && href !== "/" && path.startsWith(href)) {
            document.querySelectorAll(".nav-link").forEach(x => x.classList.remove("is-active"));
            a.classList.add("is-active");
        }
    });
})();

// (Opzionale) Se più avanti vuoi caricare films via API e renderizzarli dinamicamente:
// fetch("/api/home").then(r => r.json()).then(data => console.log(data));
