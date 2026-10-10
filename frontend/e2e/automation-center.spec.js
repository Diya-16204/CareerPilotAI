import { expect, test } from '@playwright/test';
test('automation center requires approval and shows a persisted verification result', async ({ page }) => {
    const resumeId = 'resume-test-id';
    const versionId = 'resume-version-test-id';
    const runId = 'automation-run-test-id';
    const confirmationId = 'persisted-confirmation-test-id';
    const createdAt = new Date().toISOString();
    let submitted;
    let accepted = false;
    await page.route('http://localhost:8080/api/v1/**', async (route) => {
        const request = route.request();
        const url = new URL(request.url());
        if (request.method() === 'POST' && url.pathname.endsWith('/auth/register')) {
            await route.fulfill({ status: 201, json: { accessToken: 'test-token', tokenType: 'Bearer', expiresIn: 900, user: { id: 'user-id', name: 'Alex Example', email: 'alex@example.test' } } });
        }
        else if (request.method() === 'GET' && url.pathname.endsWith('/resumes')) {
            await route.fulfill({ json: [{ id: resumeId, title: 'Product resume', activeVersionNumber: 1, createdAt, updatedAt: createdAt, versionCount: 1, latestFileName: 'resume.pdf' }] });
        }
        else if (request.method() === 'GET' && url.pathname.endsWith(`/resumes/${resumeId}`)) {
            await route.fulfill({ json: { id: resumeId, title: 'Product resume', activeVersionNumber: 1, createdAt, updatedAt: createdAt, versions: [{ id: versionId, versionNumber: 1, originalFileName: 'resume.pdf', contentType: 'application/pdf', extractedEmail: 'alex@example.test', extractedPhone: null, skills: ['SQL'], reviewed: true, createdAt, extractedText: 'Product analytics experience' }] } });
        }
        else if (request.method() === 'GET' && url.pathname.endsWith('/automation-runs')) {
            const items = submitted ? [run(accepted ? 'VERIFIED_SUCCESS' : 'PENDING')] : [];
            await route.fulfill({ json: { items, page: 0, size: 50, totalItems: items.length, totalPages: items.length ? 1 : 0 } });
        }
        else if (request.method() === 'GET' && url.pathname.endsWith('/simulated-portals/portal_a/jobs')) {
            await route.fulfill({ json: [{ key: 'product-analyst', title: 'Product Analyst', department: 'Product', location: 'Remote', description: 'Improve decisions with research.', requirements: ['SQL'], questions: [] }] });
        }
        else if (request.method() === 'POST' && url.pathname.endsWith('/automation-runs')) {
            submitted = request.postDataJSON();
            expect(request.headers()['idempotency-key']).toBeTruthy();
            await route.fulfill({ status: 202, json: run('PENDING') });
            setTimeout(() => { accepted = true; }, 500);
        }
        else if (request.method() === 'GET' && url.pathname.endsWith(`/automation-runs/${runId}`)) {
            accepted = true;
            await route.fulfill({ json: run('VERIFIED_SUCCESS') });
        }
        else {
            await route.fulfill({ status: 404, json: { message: `Unmocked route: ${url.pathname}` } });
        }
    });
    function run(status) {
        return { id: runId, portalKey: 'PORTAL_A', jobKey: 'product-analyst', status, confirmationId: status === 'VERIFIED_SUCCESS' ? confirmationId : null, message: status === 'VERIFIED_SUCCESS' ? 'Submission persisted and independently verified.' : null, createdAt, startedAt: status === 'PENDING' ? null : createdAt, completedAt: status === 'VERIFIED_SUCCESS' ? createdAt : null };
    }
    await page.goto('/register');
    await page.getByLabel('Full name').fill('Alex Example');
    await page.getByLabel('Email address').fill('alex@example.test');
    await page.getByLabel('Password', { exact: true }).fill('AutomationMock123!');
    await page.getByRole('button', { name: 'Create account' }).click();
    await page.getByRole('link', { name: 'Automation center' }).click();
    await expect(page.getByRole('heading', { name: 'Automation center' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Approve and start simulation' })).toBeDisabled();
    await page.getByLabel('Cover letter').fill('I would bring careful research and clear communication to this role.');
    await page.getByRole('checkbox').check();
    await page.getByRole('button', { name: 'Approve and start simulation' }).click();
    await expect(page.getByText('VERIFIED SUCCESS')).toBeVisible({ timeout: 8000 });
    await expect(page.getByText(confirmationId)).toBeVisible();
    expect(submitted?.consent).toBe(true);
    expect(submitted?.resumeVersionId).toBe(versionId);
    expect(submitted?.coverLetter).toBe('I would bring careful research and clear communication to this role.');
});
