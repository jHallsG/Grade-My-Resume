# Grade-My-Resume
An AI-powered tool that evaluates resumes against job descriptions, providing insightful feedback and a rating from 1 to 10. Using advanced AI and local embeddings, it highlights strengths, identifies gaps, and offers improvement suggestions—helping you refine your resume with confidence.

# Disclaimer
This is an AI-powered tool, and its results do not define you. No matter the outcome, trust in your skills, keep growing, and believe in yourself. AI can analyze a resume, but only you can showcase your true potential.

# Requirements
This application requires the following before running :
1. 🧠 OpenAI API Keys(Optional). This application's chat model makes use of GPT-4o-mini for faster responses. You can register and get one at [OPENAI](https://platform.openai.com/api-keys). if you have a decent PC with a dedicated GPU, you may skip this one and use a local chat model instead. :brain
2. 🐳 Docker. The application is configured to auto generate the PGVector Database docker image. This is necessary for storing the vector embeddings. [Docker](https://docs.docker.com/get-started/get-docker/)
3. 🦙 Ollama. This is the application used for interacting with our local AI chat model or embedding models. [Ollama](https://ollama.com/download/windows)

# API Endpoints
| Method | Endpoint | Description |
|----------|----------|----------|
| GET  | /description  | Accepts a String of user input. The job description is provided here.  |
| GET  | /ask  | Accepts a String of user input. The user can ask any questions relating to the provided resume like what areas to improve, ATS friendliness, etc...  |
| POST  | /upload  | Accepts pdf, docx, or txt files. Provide the resume / cv file here.  |
