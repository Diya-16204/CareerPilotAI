import { describe, expect, it } from 'vitest';
import { missingApplicationFields, prefillApplicationFields } from './autoApplyFields.jsx';

describe('Auto Apply application fields', () => {
  it('prefills verified profile and reviewed resume values but never sensitive eligibility fields', () => {
    const fields = [
      { key: 'name', label: 'Applicant full name', type: 'TEXT', required: true, value: '' },
      { key: 'email', label: 'Email address', type: 'EMAIL', required: true, value: '' },
      { key: 'phone', label: 'Phone number', type: 'PHONE', required: false, value: '' },
      { key: 'sponsor', label: 'Will you need visa sponsorship?', type: 'RADIO', required: true, options: ['Yes', 'No'], value: '' },
    ];
    const result = prefillApplicationFields(fields, {
      profile: { name: 'A. Candidate', email: 'candidate@example.com', location: 'Delhi' },
      resumeVersion: { reviewed: true, extractedPhone: '+91 99999 00000', extractedEmail: 'resume@example.com' },
    });
    expect(result.map(field => field.value)).toEqual(['A. Candidate', 'candidate@example.com', '+91 99999 00000', '']);
    expect(missingApplicationFields(result).map(field => field.key)).toEqual(['sponsor']);
  });

  it('validates selected choices and required values', () => {
    expect(missingApplicationFields([
      { key: 'work', label: 'Work type', type: 'SELECT', required: true, options: ['Remote', 'Hybrid'], value: 'Remote' },
      { key: 'email', label: 'Email', type: 'EMAIL', required: true, value: 'bad' },
      { key: 'start', label: 'Start date', type: 'DATE', required: true, value: '2026-02-31' },
    ])).toHaveLength(2);
  });
});
