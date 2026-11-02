const ALLOWED_SENDERS = new Set(['localhost', '127.0.0.1', 'career-pilot-ai-blond-seven.vercel.app']);
const MAX_AGE_MS = 30 * 60 * 1000;

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message?.type === 'REGISTER_ACTIVE_SESSION') {
    const senderUrl = sender.tab?.url;
    let host;
    try { host = new URL(senderUrl).hostname; } catch { host = ''; }
    if (!ALLOWED_SENDERS.has(host) || !/^[0-9a-f-]{36}$/i.test(message.sessionId ?? '')) {
      sendResponse({ ok: false });
      return false;
    }
    chrome.storage.session.set({ activeSession: { sessionId: message.sessionId, appTabId: sender.tab.id } })
      .then(() => sendResponse({ ok: true })).catch(() => sendResponse({ ok: false }));
    return true;
  }
  if (message?.type === 'GET_ACTIVE_SESSION') {
    chrome.storage.session.get('activeSession').then(({ activeSession }) => sendResponse({ activeSession: activeSession ?? null }))
      .catch(() => sendResponse({ activeSession: null }));
    return true;
  }
  if (message?.type !== 'SAVE_APPROVED_DRAFT') return false;
  const senderUrl = sender.tab?.url;
  let host;
  try { host = new URL(senderUrl).hostname; } catch { host = ''; }
  const payload = message.payload;
  if (!ALLOWED_SENDERS.has(host) || payload?.status !== 'APPROVED' || !payload.sessionId || !payload.job?.title) {
    sendResponse({ ok: false });
    return false;
  }
  const safe = {
    sessionId: String(payload.sessionId).slice(0, 64),
    createdAt: Date.now(),
    job: {
      title: String(payload.job.title).slice(0, 200),
      company: String(payload.job.company ?? '').slice(0, 200),
      url: typeof payload.job.url === 'string' && payload.job.url.startsWith('https://') ? payload.job.url.slice(0, 2000) : '',
    },
    profile: Object.fromEntries(['name', 'email', 'phone', 'location', 'headline'].map(key => [key, String(payload.profile?.[key] ?? '').slice(0, 500)])),
    coverLetter: String(payload.coverLetter ?? '').slice(0, 12000),
    answers: Array.isArray(payload.answers) ? payload.answers.slice(0, 20).map(answer => ({
      label: String(answer.label ?? '').slice(0, 300), value: String(answer.value ?? '').slice(0, 2000),
    })) : [],
  };
  chrome.storage.session.set({ approvedDraft: safe }).then(() => sendResponse({ ok: true })).catch(() => sendResponse({ ok: false }));
  return true;
});

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  if (message?.type === 'GET_APPROVED_DRAFT') {
    chrome.storage.session.get('approvedDraft').then(({ approvedDraft }) => {
      if (!approvedDraft || Date.now() - approvedDraft.createdAt > MAX_AGE_MS) {
        chrome.storage.session.remove('approvedDraft').then(() => sendResponse({ draft: null }));
      } else sendResponse({ draft: approvedDraft });
    }).catch(() => sendResponse({ draft: null }));
    return true;
  }
  if (message?.type === 'CLEAR_APPROVED_DRAFT') {
    chrome.storage.session.remove('approvedDraft').then(() => sendResponse({ ok: true })).catch(() => sendResponse({ ok: false }));
    return true;
  }
  return false;
});

//Domain Allowlist (ALLOWED_SENDERS): Defines a Set of trusted hostnames (localhost, 127.0.0.1, and the Vercel domain) allowed to trigger extension background operations.
//
// Draft Expiration Calculation (MAX_AGE_MS): Sets a 30-minute time-to-live limit (
// 30×60×1000 ms) for stored application drafts (note: contains - diff formatting artifacts on lines 2–3).
//
// Web App Message Listener: Registers the main background listener to filter and process cross-context messages sent from host application tabs.
//
// Session Registration (REGISTER_ACTIVE_SESSION): Verifies the sender's origin host and valid UUID structure before saving the sessionId and tab ID into chrome.storage.session.
//
// Session Lookup (GET_ACTIVE_SESSION): Reads and returns the currently stored active session metadata from session storage.
//
// Draft Validation & Storage (SAVE_APPROVED_DRAFT): Enforces origin security, validates the APPROVED payload status, truncates text inputs (profile, cover letter, Q&As), and saves the timestamped draft.
//
// Internal Extension Listener: Registers a second listener dedicated to handling draft retrieval and cleanup requests from the extension popup context.
//
// Draft Retrieval & TTL Check (GET_APPROVED_DRAFT): Retrieves the cached draft and automatically purges it from storage if its age exceeds the 30-minute TTL (MAX_AGE_MS).
//
// Draft Removal (CLEAR_APPROVED_DRAFT): Deletes the approvedDraft key from chrome.storage.session when triggered by the popup interface.
