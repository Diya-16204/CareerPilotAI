# CareerPilot Autofill browser helper

CareerPilot's Auto Apply page prepares and requires approval of a job-specific draft. The optional Chrome Manifest V3 helper can discover visible form labels/types/options/required flags and fill supported empty text fields. Captured field schemas are returned to the currently selected Auto Apply session; the extension does not collect the values already typed into the employer site.

## What it does

- Transfers only the selected approved session, profile name/email/location/headline, a reviewed resume phone number, the saved cover-letter draft, and confirmed non-sensitive application answers.
- Keeps that data in `chrome.storage.session` for at most 30 minutes. It is not sent to a CareerPilot server by the extension, and you can clear it in the popup.
- Fills only supported empty text/email/phone/textarea fields, using their labels or attributes. Existing values are left unchanged.
- Captures visible text, email, phone, date, number, URL, textarea, select, radio, and checkbox fields. Repeating capture on later wizard screens merges those fields into the same session.
- CareerPilot pre-fills only verified account profile data and reviewed resume contact data where field labels match. Eligibility, sponsorship, protected, and other sensitive answers remain blank for the user.
- Skips password/file/checkbox/radio/search controls, selects, and labels related to sensitive eligibility or protected personal information. It cannot attach the resume PDF; select that file yourself.
- Never clicks Submit. Review every field and submit the form yourself.

The helper is best-effort. Employer forms may use custom widgets, inaccessible labels, or fields that are not supported; complete those manually. No employer page adapter or real-site workflow is claimed.

## Install in Chrome for local development

1. Open `chrome://extensions`.
2. Turn on **Developer mode** and choose **Load unpacked**.
3. Select the repository folder `browser-extension/chrome`.
4. Keep the extension enabled. If it was installed after the CareerPilot tab was opened, reload the CareerPilot tab.
5. In CareerPilot, select the job and resume and prepare a session. Keep that Auto Apply page open.
6. Open the employer application form in another Chrome tab. Click the extension and choose **Capture form fields to Auto Apply**. Repeat on each visible step of a multi-step form. Return to Auto Apply, enter missing answers, and save them.
7. Review the exact application, approve it, and click **Send approved draft to browser autofill**. The extension handoff is required before the **Final Apply** review link appears.
8. Open the employer application form and click **Fill supported empty fields**. Review every populated value, attach the resume manually, and answer skipped fields.
9. Final Apply can record a submission only to CareerPilot's fictional demo portal. For a real employer, open its application page and click its submit control yourself; CareerPilot never claims that opening/filling a page submitted it.

### Allow the deployed CareerPilot site

Chrome may install the extension without granting it access to the hosted app. Open `chrome://extensions`, select **Details** for CareerPilot Autofill, open **Site access**, and enable `https://career-pilot-ai-blond-seven.vercel.app/*`. Then reload the CareerPilot tab and retry the handoff. The extension manifest allowlists this exact CareerPilot origin; it does not need broad access to all sites. Enabling the site permission lets the extension communicate with CareerPilot, while access to an employer tab is separately granted only when you open the extension popup and choose an action.

The extension bridge is restricted to `localhost:5173`, `127.0.0.1:5173`, and the current CareerPilot Vercel hostname. For a different trusted CareerPilot hostname, add that exact origin in `manifest.json` and `careerpilot-bridge.js`, reload the unpacked extension, and reload the app. The extension does not need broad access to employer domains; `activeTab` grants access only after you open its popup and click the fill action.

## Limitations

- This is a local unpacked extension; it is not published in the Chrome Web Store.
- It does not work in the hosted UI until the CareerPilot frontend containing the transfer button is deployed.
- It does not bypass CAPTCHA, authentication, MFA, anti-bot controls, or employer site terms.
- Browser-level behavior is not yet verified against real employer forms. Unit tests exercise field selection and sensitive-field skipping; the user must validate employer-specific pages manually.
- Capture reads only currently visible controls. Hidden or dynamically rendered steps must be opened before capture; file upload controls remain manual.
