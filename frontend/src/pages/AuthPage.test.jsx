import { describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { AuthPage } from './AuthPage';
describe('AuthPage', () => {
    it('shows accessible field validation before making a request', async () => {
        render(<MemoryRouter><AuthProvider><AuthPage mode="register"/></AuthProvider></MemoryRouter>);
        fireEvent.click(screen.getByRole('button', { name: 'Create account' }));
        expect(await screen.findByText('Enter at least 2 characters.')).toBeInTheDocument();
        expect(screen.getByText('Enter a valid email address.')).toBeInTheDocument();
        expect(screen.getByText('Use at least 10 characters.')).toBeInTheDocument();
    });
});
