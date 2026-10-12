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
