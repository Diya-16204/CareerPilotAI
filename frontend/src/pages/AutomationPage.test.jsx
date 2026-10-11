import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AutomationPage } from './AutomationPage';

const { resumes, resume, automationRuns, portalJobs } = vi.hoisted(() => ({
    resumes: vi.fn(),
    resume: vi.fn(),
    automationRuns: vi.fn(),
    portalJobs: vi.fn(),
}));

vi.mock('../api/client', () => ({
    careerApi: { resumes, resume, automationRuns, portalJobs },
    apiMessage: error => error?.message ?? 'Service unavailable.',
}));

describe('AutomationPage resume loading', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        resumes.mockResolvedValue([{ id: 'resume-1', title: 'Shambhavi resume' }]);
        resume.mockResolvedValue({ id: 'resume-1', versions: [{ id: 'version-1', versionNumber: 1, originalFileName: 'resume.pdf' }] });
        automationRuns.mockResolvedValue({ items: [] });
        portalJobs.mockResolvedValue([]);
    });

    it('shows available resumes even when automation history fails', async () => {
        automationRuns.mockRejectedValue(new Error('History service unavailable.'));
        render(<AutomationPage />);

        expect(await screen.findByRole('option', { name: 'Shambhavi resume' })).toBeInTheDocument();
        expect(await screen.findByRole('alert')).toHaveTextContent('Resume list loaded');
        expect(screen.queryByText(/Upload and review .* before starting a run/)).not.toBeInTheDocument();
    });
});
