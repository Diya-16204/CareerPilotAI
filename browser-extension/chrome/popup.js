let currentDraft = null;
const statusNode = document.getElementById('status');
const draftNode = document.getElementById('draft');

async function loadDraft() {
  const result = await chrome.runtime.sendMessage({ type: 'GET_APPROVED_DRAFT' });
  currentDraft = result?.draft ?? null;
  if (!currentDraft) {
    statusNode.textContent = 'No approved draft is ready. In CareerPilot, approve a preparation and send it to browser autofill.';
    draftNode.hidden = true;
    return;
  }
  document.getElementById('job').textContent = currentDraft.job.title;
  document.getElementById('company').textContent = currentDraft.job.company;
  statusNode.textContent = 'Approved application draft ready';
  draftNode.hidden = false;
}

document.getElementById('fill').addEventListener('click', async event => {
  const button = event.currentTarget;
  button.disabled = true;
  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!tab?.id || !/^https?:/.test(tab.url ?? '')) throw new Error('Open the employer application page in this tab first.');
    await chrome.scripting.executeScript({ target: { tabId: tab.id }, files: ['shared/form-filler.js'] });
    const [result] = await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      args: [currentDraft],
      func: draft => globalThis.CareerPilotFormFiller.fill(document, draft),
    });
    const filled = result?.result;
    statusNode.textContent = filled?.count
      ? `Filled ${filled.count} field(s): ${filled.filled.slice(0, 8).join(', ')}. Review every field and submit manually.`
      : 'No supported empty fields were found. Complete the form manually; nothing was changed.';
  } catch (error) {
    statusNode.textContent = error.message || 'Could not fill this page. Check that the tab is a normal website.';
  } finally { button.disabled = false; }
});

document.getElementById('capture').addEventListener('click', async event => {
  const button = event.currentTarget;
  button.disabled = true;
  try {
    const [employerTab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!employerTab?.id || !/^https?:/.test(employerTab.url ?? '')) throw new Error('Open the employer application form in this tab first.');
    await chrome.scripting.executeScript({ target: { tabId: employerTab.id }, files: ['shared/form-capture.js'] });
    const [captureResult] = await chrome.scripting.executeScript({ target: { tabId: employerTab.id }, func: () => globalThis.CareerPilotFormCapture.capture(document) });
    const fields = captureResult?.result ?? [];
    if (!fields.length) throw new Error('No visible supported form fields were found.');
    const activeSessionResult = await chrome.runtime.sendMessage({ type: 'GET_ACTIVE_SESSION' });
    const activeSession = activeSessionResult?.activeSession;
    const appTab = activeSession?.appTabId ? await chrome.tabs.get(activeSession.appTabId).catch(() => null) : null;
    if (!appTab?.id) throw new Error('Keep the matching Auto Apply session open in another CareerPilot tab.');
    await chrome.scripting.executeScript({ target: { tabId: appTab.id }, args: [activeSession.sessionId, fields], func: (sessionId, capturedFields) => {
      window.postMessage({ source: 'careerpilot-autofill-extension', type: 'CAPTURED_FORM_FIELDS', sessionId, fields: capturedFields }, window.location.origin);
    } });
    statusNode.textContent = `Sent ${fields.length} form field(s) to Auto Apply. Review the labels and required flags before entering answers.`;
  } catch (error) {
    statusNode.textContent = error.message || 'Could not capture form fields.';
  } finally { button.disabled = false; }
});

document.getElementById('clear').addEventListener('click', async () => {
  await chrome.runtime.sendMessage({ type: 'CLEAR_APPROVED_DRAFT' });
  currentDraft = null;
  await loadDraft();
});

void loadDraft();
