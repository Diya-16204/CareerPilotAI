import { chromium } from '@playwright/test'
import { mkdir, readFile } from 'node:fs/promises'
import path from 'node:path'

let inputText = ''
for await (const chunk of process.stdin) inputText += chunk
const input = JSON.parse(inputText)
let browser
let page
let submissionAttempted = false
let evidenceFile

try {
  const launchOptions = { headless: true }
  if (input.browser === 'msedge') launchOptions.channel = 'msedge'
  browser = await chromium.launch(launchOptions)
  const context = await browser.newContext()
  page = await context.newPage()
  await page.route(`${input.apiBaseUrl}/**`, async (route) => {
    const headers = { ...route.request().headers(), authorization: `Bearer ${input.accessToken}` }
    if (route.request().method() === 'POST' && route.request().url().includes('/submissions')) {
      headers['idempotency-key'] = input.idempotencyKey
    }
    await route.continue({ headers })
  })

  const portalPath = input.portalKey.toLowerCase()
  await page.goto(`${input.frontendUrl}/simulation/${portalPath}`, { waitUntil: 'domcontentloaded' })
  await page.getByRole('button', { name: new RegExp(escapeRegExp(input.jobTitle)) }).click()
  await page.getByLabel('Full name').fill(input.applicantName)
  await page.getByLabel('Email address').fill(input.applicantEmail)

  if (input.portalKey === 'PORTAL_A') {
    await page.getByLabel('Cover letter').fill(input.coverLetter)
  }
  if (input.portalKey === 'PORTAL_B') {
    await page.getByRole('button', { name: 'Review application' }).click()
    await page.getByLabel('Highest completed education').fill(input.education)
    await page.getByLabel('Relevant experience').fill(input.experience)
    await page.getByRole('button', { name: 'Review application' }).click()
  }

  const resumeExtension = path.extname(input.resumeFileName).toLowerCase()
  const resumeMimeType = resumeExtension === '.pdf'
    ? 'application/pdf'
    : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  await page.locator('input[type="file"]').setInputFiles({
    name: input.resumeFileName,
    mimeType: resumeMimeType,
    buffer: await readFile(input.resumeFile),
  })
  for (const [question, answer] of Object.entries(input.answers)) {
    await page.getByLabel(question).fill(answer)
  }
  await page.getByRole('button', { name: 'Review application' }).click()
  await page.getByRole('checkbox').check()

  const submissionResponse = page.waitForResponse((response) =>
    response.request().method() === 'POST' &&
    response.url().includes(`/simulated-portals/${input.portalKey}/submissions`),
  )
  submissionAttempted = true
  await page.getByRole('button', { name: 'Confirm simulated submission' }).click()
  const response = await submissionResponse
  if (!response.ok()) throw new Error(`Submission endpoint returned HTTP ${response.status()}: ${(await response.text()).slice(0, 180)}`)
  const saved = await response.json()

  await page.getByRole('heading', { name: 'Application verified' }).waitFor({ state: 'visible' })
  const verificationResponse = await fetch(`${input.apiBaseUrl}/simulated-portals/submissions/${saved.confirmationId}`, {
    headers: { Authorization: `Bearer ${input.accessToken}` },
  })
  if (!verificationResponse.ok) throw new Error('Independent submission verification failed.')
  const verified = await verificationResponse.json()
  const independentlyVerified = verified.status === 'PERSISTED' &&
    verified.confirmationId === saved.confirmationId &&
    verified.portalKey === input.portalKey && verified.jobKey === input.jobKey
  if (!independentlyVerified) throw new Error('Saved submission does not match the expected portal and job.')

  process.stdout.write(JSON.stringify({
    confirmationId: verified.confirmationId,
    portalKey: verified.portalKey,
    jobKey: verified.jobKey,
    independentlyVerified,
  }) + '\n')
} catch (failure) {
  if (page && input.evidenceDirectory) {
    try {
      await mkdir(input.evidenceDirectory, { recursive: true })
      evidenceFile = path.join(input.evidenceDirectory, `${input.runId}.png`)
      await page.screenshot({ path: evidenceFile, fullPage: true })
    } catch {
      evidenceFile = undefined
    }
  }
    process.stdout.write(JSON.stringify({
    error: 'The simulated portal workflow did not complete verification.',
    diagnostic: failure instanceof Error ? failure.message.slice(0, 300) : 'Unknown browser workflow error.',
    outcomeMayBeUnknown: submissionAttempted,
    evidenceFile,
  }) + '\n')
  process.exitCode = 1
} finally {
  await browser?.close()
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
