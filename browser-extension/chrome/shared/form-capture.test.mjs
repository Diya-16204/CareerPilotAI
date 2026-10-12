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
