import { test } from 'node:test';
import assert from 'node:assert/strict';
import './form-capture.js';

globalThis.getComputedStyle = () => ({ visibility: 'visible' });
const control = ({ type, name, id, label, value = '', required = false, options = [] }) => {
  const labels = label ? [{ innerText: label, textContent: label }] : [];
  return { type, name, id, value, required, disabled: false, labels, options,
    getClientRects: () => [1], getAttribute: () => null,
    tagName: type === 'textarea' ? 'TEXTAREA' : 'INPUT' };
};

test('captures visible supported application fields, choices, and required flags without values', () => {
  const email = control({ type: 'email', name: 'email', label: 'Email address', value: 'private@example.com', required: true });
  const select = control({ type: 'select-one', name: 'workMode', label: 'Work mode', options: [{ value: '', textContent: 'Choose' }, { value: 'remote', textContent: 'Remote' }] });
  const yes = control({ type: 'radio', name: 'sponsorship', label: 'Yes', value: 'yes', required: true });
  const no = control({ type: 'radio', name: 'sponsorship', label: 'No', value: 'no', required: true });
  const fieldset = { querySelector: () => ({ innerText: 'Will you need sponsorship?', textContent: 'Will you need sponsorship?' }) };
  yes.closest = () => fieldset;
  no.closest = () => fieldset;
  const fields = globalThis.CareerPilotFormCapture.capture({ querySelectorAll: () => [email, select, yes, no] });
  assert.equal(fields.length, 3);
  assert.equal(fields[0].type, 'EMAIL');
  assert.equal(fields[0].required, true);
  assert.equal(fields[0].value, '');
  assert.deepEqual(fields[1].options, ['Remote']);
  assert.equal(fields[2].type, 'RADIO');
  assert.equal(fields[2].label, 'Will you need sponsorship?');
  assert.deepEqual(fields[2].options, ['Yes', 'No']);
  assert.equal(fields[2].value, '');
});

//Imports (import {test} ...): Loads native Node.js test runner, strict assertion library, and the form-capture.js source file.
//
// DOM Global Mock (globalThis.getComputedStyle = ...): Mocks browser CSS visibility logic in Node.js so element visibility checks pass during headless testing.
//
// Mock Factory Function (const control = ...): Generates simulated DOM form control objects with configurable types, labels, options, and bounding rects.
//
// Test Suite Definition (test('captures ...', () => { ... })): Registers a unit test verifying that form extraction normalizes fields, choices, and required flags without retaining pre-filled values.
//
// Email & Select Field Setup (const email = ..., const select = ...): Creates mock DOM elements for a required email input and a single-choice dropdown with options.
//
// Radio & Fieldset Mocking (const yes = ..., const fieldset = ...): Simulates a radio button pair linked to a parent <fieldset> container with a <legend> title.
//
// Capture Execution (globalThis.CareerPilotFormCapture.capture(...)): Runs the form capture function against the mock DOM tree to generate the field metadata array.
//
// Test Assertions (assert.equal(...), assert.deepEqual(...)): Asserts that 3 unified field objects are generated with mapped types, extracted options, clean labels, and empty value strings.

//assert.equal(fields.length, 3);: Confirms that exactly 3 distinct form fields were captured (email, select dropdown, and radio group).
//
// assert.equal(fields[0].type, 'EMAIL');: Verifies that the first field was correctly mapped to the EMAIL type enum.
//
// assert.equal(fields[0].required, true);: Checks that the email field was properly flagged as required.
//
// assert.equal(fields[0].value, '');: Ensures any pre-existing text in the email field was erased for client privacy.
//
// assert.deepEqual(fields[1].options, ['Remote']);: Verifies that the select dropdown extracted only actual options (['Remote']), ignoring placeholder choices like "Choose".
//
// assert.equal(fields[2].type, 'RADIO');: Confirms the radio button group was mapped as a single RADIO field.
//
// assert.equal(fields[2].label, 'Will you need sponsorship?');: Checks that the question label was accurately extracted from the parent <fieldset> legend.
//
// assert.deepEqual(fields[2].options, ['Yes', 'No']);: Verifies that both radio choices ('Yes' and 'No') were gathered into the field options list.
//
// assert.equal(fields[2].value, '');: Confirms the radio group's initial value was kept empty for user safety.
