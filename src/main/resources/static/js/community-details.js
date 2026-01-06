const params = new URLSearchParams(window.location.search);
        const commId = params.get('id');
        const user = JSON.parse(localStorage.getItem("user"));
        let isMember = false;

        async function init() {
            if(!commId) return;
            const resC = await fetch(`/api/community/${commId}`);
            if(resC.ok) {
                const comm = await resC.json();
                document.getElementById('cName').innerText = comm.nome;
                document.getElementById('cDesc').innerText = comm.descrizione;
                document.getElementById('cMembers').innerText = `${comm.numMembri} Membri`;
                updateUI(false);
            }
            loadThreads();
        }

        async function loadThreads() {
            const resT = await fetch(`/api/community/${commId}/threads`);
            const list = document.getElementById('threadList');
            list.innerHTML = '';
            if(resT.ok) {
                const threads = await resT.json();
                if(threads.length === 0) {
                    list.innerHTML = '<div style="text-align:center; padding:40px; color:var(--muted);">Nessuna discussione.</div>';
                    return;
                }
                threads.forEach(t => {
                    const el = document.createElement('a');
                    el.href = '#'; el.className = 'thread-card';
                    el.innerHTML = `
                        <div class="tc-head">
                            <div class="tc-user">
                                <span class="tc-username">${t.autoreUsername}</span>
                            </div>
                            <span class="tc-date">${new Date(t.dataCreazione).toLocaleDateString()}</span>
                        </div>
                        <div class="tc-title">${t.titolo}</div>
                        <div class="tc-preview">${t.contenuto}</div>
                        <div class="tc-footer">💬 ${t.numRisposte} commenti</div>
                    `;
                    list.appendChild(el);
                });
            }
        }

        function updateUI(joined) {
            isMember = joined;
            const btn = document.getElementById('joinBtn');
            const input = document.getElementById('inputArea');
            if(joined) {
                btn.innerText = "Lascia Community";
                btn.classList.remove('primary');
                input.style.display = 'block';
            } else {
                btn.innerText = "Unisciti";
                btn.classList.add('primary');
                input.style.display = 'none';
            }
        }

        async function toggleJoin() {
            if(!user) return alert("Accedi prima!");
            const method = isMember ? 'DELETE' : 'POST';
            const res = await fetch(`/api/community/${commId}/iscrizione?utenteId=${user.id}`, { method });
            if(res.ok) {
                updateUI(!isMember);
                const curr = parseInt(document.getElementById('cMembers').innerText);
                document.getElementById('cMembers').innerText = `${isMember ? curr+1 : curr-1} Membri`;
            }
        }

        async function postThread() {
            const title = document.getElementById('tTitle').value;
            const content = document.getElementById('tContent').value;
            if(!title) return;
            const res = await fetch(`/api/community/${commId}/threads?autoreId=${user.id}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({titolo: title, contenuto: content})
            });
            if(res.ok) {
                document.getElementById('tTitle').value = '';
                document.getElementById('tContent').value = '';
                loadThreads();
            } else {
                alert(await res.text());
            }
        }

        init();