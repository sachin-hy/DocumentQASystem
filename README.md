# Document Q&A System (RAG)

A web app where you upload a document (or paste a link to one) and then **ask questions about it in a chat**. The answers come only from your document, not from the AI's general knowledge. This is done using **RAG (Retrieval-Augmented Generation)**.

I built this project to learn how AI apps actually work behind the scenes: how text is turned into embeddings, how a vector database finds the right part of a document, and how an LLM uses it to answer. This repository has the **Spring Boot backend**. The frontend is made with React and Tailwind CSS.

---

## Features

**Adding documents**
- Upload **PDF** and **TXT** files (up to 5 MB)
- Or paste a **URL**: direct PDF/DOCX links, Google Drive, Dropbox, Cloudinary, arXiv papers and normal web pages are supported
- Text is extracted with Apache Tika (and jsoup for web pages) and cleaned before use

**Smart chunking and search**
- **Semantic chunking:** the text is split into sentences, each sentence gets an embedding, and a new chunk starts when two neighbouring sentences are not similar enough (cosine similarity below 0.50). So chunks follow the meaning of the text, not just a fixed size
- Chunks are stored in **PostgreSQL with pgvector** (768-dimension embeddings, HNSW index, cosine distance)
- At question time the top 5 chunks above a similarity score of 0.40 are retrieved, only from the selected document
- If nothing matches, it falls back to the document summary

**Chat**
- Answers are generated only from the retrieved context. If the answer is not in the document, the bot says so instead of making something up
- **Chat memory** is saved for every user and document, so follow-up questions work
- Each user only sees their own documents

**Background processing**
- After upload, the document is sent to a **RabbitMQ** queue. A consumer extracts the text, creates the chunks and stores the embeddings in the background, so the upload API responds quickly even for bigger files
- Each document has an `indexed` flag that shows whether processing has finished

**Security**
- Sign up and login with **JWT** (token valid for 12 hours)
- Passwords are encrypted with Spring Security
- Global exception handling with clear error messages

---

## Tech Stack

| Area | Technology |
|------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| AI | Spring AI 1.0.0 (chat client, embeddings, vector store, chat memory) |
| Vector database | PostgreSQL + pgvector |
| Message queue | RabbitMQ |
| Security | Spring Security + JWT |
| Document parsing | Apache Tika, jsoup |
| File storage | Cloudinary |
| Frontend | React, Tailwind CSS |
| Build tool | Maven |
| Deployment | Docker, Render |

---

## How It Works

**1. Uploading and indexing a document**
```
User uploads file / URL
        |
        v
  Backend saves file (Cloudinary) + document record in DB
        |
        v
  Message sent to RabbitMQ queue  ---> API replies immediately
        |
        v
  Consumer picks up the message
        |
        v
  Extract text (Tika / jsoup) -> clean text -> semantic chunking
        |
        v
  Create embeddings -> store in pgvector (with document_id)
        |
        v
  Document marked as indexed
```

**2. Asking a question**
```
Question
   |
   v
Embed the question -> search pgvector (top 5, score >= 0.40, same document)
   |
   v
Join the matching chunks into context
   |
   v
Send context + question + chat history to the LLM
(strict prompt: answer only from context)
   |
   v
Answer returned to the user
```

---

## Project Structure

```
src/main/java/com/DocumentQ/ASystem
├── controller/      # Login, signup, documents, chat
├── service/         # DocumentService, RagService, ChatService, RabbitMQ producer/consumer
│   └── preprocessing/   # Text extraction, cleaning, semantic chunking
├── entity/          # Users, DocumentDetails, ChatMessage, DocumentEmbeddings
├── repository/      # Spring Data JPA repositories
├── dto/             # Request and response objects
├── congifuration/   # Security, RabbitMQ, WebSocket, Cloudinary config
├── filter/          # JWT filter
├── exception/       # Custom exceptions + global handler
├── runners/         # Startup check for the AI models
└── utils/           # JwtUtil, URL detector, file uploader
```

---

## API Endpoints

All endpoints except login and signup need `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/signup` | Create an account |
| POST | `/login` | Login and get a JWT |
| POST | `/documents/upload` | Upload a PDF or TXT file |
| POST | `/documents/url?url=...` | Add a document from a URL |
| GET | `/documents` | List my documents |
| GET | `/documents/document?documentId=...` | Details and chat history of one document |
| POST | `/chat/query` | Ask a question about a document |

---

## Getting Started

### What you need
- Java 21 and Maven (or use `mvnw`)
- PostgreSQL with the **pgvector** extension
- RabbitMQ
- A Cloudinary account (free plan is fine)
- An LLM API key (the app uses an OpenAI-compatible chat endpoint) and an embedding model with 768 dimensions

### 1. Clone the project
```bash
git clone <your-repo-url>
cd DocumentQASystem
```

### 2. Start PostgreSQL (with pgvector) and RabbitMQ
```bash
docker run -d --name pgvector -p 5432:5432 \
  -e POSTGRES_DB=document_qa -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  ankane/pgvector:latest

docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```
The file `db-init.sql` has the command that enables the vector extension:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. Add your configuration
Edit `src/main/resources/application.properties` with your own values:

```properties
server.port=9090

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/document_qa
spring.datasource.username=postgres
spring.datasource.password=your_password

# RabbitMQ
spring.rabbitmq.addresses=amqp://guest:guest@localhost:5672

# Cloudinary
cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_api_key
cloudinary.api_secret=your_api_secret

# LLM (OpenAI-compatible)
spring.ai.openai.api-key=your_llm_api_key
spring.ai.openai.base-url=your_llm_base_url
spring.ai.openai.chat.options.model=your_model_name

# Embeddings (must output 768 dimensions to match pgvector setting)
spring.ai.huggingface.embedding.api-key=your_embedding_api_key
spring.ai.huggingface.embedding.base-url=your_embedding_base_url

# pgvector
spring.ai.vectorstore.pgvector.dimensions=768
spring.ai.vectorstore.pgvector.index-type=HNSW
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.initialize-schema=true
```

> Do not push your real keys and passwords to GitHub. Use environment variables or keep a local copy of this file that is not committed. If the dimensions of your embedding model are different from 768, change the pgvector `dimensions` value too.

### 4. Run the backend
```bash
./mvnw spring-boot:run
```
The backend runs at `http://localhost:9090`.

### Run with Docker
```bash
docker build -t document-qa-app .
```
Then run the container with the same settings given as environment variables, and make sure it can reach PostgreSQL and RabbitMQ.

---

## Screenshots

Add your frontend screenshots here, for example:

```markdown
![Home page](link-to-image)
![Chat page](link-to-image)
```

---

## Things I Learned

- How RAG works from start to end: extracting text, chunking, embeddings, vector search and prompting the LLM
- Why chunking matters, and how splitting by meaning gives better answers than fixed-size chunks
- Using pgvector as a vector database and what HNSW index and cosine distance mean
- Keeping the LLM from making things up by using a strict prompt and a similarity threshold
- Using RabbitMQ to move slow work to the background
- Saving chat history per user and per document with Spring AI chat memory
- Securing the API with JWT and Spring Security

## Future Improvements

- Show live indexing progress in the frontend
- Retry and dead-letter queue in RabbitMQ for documents that fail to process
- Support DOCX file upload (right now DOCX works only through a URL)
- Streaming answers token by token
- Show the source chunks with every answer
- Make sure every document and chat request is checked against the logged-in user
- Add tests and API documentation with Swagger

---
