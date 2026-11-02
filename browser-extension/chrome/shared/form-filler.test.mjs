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

//Imports (import ...): Loads the native Node.js test runner, strict assertion library, and the form-filler.js module under test.
//
// Mock DOM Control Class (class Control): Constructs simulated HTML input elements with labels, dataset properties, attribute getters, and event dispatch stubs.
//
// Test Suite Definition (test('fills supported ...', ...)): Registers a unit test verifying that form filling populates empty supported fields while leaving pre-filled text and sensitive questions untouched.
//
// Form Control Instantiation (const name = ...): Creates mock inputs for name, email, custom question, pre-filled phone, work authorization (sensitive), and password.
//
// Mock Root Container (const root = ...): Mocks a DOM root element whose querySelectorAll method returns the list of simulated form controls.
//
// Draft Data Fixture (const draft = ...): Defines applicant candidate profile values, job details, cover letter text, and pre-confirmed answer items.
//
// Autofill Execution (globalThis.CareerPilotFormFiller.fill(...)): Executes the form-filling engine against the mock root container using the test draft.
//
// Autofill Assertions (assert.equal(name.value, ...)): Verifies that empty name, email, and custom question fields are correctly populated with draft content.
//
// Safety Assertions (assert.equal(prefilled.value, ...)): Confirms that existing text is preserved, work authorization fields are left empty, and password inputs are ignored.
//
// Filled Count Assertion (assert.equal(result.count, 3)): Validates that the autofill operation successfully modified exactly 3 fields in total.
