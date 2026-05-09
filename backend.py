from fastapi import FastAPI, File, UploadFile, HTTPException
from pydantic import BaseModel
import json
import re
import time
import os
from dotenv import load_dotenv
from google import genai
from google.genai import types
from ollama import chat, ChatResponse
import pypdf

# TO Run Program
# ➜ uvicorn backend:app --reload --host 0.0.0.0 --port 8080
# source


# ================== SETUP ==================
load_dotenv()
API_KEY = os.getenv("API_KEY")

gemini_model = "gemini-2.0-flash"
deepseek_model = "deepseek-coder"

user = genai.Client(api_key=API_KEY)
app = FastAPI()


# ================== MODELS ==================
class QuestionAnswer(BaseModel):
    question: str
    answer: str


class UploadResponse(BaseModel):
    filename: str
    title: str
    summary: str
    questions: list[QuestionAnswer]


# ================== HELPERS ==================
def extract_json(text: str):
    """
    Extracts a clean JSON object from the LLM response.
    Handles cases where markdown or junk text is present.
    """
    try:
        # Remove markdown fences like ```json or ```
        cleaned = re.sub(r"```(?:json)?", "", text).strip()

        # Extract JSON object (not just an array anymore)
        match = re.search(r"\{.*\}", cleaned, re.DOTALL)
        if match:
            cleaned = match.group(0)

        return json.loads(cleaned)
    except Exception as e:
        print(f"JSON parsing failed: {e}")
        return {"title": "Untitled Flashcards", "flashcards": []}


def extract_text_from_pdf(pdf_path):
    """Reads text from a PDF file"""
    text = ""
    try:
        with open(pdf_path, 'rb') as pdf_file:
            reader = pypdf.PdfReader(pdf_file)
            for page in reader.pages:
                text += page.extract_text()
    except FileNotFoundError:
        return "Error: PDF file not found."
    except Exception as e:
        return f"Error: {e}"
    return text


def read_image_text(image_bytes, mime_type):
    """Extracts text from an image using Gemini"""
    try:
        print("📷 Reading Image Text...")
        response = user.models.generate_content(
            model=gemini_model,
            contents=[
                types.Part.from_bytes(data=image_bytes, mime_type=mime_type),
                "Extract all readable text from this image exactly as it appears and format into a single line."
            ]
        )
        return response.text
    except Exception as e:
        print("Error during read_image_text:", e)
        raise


def summarize_and_generate_questions(text: str, use_gemini=True, num_questions=10):
    """Summarizes text and generates question-answer pairs with a title."""
    try:
        if not text or len(text.strip()) < 10:
            raise ValueError(
                "Image text is too short or empty. Cannot summarize.")

        # ===================== GEMINI =====================
        if use_gemini:
            print("...ummarizing with Gemini...")

            summary = user.models.generate_content(
                model=gemini_model,
                contents=[f"Summarize this text: {text}"]
            ).text

            time.sleep(1)

            questions_raw = user.models.generate_content(
                model=gemini_model,
                contents=[f"""
                Create a set of flashcards for students based on the following text:

                {text}

                Output MUST be ONLY valid JSON.

                ULES:
                - DO NOT add any explanations, titles, or text outside the JSON.
                - DO NOT use markdown or code blocks (no ```json).
                - DO NOT add trailing commas.
                - MUST be a single JSON object with a "title" string and a "flashcards" array like this:

                {{
                  "title": "Networking Basics",
                  "flashcards": [
                    {{"question": "What is AI?", "answer": "Artificial Intelligence is the simulation of human intelligence in machines."}},
                    {{"question": "What is a neural network?", "answer": "A neural network is a series of algorithms that mimic the operations of a human brain."}}
                  ]
                }}
                """]
            ).text

            parsed = extract_json(questions_raw)
            title = parsed.get("title", "Untitled Flashcards")
            questions_list = parsed.get("flashcards", [])

        # ===================== DEEPSEEK FALLBACK =====================
        else:
            print("Falling back to DeepSeek...")

            summary_resp: ChatResponse = chat(model=deepseek_model, messages=[
                {'role': 'user', 'content': f'Summarize the text: {text}'},
            ])
            time.sleep(1)

            question_resp: ChatResponse = chat(model=deepseek_model, messages=[
                {'role': 'user',
                 'content': f"""
                Create a set of flashcards for students based on the following text:

                {text}

                Output MUST be ONLY valid JSON.

                RULES:
                - DO NOT add any explanations, titles, or text outside the JSON.
                - DO NOT use markdown or code blocks (no ```json).
                - DO NOT add trailing commas.
                - MUST be a single JSON object with a "title" string and a "flashcards" array like this:

                {{
                  "title": "Networking Basics",
                  "flashcards": [
                    {{"question": "What is AI?", "answer": "Artificial Intelligence is the simulation of human intelligence in machines."}},
                    {{"question": "What is a neural network?", "answer": "A neural network is a series of algorithms that mimic the operations of a human brain."}}
                  ]
                }}
                """}
            ])

            summary = summary_resp.message.content
            parsed = extract_json(question_resp.message.content)
            title = parsed.get("title", "Untitled Flashcards")
            questions_list = parsed.get("flashcards", [])

        return summary, title, questions_list

    except Exception as e:
        print(
            f"Error during {'Gemini' if use_gemini else 'DeepSeek'} summarization: {e}")
        if use_gemini:
            return summarize_and_generate_questions(text, use_gemini=False)
        else:
            raise RuntimeError(
                "Both Gemini and DeepSeek summarization failed.") from e


# ================== ROUTES ==================
@app.post("/upload_image", response_model=UploadResponse)
async def upload_image(image: UploadFile = File(...)) -> UploadResponse:
    """Handles image upload, extracts text, summarizes, and generates questions"""
    try:
        image_bytes = await image.read()
        mime_type = image.content_type or "image/jpeg"

        image_text = read_image_text(image_bytes, mime_type)

        summary, title, questions_list = summarize_and_generate_questions(
            text=image_text)

        return UploadResponse(
            filename=image.filename,
            title=title,
            summary=summary,
            questions=questions_list
        )

    except Exception as e:
        print(f"Problem during image upload: {e}")
        raise HTTPException(
            status_code=500, detail=f"Error processing image: {e}")


@app.post("/upload_file", response_model=UploadResponse)
async def upload_file(file: UploadFile = File(...)) -> UploadResponse:
    """Handles PDF upload, extracts text, summarizes, and generates questions"""
    try:
        contents = await file.read()

        temp_path = f"temp_{file.filename}"
        with open(temp_path, "wb") as f:
            f.write(contents)

        file_text = extract_text_from_pdf(temp_path)

        os.remove(temp_path)

        summary, title, questions_list = summarize_and_generate_questions(
            text=file_text)

        return UploadResponse(
            filename=file.filename,
            title=title,
            summary=summary,
            questions=questions_list
        )

    except Exception as e:
        print(f"Problem during file upload: {e}")
        raise HTTPException(
            status_code=500, detail=f"Error processing file: {e}")
