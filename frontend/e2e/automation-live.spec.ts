import { readFile } from 'node:fs/promises'
import { expect, test } from '@playwright/test'

test('uploads a resume and verifies an automation run through the protected UI and API', async ({ page }) => {
  test.skip(process.env.CAREERPILOT_LIVE_E2E !== '1' || !process.env.CAREERPILOT_E2E_BASE_URL, 'Set CAREERPILOT_LIVE_E2E=1 and CAREERPILOT_E2E_BASE_URL with the Spring API, PostgreSQL and Vite running.')
  test.setTimeout(150_000)

  const email = `automation-ui-${crypto.randomUUID()}@example.test`
  await page.goto('/register')
  await page.getByLabel('Full name').fill('Automation UI Tester')
  await page.getByLabel('Email address').fill(email)
  await page.getByLabel('Password', { exact: true }).fill('AutomationUiTest123!')
  await page.getByRole('button', { name: 'Create account' }).click()
  await expect(page).toHaveURL(/\/app$/)

  await page.getByRole('link', { name: 'Resume studio' }).click()
  await page.getByLabel('Choose a resume').setInputFiles({
    name: 'integration-resume.pdf',
    mimeType: 'application/pdf',
    buffer: await readFile(new URL('./fixtures/integration-resume.pdf', import.meta.url)),
  })
  await page.getByRole('button', { name: 'Upload resume' }).click()
  await expect(page.getByText('Version 1')).toBeVisible({ timeout: 20_000 })

  await page.getByRole('link', { name: 'Automation center' }).click()
  await page.getByLabel('Cover letter').fill('I am interested in this product analyst role and can contribute careful, data-informed research.')
  await page.getByRole('checkbox').check()
  await page.getByRole('button', { name: 'Approve and start simulation' }).click()

  await expect(page.getByText('VERIFIED SUCCESS')).toBeVisible({ timeout: 120_000 })
  await expect(page.getByText(/^[0-9a-f-]{36}$/i)).toBeVisible()
  await expect(page.getByText('Submission persisted and independently verified.')).toBeVisible()
})
