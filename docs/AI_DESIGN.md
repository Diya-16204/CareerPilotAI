# AI Design

Domain services depend on an `AiProvider` port, never directly on a vendor SDK. The only configured implementation today is `LOCAL_TEMPLATE`; a non-local provider setting fails closed with 503 instead of pretending a provider ran. There is no remote LLM adapter or AI API key integration yet. The local cover-letter draft uses only the candidate name, job title/company, and resume-version skill list supplied by the application.

Core matching uses normalized skills and a versioned deterministic formula with evidence. Interview questions are seeded and evaluations identify their source. Current interview evaluation is only a local keyword-coverage heuristic and cannot judge technical correctness or run code. Provider API credentials are not consumed by this build. Uploaded resumes and job descriptions are untrusted data and cannot override system instructions.

Resume/cover-letter drafts use only supplied facts. Unsupported achievements are never invented; missing metrics are requested or marked as user-verification placeholders. Provider output is reviewed and edited by the user before any resume change is applied. Production AI use requires cost limits, abuse throttling, privacy disclosure, and retention controls.
