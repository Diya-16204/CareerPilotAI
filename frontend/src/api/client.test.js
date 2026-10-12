import { describe, expect, it } from 'vitest';
import axios from 'axios';
import { apiMessage } from './client';

const responseError = data => new axios.AxiosError('Request failed', 'ERR_BAD_RESPONSE', undefined, undefined, {
    status: 500,
    statusText: 'Internal Server Error',
    headers: {},
    config: {},
    data,
});

describe('apiMessage', () => {
    it('shows safe actionable API validation guidance', () => {
        expect(apiMessage(responseError({ message: 'Resume file is unavailable. Re-upload this resume version.' })))
            .toBe('Resume file is unavailable. Re-upload this resume version.');
    });

    it('does not expose internal server errors or diagnostic details', () => {
        expect(apiMessage(responseError({ errorCode: 'INTERNAL_ERROR', message: 'An unexpected error occurred.' })))
            .toContain('check the backend log');
        expect(apiMessage(responseError({ message: 'SQLException: SELECT * FROM users WHERE password=secret' })))
            .toBe('Something went wrong. Please try again.');
    });
});
