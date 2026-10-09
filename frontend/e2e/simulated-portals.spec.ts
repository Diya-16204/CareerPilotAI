import { expect, test } from '@playwright/test'

const portals = [
  { key: 'PORTAL_A', route: 'portal_a', title: 'Product Analyst', questions: ['Why are you interested in product analysis?'], wizard: false },
  { key: 'PORTAL_B', route: 'portal_b', title: 'Software Engineer', questions: ['What is your highest completed education?', 'Describe a project you are proud of.'], wizard: true },
  { key: 'PORTAL_C', route: 'portal_c', title: 'UX Researcher', questions: ['How would you recruit participants for a new feature?', 'What accessibility checks do you include in research?'], wizard: false },
]

for (const portal of portals) {
  test(`${portal.key} previews and verifies a simulated application`, async ({ page }) => {
    const confirmationId = `confirmation-${portal.key.toLowerCase()}`
    let submitted: Record<string, unknown> | undefined
    await page.route('http://localhost:8080/api/v1/simulated-portals/**', async (route) => {
      const url = new URL(route.request().url())
      if (route.request().method() === 'GET' && url.pathname.endsWith('/jobs')) {
        const questions = portal.questions
        await route.fulfill({ json: [{ key: `job-${portal.key.toLowerCase()}`, title: portal.title, department: 'Simulation', location: 'Remote', description: 'A fictional test role.', requirements: ['Clear communication'], questions }] })
      } else if (route.request().method() === 'POST' && url.pathname.endsWith('/submissions')) {
        submitted = route.request().postDataJSON() as Record<string, unknown>
        expect(route.request().headers()['idempotency-key']).toBeTruthy()
        await route.fulfill({ status: 201, json: { confirmationId, portalKey: portal.key, jobKey: `job-${portal.key.toLowerCase()}`, jobTitle: portal.title, createdAt: new Date().toISOString(), reused: false } })
      } else if (url.pathname.endsWith(`/submissions/${confirmationId}`)) {
        await route.fulfill({ json: { confirmationId, portalKey: portal.key, jobKey: `job-${portal.key.toLowerCase()}`, jobTitle: portal.title, createdAt: new Date().toISOString(), status: 'PERSISTED' } })
      } else {
        await route.fulfill({ status: 404, json: { message: 'Unmocked simulation route' } })
      }
    })

    await page.goto(`/simulation/${portal.route}`)
    await page.getByRole('button', { name: new RegExp(portal.title) }).click()
    await page.getByLabel('Full name').fill('Alex Example')
    await page.getByLabel('Email address').fill('alex@example.test')
    if (portal.wizard) {
      await page.getByRole('button', { name: 'Review application' }).click()
      await page.getByLabel('Highest completed education').fill('Bachelor degree')
      await page.getByLabel('Relevant experience').fill('Built and tested a portfolio application.')
      await page.getByRole('button', { name: 'Review application' }).click()
    }
    await page.locator('input[type="file"]').setInputFiles({ name: 'resume.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.7 test resume') })
    for (const question of portal.questions) await page.getByLabel(question).fill('This is an approved test answer.')
    await page.getByRole('button', { name: 'Review application' }).click()
    await expect(page.getByText('Review your application')).toBeVisible()
    await expect(page.getByText('This is an approved test answer.').first()).toBeVisible()
    await page.getByRole('checkbox').check()
    await page.getByRole('button', { name: 'Confirm simulated submission' }).click()
    await expect(page.getByRole('heading', { name: 'Application verified' })).toBeVisible()
    await expect(page.getByText(confirmationId)).toBeVisible()
    expect(submitted?.consent).toBe(true)
    expect(submitted?.jobKey).toBe(`job-${portal.key.toLowerCase()}`)
    expect(Object.keys(submitted?.answers as object)).toHaveLength(portal.questions.length)
  })
}
