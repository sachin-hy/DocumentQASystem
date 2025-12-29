# Document Q&A System – AI-powered Document Understanding with RAG

##  Overview
A full-stack web application where users can **upload documents (PDF, DOCX, TXT)** and then **ask questions** about the content.  
The system uses **RAG (Retrieval-Augmented Generation)** to retrieve relevant sections and generate accurate answers.

---

## ✨ Key Features

### 📄 Intelligent Document Processing
- Automatic document ingestion with text extraction and intelligent chunking
- Vector-based indexing for fast and accurate semantic search
- Support for large documents with optimized batch processing

### 🧠 AI-Powered Question Answering
- Semantic similarity search using **pgvector** and embeddings
- Context-aware response generation using **Ollama-hosted LLMs**
- Strict document-grounded answers (no hallucinated responses)

### 💬 Conversational Experience
- Real-time chat-based document querying
- Persistent conversation history per user and document
- Context-aware multi-turn interactions

### 🔐 Security & Authentication
- JWT-based authentication and authorization
- Secure document access per user
- API-level access control

### ⚙️ Scalable & Containerized Architecture
- Fully containerized using **Docker**
- Multi-service orchestration via **Docker Compose**
- Isolated services for:
  - Spring Boot backend
  - PostgreSQL with pgvector
  - Ollama LLM runtime
- Environment-based configuration for easy deployment

### 🧩 Modern Tech Stack
- **Backend:** Spring Boot (REST APIs)
- **Frontend:** React
- **Database:** PostgreSQL + pgvector
- **LLM Runtime:** Ollama (local, offline-first inference)
- **AI Framework:** Spring AI

### 🔌 RESTful API Design
- APIs for document upload and indexing
- APIs for semantic search and chat responses
- APIs for conversation and chat history retrieval


---

##  Tech Stack

### **Backend**
-  **Java**
-  **Spring Boot**
-  **Spring Security**
-  **JWT Token**
-  **Spring Hibernate**
-  **Spring AI**
-  **PostgreSQL + pgvector**

### **Frontend**
-  **React**
-  **Tailwind CSS**
-  **REST API Integration**

### ***Frontend View***
<img width="1881" height="968" alt="image" src="https://github.com/user-attachments/assets/5afdf9a0-2c2a-420d-a2ad-51ddbcc0aa2c" />
<img width="1886" height="961" alt="image" src="https://github.com/user-attachments/assets/0b65fa3e-b9a3-460a-9a94-f522abc56d63" />
<img width="1888" height="956" alt="image" src="https://github.com/user-attachments/assets/5c745d81-53bd-4121-99c1-a2833fe5ece3" />



