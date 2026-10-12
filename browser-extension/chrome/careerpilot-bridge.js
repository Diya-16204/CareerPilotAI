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
