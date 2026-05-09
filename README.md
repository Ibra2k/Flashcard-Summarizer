# Flashcard Summarizer

An AI-powered study tool that extracts content from **PDFs and lecture slide images**, summarizes it, and generates **flashcard question-answer pairs** — built for students who want to study smarter.

---

## What It Does

Upload a lecture PDF or slide image and the app will:

1. **Extract** all readable text from the file
2. **Summarize** the content into concise notes
3. **Generate flashcards** as structured question-answer pairs
4. Return everything in a clean JSON response

The backend uses **Google Gemini** as the primary LLM, with **DeepSeek** (via Ollama) as a local fallback if Gemini fails.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend API | Python, FastAPI |
| Mobile Frontend | Kotlin (Jetpack Compose) |
| Primary LLM | Google Gemini 2.0 Flash |
| Fallback LLM | DeepSeek (via Ollama, runs locally) |
| PDF Parsing | pypdf |
| Image OCR | Gemini Vision |

---

## Getting Started

### Prerequisites

- Python 3.10+
- A Google Gemini API key
- [Ollama](https://ollama.com/) installed (for DeepSeek fallback)
- Android Studio (if running the Kotlin frontend)

### 1. Clone the repo

```bash
git clone https://github.com/Ibra2k/Flashcard-Summarizer.git
cd Flashcard-Summarizer
```

### 2. Install Python dependencies

```bash
pip install fastapi uvicorn python-dotenv google-genai pypdf ollama pydantic
```

### 3. Set up your environment variables

Create a `.env` file in the root directory:

```
API_KEY=your_google_gemini_api_key_here
```

### 4. (Optional) Set up DeepSeek fallback

If you want the local LLM fallback to work, install and run Ollama with the DeepSeek model:

```bash
ollama pull deepseek-coder
ollama serve
```

### 5. Run the backend

```bash
uvicorn backend:app --reload --host 0.0.0.0 --port 8080
```

The API will be available at `http://localhost:8080`.

---

## API Endpoints

### `POST /upload_image`

Upload a lecture slide image (JPG, PNG, etc.) to extract text, summarize, and generate flashcards.

**Form field:** `image` (file)

**Response:**
```json
{
  "filename": "slide1.png",
  "title": "Networking Basics",
  "summary": "This lecture covers...",
  "questions": [
    { "question": "What is a subnet?", "answer": "A subnet is..." },
    ...
  ]
}
```

---

### `POST /upload_file`

Upload a PDF file to extract text, summarize, and generate flashcards.

**Form field:** `file` (file)

**Response:** Same structure as `/upload_image`.

---

## Project Structure

```
Flashcard-Summarizer/
├── backend.py              # FastAPI backend — LLM integration, PDF/image processing
├── SummaryAndFlashcard/    # Kotlin Android frontend
├── .gitignore
└── README.md
```

---

## LLM Fallback Logic

The app tries **Gemini first**. If Gemini fails for any reason (rate limit, API error, etc.), it automatically retries with **DeepSeek via Ollama** running locally — so the app still works offline or without an API key.

---

## Notes

- Image text extraction uses Gemini's vision capability — it works best on clear, high-contrast lecture slides.
- PDF extraction is purely text-based; scanned/image-only PDFs may return limited content.
- The number of flashcards generated is controlled by the LLM prompt and may vary per document.
