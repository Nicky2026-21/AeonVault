// ÆonVault Web Application Core
const VAULT_KEY = "aeonvault_state_v1";

const defaultState = {
  user: {
    username: "NexusCommander",
    email: "vault.commander@aeonvaultfilemanager.vercel.app",
    quotaUsedBytes: 45900000,
    planTier: "Æon Prime"
  },
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
  if (tabId === "files") renderVaultFiles();
  if (tabId === "shared") renderSharedLinks();
  if (tabId === "activity") renderAuditLedger();
}

document.querySelectorAll(".nav-item").forEach(btn => {
  btn.addEventListener("click", () => switchTab(btn.getAttribute("data-tab")));
});

// Render Recent Files Table
function renderRecentFiles() {
  const tbody = document.getElementById("recentFilesBody");
  if (!tbody) return;
  tbody.innerHTML = vault.files.map(f => `
    <tr>
      <td><strong>${f.name}</strong></td>
      <td><span class="badge badge-cyan">${f.category}</span></td>
      <td>${f.formattedSize}</td>
      <td><code>${f.encryption}</code></td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="openFilePreview('${f.id}')">Inspect</button>
        <button class="btn btn-outline btn-sm" onclick="openShareModal('${f.id}')">Share</button>
      </td>
    </tr>
  `).join("");
}

// Render Vault Files (My Vault)
function renderVaultFiles() {
  const container = document.getElementById("vaultFilesContainer");
  if (!container) return;

  const filtered = vault.files.filter(f => {
    if (activeCategory === "ALL") return true;
    if (activeCategory === "FAVORITES") return f.isFavorite;
    return f.category === activeCategory;
  });

  if (filtered.length === 0) {
    container.innerHTML = `<div style="padding:48px; text-align:center; color:var(--text-muted);">No items in this category.</div>`;
    return;
  }

  container.innerHTML = `
    <table class="files-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Category</th>
          <th>Size</th>
          <th>Last Modified</th>
          <th>Integrity</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${filtered.map(f => `
          <tr>
            <td><strong>${f.name}</strong></td>
            <td><span class="badge badge-cyan">${f.category}</span></td>
            <td>${f.formattedSize}</td>
            <td>${f.updatedAt}</td>
            <td><span class="badge badge-emerald">SHA-256 OK</span></td>
            <td>
              <button class="btn btn-outline btn-sm" onclick="openFilePreview('${f.id}')">Inspect</button>
              <button class="btn btn-outline btn-sm" onclick="openShareModal('${f.id}')">Share</button>
              <button class="btn btn-outline btn-sm" style="color:var(--coral)" onclick="deleteFile('${f.id}')">Delete</button>
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
    alert(`Downloading "${file.name}" to your local browser storage.`);
  };
  openModal("filePreviewModal");
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

// Initialize
renderRecentFiles();
updateQuotaDisplay();
