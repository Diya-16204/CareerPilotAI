import React from 'react';

const sensitive = /\b(work authorization|authorized to work|visa|sponsor(ship)?|ethnicity|race|gender|disability|criminal|conviction|citizenship|veteran|salary history|date of birth|medical|religion|marital|ssn|social security|background check|right to work|sponsorship)\b/i;
const normalize = value => String(value ?? '').toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();

export function validApplicationField(field) {
  const value = String(field.value ?? '').trim();
  if (!value) return !field.required;
  switch (field.type) {
    case 'EMAIL': return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
    case 'DATE': {
      if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
      const date = new Date(`${value}T00:00:00Z`);
      return !Number.isNaN(date.valueOf()) && date.toISOString().slice(0, 10) === value;
    }
    case 'NUMBER': return /^-?\d+(\.\d+)?$/.test(value);
    case 'URL': return /^https?:\/\/\S+$/.test(value);
    case 'SELECT': case 'RADIO': return !field.options?.length || field.options.includes(value);
    case 'CHECKBOX': return value.toLowerCase() === 'true';
    default: return true;
  }
}

export function missingApplicationFields(fields = []) {
  return fields.filter(field => field.required && !validApplicationField(field));
}

export function prefillApplicationFields(fields, { profile = {}, resumeVersion = null, coverLetter = '', job = {}, answers = [] } = {}) {
  const verified = resumeVersion?.reviewed === true;
  return fields.map(field => {
    if (field.value || sensitive.test(field.label)) return field;
    const label = normalize(field.label);
    let value = '';
    if (/\b(email|e mail)\b/.test(label)) value = profile.email || (verified ? resumeVersion.extractedEmail : '') || '';
    else if (/\b(phone|mobile|telephone)\b/.test(label)) value = verified ? resumeVersion.extractedPhone ?? '' : '';
    else if (/\b(full name|your name|applicant name|first and last name)\b/.test(label)) value = profile.name ?? '';
    else if (/\b(city|location|address)\b/.test(label)) value = profile.location ?? '';
    else if (/\b(job title|position applied|role applied|job role)\b/.test(label)) value = job.title ?? '';
    else if (/\b(company name|employer name)\b/.test(label)) value = job.company ?? '';
    else if (/\b(cover letter|letter to the hiring manager)\b/.test(label)) value = coverLetter;
    else {
      const answer = answers.find(item => normalize(item.label) === label && item.confirmed && !item.consequential && !sensitive.test(item.label));
      value = answer?.confirmedAnswer ?? '';
    }
    if (field.options?.length && value && !field.options.includes(value)) value = '';
    return { ...field, value: String(value ?? '') };
  });
}

export function ApplicationFieldsEditor({ fields, onChange, disabled = false, invalid = false }) {
  if (!fields?.length) return <p className="rounded-xl border border-line bg-ink p-4 text-xs leading-5 text-muted">No employer fields captured yet. Open the application form, use the CareerPilot Autofill extension’s “Capture form fields” action, then return here to enter and save your answers.</p>;
  return <div className="space-y-3">{fields.map((field, index) => {
    const bad = invalid && field.required && !validApplicationField(field);
    const labelId = `application-field-label-${field.key}-${index}`;
    const controlId = `application-field-${field.key}-${index}`;
    const common = `field-input ${bad ? '!border-rose-300' : ''}`;
    const setValue = value => onChange(fields.map((item, itemIndex) => itemIndex === index ? { ...item, value } : item));
    let control;
    if (field.type === 'SELECT') control = <select id={controlId} className={common} value={field.value ?? ''} disabled={disabled} onChange={event => setValue(event.target.value)}><option value="">Choose…</option>{(field.options ?? []).map(option => <option key={option} value={option}>{option}</option>)}</select>;
    else if (field.type === 'RADIO') control = <fieldset className="space-y-2 rounded-xl border border-line bg-ink p-3" aria-labelledby={labelId}>{(field.options ?? []).map(option => <label key={option} className="flex items-center gap-2 text-xs"><input type="radio" name={field.key} value={option} checked={field.value === option} disabled={disabled} onChange={() => setValue(option)}/>{option}</label>)}</fieldset>;
    else if (field.type === 'CHECKBOX') control = <label className="flex min-h-11 items-center gap-2 rounded-xl border border-line bg-ink px-3 text-xs"><input id={controlId} type="checkbox" checked={String(field.value).toLowerCase() === 'true'} disabled={disabled} onChange={event => setValue(event.target.checked ? 'true' : 'false')}/>{field.options?.[0] || field.label}</label>;
    else if (field.type === 'TEXTAREA') control = <textarea id={controlId} className={`${common} !h-auto min-h-24 resize-y py-3`} maxLength={12000} value={field.value ?? ''} disabled={disabled} onChange={event => setValue(event.target.value)}/>;
    else control = <input id={controlId} type={({ EMAIL: 'email', PHONE: 'tel', DATE: 'date', NUMBER: 'number', URL: 'url' })[field.type] ?? 'text'} className={common} value={field.value ?? ''} disabled={disabled} onChange={event => setValue(event.target.value)} maxLength={12000}/>;
    const label = field.type === 'RADIO' || field.type === 'CHECKBOX'
      ? <p id={labelId} className="mb-2 block text-xs font-medium">{field.label}{field.required ? ' *' : ''}</p>
      : <label id={labelId} htmlFor={controlId} className="mb-2 block text-xs font-medium">{field.label}{field.required ? ' *' : ''}</label>;
    return <div key={`${field.key}-${index}`} className="rounded-xl border border-line bg-ink p-3">{label}{control}<p className={`mt-1 text-[9px] ${bad ? 'text-rose-200' : 'text-muted'}`}>{bad ? 'Required field: enter a valid answer.' : field.value ? 'Filled — verify this answer.' : field.required ? 'Required — your answer is needed.' : 'Optional — leave blank if not applicable.'}</p></div>;
  })}</div>;
}

export const fieldSafetyNote = 'Sensitive eligibility and legal answers are never inferred. Verify every prefilled value.';
