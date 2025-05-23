# Bulk User Import System

A full-stack CSV user import system developed as part of the AllRide coding challenge. This project demonstrates clean architectural design using Kotlin + Spring Boot (backend) and React + TypeScript + Tailwind CSS (frontend), with an in-memory pub/sub mechanism powered by Kotlin Coroutines.

---

## Tech Stack

### Backend

- **Language**: Kotlin
- **Framework**: Spring Boot
- **Messaging**: Kotlin Coroutines Channels (in-memory pub/sub)
- **Build Tool**: Gradle

### Frontend

- **Library**: React
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Form Handling**: `react-hook-form` + `zod` (schema validation)
- **Build Tool**: Yarn + Vite

---

## Features

### Frontend

- Sample CSV file download
- Drag-and-drop and manual CSV file upload
- Client-side validation: `.csv` format check and file size
- Upload status feedback: success/error indicators

### Backend

- REST API: `POST /api/upload` to accept and temporarily store CSV files
- Event publishing: emits a `FileUploadedEvent` with file path and metadata
- Event subscription: processes CSV in background on event reception
- Row-wise parsing, validation, and feedback
- In-memory pub/sub via `Channel`
- Robust error handling for malformed rows

---

## Getting Started

### Prerequisites

- **Java** 17+
- **Node.js** 18+
- **Yarn** 1.22+

---

### 🛠️ Setup & Run

From the root of the project:

```bash
# Install dependencies for both backend and frontend
yarn install-all

# Start the backend (port 8080) and frontend (port 5173 with proxy)
yarn dev
```

> The frontend proxies API calls to `http://localhost:8080` automatically.

---

### Upload a CSV

You can test the upload via CURL:

```bash
curl -X POST http://localhost:8080/api/upload \
  -H "Content-Type: multipart/form-data" \
  -F "file=@sample.csv"
```

---

## 🧾 Sample CSV Format

```
id,firstName,lastName,email
1,Alice,Johnson,alice@example.com
2,Bob,Smith,bob.smith@example.com
```

---

## Architecture & Design

### Event-Driven Workflow

- `UploadController` receives the CSV and emits `FileUploadedEvent`
- `EventBus` manages event publishing/subscription using Kotlin `Channel`
- `FileProcessingService` subscribes to the event and:
  - Reads each line
  - Validates fields
  - Emits row-wise results

### Design Highlights

- Clean separation of concerns (controller, service, event layer, model)
- Type-safe validation (zod + react-hook-form on frontend, manual validation on backend)
- Lightweight and scalable pub/sub simulation using native coroutines

---

## Project Structure

```
bulk-user-import/
├── backend/                   # Kotlin + Spring Boot application
│   ├── controller/            # REST endpoint
│   ├── service/               # CSV processing logic
│   ├── pubsub/                # Event bus and events
│   ├── model/                 # Data model
├── frontend/                  # React + TypeScript app
│   ├── components/            # UI components
│   ├── hooks/                 # SSE integration
│   ├── utils/                 # CSV validation helpers
```

---

## Author

Developed by **Dhyey Buddhadev**  