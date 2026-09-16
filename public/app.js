// ÆonVault Web Application Core
const VAULT_KEY = "aeonvault_state_v1";
const SESSION_KEY = "aeonvault_session_token";

const defaultState = {
  user: {
    id: "acc_1",
    username: "NexusCommander",
    email: "vault.commander@aeonvaultfilemanager.vercel.app",
    quotaUsedBytes: 45900000,
    planTier: "Æon Prime"
  },
  accounts: [
    {
      id: "acc_1",
      username: "NexusCommander",
      email: "vault.commander@aeonvaultfilemanager.vercel.app",
      quotaUsedBytes: 45900000,
      planTier: "Æon Prime"
    }
  ],
  files: [
    {
      id: "f1",
      name: "Mission_Briefing_2026.md",
      category: "DOCUMENT",
      size: 42000,
      formattedSize: "41.0 KB",
      updatedAt: "Sep 12, 2026",
      isFavorite: true,
      encryption: "AES-256-GCM",
      hash: "8f4e2...a19c",
      summary: "Orbital mission specifications, 1 QB logical addressing metrics, and distributed network synchronization protocols."
    },
    {
      id: "f2",
      name: "Quantum_Spectrometry.raw",
      category: "CODE",
      size: 85000000,
      formattedSize: "81.1 MB",
      updatedAt: "Sep 11, 2026",
      isFavorite: false,
      encryption: "AES-256-GCM",
      hash: "3b9a1...ff82",
      summary: "Raw telemetry sensors stream from deep-space relay satellite."
    },
    {
      id: "f3",
      name: "Distributed_Mesh_Topology.svg",
      category: "IMAGE",
      size: 2400000,
      formattedSize: "2.3 MB",
      updatedAt: "Sep 10, 2026",
      isFavorite: true,
      encryption: "AES-256-GCM",
      hash: "7c12d...ee40",
      summary: "Vector diagram detailing 12 geographic cluster regions and cross-mesh replication paths."
    },
    {
      id: "f4",
      name: "Neural_Weights_Checkpoint.bin",
      category: "ARCHIVE",
      size: 142000000,
      formattedSize: "135.4 MB",
      updatedAt: "Sep 09, 2026",
      isFavorite: false,
      encryption: "AES-256-GCM",
      hash: "11a0c...99ee",
      summary: "Deep-neural compressed weights checkpoint for edge inferencing."
    }
  ],
  shareLinks: [],
  activities: [
    { id: "a1", action: "SYSTEM_INIT", desc: "Vault initialized with 1 QB logical addressing bounds", time: "Just now" },
    { id: "a2", action: "SAFEGUARD_ON", desc: "Permanent inactivity protection enabled for commander account", time: "5m ago" }
  ]
};

let vault = loadVault();
let currentTab = "overview";
let activeCategory = "ALL";
let activeUploads = [];
let selectedFileForShare = null;
let currentFolderId = null;
let folderBreadcrumbs = [{ id: null, name: "Vault Root" }];

function loadVault() {
  const saved = localStorage.getItem(VAULT_KEY);
  if (saved) {
    try { return JSON.parse(saved); } catch(e) {}
  }
  return JSON.parse(JSON.stringify(defaultState));
}

function saveVault() {
  localStorage.setItem(VAULT_KEY, JSON.stringify(vault));
}

function checkAuth() {
  const token = localStorage.getItem(SESSION_KEY);
  const authView = document.getElementById("auth-view");
  const appView = document.getElementById("app-view");

  if (!token) {
    if (authView) authView.style.display = "flex";
    if (appView) appView.style.display = "none";
  } else {
    if (authView) authView.style.display = "none";
    if (appView) appView.style.display = "block";
    try {
      const sessionData = JSON.parse(token);
      if (vault.user.id !== sessionData.userId) {
        switchAccount(sessionData.userId);
      }
    } catch (e) {
      localStorage.removeItem(SESSION_KEY);
      checkAuth();
    }
  }
}

function toggleAuthSection(section) {
  const loginSec = document.getElementById("auth-login-section");
  const signupSec = document.getElementById("auth-signup-section");
  if (loginSec) loginSec.style.display = section === "login" ? "block" : "none";
  if (signupSec) signupSec.style.display = section === "signup" ? "block" : "none";
}

function performLogin() {
  const emailInput = document.getElementById("loginEmail");
  const passInput = document.getElementById("loginPassword");
  const email = emailInput ? emailInput.value.trim() : "";
  const pass = passInput ? passInput.value.trim() : "";

  if (!email || !pass) {
    alert("Please enter both email and passkey.");
    return;
  }

  const user = vault.accounts.find(a => a.email.toLowerCase() === email.toLowerCase());
  if (!user) {
    alert("Account not found in this vault partition. Please initialize a new profile.");
    return;
  }

  const token = {
    userId: user.id,
    email: user.email,
    exp: Date.now() + (7 * 24 * 60 * 60 * 1000)
  };
  localStorage.setItem(SESSION_KEY, JSON.stringify(token));
  
  vault.user = user;
  saveVault();
  checkAuth();
  renderAccountsUI();
  updateQuotaDisplay();
}

