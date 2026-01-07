const user = JSON.parse(localStorage.getItem("user"));

// Funzione per mostrare alert migliorati
function showAlert(message, type = "info") {
    const existingAlert = document.querySelector('.custom-alert');
    if (existingAlert) existingAlert.remove();

    const alertDiv = document.createElement('div');
    alertDiv.className = `custom-alert alert-${type}`;
    alertDiv.innerHTML = `
        <span class="alert-message">${message}</span>
        <button class="alert-close" onclick="this.parentElement.remove()">×</button>
    `;
    document.body.prepend(alertDiv);
    setTimeout(() => alertDiv.remove(), 4000);
}

// 1. Carica le community
async function loadCommunities() {
    try {
        const res = await fetch(`/api/community`);
        if(res.ok) {
            const communities = await res.json();
            const container = document.getElementById('communityContainer');
            container.innerHTML = '';

            if(communities.length === 0) {
                container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: #666;">Nessuna community trovata.</div>';
                return;
            }

            communities.forEach(c => {
                const card = document.createElement('a');
                // Link alla pagina di dettaglio (verifica che il nome file sia corretto)
                card.href = `/community-details?id=${c.id}`;
                card.className = 'comm-list-card';
                card.innerHTML = `
                    <div class="clc-icon">🎬</div>
                    <h3 class="clc-name">${c.nome}</h3>
                    <p class="clc-desc">${c.descrizione}</p>
                    <div class="clc-footer">
                        <span>${c.numMembri} Membri</span>
                        <span class="clc-link">Entra →</span>
                    </div>
                `;
                container.appendChild(card);
            });
        }
    } catch (err) { console.error(err); }
}

loadCommunities();

// 2. Modale
function openModal() {
        document.getElementById('createModal').classList.add('active');
    }
    function closeModal() { document.getElementById('createModal').classList.remove('active'); }

    document.getElementById('createModal').addEventListener('click', (e) => {
        if(e.target === document.getElementById('createModal')) closeModal();
    });

    // --- CREAZIONE (Gestione Auth lato Server) ---
    document.getElementById('createCommunityForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const nome = document.getElementById('cName').value.trim();
        const descrizione = document.getElementById('cDesc').value.trim();

        if (!nome) {
            showAlert("Inserisci un nome.", "warning");
            return;
        }

        try {
            const res = await fetch(`/api/community`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include', // FONDAMENTALE PER SPRING SECURITY
                body: JSON.stringify({ nome, descrizione })
            });

            if (res.ok) {
                closeModal();
                document.getElementById('cName').value = '';
                document.getElementById('cDesc').value = '';
                loadCommunities();
                showAlert("Community creata!", "success");
            } else if (res.status === 401) {
                // IL SERVER CI DICE CHE NON SIAMO LOGGATI
                showAlert("Devi effettuare il login per creare una community.", "error");
                // Opzionale: window.location.href = "/login";
            } else if (res.status === 403) {
                // IL SERVER CI DICE CHE NON ABBIAMO I PERMESSI (Non siamo Fedeli)
                showAlert("Solo gli utenti 'Fedele' possono creare community.", "error");
            } else {
                const errJson = await res.json();
                showAlert("Errore: " + (errJson.error || "Sconosciuto"), "error");
            }
        } catch (err) {
            showAlert("Errore di connessione.", "error");
        }
    });