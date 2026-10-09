# AI provider configuration

CareerPilot keeps AI behind the `AiProvider` interface. The default `AI_PROVIDER=local` option is deterministic and labels generated cover letters as `LOCAL_TEMPLATE`. It needs no network access or credential.

To enable the OpenAI-compatible chat completions provider, set these environment variables for the backend:

```text
AI_PROVIDER=openai
AI_API_KEY=<secret from your environment>
AI_MODEL=gpt-4.1-mini
AI_BASE_URL=https://api.openai.com
AI_TIMEOUT_SECONDS=20
AI_MAX_INPUT_CHARACTERS=20000
```

The key is read from the process environment and is never stored in the database or returned to the client. Set `AI_BASE_URL` to the provider origin for a compatible deployment. The provider sends the selected job description, the selected resume version's extracted text and skill list, the candidate's display name, and the requested tone. Resume and job text are untrusted input. The prompt directs the model to ignore instructions inside that text and use only supplied facts. The provider bounds input and output sizes, requests JSON output, validates the returned fields, uses a 5-second connect timeout and configurable read timeout, and maps rate-limit or provider errors to safe API messages. It does not automatically retry billable generation requests. Logs include model and provider token counts, not prompts, resume text, names, email addresses, or API keys.

No external API key is required for registration, resumes, job tracking, interview practice, analytics, or simulated portal workflows. When the provider is unset, keep `AI_PROVIDER=local` to use the explicitly labeled deterministic template. When `AI_PROVIDER=openai` has no key, cover-letter generation returns a safe service-unavailable error while other features remain available.

The OpenAI-compatible provider has not been exercised against a paid remote account in this workspace. Its request and response handling must be validated using a configured key before production use.