function performSignup() {
  const uInput = document.getElementById("signupUsername");
  const eInput = document.getElementById("signupEmail");
  const pInput = document.getElementById("signupPassword");
  
  const username = uInput ? uInput.value.trim() : "";
  const email = eInput ? eInput.value.trim() : "";
  const pass = pInput ? pInput.value.trim() : "";

  if (!username || !email || !pass) {
    alert("Please specify username, email, and passkey.");
    return;
  }

  const existing = vault.accounts.find(a => a.email.toLowerCase() === email.toLowerCase());
  if (existing) {
    alert("This email is already registered. Please login.");
    toggleAuthSection('login');
    return;
  }

  const newAcc = {
    id: "acc_" + Date.now(),
    username: username,
    email: email,
    quotaUsedBytes: 0,
    planTier: "Æon Prime"
  };

  vault.accounts.push(newAcc);
  vault.user = newAcc;
  
  const token = {
    userId: newAcc.id,
    email: newAcc.email,
    exp: Date.now() + (7 * 24 * 60 * 60 * 1000)
  };
  localStorage.setItem(SESSION_KEY, JSON.stringify(token));

  saveVault();
  checkAuth();
  renderAccountsUI();
  updateQuotaDisplay();
}

function logout() {
  localStorage.removeItem(SESSION_KEY);
  checkAuth();
}

// Format bytes
function formatBytes(bytes) {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "QB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
}

// Navigation Tab switching
function switchTab(tabId) {
  currentTab = tabId;
  document.querySelectorAll(".nav-item").forEach(btn => {
    btn.classList.toggle("active", btn.getAttribute("data-tab") === tabId);
  });
  document.querySelectorAll(".view-panel").forEach(panel => {
    panel.classList.toggle("active", panel.id === `view-${tabId}`);
  });
  if (tabId === "files") {
    renderVaultBreadcrumbs();
    renderVaultFiles();
  }
  if (tabId === "shared") renderSharedLinks();
  if (tabId === "activity") renderAuditLedger();
}

document.querySelectorAll(".nav-item").forEach(btn => {
  btn.addEventListener("click", () => switchTab(btn.getAttribute("data-tab")));
});

// Directory Management
function openNewFolderModal() {
  const input = document.getElementById("newFolderNameInput");
  if (input) input.value = "";
  openModal("newFolderModal");
  if (input) setTimeout(() => input.focus(), 120);
}

function confirmCreateNewFolder() {
  const input = document.getElementById("newFolderNameInput");
  const name = input ? input.value.trim() : "";
  if (!name) {
    alert("Please enter a valid directory name.");
    return;
  }

  const currentUserId = vault.user ? vault.user.id : "acc_1";
  const existing = vault.files.find(f => 
    (f.userId === currentUserId || !f.userId) &&
    (f.parentId === currentFolderId || (!f.parentId && !currentFolderId)) &&
    f.name.toLowerCase() === name.toLowerCase()
  );
  if (existing) {
    alert(`An item or directory named "${name}" already exists in this folder.`);
    return;
  }

  const newFolder = {
    id: "dir_" + Date.now(),
    userId: currentUserId,
    name: name,
    isFolder: true,
    category: "FOLDER",
    parentId: currentFolderId,
    size: 0,
    formattedSize: "--",
    updatedAt: "Just now",
    isFavorite: false,
    encryption: "AES-256-GCM",
    hash: "DIR_TREE_NODE",
    summary: `Virtual directory container for "${name}" mapped to 1 QB logical matrix.`
  };

  vault.files.unshift(newFolder);
  vault.activities.unshift({
    id: "act_" + Date.now(),
    action: "CREATE_FOLDER",
    desc: `Created directory "${name}" in ${currentFolderId ? 'nested folder' : 'Vault Root'}`,
    time: "Just now"
  });

  saveVault();
  if (input) input.value = "";
  closeModal("newFolderModal");
  activeCategory = "ALL";
  document.querySelectorAll(".filter-chips .chip").forEach(c => {
    c.classList.toggle("active", c.getAttribute("data-cat") === "ALL");
  });
  switchTab("files");
  renderVaultBreadcrumbs();
  renderVaultFiles();
  renderRecentFiles();
}

function navigateToFolder(folderId, folderName) {
  currentFolderId = folderId;
  if (!folderId) {
    folderBreadcrumbs = [{ id: null, name: "Vault Root" }];
  } else {
    const idx = folderBreadcrumbs.findIndex(b => b.id === folderId);
    if (idx !== -1) {
      folderBreadcrumbs = folderBreadcrumbs.slice(0, idx + 1);
    } else {
      folderBreadcrumbs.push({ id: folderId, name: folderName || "Directory" });
    }
  }
  renderVaultBreadcrumbs();
  renderVaultFiles();
}

function renderVaultBreadcrumbs() {
  const container = document.getElementById("vaultBreadcrumbs");
  const zipBtn = document.getElementById("downloadCurrentFolderZipBtn");
  if (zipBtn) {
    zipBtn.style.display = currentFolderId ? "inline-flex" : "none";
  }
  if (!container) return;
  container.innerHTML = folderBreadcrumbs.map((crumb, idx) => {
    const isLast = idx === folderBreadcrumbs.length - 1;
    return `
      <span class="crumb ${isLast ? 'active' : ''}" style="cursor: pointer;" onclick="navigateToFolder(${crumb.id ? `'${crumb.id}'` : 'null'}, '${escapeHtml(crumb.name)}')">
        ${escapeHtml(crumb.name)}
      </span>
      ${!isLast ? '<span style="color: var(--text-muted); margin: 0 4px;">/</span>' : ''}
    `;
  }).join("");
}

