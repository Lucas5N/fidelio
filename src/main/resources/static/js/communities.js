const user = JSON.parse(localStorage.getItem("user"));

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
                        // Link alla pagina di dettaglio
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
            if(!user) return alert("Devi essere loggato!");
            document.getElementById('createModal').classList.add('active');
        }
        function closeModal() { document.getElementById('createModal').classList.remove('active'); }

        // Chiudi modale cliccando fuori
        document.getElementById('createModal').addEventListener('click', (e) => {
            if(e.target === document.getElementById('createModal')) closeModal();
        });

        // 3. Creazione
        document.getElementById('createCommunityForm').addEventListener('submit', async (e) => {
            e.preventDefault();

            const payload = {
                nome: document.getElementById('cName').value,
                descrizione: document.getElementById('cDesc').value
            };

            const res = await fetch(`/api/community?creatoreId=${user.id}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if(res.ok) {
                closeModal();
                // Pulisci form
                document.getElementById('cName').value = '';
                document.getElementById('cDesc').value = '';
                loadCommunities();
            } else {
                const msg = await res.text();
                alert("Errore: " + msg);
            }
        });