(() => {
  const normalize = value => String(value ?? '').toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
  const sensitive = /\b(work authorization|authorized to work|visa|sponsor(ship)?|ethnicity|race|gender|disability|criminal|conviction|citizenship|veteran|salary history|date of birth|medical|religion|marital|ssn|social security|background check|right to work)\b/i;

  function labelsFor(control) {
    const labels = [...(control.labels ?? [])].map(label => label.innerText || label.textContent || '');
    return [...labels, control.getAttribute('aria-label'), control.getAttribute('placeholder'), control.getAttribute('name'), control.id]
      .map(normalize).filter(Boolean);
  }

  function profileValue(labels, type, profile, job, coverLetter) {
    if (type === 'email' || labels.some(label => /\b(email|e mail)\b/.test(label))) return profile.email;
    if (type === 'tel' || labels.some(label => /\b(phone|mobile|telephone)\b/.test(label))) return profile.phone;
    if (labels.some(label => /\b(full name|your name|name)\b/.test(label))) return profile.name;
    if (labels.some(label => /\b(city|location)\b/.test(label))) return profile.location;
    if (labels.some(label => /\b(headline|professional title)\b/.test(label))) return profile.headline;
    if (labels.some(label => /\b(cover letter|letter to the hiring manager)\b/.test(label))) return coverLetter;
    if (labels.some(label => /\b(position applied|job title|role applied)\b/.test(label))) return job.title;
    return '';
  }

  function fill(root, draft) {
    const eligible = [...root.querySelectorAll('input, textarea')].filter(control => {
      const type = (control.getAttribute('type') || 'text').toLowerCase();
      return !['hidden', 'password', 'file', 'checkbox', 'radio', 'submit', 'button', 'reset', 'image', 'search'].includes(type) &&
        !control.disabled && !control.readOnly && !control.hasAttribute('hidden') && !String(control.value ?? '').trim();
    });
    const results = [];
    const answers = draft.answers ?? [];
    for (const control of eligible) {
      const labels = labelsFor(control);
      const combined = labels.join(' ');
      if (sensitive.test(combined)) continue;
      let value = profileValue(labels, (control.getAttribute('type') || 'text').toLowerCase(), draft.profile ?? {}, draft.job ?? {}, draft.coverLetter ?? '');
      if (!value) {
        const exact = new Set(labels);
        const answer = answers.find(item => !sensitive.test(item.label) && exact.has(normalize(item.label)));
        if (answer) value = answer.value;
      }
      if (!String(value ?? '').trim()) continue;
      control.value = String(value);
      control.dispatchEvent(new Event('input', { bubbles: true }));
      control.dispatchEvent(new Event('change', { bubbles: true }));
      control.dataset.careerpilotAutofilled = 'true';
      results.push(labels[0] || control.tagName.toLowerCase());
    }
    return { filled: results, count: results.length };
  }

  globalThis.CareerPilotFormFiller = { fill };
})();

//IIFE Module Wrapper ((() => { ... })()): Encapsulates all filler logic within a private scope to prevent global variable collisions on host web pages.
//
// normalize(): Converts text to lowercase and replaces non-alphanumeric characters with spaces for clean keyword matching.
//
// sensitive Safety Pattern: Defines a strict denylist of sensitive keywords (e.g., SSN, visa/sponsorship status, race, criminal history, salary) to prevent automated injection into high-risk form fields.
//
// labelsFor(): Extracts and normalizes all available metadata descriptors for an input field (<label>, aria-label, placeholder, name, id).
//
// profileValue(): Maps normalized field labels or input types (email, phone, name, location, headline, cover letter, job title) to candidate profile data.
//
// fill() Target Discovery: Queries input and textarea elements while filtering out disabled, read-only, hidden, non-text inputs, and fields that already contain text.
//
// fill() Match & Injection: Skips sensitive fields, retrieves matching candidate data (or fallback draft.answers), and populates control.value.
//
// DOM Event Dispatching: Triggers native input and change events so reactive frameworks (React, Vue, Angular) detect the newly injected text.
//
// Autofill Tracking & Export: Flags filled controls with data-careerpilot-autofilled="true", returns a count/list of filled elements, and exposes CareerPilotFormFiller globally via globalThis.