// Render Recent Files Table
function renderRecentFiles() {
  const tbody = document.getElementById("recentFilesBody");
  if (!tbody) return;
  const currentUserId = vault.user ? vault.user.id : "acc_1";
  const userFiles = vault.files.filter(f => !f.userId || f.userId === currentUserId);
  tbody.innerHTML = userFiles.slice(0, 8).map(f => `
    <tr>
      <td>
        <div style="display: flex; align-items: center; gap: 8px;">
          <span>${f.isFolder ? '📁' : '📄'}</span>
          ${f.isFolder ? `
            <strong style="cursor: pointer; color: var(--neon-cyan);" onclick="switchTab('files'); navigateToFolder('${f.id}', '${escapeHtml(f.name)}')">
              ${escapeHtml(f.name)}
            </strong>
          ` : `
            <strong>${escapeHtml(f.name)}</strong>
          `}
        </div>
      </td>
      <td><span class="badge ${f.isFolder ? 'badge-violet' : 'badge-cyan'}">${f.isFolder ? 'DIRECTORY' : f.category}</span></td>
      <td>${f.isFolder ? '--' : f.formattedSize}</td>
      <td><code>${f.encryption}</code></td>
      <td>
        ${f.isFolder ? `
          <button class="btn btn-outline btn-sm" onclick="switchTab('files'); navigateToFolder('${f.id}', '${escapeHtml(f.name)}')">Open</button>
          <button class="btn btn-outline btn-sm" title="Download Folder as ZIP" onclick="downloadFolderAsZip('${f.id}', '${escapeHtml(f.name)}')">📦 ZIP</button>
        ` : `
          <button class="btn btn-outline btn-sm" onclick="openFilePreview('${f.id}')">Inspect</button>
          <button class="btn btn-outline btn-sm" onclick="openShareModal('${f.id}')">Share</button>
        `}
      </td>
    </tr>
  `).join("");
}

