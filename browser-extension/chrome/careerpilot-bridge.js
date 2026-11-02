(() => {
  const allowedHosts = new Set(['localhost', '127.0.0.1', 'career-pilot-ai-blond-seven.vercel.app']);
  if (!allowedHosts.has(window.location.hostname)) return;

  window.addEventListener('message', event => {
    if (event.source !== window || event.origin !== window.location.origin) return;
    const message = event.data;
    if (message?.source === 'careerpilot-app' && message.type === 'REGISTER_ACTIVE_SESSION' && typeof message.sessionId === 'string') {
      chrome.runtime.sendMessage({ type: 'REGISTER_ACTIVE_SESSION', sessionId: message.sessionId })
        .then(result => window.postMessage({ source: 'careerpilot-autofill-extension', type: 'ACTIVE_SESSION_REGISTERED', requestId: message.requestId, ok: Boolean(result?.ok) }, window.location.origin))
        .catch(() => window.postMessage({ source: 'careerpilot-autofill-extension', type: 'ACTIVE_SESSION_REGISTERED', requestId: message.requestId, ok: false }, window.location.origin));
      return;
    }
    if (message?.source === 'careerpilot-autofill-extension' && message.type === 'CAPTURED_FORM_FIELDS' && Array.isArray(message.fields)) {
      window.dispatchEvent(new CustomEvent('careerpilot:captured-form-fields', { detail: message }));
      return;
    }
    if (message?.source !== 'careerpilot-app' || message.type !== 'SAVE_APPROVED_DRAFT') return;
    const payload = message.payload;
    if (payload?.status !== 'APPROVED' || typeof payload.sessionId !== 'string' ||
        !payload.job || typeof payload.job.title !== 'string' || !Array.isArray(payload.answers)) {
      window.postMessage({ source: 'careerpilot-autofill-extension', requestId: message.requestId, ok: false,
        message: 'Only an approved preparation session can be shared.' }, window.location.origin);
      return;
    }
    chrome.runtime.sendMessage({ type: 'SAVE_APPROVED_DRAFT', payload })
      .then(() => window.postMessage({ source: 'careerpilot-autofill-extension', requestId: message.requestId, ok: true }, window.location.origin))
      .catch(() => window.postMessage({ source: 'careerpilot-autofill-extension', requestId: message.requestId, ok: false,
        message: 'The extension could not save this draft for the current browser session.' }, window.location.origin));
  });
})();

//IIFE Wrapper ((() => { ... })()): Encapsulates the script execution inside a private closure to avoid polluting the global window scope.
//
// Domain Access Restriction (allowedHosts): Restricts execution to authorized app domains (localhost, 127.0.0.1, and the Vercel app host) to prevent untrusted sites from calling the extension bridge.
//
// Window Message Listener (window.addEventListener('message', ...)): Listens for cross-context postMessage events sent between the host web application and the content script.
//
// Origin Security Filter (event.origin !== window.location.origin): Validates that incoming window messages originate strictly from the current window and matching origin to prevent cross-site script exploits.
//
// Session Registration Handler (REGISTER_ACTIVE_SESSION): Relays session registration requests from the web app to the extension background script and posts back execution confirmation.
//
// Captured Form Event Relay (CAPTURED_FORM_FIELDS): Converts form field metadata captured by the extension into a native custom DOM event (careerpilot:captured-form-fields) on the web page.
//
// Draft Save Filter Guard (SAVE_APPROVED_DRAFT): Ignores any incoming message that isn't an explicit request from the web app to save an application draft.
//
// Draft Payload Validator (payload?.status !== 'APPROVED'): Ensures draft payloads are explicitly marked APPROVED and contain valid job details and answers before saving.
//
// Background Storage Handoff (chrome.runtime.sendMessage): Sends validated draft payloads to the extension background script and notifies the web app window of success or failure via window.postMessage.
