// ===== DATA LAYER =====
const STORAGE_KEY = 'board_kanban_data';

function loadData() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : { boards: [], nextBoardId: 1, nextCardId: 1 };
    } catch { return { boards: [], nextBoardId: 1, nextCardId: 1 }; }
}

function saveData(data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

let data = loadData();
let currentBoard = null;

// ===== MODAL SYSTEM =====
function showModal(html) {
    document.getElementById('modal-content').innerHTML = html;
    document.getElementById('modal-overlay').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('modal-overlay').classList.add('hidden');
}
document.getElementById('modal-overlay').addEventListener('click', e => {
    if (e.target === e.currentTarget) closeModal();
});

// ===== HOME VIEW =====
function renderHome() {
    document.getElementById('home-view').classList.remove('hidden');
    document.getElementById('board-view').classList.add('hidden');
    document.getElementById('btn-back').classList.add('hidden');
    document.getElementById('board-name-header').classList.add('hidden');
    currentBoard = null;

    const container = document.getElementById('board-list');
    if (!data.boards.length) {
        container.innerHTML = `<div class="empty-state">
            <span class="emoji">📋</span>
            <p>Nenhum board ainda. Crie o primeiro!</p>
        </div>`;
        return;
    }
    container.innerHTML = data.boards.map(b => `
        <div class="board-card" onclick="enterBoard(${b.id})">
            <div class="board-card-info">
                <h3>${esc(b.name)}</h3>
                <span>${b.columns.reduce((s, c) => s + c.cards.length, 0)} cards · ${b.columns.length} colunas</span>
            </div>
            <div class="board-card-actions">
                <button class="btn btn-danger btn-small" onclick="event.stopPropagation(); confirmDeleteBoard(${b.id})">🗑</button>
            </div>
        </div>
    `).join('');
}

function showCreateBoardModal() {
    showModal(`
        <h2>Criar Novo Board</h2>
        <div class="field"><label>Nome do board</label><input id="board-name-input" placeholder="Ex: Meu Projeto" autofocus></div>
        <div class="field"><label>Colunas extras (além de Inicial, Final e Cancelar)</label><input id="extra-columns-input" type="number" min="0" value="0" onchange="updateExtraColumnNames(this.value)"></div>
        <div id="extra-column-names"></div>
        <div class="modal-actions">
            <button class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
            <button class="btn btn-primary" onclick="createBoard()">Criar</button>
        </div>
    `);
    setTimeout(() => document.getElementById('board-name-input')?.focus(), 100);
}

function updateExtraColumnNames(count) {
    const container = document.getElementById('extra-column-names');
    const current = parseInt(count) || 0;
    let html = '';
    for (let i = 0; i < current; i++) {
        html += `<div class="field"><label>Nome da coluna pendente ${i + 1}</label><input class="extra-col-name" placeholder="Ex: Em Andamento" value="Pendente ${i + 1}"></div>`;
    }
    container.innerHTML = html;
}

function createBoard() {
    const name = document.getElementById('board-name-input').value.trim();
    if (!name) return alert('Informe um nome para o board');

    const extras = [...document.querySelectorAll('.extra-col-name')].map(el => el.value.trim() || 'Pendente');
    const columns = [
        { id: Date.now() + 1, name: 'A Fazer', kind: 'INITIAL', order: 0, cards: [] },
        ...extras.map((n, i) => ({ id: Date.now() + 2 + i, name: n, kind: 'PENDING', order: i + 1, cards: [] })),
        { id: Date.now() + 99, name: 'Concluído', kind: 'FINAL', order: extras.length + 1, cards: [] },
        { id: Date.now() + 100, name: 'Cancelado', kind: 'CANCEL', order: extras.length + 2, cards: [] },
    ];

    data.boards.push({ id: data.nextBoardId++, name, columns });
    saveData(data);
    closeModal();
    renderHome();
}

function confirmDeleteBoard(id) {
    if (confirm('Tem certeza que deseja excluir este board?')) {
        data.boards = data.boards.filter(b => b.id !== id);
        saveData(data);
        renderHome();
    }
}

// ===== BOARD VIEW =====
function enterBoard(id) {
    const board = data.boards.find(b => b.id === id);
    if (!board) return;
    currentBoard = board;

    document.getElementById('home-view').classList.add('hidden');
    document.getElementById('board-view').classList.remove('hidden');
    document.getElementById('btn-back').classList.remove('hidden');
    document.getElementById('board-name-header').textContent = board.name;
    document.getElementById('board-name-header').classList.remove('hidden');

    renderBoard();
}

function goHome() { renderHome(); }

function renderBoard() {
    if (!currentBoard) return;
    const container = document.getElementById('column-container');

    container.innerHTML = currentBoard.columns.map(col => {
        const kindClass = col.kind.toLowerCase();
        return `
            <div class="column" data-column-id="${col.id}">
                <div class="column-header ${kindClass}">
                    <span>${esc(col.name)}</span>
                    <span class="count">${col.cards.length}</span>
                </div>
                <div class="column-body" data-column-id="${col.id}"
                     ondragover="event.preventDefault(); this.classList.add('drag-over');"
                     ondragleave="this.classList.remove('drag-over');"
                     ondrop="dropCard(event, ${col.id})">
                    ${col.cards.map(c => renderCard(c)).join('')}
                </div>
                ${col.kind === 'INITIAL' || col.kind === 'PENDING' || col.kind === 'FINAL'
                    ? `<div style="padding: 0 .5rem .5rem"><button class="add-card-btn" onclick="showCreateCardModal(${col.id})">+ Novo Card</button></div>`
                    : ''}
            </div>
        `;
    }).join('');
}

function renderCard(card) {
    const blockedClass = card.blocked ? 'blocked' : '';
    const col = findColumn(card.columnId);
    const kindClass = col ? col.kind.toLowerCase() : '';
    const blockCount = (card.blocks || []).length;
    return `
        <div class="card ${blockedClass}" draggable="true"
             data-card-id="${card.id}"
             ondragstart="dragStart(event, ${card.id})"
             ondragend="dragEnd(event)"
             onclick="viewCard(${card.id})">
            <div class="card-title">${esc(card.title)}</div>
            ${card.description ? `<div class="card-desc">${esc(card.description)}</div>` : ''}
            ${blockCount > 0 ? `<div class="blocks-count">⛔ Bloqueios: ${blockCount}x</div>` : ''}
            <div class="card-actions" onclick="event.stopPropagation()">
                ${!card.blocked && col && col.kind !== 'FINAL' && col.kind !== 'CANCEL'
                    ? `<button class="btn btn-primary btn-small" onclick="moveCard(${card.id})">→</button>`
                    : ''}
                ${!card.blocked && col && col.kind !== 'FINAL' && col.kind !== 'CANCEL'
                    ? `<button class="btn btn-danger btn-small" onclick="blockCard(${card.id})">🔒</button>`
                    : ''}
                ${card.blocked
                    ? `<button class="btn btn-success btn-small" onclick="unblockCard(${card.id})">🔓</button>`
                    : ''}
                ${col && col.kind !== 'FINAL' && col.kind !== 'CANCEL'
                    ? `<button class="btn btn-secondary btn-small" onclick="cancelCard(${card.id})">✕</button>`
                    : ''}
            </div>
        </div>
    `;
}

// ===== CARD OPERATIONS =====
function showCreateCardModal(columnId) {
    showModal(`
        <h2>Novo Card</h2>
        <div class="field"><label>Título</label><input id="card-title-input" placeholder="Título do card" autofocus></div>
        <div class="field"><label>Descrição</label><textarea id="card-desc-input" placeholder="Descrição (opcional)"></textarea></div>
        <div class="modal-actions">
            <button class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
            <button class="btn btn-primary" onclick="createCard(${columnId})">Criar</button>
        </div>
    `);
    setTimeout(() => document.getElementById('card-title-input')?.focus(), 100);
}

function createCard(columnId) {
    const title = document.getElementById('card-title-input').value.trim();
    if (!title) return alert('Informe um título para o card');
    const desc = document.getElementById('card-desc-input').value.trim();

    const board = currentBoard || data.boards[data.boards.length - 1];
    const col = board.columns.find(c => c.id === columnId);
    if (!col) return;

    const card = {
        id: data.nextCardId++,
        title,
        description: desc,
        columnId,
        blocked: false,
        blockReason: null,
        blockedAt: null,
        unblockedAt: null,
        blocks: [],
        createdAt: new Date().toISOString()
    };
    col.cards.push(card);
    saveData(data);
    closeModal();
    renderBoard();
}

function moveCard(cardId) {
    const card = findCard(cardId);
    if (!card || card.blocked) return;
    const board = currentBoard;
    const currentCol = board.columns.find(c => c.id === card.columnId);
    if (!currentCol) return;
    const nextCol = board.columns.find(c => c.order === currentCol.order + 1);
    if (!nextCol) return;

    card.columnId = nextCol.id;
    // Remove from old column
    currentCol.cards = currentCol.cards.filter(c => c.id !== cardId);
    // Add to new column - actually it's already updated via ref... let me rebuild
    // Wait, the card object is shared by reference. But currentCol.cards is filtered.
    // Need to add to nextCol.cards
    nextCol.cards.push(card);
    // But we removed from currentCol.cards by filtering. The card ref is still there.
    // However, we pushed to nextCol.cards AFTER removing from currentCol.cards.
    // Since the card object is the same reference, we just need to make sure it's in the right list.
    // Actually the card was removed from currentCol.cards, so it's only in nextCol.cards now.
    // But nextCol.cards might have it twice if the card was already there (it shouldn't be).
    saveData(data);
    renderBoard();
}

function cancelCard(cardId) {
    if (!confirm('Cancelar este card?')) return;
    const board = currentBoard;
    const cancelCol = board.columns.find(c => c.kind === 'CANCEL');
    if (!cancelCol) return;
    const card = findCard(cardId);
    if (!card) return;
    const oldCol = board.columns.find(c => c.id === card.columnId);
    if (!oldCol) return;

    card.columnId = cancelCol.id;
    oldCol.cards = oldCol.cards.filter(c => c.id !== cardId);
    cancelCol.cards.push(card);
    saveData(data);
    renderBoard();
}

function blockCard(cardId) {
    const card = findCard(cardId);
    if (!card || card.blocked) return;
    // Get reason via prompt
    showModal(`
        <h2>🔒 Bloquear Card #${cardId}</h2>
        <div class="field"><label>Motivo do bloqueio</label><textarea id="block-reason-input" placeholder="Por que este card está sendo bloqueado?" autofocus></textarea></div>
        <div class="modal-actions">
            <button class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
            <button class="btn btn-danger" onclick="doBlock(${cardId})">Bloquear</button>
        </div>
    `);
    setTimeout(() => document.getElementById('block-reason-input')?.focus(), 100);
}

function doBlock(cardId) {
    const reason = document.getElementById('block-reason-input').value.trim();
    if (!reason) return alert('Informe o motivo do bloqueio');
    const card = findCard(cardId);
    if (!card) return;
    card.blocked = true;
    card.blockReason = reason;
    card.blockedAt = new Date().toISOString();
    if (!card.blocks) card.blocks = [];
    card.blocks.push({ type: 'block', reason, at: card.blockedAt });
    saveData(data);
    closeModal();
    renderBoard();
}

function unblockCard(cardId) {
    const card = findCard(cardId);
    if (!card || !card.blocked) return;
    showModal(`
        <h2>🔓 Desbloquear Card #${cardId}</h2>
        <div class="field"><label>Motivo do desbloqueio</label><textarea id="unblock-reason-input" placeholder="Por que o card foi resolvido?" autofocus></textarea></div>
        <div class="modal-actions">
            <button class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
            <button class="btn btn-success" onclick="doUnblock(${cardId})">Desbloquear</button>
        </div>
    `);
    setTimeout(() => document.getElementById('unblock-reason-input')?.focus(), 100);
}

function doUnblock(cardId) {
    const reason = document.getElementById('unblock-reason-input').value.trim();
    if (!reason) return alert('Informe o motivo do desbloqueio');
    const card = findCard(cardId);
    if (!card) return;
    card.blocked = false;
    card.unblockedAt = new Date().toISOString();
    if (!card.blocks) card.blocks = [];
    card.blocks.push({ type: 'unblock', reason, at: card.unblockedAt });
    saveData(data);
    closeModal();
    renderBoard();
}

// ===== VIEW CARD DETAIL =====
function viewCard(cardId) {
    const card = findCard(cardId);
    if (!card) return;
    const col = findColumn(card.columnId);
    const kindColor = col ? col.kind.toLowerCase() : 'initial';
    const statusClass = card.blocked ? 'badge-cancel' : 'badge-' + kindColor;
    const statusText = card.blocked ? 'Bloqueado' : (col ? col.kind : 'Desconhecido');
    const blocks = (card.blocks || []).map(b =>
        `<div class="block-history-item ${b.type === 'unblock' ? 'unblocked' : ''}">
            ${b.type === 'block' ? '🔒 Bloqueado' : '🔓 Desbloqueado'} — <strong>${esc(b.reason)}</strong><br>
            <small style="color:var(--text2)">${new Date(b.at).toLocaleString('pt-BR')}</small>
        </div>`
    ).join('') || '<p style="color:var(--text2)">Nenhum bloqueio registrado.</p>';

    showModal(`
        <div class="card-detail-header">
            <h2>🃏 #${card.id} — ${esc(card.title)}</h2>
        </div>
        <div style="margin-bottom:1rem"><span class="card-detail-status ${statusClass}">${statusText}</span></div>
        <div class="card-detail-section"><h3>Descrição</h3><p>${card.description || '<em style="color:var(--text2)">Sem descrição</em>'}</p></div>
        <div class="card-detail-section"><h3>Coluna</h3><p>${col ? esc(col.name) : '—'}</p></div>
        ${card.blocked ? `
        <div class="card-detail-section"><h3>Bloqueio</h3><p>🔒 ${esc(card.blockReason)}<br><small style="color:var(--text2)">${new Date(card.blockedAt).toLocaleString('pt-BR')}</small></p></div>` : ''}
        <div class="card-detail-section"><h3>Histórico de Bloqueios (${(card.blocks || []).length})</h3>${blocks}</div>
        <div class="card-detail-section"><h3>Criado em</h3><p style="color:var(--text2)">${new Date(card.createdAt).toLocaleString('pt-BR')}</p></div>
        <div class="modal-actions">
            <button class="btn btn-secondary" onclick="closeModal()">Fechar</button>
        </div>
    `);
}

// ===== DRAG & DROP =====
let draggedCardId = null;

function dragStart(event, cardId) {
    draggedCardId = cardId;
    event.dataTransfer.effectAllowed = 'move';
    event.target.classList.add('dragging');
}

function dragEnd(event) {
    event.target.classList.remove('dragging');
    document.querySelectorAll('.column-body').forEach(el => el.classList.remove('drag-over'));
    draggedCardId = null;
}

function dropCard(event, targetColumnId) {
    event.preventDefault();
    event.target.classList.remove('drag-over');

    if (!draggedCardId) return;
    const card = findCard(draggedCardId);
    if (!card || card.blocked) return;

    const targetCol = currentBoard.columns.find(c => c.id === targetColumnId);
    if (!targetCol) return;
    const sourceCol = currentBoard.columns.find(c => c.id === card.columnId);
    if (!sourceCol) return;

    // Remove from source
    sourceCol.cards = sourceCol.cards.filter(c => c.id !== draggedCardId);
    // Update column
    card.columnId = targetColumnId;
    // Add to target
    targetCol.cards.push(card);

    saveData(data);
    renderBoard();
}

// ===== HELPERS =====
function findCard(id) {
    if (!currentBoard) return null;
    for (const col of currentBoard.columns) {
        const card = col.cards.find(c => c.id === id);
        if (card) return card;
    }
    return null;
}

function findColumn(id) {
    if (!currentBoard) return null;
    return currentBoard.columns.find(c => c.id === id);
}

function esc(s) {
    const div = document.createElement('div');
    div.textContent = s || '';
    return div.innerHTML;
}

// ===== INIT =====
renderHome();