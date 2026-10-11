import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { ReferralPage } from './ReferralPage';

const { savedJobs, resumes } = vi.hoisted(() => ({ savedJobs: vi.fn(), resumes: vi.fn() }));
vi.mock('../api/client', () => ({
    careerApi: { savedJobs, resumes },
    apiMessage: error => error?.message ?? 'Service unavailable.',
}));

const savedJob = { id: 'saved-job-1', title: 'Junior Java Engineer', company: 'Northstar Labs', location: 'Remote', saved: true };

function renderPage() {
    return render(<MemoryRouter><AuthProvider><ReferralPage /></AuthProvider></MemoryRouter>);
}

describe('ReferralPage saved jobs', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        resumes.mockResolvedValue([]);
    });

    it('loads jobs from the dedicated saved-jobs endpoint', async () => {
        savedJobs.mockResolvedValue({ items: [savedJob] });
        renderPage();

        expect(await screen.findByRole('option', { name: 'Junior Java Engineer · Northstar Labs' })).toBeInTheDocument();
        expect(savedJobs).toHaveBeenCalledWith({ page: 0, size: 100 });
    });

    it('shows a retryable load error instead of claiming there are no saved jobs', async () => {
        savedJobs.mockRejectedValueOnce(new Error('The API is waking up.')).mockResolvedValueOnce({ items: [savedJob] });
        renderPage();

        expect(await screen.findByRole('alert')).toHaveTextContent('The API is waking up.');
        expect(screen.queryByRole('option', { name: 'No saved jobs yet' })).not.toBeInTheDocument();
        fireEvent.click(screen.getByRole('button', { name: 'Retry loading' }));
        expect(await screen.findByRole('option', { name: 'Junior Java Engineer · Northstar Labs' })).toBeInTheDocument();
        await waitFor(() => expect(screen.queryByRole('button', { name: 'Retry loading' })).not.toBeInTheDocument());
    });
});
