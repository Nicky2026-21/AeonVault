ÆonVault

The Vault That Never Forgets.

ÆonVault is a futuristic cloud-storage platform designed around high-speed uploads, persistent accounts, cross-platform access, and AI-powered file management.

🌐 Website: https://aeonvaultfilemanager.vercel.app

📱 Native Android: Available as a genuine Android application

---

✨ Features

- ☁️ 1 QB logical storage quota
- 🚀 High-speed uploads
- 📦 Chunked and multipart uploads
- 🔄 Resumable uploads
- ⏸️ Pause, resume, cancel, and retry uploads
- ⚡ Parallel/concurrent uploads
- 📊 Real-time upload speed, progress, and ETA
- ♾️ No artificial daily or monthly upload limits
- 👤 Persistent user accounts
- 🔐 Secure authentication
- 📁 Full file and folder management
- 🔗 Public and private sharing links
- 🗑️ Trash and file recovery
- ⭐ Favorites
- 🔎 Advanced file search
- 👁️ File previews
- 🤖 Google Gemini AI integration
- 📱 Native Android application
- 🌐 Responsive web application
- 🍎 iOS and iPadOS browser support
- 💻 Desktop browser support
- 🌙 Dark and light modes
- 🔒 Security-focused architecture

---

🤖 Gemini AI

ÆonVault integrates Google Gemini to provide intelligent file-management features.

Gemini can help with:

- Natural-language file searches
- Document summaries
- Automatic categorization
- File tagging
- Image understanding
- File descriptions
- Metadata generation
- Folder organization suggestions
- Duplicate/similar-file assistance
- Intelligent search
- Storage insights
- Natural-language vault commands

---

🚀 Upload System

ÆonVault is designed to make large uploads as fast and reliable as possible.

The upload engine supports:

- Multipart uploads
- Chunked transfers
- Parallel uploads
- Resumable transfers
- Dynamic chunk sizing
- Automatic retries
- Connection reuse
- Network-failure recovery
- Persistent upload queues
- Background uploads on supported Android versions

ÆonVault does not intentionally throttle upload speeds. Actual performance depends on the user's network, device, server infrastructure, and other technical factors.

---

📦 Storage Quota

Each eligible account receives a:

1 QB (Quettabyte) logical storage quota

Important Storage Disclaimer

«Storage Quota Notice: ÆonVault provides a 1 QB logical storage quota for eligible accounts. This represents the maximum storage allocation assigned to an account and does not mean that 1 QB of physical storage is individually reserved or immediately available on a single server. Actual capacity depends on ÆonVault's distributed storage infrastructure, availability, technical limitations, and applicable service policies.»

Compact version:

«1 QB logical quota · Actual physical capacity depends on distributed infrastructure.»

The quota should never be interpreted as a guarantee that 1 QB of physical hardware is personally reserved for an individual account.

---

👤 Persistent Accounts

ÆonVault is designed around durable user accounts.

The architecture includes:

- Secure registration
- Login/logout
- Password hashing
- Account recovery
- Persistent sessions
- Database backups
- Replication
- Disaster recovery
- Durable account metadata

Accounts should not be automatically deleted because of inactivity.

---

📱 Native Android

ÆonVault includes a genuine native Android application, rather than simply wrapping the website in a WebView.

Android features include:

- Native file picker
- Multi-file selection
- Android Share Sheet
- Background uploads where supported
- Upload/download notifications
- Resumable transfers
- Secure local session storage
- Device-friendly file management
- Phone and tablet support

The Android app can launch the full web version at:

https://aeonvaultfilemanager.vercel.app

---

🌐 Web Platform

The ÆonVault web application is independently accessible through:

https://aeonvaultfilemanager.vercel.app

It is designed for:

- Android
- iPhone
- iPad
- Windows
- macOS
- Linux
- Modern desktop browsers
- Modern mobile browsers

The website does not require the Android application.

### 🚀 Deploying to Vercel (Resolving DEPLOYMENT_NOT_FOUND)

If you see a `DEPLOYMENT_NOT_FOUND` error when visiting `https://aeonvaultfilemanager.vercel.app`, this means the project repository has not yet been linked and deployed to a Vercel project.

