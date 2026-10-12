import { test } from 'node:test';
import assert from 'node:assert/strict';
import './form-filler.js';

class Control {
  constructor({ label = '', type = 'text', value = '', disabled = false, readOnly = false } = {}) {
    this.labels = label ? [{ textContent: label }] : [];
    this.type = type;
    this.value = value;
    this.disabled = disabled;
    this.readOnly = readOnly;
    this.dataset = {};
    this.tagName = type === 'textarea' ? 'TEXTAREA' : 'INPUT';
    this.id = '';
    this.attrs = { type };
    if (label) this.attrs['aria-label'] = label;
  }
  getAttribute(key) { return this.attrs[key] ?? null; }
  hasAttribute(key) { return key === 'hidden' && Boolean(this.attrs.hidden); }
  dispatchEvent() { return true; }
}

test('fills supported empty fields from an approved draft, preserving existing and sensitive fields', () => {
  const name = new Control({ label: 'Full name' });
  const email = new Control({ type: 'email', label: 'Email address' });
  const confirmedAnswer = new Control({ label: 'Why are you interested in this role?' });
  const prefilled = new Control({ label: 'Phone', type: 'tel', value: 'already entered' });
  const visa = new Control({ label: 'Work authorization status' });
  const password = new Control({ label: 'Email', type: 'password' });
  const root = { querySelectorAll: () => [name, email, confirmedAnswer, prefilled, visa, password] };
  const draft = {
    profile: { name: 'Sam Example', email: 'sam@example.test', phone: '555-0100' },
    job: { title: 'Software Engineer' },
    coverLetter: 'I am interested in this role.',
    answers: [
      { label: 'Why are you interested in this role?', value: 'My confirmed answer.' },
      { label: 'Work authorization status', value: 'Confirmed sensitive answer.' },
    ],
  };

  const result = globalThis.CareerPilotFormFiller.fill(root, draft);

  assert.equal(name.value, 'Sam Example');
  assert.equal(email.value, 'sam@example.test');
  assert.equal(confirmedAnswer.value, 'My confirmed answer.');
  assert.equal(prefilled.value, 'already entered');
  assert.equal(visa.value, '');
  assert.equal(password.value, '');
  assert.equal(result.count, 3);
});