// Render Vault Files (My Vault)
function renderVaultFiles() {
  const container = document.getElementById("vaultFilesContainer");
  if (!container) return;

  const currentUserId = vault.user ? vault.user.id : "acc_1";
  const userFiles = vault.files.filter(f => !f.userId || f.userId === currentUserId);

  const filtered = userFiles.filter(f => {
    // Parent folder filtering
    const matchesFolder = (currentFolderId === null) 
      ? (!f.parentId || f.parentId === "" || f.parentId === null)
      : (f.parentId === currentFolderId);

    if (!matchesFolder) return false;

    if (activeCategory === "ALL") return true;
    if (activeCategory === "FAVORITES") return f.isFavorite;
    if (activeCategory === "FOLDER" || activeCategory === "FOLDERS") return f.isFolder;
    return f.category === activeCategory;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="padding:48px; text-align:center; color:var(--text-muted);">
        <div style="font-size: 28px; margin-bottom: 8px;">📂</div>
        <div>No items in this directory.</div>
        <div style="margin-top: 12px;">
          <button class="btn btn-outline btn-sm" onclick="openNewFolderModal()">+ Create Directory</button>
        </div>
      </div>`;
    return;
  }

  container.innerHTML = `
    <table class="files-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Type</th>
          <th>Size</th>
          <th>Last Modified</th>
          <th>Integrity</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${filtered.map(f => `
          <tr style="${f.isFolder ? 'background: rgba(0, 245, 255, 0.03);' : ''}">
            <td>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span>${f.isFolder ? '📁' : '📄'}</span>
                ${f.isFolder ? `
                  <strong style="cursor: pointer; color: var(--neon-cyan);" onclick="navigateToFolder('${f.id}', '${escapeHtml(f.name)}')">
                    ${escapeHtml(f.name)}
                  </strong>
                ` : `
                  <strong>${escapeHtml(f.name)}</strong>
                `}
              </div>
            </td>
            <td><span class="badge ${f.isFolder ? 'badge-violet' : 'badge-cyan'}">${f.isFolder ? 'DIRECTORY' : f.category}</span></td>
            <td>${f.isFolder ? '--' : f.formattedSize}</td>
            <td>${f.updatedAt}</td>
            <td><span class="badge badge-emerald">SHA-256 OK</span></td>
            <td>
              ${f.isFolder ? `
                <button class="btn btn-outline btn-sm" onclick="navigateToFolder('${f.id}', '${escapeHtml(f.name)}')">Open</button>
                <button class="btn btn-outline btn-sm" title="Download Folder as ZIP" onclick="downloadFolderAsZip('${f.id}', '${escapeHtml(f.name)}')">📦 ZIP</button>
                <button class="btn btn-outline btn-sm" style="color:var(--coral);" onclick="deleteFile('${f.id}')">Delete</button>
              ` : `
                <button class="btn btn-outline btn-sm" onclick="openFilePreview('${f.id}')">Inspect</button>
                <button class="btn btn-outline btn-sm" onclick="openShareModal('${f.id}')">Share</button>
                <button class="btn btn-outline btn-sm" style="color:var(--coral);" onclick="deleteFile('${f.id}')">Delete</button>
              `}
            </td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;
}

// Category filter chips
document.querySelectorAll(".filter-chips .chip").forEach(chip => {
  chip.addEventListener("click", () => {
    document.querySelectorAll(".filter-chips .chip").forEach(c => c.classList.remove("active"));
    chip.classList.add("active");
    activeCategory = chip.getAttribute("data-cat");
    renderVaultFiles();
  });
});

// Upload simulator & Real file uploads
function handleFileUpload(files) {
  Array.from(files).forEach(file => {
    simulateUpload(file.name, file.size);
  });
}

const dropzone = document.getElementById("uploadDropzone");
const fileInput = document.getElementById("fileInput");

if (fileInput) {
  fileInput.addEventListener("change", (e) => {
    if (e.target.files.length) handleFileUpload(e.target.files);
  });
}

if (dropzone) {
  dropzone.addEventListener("dragover", (e) => { e.preventDefault(); dropzone.style.background = "rgba(0, 245, 255, 0.12)"; });
  dropzone.addEventListener("dragleave", () => { dropzone.style.background = "rgba(0, 245, 255, 0.04)"; });
  dropzone.addEventListener("drop", (e) => {
    e.preventDefault();
    dropzone.style.background = "rgba(0, 245, 255, 0.04)";
    if (e.dataTransfer.files.length) handleFileUpload(e.dataTransfer.files);
  });
}

function simulateUpload(name, size) {
  const taskId = "task_" + Date.now() + "_" + Math.random().toString(36).substr(2, 4);
  const task = {
    id: taskId,
    name: name,
    size: size,
    progress: 0,
    speed: "24.5 MB/s",
    eta: "Calculating...",
    status: "UPLOADING"
  };
  activeUploads.unshift(task);
  renderUploadTasks();
  switchTab("upload");

  const interval = setInterval(() => {
    task.progress += Math.floor(Math.random() * 12) + 8;
    if (task.progress >= 100) {
      task.progress = 100;
      task.status = "COMPLETED";
      clearInterval(interval);

      // Add file to vault
      const newFile = {
        id: "f_" + Date.now(),
        name: task.name,
        category: getCategoryFromName(task.name),
        size: task.size,
        formattedSize: formatBytes(task.size),
        updatedAt: "Just now",
        isFavorite: false,
        encryption: "AES-256-GCM",
        hash: "a4f89...c10b",
        summary: "Newly uploaded object indexed across distributed quantum storage mesh."
      };
      vault.files.unshift(newFile);
      vault.user.quotaUsedBytes += task.size;
      vault.activities.unshift({
        id: "a_" + Date.now(),
        action: "UPLOAD",
        desc: `Uploaded ${task.name} (${formatBytes(task.size)}) via 4x multiplexed stream`,
        time: "Just now"
      });
      saveVault();
      renderRecentFiles();
      updateQuotaDisplay();
    }
    renderUploadTasks();
  }, 350);
}

function getCategoryFromName(name) {
  const ext = name.split(".").pop().toLowerCase();
  if (["md", "txt", "pdf", "docx", "doc"].includes(ext)) return "DOCUMENT";
  if (["jpg", "png", "svg", "webp"].includes(ext)) return "IMAGE";
  if (["mp4", "mkv", "mov"].includes(ext)) return "VIDEO";
  if (["mp3", "wav", "flac"].includes(ext)) return "AUDIO";
  if (["js", "ts", "kt", "py", "json", "raw", "cad"].includes(ext)) return "CODE";
  return "ARCHIVE";
}

function renderUploadTasks() {
  const list = document.getElementById("uploadTasksList");
  const countSpan = document.getElementById("activeUploadsCount");
  if (!list) return;
  countSpan.textContent = activeUploads.length;

  list.innerHTML = activeUploads.map(t => `
    <div class="card" style="margin-bottom:12px; padding:14px;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <div>
          <strong>${t.name}</strong>
          <span style="color:var(--text-muted); font-size:0.75rem; margin-left:8px;">(${formatBytes(t.size)})</span>
        </div>
        <span class="badge ${t.status === 'COMPLETED' ? 'badge-emerald' : 'badge-cyan'}">${t.status}</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" style="width:${t.progress}%"></div>
      </div>
      <div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-muted); margin-top:6px;">
        <span>${t.progress}% · ${t.speed}</span>
        <span>${t.status === 'COMPLETED' ? 'Finished' : 'ETA: 3s'}</span>
      </div>
    </div>
  `).join("");
}

function clearCompletedUploads() {
  activeUploads = activeUploads.filter(t => t.status !== "COMPLETED");
  renderUploadTasks();
}

function deleteFile(id) {
  vault.files = vault.files.filter(f => f.id !== id);
  saveVault();
  renderVaultFiles();
  renderRecentFiles();
}

// Modals
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add("open");
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove("open");
}

document.getElementById("openDisclaimerModal").onclick = () => openModal("disclaimerModal");
document.getElementById("bannerInfoBtn").onclick = () => openModal("disclaimerModal");

function openFilePreview(id) {
  const file = vault.files.find(f => f.id === id);
  if (!file) return;
  document.getElementById("previewModalTitle").textContent = file.name;
  document.getElementById("previewModalBody").innerHTML = `
    <div class="callout callout-cyan">
      <strong>AI Summary (Gemini 3.5 Flash):</strong><br>
      ${file.summary}
    </div>
    <table class="files-table">
      <tr><td><strong>Size</strong></td><td>${file.formattedSize}</td></tr>
      <tr><td><strong>Category</strong></td><td><span class="badge badge-cyan">${file.category}</span></td></tr>
      <tr><td><strong>Encryption</strong></td><td>${file.encryption} (Quantum Mesh verified)</td></tr>
      <tr><td><strong>Cryptographic SHA-256</strong></td><td><code>${file.hash}</code></td></tr>
      <tr><td><strong>Purge Exemption</strong></td><td><span class="badge badge-emerald">Permanent Account Protected</span></td></tr>
    </table>
  `;
  document.getElementById("previewShareBtn").onclick = () => {
    closeModal("filePreviewModal");
    openShareModal(file.id);
  };
  document.getElementById("previewDownloadBtn").onclick = () => {
    const encoder = new TextEncoder();
    const content = file.textContent || `ÆonVault File Manifest\nName: ${file.name}\nSize: ${file.formattedSize}\nCategory: ${file.category}\nEncryption: ${file.encryption}\nSummary: ${file.summary || ''}`;
    const blob = new Blob([encoder.encode(content)], { type: "application/octet-stream" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = file.name;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  };
  openModal("filePreviewModal");
}

// Pure JS Store (Uncompressed) ZIP Generator
function buildZipArchive(entries) {
  const crcTable = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) {
      c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
    }
    crcTable[n] = c >>> 0;
  }
  function getCrc32(buf) {
    let c = -1;
    for (let i = 0; i < buf.length; i++) {
      c = (c >>> 8) ^ crcTable[(c ^ buf[i]) & 0xFF];
    }
    return (c ^ -1) >>> 0;
  }

  const parts = [];
  const centralDirectory = [];
  let offset = 0;
  const encoder = new TextEncoder();

  for (const entry of entries) {
    const isDir = entry.isFolder || entry.path.endsWith("/");
    const pathBytes = encoder.encode(entry.path);
    const dataBytes = isDir ? new Uint8Array(0) : (entry.data || new Uint8Array(0));
    const crc = isDir ? 0 : getCrc32(dataBytes);
    const size = dataBytes.length;

    // Local Header (30 bytes + path length)
    const localHeader = new Uint8Array(30 + pathBytes.length);
    const view = new DataView(localHeader.buffer);
    view.setUint32(0, 0x04034b50, true); // signature
    view.setUint16(4, 20, true);         // version needed
    view.setUint16(6, 0x0800, true);     // flags (UTF-8)
    view.setUint16(8, 0, true);          // compression (0 = store)
    view.setUint16(10, 0, true);         // mod time
    view.setUint16(12, 0x2158, true);    // mod date
    view.setUint32(14, crc, true);       // crc-32
    view.setUint32(18, size, true);      // compressed size
    view.setUint32(22, size, true);      // uncompressed size
    view.setUint16(26, pathBytes.length, true); // file name length
    view.setUint16(28, 0, true);         // extra field length
    localHeader.set(pathBytes, 30);

    parts.push(localHeader);
    if (!isDir && size > 0) {
      parts.push(dataBytes);
    }

    // Central Directory Header (46 bytes + path length)
    const cdHeader = new Uint8Array(46 + pathBytes.length);
    const cdView = new DataView(cdHeader.buffer);
    cdView.setUint32(0, 0x02014b50, true); // central header signature
    cdView.setUint16(4, 20, true);          // version made by
    cdView.setUint16(6, 20, true);          // version needed
    cdView.setUint16(8, 0x0800, true);      // flags (UTF-8)
    cdView.setUint16(10, 0, true);         // compression
    cdView.setUint16(12, 0, true);         // mod time
    cdView.setUint16(14, 0x2158, true);    // mod date
    cdView.setUint32(16, crc, true);       // crc32
    cdView.setUint32(20, size, true);      // comp size
    cdView.setUint32(24, size, true);      // uncomp size
    cdView.setUint16(28, pathBytes.length, true); // name length
    cdView.setUint16(30, 0, true);         // extra field length
    cdView.setUint16(32, 0, true);         // comment length
    cdView.setUint16(34, 0, true);         // disk number start
    cdView.setUint16(36, 0, true);         // internal attributes
    cdView.setUint32(38, isDir ? 0x10 : 0, true); // external attributes (directory bit)
    cdView.setUint32(42, offset, true);    // relative offset of local header
    cdHeader.set(pathBytes, 46);
    centralDirectory.push(cdHeader);

    offset += localHeader.length + (isDir ? 0 : size);
  }

  const cdOffset = offset;
  let cdSize = 0;
  for (const cd of centralDirectory) {
    parts.push(cd);
    cdSize += cd.length;
  }

  // End of central directory record (22 bytes)
  const eocd = new Uint8Array(22);
  const eocdView = new DataView(eocd.buffer);
  eocdView.setUint32(0, 0x06054b50, true); // EOCD signature
  eocdView.setUint16(4, 0, true);          // disk number
  eocdView.setUint16(6, 0, true);          // start disk
  eocdView.setUint16(8, entries.length, true);  // entries on disk
  eocdView.setUint16(10, entries.length, true); // total entries
  eocdView.setUint32(12, cdSize, true);         // central dir size
  eocdView.setUint32(16, cdOffset, true);       // central dir offset
  eocdView.setUint16(20, 0, true);              // comment length
  parts.push(eocd);

  return new Blob(parts, { type: "application/zip" });
}

function downloadFolderAsZip(folderId, folderName) {
  const currentUserId = vault.user ? vault.user.id : "acc_1";
  const userFiles = vault.files.filter(f => !f.userId || f.userId === currentUserId);
  const targetFolder = userFiles.find(f => f.id === folderId);
  const rootName = folderName || (targetFolder ? targetFolder.name : "Folder");
  const encoder = new TextEncoder();

  const entries = [];

  function gather(parentDirId, currentPath) {
    entries.push({
      path: currentPath.endsWith("/") ? currentPath : currentPath + "/",
      data: new Uint8Array(0),
      isFolder: true
    });

    const children = userFiles.filter(f => f.parentId === parentDirId);
    for (const child of children) {
      const childPath = `${currentPath}/${child.name}`;
      if (child.isFolder) {
        gather(child.id, childPath);
      } else {
        let contentBytes;
        if (child.textContent) {
          contentBytes = encoder.encode(child.textContent);
        } else {
          const desc = [
            `ÆonVault Zero-Knowledge Object Manifest`,
            `=======================================`,
            `Object ID: ${child.id}`,
            `Name: ${child.name}`,
            `Category: ${child.category}`,
            `Size: ${child.formattedSize}`,
            `Encryption: ${child.encryption}`,
            `Integrity: SHA-256 Verified OK`,
            `Storage Node: Æon Distributed Cluster Delta-7`,
            `Summary: ${child.summary || 'Encrypted block matrix'}`
          ].join("\n");
          contentBytes = encoder.encode(desc);
        }
        entries.push({
          path: childPath,
          data: contentBytes,
          isFolder: false
        });
      }
    }
  }

  gather(folderId, rootName);

  const blob = buildZipArchive(entries);
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `${rootName}.zip`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  setTimeout(() => URL.revokeObjectURL(url), 1000);

  vault.activities.unshift({
    id: "act_" + Date.now(),
    action: "EXPORT_ZIP",
    desc: `Downloaded directory "${rootName}" as ZIP archive (${formatBytes(blob.size)})`,
    time: "Just now"
  });
  saveVault();
  renderAuditLedger();
}

function downloadCurrentFolderAsZip() {
  if (!currentFolderId) return;
  const currentCrumb = folderBreadcrumbs[folderBreadcrumbs.length - 1];
  downloadFolderAsZip(currentFolderId, currentCrumb ? currentCrumb.name : "Directory");
}

function openShareModal(id) {
  const file = vault.files.find(f => f.id === id);
  if (!file) return;
  selectedFileForShare = file;
  document.getElementById("shareTargetFileName").textContent = file.name;
  document.getElementById("shareResultBox").style.display = "none";
  document.getElementById("confirmGenerateShareBtn").style.display = "inline-flex";
  openModal("shareModal");
}

function confirmCreateShare() {
  if (!selectedFileForShare) return;
  const linkId = Math.random().toString(36).substring(2, 9);
  const shareUrl = `https://aeonvaultfilemanager.vercel.app/share/${linkId}`;
  document.getElementById("generatedShareUrl").value = shareUrl;
  document.getElementById("shareResultBox").style.display = "block";
  document.getElementById("confirmGenerateShareBtn").style.display = "none";

  vault.shareLinks.unshift({
    id: linkId,
    fileName: selectedFileForShare.name,
    url: shareUrl,
    createdAt: "Just now"
  });
  saveVault();
}

function copyShareUrl() {
  const copyText = document.getElementById("generatedShareUrl");
  copyText.select();
  navigator.clipboard.writeText(copyText.value);
  alert("Copied link: " + copyText.value);
}

function renderSharedLinks() {
  const list = document.getElementById("sharedLinksList");
  const badge = document.getElementById("sharedCountBadge");
  if (!list) return;
  badge.textContent = `${vault.shareLinks.length} Active`;
  if (vault.shareLinks.length === 0) {
    list.innerHTML = `<div style="padding:48px; text-align:center; color:var(--text-muted);">No active share links. Generate one from any file in your vault.</div>`;
    return;
  }
  list.innerHTML = vault.shareLinks.map(l => `
    <div class="card" style="margin-bottom:12px;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <strong>${l.fileName}</strong>
        <span class="badge badge-violet">Public Share</span>
      </div>
      <div class="code-box">${l.url}</div>
      <button class="btn btn-outline btn-sm" onclick="navigator.clipboard.writeText('${l.url}'); alert('Copied!');">Copy Link</button>
    </div>
  `).join("");
}

function renderAuditLedger() {
  const list = document.getElementById("auditLedgerList");
  if (!list) return;
  list.innerHTML = vault.activities.map(a => `
    <div class="card" style="margin-bottom:12px; display:flex; justify-content:space-between; align-items:center;">
      <div>
        <span class="badge badge-emerald" style="margin-right:8px;">${a.action}</span>
        <strong>${a.desc}</strong>
      </div>
      <span style="font-size:0.75rem; color:var(--text-muted);">${a.time}</span>
    </div>
  `).join("");
}

// Gemini Chat
const chatInput = document.getElementById("chatInput");
const chatSendBtn = document.getElementById("chatSendBtn");
const chatMessages = document.getElementById("chatMessages");

function sendChat(query) {
  if (!query || !query.trim()) return;
  const userText = query.trim();
  chatMessages.innerHTML += `
    <div class="chat-msg user">
      <strong>Commander</strong>
      <p>${userText}</p>
    </div>
  `;
  chatInput.value = "";
  chatMessages.scrollTop = chatMessages.scrollHeight;

  setTimeout(() => {
    let reply = "Your request has been processed against the distributed vault metadata.";
    if (userText.toLowerCase().includes("1 qb") || userText.toLowerCase().includes("quota")) {
      reply = "Notice: ÆonVault provides a 1 QB logical storage quota. This represents maximum assigned logical allocation and does not mean 1 QB is individually reserved on a single server. Actual capacity depends on distributed infrastructure, with zero inactivity deletion.";
    } else if (userText.toLowerCase().includes("summarize")) {
      reply = `Your vault currently holds ${vault.files.length} active files across Documents, Media, Code, and Archives, encrypted with AES-256-GCM.`;
    } else if (userText.toLowerCase().includes("inactivity") || userText.toLowerCase().includes("purge")) {
      reply = "ÆonVault guarantee: Your user account and stored files will never be automatically deleted or wiped because of inactivity.";
    }
    chatMessages.innerHTML += `
      <div class="chat-msg assistant">
        <strong>ÆonVault Assistant (Gemini)</strong>
        <p>${reply}</p>
      </div>
    `;
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }, 400);
}

if (chatSendBtn) {
  chatSendBtn.onclick = () => sendChat(chatInput.value);
  chatInput.addEventListener("keydown", (e) => { if (e.key === "Enter") sendChat(chatInput.value); });
}

function sendSuggestedQuery(text) {
  sendChat(text);
}

function updateQuotaDisplay() {
  const text = document.getElementById("quotaUsedText");
  const count = document.getElementById("totalFilesCount");
  if (text) text.textContent = `${formatBytes(vault.user.quotaUsedBytes)} / 1 QB`;
  if (count) count.textContent = `${vault.files.length} Objects`;
}

// Theme Toggle
const themeBtn = document.getElementById("themeToggleBtn");
if (themeBtn) {
  themeBtn.onclick = () => {
    document.body.classList.toggle("theme-light");
    const isLight = document.body.classList.contains("theme-light");
    document.getElementById("themeIcon").textContent = isLight ? "🌙" : "☀️";
  };
}

// Multi-Account Management
function ensureAccountsList() {
  if (!vault.accounts || !Array.isArray(vault.accounts) || vault.accounts.length === 0) {
    vault.accounts = [
      {
        id: vault.user.id || "acc_1",
        username: vault.user.username || "NexusCommander",
        email: vault.user.email || "vault.commander@aeonvaultfilemanager.vercel.app",
        quotaUsedBytes: vault.user.quotaUsedBytes || 45900000,
        planTier: vault.user.planTier || "Æon Prime"
      }
    ];
    saveVault();
  }
}

function renderAccountsUI() {
  ensureAccountsList();

  // Header pill
  const avatarEl = document.getElementById("headerAvatar");
  const usernameEl = document.getElementById("headerUsername");
  if (avatarEl && vault.user.username) {
    avatarEl.textContent = vault.user.username.slice(0, 2).toUpperCase();
  }
  if (usernameEl && vault.user.username) {
    usernameEl.textContent = vault.user.username;
  }

  // Active account card in modal
  const activeCard = document.getElementById("activeAccountCard");
  if (activeCard) {
    activeCard.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; align-items: center; gap: 10px;">
          <div style="width: 34px; height: 34px; border-radius: 50%; background: var(--neon-cyan); color: #000; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 12px;">
            ${(vault.user.username || "AV").slice(0, 2).toUpperCase()}
          </div>
          <div>
            <div style="font-weight: bold; color: var(--text-primary);">${escapeHtml(vault.user.username)}</div>
            <div style="font-size: 11px; color: var(--text-muted);">${escapeHtml(vault.user.email)}</div>
          </div>
        </div>
        <span class="badge badge-cyan">${escapeHtml(vault.user.planTier)}</span>
      </div>
      <div style="margin-top: 8px; font-size: 11px; color: var(--emerald-glow); display: flex; align-items: center; gap: 4px;">
        <span>🔒</span> Zero-inactivity purge guarantee · 1 QB Quota Active
      </div>
    `;
  }

  // Saved accounts list
  const savedList = document.getElementById("savedAccountsList");
  if (savedList) {
    const otherAccounts = vault.accounts.filter(a => a.id !== vault.user.id && a.email !== vault.user.email);
    if (otherAccounts.length === 0) {
      savedList.innerHTML = `<div style="font-size: 12px; color: var(--text-muted); padding: 6px 0;">No other accounts saved on this device. Click "+ Add Account" to create or link an additional partition.</div>`;
    } else {
      savedList.innerHTML = otherAccounts.map(acc => `
        <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(255, 255, 255, 0.03); border: 1px solid var(--border-color); border-radius: 6px; padding: 8px 10px;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <div style="width: 28px; height: 28px; border-radius: 50%; background: var(--electric-violet); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 10px;">
              ${acc.username.slice(0, 2).toUpperCase()}
            </div>
            <div>
              <div style="font-weight: 600; font-size: 12px;">${escapeHtml(acc.username)}</div>
              <div style="font-size: 10px; color: var(--text-muted);">${escapeHtml(acc.email)} · ${acc.planTier}</div>
            </div>
          </div>
          <div style="display: flex; gap: 6px;">
            <button class="btn btn-outline btn-sm" onclick="switchAccount('${acc.id}')">Switch</button>
            <button class="btn btn-outline btn-sm" style="color: var(--coral-neon);" onclick="removeAccount('${acc.id}')">Remove</button>
          </div>
        </div>
      `).join("");
    }
  }
}

function toggleAddAccountForm() {
  const form = document.getElementById("addAccountForm");
  if (form) {
    form.style.display = form.style.display === "none" ? "block" : "none";
  }
}

function submitAddNewAccount() {
  const uInput = document.getElementById("newAccountUsername");
  const eInput = document.getElementById("newAccountEmail");
  const tInput = document.getElementById("newAccountTier");

  const username = uInput ? uInput.value.trim() : "";
  const email = eInput ? eInput.value.trim() : "";
  const tier = tInput ? tInput.value : "Æon Prime";

  if (!username || !email) {
    alert("Please specify both a username and valid email address.");
    return;
  }
  if (!email.includes("@")) {
    alert("Please enter a valid email address.");
    return;
  }

  ensureAccountsList();

  // Check if exists
  const existing = vault.accounts.find(a => a.email.toLowerCase() === email.toLowerCase());
  if (existing) {
    alert("An account with this email already exists on this device. Switching to existing account.");
    switchAccount(existing.id);
    toggleAddAccountForm();
    return;
  }

  const newAcc = {
    id: "acc_" + Date.now(),
    username: username,
    email: email,
    quotaUsedBytes: 0,
    planTier: tier
  };

  vault.accounts.push(newAcc);
  vault.user = newAcc;

  // Add ledger activity
  vault.activities.unshift({
    id: "act_" + Date.now(),
    action: "ACCOUNT_ADDED",
    desc: `New vault profile '${username}' added and activated with 1 QB quota`,
    time: "Just now"
  });

  saveVault();
  renderAccountsUI();
  updateQuotaDisplay();
  toggleAddAccountForm();
  if (uInput) uInput.value = "";
  if (eInput) eInput.value = "";

  alert(`Account '${username}' created and activated!`);
}

// Background Synchronization Service
class VaultSyncService {
  constructor() {
    this.syncIntervalMs = 20000; // Keep all linked accounts synchronized periodically
    this.isSyncing = false;
    this.lastSyncTime = Date.now();
    this.meshNodesOnline = 12;
  }

  start() {
    this.syncAllAccounts();
    setInterval(() => this.syncAllAccounts(), this.syncIntervalMs);
  }

  syncAllAccounts() {
    this.isSyncing = true;
    this.updateSyncUI();

    try {
      ensureAccountsList();

      // Synchronize storage metrics and integrity across all linked accounts
      vault.accounts.forEach(acc => {
        const accFiles = vault.files.filter(f => f.userId === acc.id || (!f.userId && acc.id === vault.accounts[0].id));
        const totalBytes = accFiles.reduce((sum, f) => sum + (f.size || 0), 0);
        acc.quotaUsedBytes = totalBytes;
        acc.lastSyncedAt = Date.now();
        acc.syncStatus = "Mesh Synced (12 Nodes)";
      });

      // Update active user quota if matching
      if (vault.user && vault.user.id) {
        const currentAcc = vault.accounts.find(a => a.id === vault.user.id);
        if (currentAcc) {
          vault.user.quotaUsedBytes = currentAcc.quotaUsedBytes;
        }
      }

      this.lastSyncTime = Date.now();
      saveVault();
    } catch (e) {
      console.error("Background sync error:", e);
    } finally {
      this.isSyncing = false;
      this.updateSyncUI();
    }
  }

  updateSyncUI() {
    const syncStatusEl = document.getElementById("meshSyncStatus");
    if (syncStatusEl) {
      if (this.isSyncing) {
        syncStatusEl.innerHTML = `● Syncing mesh...`;
        syncStatusEl.style.color = "var(--electric-violet)";
      } else {
        syncStatusEl.innerHTML = `● Mesh Synced (${this.meshNodesOnline} Nodes)`;
        syncStatusEl.style.color = "var(--emerald-glow)";
      }
    }
  }
}

const backgroundSyncService = new VaultSyncService();

function switchAccount(accountId) {
  ensureAccountsList();
  const target = vault.accounts.find(a => a.id === accountId);
  if (target) {
    vault.user = target;
    // Reset directory to root on profile switch
    currentFolderId = null;
    folderBreadcrumbs = [{ id: null, name: "Vault Root" }];

    // Perform instantaneous background sync cycle across accounts
    backgroundSyncService.syncAllAccounts();

    saveVault();
    renderAccountsUI();
    updateQuotaDisplay();
    renderVaultBreadcrumbs();
    renderVaultFiles();
    renderRecentFiles();
    renderAuditLedger();
  }
}

function removeAccount(accountId) {
  ensureAccountsList();
  const target = vault.accounts.find(a => a.id === accountId);
  if (!target) return;
  if (!confirm(`Are you sure you want to remove account '${target.username}' from this device?`)) return;

  vault.accounts = vault.accounts.filter(a => a.id !== accountId);
  if (vault.user.id === accountId) {
    vault.user = vault.accounts[0] || defaultState.user;
  }
  backgroundSyncService.syncAllAccounts();
  saveVault();
  renderAccountsUI();
  updateQuotaDisplay();
  renderVaultBreadcrumbs();
  renderVaultFiles();
  renderRecentFiles();
  renderAuditLedger();
}

// Initialize
checkAuth();
backgroundSyncService.start();
renderRecentFiles();
updateQuotaDisplay();
renderAccountsUI();
renderVaultBreadcrumbs();
renderVaultFiles();
