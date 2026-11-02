(() => {
  const clean = value => String(value ?? '').replace(/\s+/g, ' ').trim().slice(0, 300);
  const slug = value => clean(value).toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '').slice(0, 70);
  const visible = element => Boolean(element.getClientRects().length) && getComputedStyle(element).visibility !== 'hidden';

  function capture(root) {
    const controls = [...root.querySelectorAll('input, textarea, select')].filter(control => {
      const type = (control.type || 'text').toLowerCase();
      return visible(control) && !control.disabled && !['hidden', 'password', 'file', 'submit', 'button', 'reset', 'image'].includes(type);
    });
    const fields = [];
    const seen = new Set();
    for (const control of controls) {
      const type = (control.type || control.tagName).toLowerCase();
      const radio = type === 'radio';
      const identity = radio ? `radio:${control.name || control.id}` : `${control.name || control.id || fields.length}`;
      if (seen.has(identity)) continue;
      seen.add(identity);
      const group = radio ? controls.filter(item => item.type === 'radio' && item.name === control.name) : [control];
      const labels = group.flatMap(item => [...(item.labels ?? [])].map(label => label.innerText || label.textContent || ''));
      const legend = radio ? control.closest?.('fieldset')?.querySelector?.('legend') : null;
      const label = clean((legend && (legend.innerText || legend.textContent)) || labels[0] || control.getAttribute('aria-label') || control.getAttribute('placeholder') || control.name || control.id);
      if (!label) continue;
      const inputType = radio ? 'RADIO' : type === 'select-one' ? 'SELECT' : type === 'textarea' ? 'TEXTAREA' : type === 'checkbox' ? 'CHECKBOX' :
        type === 'email' ? 'EMAIL' : type === 'tel' ? 'PHONE' : type === 'date' ? 'DATE' : type === 'number' ? 'NUMBER' : type === 'url' ? 'URL' : 'TEXT';
      const options = inputType === 'SELECT' ? [...control.options].filter(option => option.value).map(option => clean(option.textContent)) :
        inputType === 'RADIO' ? group.map(item => clean(item.labels?.[0]?.innerText || item.value)).filter(Boolean) : [];
      const required = group.some(item => item.required || item.getAttribute('aria-required') === 'true');
      const key = slug(control.name || control.id || label) || `field_${fields.length + 1}`;
      fields.push({ key, label, type: inputType, required, options, value: '' });
    }
    return fields;
  }

  globalThis.CareerPilotFormCapture = { capture };
})();

//IIFE Wrapper ((() => { ... })()): Encapsulates all script logic within a private scope to prevent global variable collisions on host web pages.
//
// clean(): Normalizes whitespace, trims text, and truncates DOM strings to 300 characters max.
//
// slug(): Converts label text or element names into clean, lowercase snake_case keys (max 70 chars) for backend schema mapping.
//
// visible(): Ensures inputs are rendered and actively visible on screen, ignoring display: none or visibility: hidden elements.
//
// Element Discovery & Filtering: Queries input, textarea, and select elements while filtering out disabled, hidden, and sensitive input types (e.g., password, file).
//
// Deduplication & Grouping: Tracks radio button groups by input name using a Set so multi-choice radio options output as a single logical field.
//
// Label Resolution: Resolves human-readable labels using a priority fallback sequence (<legend>, <label>, aria-label, placeholder, name/id).
//
// Type & Option Extraction: Normalizes control types into standard enums (TEXT, RADIO, SELECT, etc.) while collecting option choices and required attributes.
//
// Payload Export: Assembles field metadata objects into an array and exposes the capture() function globally via globalThis.CareerPilotFormCapture.
