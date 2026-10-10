import { expect, test } from '@playwright/test';
test('registers in the API and verifies a persisted portal application through the UI', async ({ page }) => {
    test.skip(process.env.CAREERPILOT_LIVE_E2E !== '1', 'Set CAREERPILOT_LIVE_E2E=1 with the Spring API and PostgreSQL running.');
    const email = `live-${crypto.randomUUID()}@example.test`;
    await page.goto('/register');
    await page.getByLabel('Full name').fill('Live Integration User');
    await page.getByLabel('Email address').fill(email);
    await page.getByLabel('Password', { exact: true }).fill('LiveIntegration123!');
    await page.getByRole('button', { name: 'Create account' }).click();
    await expect(page).toHaveURL(/\/app$/);
    await page.getByRole('link', { name: 'Standard application' }).click();
    await page.getByRole('button', { name: /Product Analyst/ }).click();
    await page.getByLabel('Full name').fill('Live Integration User');
    await page.getByLabel('Email address').fill(email);
    await page.getByLabel('Cover letter').fill('I am interested in this simulated product analyst role.');
    await page.locator('input[type="file"]').setInputFiles({
        name: 'integration-resume.pdf',
        mimeType: 'application/pdf',
        buffer: Buffer.from('%PDF-1.7 live integration fixture'),
    });
    await page.getByRole('button', { name: 'Review application' }).click();
    await page.getByRole('checkbox').check();
    await page.getByRole('button', { name: 'Confirm simulated submission' }).click();
    await expect(page.getByRole('heading', { name: 'Application verified' })).toBeVisible();
    await expect(page.getByText(/^[0-9a-f-]{36}$/i)).toBeVisible();
});