To activate the deployment:
1. **Push or Import to GitHub**: Push this repository to your GitHub account (or export the project).
2. **Import into Vercel**:
   - Go to [vercel.com/new](https://vercel.com/new).
   - Select your GitHub repository.
   - Set the Project Name to `aeonvaultfilemanager` (or assign `aeonvaultfilemanager.vercel.app` under **Project Settings → Domains**).
3. **Deploy**:
   - The included `vercel.json` and static web application in `/public` (`index.html`, `style.css`, `app.js`) will deploy automatically.
   - Once deployed, the live web app with 1 QB logical storage, upload streams, and Gemini Assistant is active.

---

🍎 iOS & iPadOS

iPhone and iPad users can access ÆonVault directly through their browser.

The responsive web application supports:

- Safari
- Touch controls
- File selection
- Uploads
- Downloads
- Sharing
- iPad landscape/portrait layouts
- Pointer and trackpad input where supported
- Add to Home Screen/PWA functionality where supported

---

📁 File Management

ÆonVault provides a complete file manager with:

- Folders
- Upload
- Rename
- Move
- Copy
- Delete
- Restore
- Permanent deletion
- Favorites
- Downloads
- Sharing
- Bulk actions
- Search
- Filtering
- Sorting
- Grid/list views
- File metadata
- Recent files
- Shared files
- Trash
- Activity history

---

🔗 File Sharing

Create secure sharing links with options including:

- Public
- Private
- Unlisted
- Password protected
- View-only
- Download-enabled
- Expiring
- Revocable

---

🔐 Security

ÆonVault is designed with security as a core requirement.

Security features include:

- HTTPS
- Secure authentication
- Password hashing
- Session/token security
- Ownership validation
- Access controls
- Private object storage
- Secure downloads
- Upload validation
- Rate limiting
- Abuse protection
- Audit logs
- Encryption at rest where supported
- Encrypted backups
- Secure environment variables
- Disaster recovery

---

🏗️ Architecture

                    ┌─────────────────────────┐
                    │      ÆonVault Web       │
                    │  https://aeonvault      │
                    │  filemanager.vercel.app │
                    └───────────┬─────────────┘
                               │
┌─────────────────┐            │            ┌─────────────────┐
│ Native Android  │────────────┼────────────│ iOS / iPadOS   │
│      App        │            │            │    Browsers     │
└─────────────────┘            │            └─────────────────┘
                               ▼
                    ┌─────────────────────┐
                    │   ÆonVault Backend  │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
        ┌───────────┐   ┌─────────────┐   ┌────────────┐
        │ PostgreSQL│   │ Object      │   │ Gemini AI  │
        │ Database  │   │ Storage     │   │            │
        └───────────┘   └─────────────┘   └────────────┘

---

⚙️ Backend

ÆonVault uses a custom production backend with:

- PostgreSQL or another production-grade database
- Scalable distributed object storage
- REST/API services
- Authentication services
- Upload processing
- Background jobs
- Backup systems
- Replication
- Disaster recovery

Supabase is not required or used.

---

🛡️ Reliability

ÆonVault is designed to prevent accidental data loss through:

- Resumable uploads
- Automatic retries
- Persistent upload queues
- Database backups
- Storage replication
- Account persistence
- Disaster recovery
- File recovery through Trash

The system should never pretend an operation succeeded when it actually failed.

---

🎨 Design

ÆonVault uses a futuristic interface inspired by:

- Glassmorphism
- Neon/futuristic UI
- Smooth animations
- Dark mode
- Light mode
- Responsive layouts
- Native Android interactions
- Fast file navigation

---

⚠️ Important

ÆonVault's 1 QB quota is a logical allocation, not a claim that 1 QB of physical storage is currently reserved for every account.

Actual storage capacity and upload performance depend on infrastructure, network conditions, technical limitations, security safeguards, and applicable service policies.

---

📜 License

Add the project's chosen open-source or proprietary license here.

---

🌌 ÆonVault

The Vault That Never Forgets.
