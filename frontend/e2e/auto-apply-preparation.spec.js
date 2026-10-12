import { expect, test } from '@playwright/test';

test('prepares, explicitly approves, and hands a reviewed draft to the manual flow', async ({ page }) => {
    const jobId = 'job-test-id';
    const resumeId = 'resume-test-id';
    const versionId = 'resume-version-test-id';
    const sessionId = 'prep-session-test-id';
    const now = new Date().toISOString();
    let currentStatus = 'READY_FOR_REVIEW';
    let savedFields = [];
    let createPayload;
    await page.route('http://localhost:8080/api/v1/**', async route => {
        const request = route.request();
        const url = new URL(request.url());
        if (request.method() === 'POST' && url.pathname.endsWith('/auth/register')) {
            await route.fulfill({ status: 201, json: { accessToken: 'mock-token', tokenType: 'Bearer', expiresIn: 900, user: { id: 'user-id', name: 'Alex Example', email: 'alex@example.test' } } });
        } else if (request.method() === 'GET' && url.pathname.endsWith('/jobs')) {
            await route.fulfill({ json: { items: [{ id: jobId, title: 'Product Analyst', company: 'Example Co', location: 'Remote', source: 'user_import', sample: false, description: 'Analyze product usage with SQL.' }] } });
        } else if (request.method() === 'GET' && url.pathname.endsWith('/resumes')) {
            await route.fulfill({ json: [{ id: resumeId, title: 'Reviewed resume', activeVersionNumber: 1 }] });
        } else if (request.method() === 'GET' && url.pathname.endsWith(`/resumes/${resumeId}`)) {
            await route.fulfill({ json: { id: resumeId, title: 'Reviewed resume', activeVersionNumber: 1, versions: [{ id: versionId, versionNumber: 1, originalFileName: 'resume.pdf', reviewed: true }] } });
        } else if (request.method() === 'GET' && url.pathname.endsWith('/users/me/profile')) {
            await route.fulfill({ json: { id: 'user-id', name: 'Alex Example', email: 'alex@example.test', headline: '', location: 'Remote' } });
        } else if (request.method() === 'GET' && url.pathname.endsWith('/auto-apply/sessions')) {
            await route.fulfill({ json: [] });
        } else if (request.method() === 'POST' && url.pathname.endsWith('/auto-apply/sessions')) {
            createPayload = request.postDataJSON();
            await route.fulfill({ status: 201, json: session(currentStatus) });
        } else if (request.method() === 'PUT' && url.pathname.endsWith(`/auto-apply/sessions/${sessionId}/fields`)) {
            savedFields = request.postDataJSON();
            await route.fulfill({ json: session(currentStatus, false, undefined, savedFields) });
        } else if (request.method() === 'POST' && url.pathname.endsWith(`/auto-apply/sessions/${sessionId}/approve`)) {
            expect(request.postDataJSON().consent).toBe(true);
            currentStatus = 'APPROVED';
            await route.fulfill({ json: session(currentStatus, true) });
        } else if (request.method() === 'POST' && url.pathname.endsWith(`/auto-apply/sessions/${sessionId}/continue-manually`)) {
            currentStatus = 'REQUIRES_MANUAL_ACTION';
            await route.fulfill({ json: session(currentStatus, true, 'No supported employer adapter is configured. Apply manually.') });
        } else {
            await route.fulfill({ status: 404, json: { message: `Unmocked route: ${url.pathname}` } });
        }
    });

    function session(status, approved = false, resultMessage = 'Drafts are ready for your review.', applicationFields = savedFields) {
        return { id: sessionId, jobId, jobTitle: 'Product Analyst', company: 'Example Co', location: 'Remote',
            resumeVersionId: versionId, resumeFileName: 'resume.pdf', coverLetterId: 'letter-id',
            applicationUrl: null, status, coverLetterDraft: 'I would bring SQL experience to Example Co.',
            coverLetterSource: 'LOCAL_TEMPLATE', answers: [], missingInformation: [], applicationFields, approved,
            approvedAt: approved ? now : null, resultMessage, createdAt: now, updatedAt: now };
    }

    await page.goto('/register');
    await page.getByLabel('Full name').fill('Alex Example');
    await page.getByLabel('Email address').fill('alex@example.test');
    await page.getByLabel('Password', { exact: true }).fill('AutoApplyMock123!');
    await page.getByRole('button', { name: 'Create account' }).click();
    await page.getByRole('link', { name: 'Auto Apply' }).click();
    await page.getByRole('button', { name: 'Prepare application drafts' }).click();
    await expect(page.getByText('READY FOR REVIEW').first()).toBeVisible();
    await page.evaluate(sessionId => window.dispatchEvent(new CustomEvent('careerpilot:captured-form-fields', { detail: {
        sessionId, fields: [{ key: 'work_mode', label: 'Work arrangement', type: 'SELECT', required: true, options: ['Remote', 'Hybrid'], value: '' }],
    } })), sessionId);
    await page.getByLabel(/Work arrangement/).selectOption('Remote');
    await page.getByRole('button', { name: 'Save application answers' }).click();
    await expect.poll(() => savedFields.some(field => field.key === 'work_mode' && field.value === 'Remote')).toBe(true);
    await page.getByRole('checkbox', { name: /I reviewed the job/ }).check();
    await page.getByRole('button', { name: 'Approve this exact draft' }).click();
    await expect(page.getByText('APPROVED').first()).toBeVisible();
    await page.getByRole('button', { name: 'Continue to manual application' }).click();
    await expect(page.getByText(/No supported employer adapter/).first()).toBeVisible();
    expect(createPayload).toMatchObject({ jobId, resumeVersionId: versionId });
    expect(savedFields).toContainEqual(expect.objectContaining({ key: 'work_mode', value: 'Remote', required: true }));
});
