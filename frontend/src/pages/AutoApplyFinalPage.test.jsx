import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AutoApplyFinalPage } from './AutoApplyFinalPage.jsx';

const { autoApplySession, submitAutoApplyDemo } = vi.hoisted(() => ({ autoApplySession: vi.fn(), submitAutoApplyDemo: vi.fn() }));
vi.mock('../api/client', () => ({ careerApi: { autoApplySession, submitAutoApplyDemo }, apiMessage: error => error?.message ?? 'Request failed.' }));

const session = { id: 'session-1', jobTitle: 'Software Engineer', company: 'Demo Co', location: 'Remote', resumeFileName: 'resume.pdf', coverLetterDraft: 'Reviewed cover letter', status: 'APPROVED', applicationUrl: 'https://example.com/apply', applicationFields: [
  { key: 'email', label: 'Email address', type: 'EMAIL', required: true, options: [], value: 'candidate@example.com' },
] };
afterEach(() => cleanup());

function renderFinalPage() {
  return render(<MemoryRouter initialEntries={['/app/auto-apply/final/session-1']}><Routes><Route path="/app/auto-apply/final/:sessionId" element={<AutoApplyFinalPage/>}/></Routes></MemoryRouter>);
}

describe('AutoApplyFinalPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    autoApplySession.mockResolvedValue(session);
    submitAutoApplyDemo.mockResolvedValue({ reference: 'submission-123', jobTitle: session.jobTitle, company: session.company, submittedAt: '2026-02-01T00:00:00Z', alreadySubmitted: false });
  });

  it('loads the saved session prefilled and records a demo application with a reference', async () => {
    renderFinalPage();
    expect(await screen.findByDisplayValue('candidate@example.com')).toBeInTheDocument();
    expect(screen.getByText('Reviewed cover letter')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('checkbox'));
    fireEvent.click(screen.getByRole('button', { name: /Apply Now/ }));
    expect(await screen.findByRole('status')).toHaveTextContent('Application completed');
    expect(await screen.findByText('submission-123')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /View application tracker/ })).toHaveAttribute('href', '/app/applications');
    expect(submitAutoApplyDemo).toHaveBeenCalledWith('session-1', session.applicationFields, true);
  });

  it('blocks Apply Now when a saved required field is missing and points back to Auto Apply', async () => {
    autoApplySession.mockResolvedValue({ ...session, applicationFields: [{ ...session.applicationFields[0], value: '' }] });
    renderFinalPage();
    expect(await screen.findByText(/Required information is missing/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Back to Auto Apply/ })).toHaveAttribute('href', '/app/auto-apply');
    expect(screen.getByRole('button', { name: /Apply Now/ })).toBeDisabled();
    expect(submitAutoApplyDemo).not.toHaveBeenCalled();
  });
});
