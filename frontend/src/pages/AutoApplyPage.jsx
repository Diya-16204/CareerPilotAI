import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, ArrowUpRight, Check, CircleAlert, FileText, Plus, Save, Sparkles, Trash2 } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { apiMessage, careerApi } from '../api/client';
import { createReferralMessages } from '../referrals/referralMessages';
import { useAuth } from '../auth/AuthContext';
import { ApplicationFieldsEditor, missingApplicationFields, prefillApplicationFields } from './autoApplyFields.jsx';

const statusTone = {
    PREPARING: 'text-cyan', NEEDS_INFORMATION: 'text-amber', READY_FOR_REVIEW: 'text-violet', APPROVED: 'text-mint',
    REQUIRES_MANUAL_ACTION: 'text-amber', FAILED: 'text-rose-200', BLOCKED: 'text-rose-200', SUBMITTED: 'text-mint',
};

export function AutoApplyPage() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [jobs, setJobs] = useState([]);
    const [resumes, setResumes] = useState([]);
    const [resume, setResume] = useState(null);
    const [sessions, setSessions] = useState([]);
    const [selected, setSelected] = useState(null);
    const [jobId, setJobId] = useState('');
    const [resumeId, setResumeId] = useState('');
    const [versionId, setVersionId] = useState('');
    const [applicationUrl, setApplicationUrl] = useState('');
    const [tone, setTone] = useState('professional');
    const [questions, setQuestions] = useState([]);
    const [newQuestion, setNewQuestion] = useState('');
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    const [notice, setNotice] = useState('');
    const [draft, setDraft] = useState('');
    const [answerInputs, setAnswerInputs] = useState({});
    const [remember, setRemember] = useState({});
    const [consent, setConsent] = useState(false);
    const [recipient, setRecipient] = useState('');
    const [fit, setFit] = useState('');
    const [referralMessages, setReferralMessages] = useState(null);
    const [autofillBusy, setAutofillBusy] = useState(false);
    const [applicationFields, setApplicationFields] = useState([]);
    const [fieldsDirty, setFieldsDirty] = useState(false);
    const [fieldsInvalid, setFieldsInvalid] = useState(false);
    const [autofillShared, setAutofillShared] = useState(false);

    const job = useMemo(() => jobs.find(item => item.id === jobId), [jobs, jobId]);

    useEffect(() => {
        let active = true;
        Promise.allSettled([careerApi.jobs({ page: 0, size: 100 }), careerApi.resumes(), careerApi.autoApplySessions()])
            .then(([jobResult, resumeResult, sessionResult]) => {
                if (!active) return;
                if (jobResult.status === 'fulfilled') {
                    const items = jobResult.value.items ?? [];
                    setJobs(items);
                    setJobId(items[0]?.id ?? '');
                } else setError(apiMessage(jobResult.reason));
                if (resumeResult.status === 'fulfilled') {
                    setResumes(resumeResult.value);
                    setResumeId(resumeResult.value[0]?.id ?? '');
                } else setError(apiMessage(resumeResult.reason));
                if (sessionResult.status === 'fulfilled') setSessions(sessionResult.value);
                else if (resumeResult.status === 'fulfilled') setError(`Your jobs and resumes loaded, but preparation history did not: ${apiMessage(sessionResult.reason)}`);
            })
            .finally(() => { if (active) setLoading(false); });
        return () => { active = false; };
    }, []);

    useEffect(() => {
        let active = true;
        if (!resumeId) { setResume(null); setVersionId(''); return; }
        careerApi.resume(resumeId).then(detail => {
            if (!active) return;
            setResume(detail);
            setVersionId(detail.versions.find(item => item.versionNumber === detail.activeVersionNumber)?.id ?? detail.versions[0]?.id ?? '');
        }).catch(reason => { if (active) setError(apiMessage(reason)); });
        return () => { active = false; };
    }, [resumeId]);

    useEffect(() => {
        setDraft(selected?.coverLetterDraft ?? '');
        setAnswerInputs(Object.fromEntries((selected?.answers ?? []).map(item => [item.key, item.confirmed ? item.confirmedAnswer : item.draftAnswer || item.reusableAnswer || ''])));
        setConsent(false);
        setReferralMessages(null);
    }, [selected?.id, selected?.updatedAt]);

    useEffect(() => {
        setApplicationFields(selected?.applicationFields ?? []);
        setFieldsDirty(false);
        setFieldsInvalid(false);
        setAutofillShared(Boolean(selected?.id && window.localStorage.getItem(`careerpilot-autofill-shared:${selected.id}`) === 'true'));
    }, [selected?.id, selected?.updatedAt]);

    useEffect(() => {
        if (!selected?.id) return;
        const requestId = crypto.randomUUID();
        window.postMessage({ source: 'careerpilot-app', type: 'REGISTER_ACTIVE_SESSION', sessionId: selected.id, requestId }, window.location.origin);
        const onCaptured = async event => {
            const detail = event.detail;
            if (detail?.sessionId !== selected.id || !Array.isArray(detail.fields)) return;
            setBusy(true); setError(''); setNotice('');
            try {
                const profile = await careerApi.profile();
                let selectedVersion = resume?.versions?.find(item => item.id === selected.resumeVersionId) ?? null;
                if (!selectedVersion) {
                    for (const resumeSummary of resumes) {
                        const resumeDetail = await careerApi.resume(resumeSummary.id).catch(() => null);
                        selectedVersion = resumeDetail?.versions?.find(item => item.id === selected.resumeVersionId) ?? null;
                        if (selectedVersion) break;
                    }
                }
                const captured = prefillApplicationFields(detail.fields, { profile, resumeVersion: selectedVersion, coverLetter: selected.coverLetterDraft ?? '', job: { title: selected.jobTitle, company: selected.company }, answers: selected.answers ?? [] });
                const capturedKeys = new Set(captured.map(field => field.key));
                const existingFields = selected.applicationFields ?? [];
                const starterKeys = new Set(['applicant_name', 'applicant_email', 'applicant_phone', 'applicant_location', 'cover_letter']);
                const firstEmployerCapture = captured.length > 0 && !existingFields.some(field => !starterKeys.has(field.key));
                const fields = [
                    ...existingFields.filter(field => !capturedKeys.has(field.key) && !(firstEmployerCapture && starterKeys.has(field.key))),
                    ...captured.map(field => {
                        const previous = existingFields.find(item => item.key === field.key && item.label === field.label);
                        return previous ? { ...field, value: previous.value ?? field.value } : field;
                    }),
                ];
                sync(await careerApi.saveAutoApplyFields(selected.id, fields));
                setNotice(`Added ${captured.length} fields from this employer form step (${fields.length} total). You can capture again on later form steps; review and complete all required answers.`);
            } catch (reason) { setError(apiMessage(reason)); }
            finally { setBusy(false); }
        };
        window.addEventListener('careerpilot:captured-form-fields', onCaptured);
        return () => window.removeEventListener('careerpilot:captured-form-fields', onCaptured);
    }, [selected?.id]);

    function addQuestion() {
        const label = newQuestion.trim();
        if (!label) return;
        if (questions.length >= 20) { setError('Add no more than 20 questions per application.'); return; }
        const key = `question-${crypto.randomUUID()}`;
        setQuestions(items => [...items, { key, label, required: true, consequential: false }]);
        setNewQuestion('');
        setError('');
    }

    async function prepare(event) {
        event.preventDefault();
        if (!jobId || !versionId) { setError('Choose a job and a reviewed resume version first.'); return; }
        setBusy(true); setError(''); setNotice(''); setSelected(null);
        try {
            const session = await careerApi.createAutoApplySession({
                jobId, resumeVersionId: versionId, applicationUrl: applicationUrl.trim() || null, tone,
                questions: questions.map(({ key, label, required, consequential }) => ({ key, label, required, consequential })),
            });
            const profile = await careerApi.profile().catch(() => ({ name: user?.name ?? '', email: user?.email ?? '' }));
            const selectedVersion = resume?.versions?.find(item => item.id === versionId);
            const starterFields = [
                { key: 'applicant_name', label: 'Applicant full name', type: 'TEXT', required: true, options: [], value: profile.name ?? user?.name ?? '' },
                { key: 'applicant_email', label: 'Email address', type: 'EMAIL', required: true, options: [], value: profile.email ?? user?.email ?? '' },
                { key: 'applicant_phone', label: 'Phone number', type: 'PHONE', required: false, options: [], value: selectedVersion?.reviewed ? selectedVersion.extractedPhone ?? '' : '' },
                { key: 'applicant_location', label: 'Location', type: 'TEXT', required: false, options: [], value: profile.location ?? '' },
                { key: 'cover_letter', label: 'Cover letter', type: 'TEXTAREA', required: false, options: [], value: session.coverLetterDraft ?? '' },
            ];
            const saved = await careerApi.saveAutoApplyFields(session.id, starterFields);
            setSessions(items => [saved, ...items.filter(item => item.id !== saved.id)]);
            setSelected(saved);
            setNotice('Preparation session created. Applicant details are prefilled from your account/reviewed resume; capture the employer form to load its actual required fields.');
        } catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    async function openSession(id) {
        setBusy(true); setError(''); setNotice('');
        try { setSelected(await careerApi.autoApplySession(id)); }
        catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    async function retryPreparation() {
        if (!selected) return;
        setBusy(true); setError(''); setNotice('');
        try {
            const questions = (selected.answers ?? []).map(({ key, label, required, consequential }) => ({ key, label, required, consequential }));
            sync(await careerApi.retryAutoApplyPreparation(selected.id, tone, questions));
        } catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    function sync(session) {
        setSelected(session);
        setSessions(items => [session, ...items.filter(item => item.id !== session.id)]);
        setNotice(session.resultMessage ?? 'Session updated.');
        setConsent(false);
        setApplicationFields(session.applicationFields ?? []);
        setFieldsDirty(false);
    }

    async function saveApplicationFields() {
        if (!selected) return;
        const missing = missingApplicationFields(applicationFields);
        setFieldsInvalid(true);
        if (missing.length) { setError(`Complete required fields: ${missing.map(field => field.label).join(', ')}`); return; }
        setBusy(true); setError(''); setNotice('');
        try { sync(await careerApi.saveAutoApplyFields(selected.id, applicationFields)); setNotice('Application answers saved to your account.'); }
        catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    async function saveLetter() {
        if (!selected || !draft.trim()) return;
        setBusy(true); setError('');
        try { sync(await careerApi.updateAutoApplyDraft(selected.id, draft)); }
        catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    async function updateAnswer(question, confirm) {
        setBusy(true); setError('');
        try {
            const session = await careerApi.updateAutoApplyAnswer(selected.id, question.key, {
                answer: answerInputs[question.key] ?? '', confirm,
                rememberForSimilarQuestions: Boolean(remember[question.key]),
            });
            sync(session);
        } catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    async function approve() {
        setBusy(true); setError('');
        try { sync(await careerApi.approveAutoApplySession(selected.id, consent)); }
        catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    async function continueManually() {
        setBusy(true); setError('');
        try { sync(await careerApi.continueAutoApplyManually(selected.id)); }
        catch (reason) { setError(apiMessage(reason)); }
        finally { setBusy(false); }
    }

    async function shareApprovedDraft() {
        if (!selected || selected.status !== 'APPROVED') return;
        setAutofillBusy(true); setError(''); setNotice('');
        try {
            const profile = await careerApi.profile();
            const selectedVersion = resume?.versions?.find(item => item.id === selected.resumeVersionId);
            const sensitive = /\b(work authorization|authorized to work|visa|sponsor(ship)?|ethnicity|race|gender|disability|criminal|conviction|citizenship|veteran|salary history|date of birth|medical|religion|marital|ssn|social security)\b/i;
            const payload = {
                status: selected.status,
                sessionId: selected.id,
                createdAt: Date.now(),
                job: { title: selected.jobTitle, company: selected.company, url: selected.applicationUrl ?? '' },
                profile: { name: profile.name ?? '', email: profile.email ?? '', location: profile.location ?? '', headline: profile.headline ?? '' , phone: selectedVersion?.reviewed ? selectedVersion.extractedPhone ?? '' : '' },
                coverLetter: selected.coverLetterDraft ?? '',
                answers: (selected.answers ?? []).filter(answer => answer.confirmed && !answer.consequential && !sensitive.test(answer.label))
                    .map(answer => ({ label: answer.label, value: answer.confirmedAnswer })),
            };
            const requestId = crypto.randomUUID();
            const response = await new Promise((resolve, reject) => {
                const timer = window.setTimeout(() => { window.removeEventListener('message', onMessage); reject(new Error('Browser helper did not respond.')); }, 1800);
                function onMessage(event) {
                    if (event.source !== window || event.origin !== window.location.origin || event.data?.source !== 'careerpilot-autofill-extension' || event.data?.requestId !== requestId) return;
                    window.clearTimeout(timer); window.removeEventListener('message', onMessage); resolve(event.data);
                }
                window.addEventListener('message', onMessage);
                window.postMessage({ source: 'careerpilot-app', type: 'SAVE_APPROVED_DRAFT', requestId, payload }, window.location.origin);
            });
            if (!response.ok) throw new Error(response.message || 'Could not share the approved draft.');
            window.localStorage.setItem(`careerpilot-autofill-shared:${selected.id}`, 'true');
            setAutofillShared(true);
            setNotice('Approved draft sent to the CareerPilot Autofill extension for this browser session. Open the extension on the employer application tab to fill supported fields.');
        } catch (reason) {
            setError(reason.message === 'Browser helper did not respond.'
                ? 'Install and enable the CareerPilot Autofill Chrome extension first, then try again.'
                : reason.message || 'Could not share the approved draft.');
        } finally { setAutofillBusy(false); }
    }

    function makeReferralDrafts() {
        if (!selected || !recipient.trim()) { setError('Enter the recipient’s name to create a referral draft.'); return; }
        setReferralMessages(createReferralMessages({ recipient: recipient.trim(), role: selected.jobTitle, company: selected.company,
            jobId: selected.jobId, jobUrl: selected.applicationUrl ?? '', relationship: 'new', fit, sender: user?.name ?? '' }));
        setError('');
    }

    async function copy(text) {
        try { await navigator.clipboard.writeText(text); setNotice('Referral draft copied. Review it before sending.'); }
        catch { setError('Copy was blocked by the browser. Select and copy the draft manually.'); }
    }

    return <main>
        <div className="eyebrow"><Sparkles size={14}/> Application preparation agent</div>
        <div className="mt-3 flex flex-wrap items-end justify-between gap-4">
            <div><h1 className="font-display text-3xl font-bold tracking-[-.055em]">Auto Apply</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-muted">Prepare job-specific drafts, resolve questions, and review the exact application before approval. Unsupported employer submissions stop for manual action.</p></div>
            <span className="rounded-full border border-amber/20 bg-amber/[.05] px-3 py-1.5 text-[10px] font-semibold uppercase tracking-wider text-amber">Review required</span>
        </div>
        <div className="mt-5 flex gap-3 rounded-2xl border border-amber/20 bg-amber/[.04] p-4 text-xs leading-5 text-[#d7cba8]"><AlertTriangle size={16} className="mt-0.5 shrink-0"/><p>AI output is a draft, never a confirmed fact. Sensitive and eligibility answers are never inferred. No real employer is contacted by this workflow; use the manual link only after reviewing each field.</p></div>
        {error && <div role="alert" className="mt-4 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs leading-5 text-rose-200">{error}</div>}
        {notice && <div role="status" className="mt-4 rounded-xl border border-mint/20 bg-mint/[.06] px-4 py-3 text-xs leading-5 text-mint">{notice}</div>}

        <div className="mt-6 grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_minmax(320px,.8fr)]">
            <form onSubmit={prepare} className="space-y-5 rounded-2xl border border-line bg-panel p-5 sm:p-6">
                <div><h2 className="text-sm font-semibold">Prepare one job</h2><p className="mt-1 text-[10px] leading-5 text-muted">Select a role and reviewed resume version. Add only questions copied from that application form.</p></div>
                <Field label="Job"><select className="field-input" value={jobId} onChange={event => setJobId(event.target.value)} disabled={loading || !jobs.length}><option value="">{loading ? 'Loading jobs…' : jobs.length ? 'Choose a job' : 'No jobs available'}</option>{jobs.map(item => <option key={item.id} value={item.id}>{item.title} · {item.company}</option>)}</select></Field>
                <div className="grid gap-4 sm:grid-cols-2"><Field label="Resume"><select className="field-input" value={resumeId} onChange={event => setResumeId(event.target.value)} disabled={loading || !resumes.length}><option value="">{loading ? 'Loading resumes…' : resumes.length ? 'Choose a resume' : 'No uploaded resume'}</option>{resumes.map(item => <option key={item.id} value={item.id}>{item.title}</option>)}</select></Field>
                    <Field label="Resume version"><select className="field-input" value={versionId} onChange={event => setVersionId(event.target.value)} disabled={!resume?.versions?.length}>{(resume?.versions ?? []).map(item => <option key={item.id} value={item.id}>Version {item.versionNumber} · {item.originalFileName}</option>)}</select></Field></div>
                {!resumes.length && !loading && <p className="text-xs text-amber">Upload a PDF or DOCX in <Link className="underline" to="/app/resumes">Resume Studio</Link>.</p>}
                {job && <div className="rounded-xl border border-line bg-ink p-4"><p className="text-xs font-semibold">{job.title} · {job.company}</p><p className="mt-1 text-[10px] text-muted">{job.location} · {job.source}{job.sample ? ' · sample listing' : ''}</p><p className="mt-3 line-clamp-4 text-[10px] leading-5 text-muted">{job.description}</p></div>}
                <Field label="Application posting URL (optional)"><input type="url" className="field-input" placeholder="https://company.com/careers/role" value={applicationUrl} onChange={event => setApplicationUrl(event.target.value)}/></Field>
                <Field label="Cover-letter tone"><select className="field-input" value={tone} onChange={event => setTone(event.target.value)}><option value="professional">Professional</option><option value="warm">Warm</option><option value="direct">Direct</option></select></Field>
                <section className="space-y-3 rounded-xl border border-line p-4"><div><h3 className="text-xs font-semibold">Application form questions</h3><p className="mt-1 text-[10px] leading-5 text-muted">No employer form adapters are configured. Add the actual questions you see; mark eligibility or other consequential fields for manual confirmation.</p></div>
                    <div className="flex gap-2"><input className="field-input" aria-label="Application question" placeholder="Paste a question from the application form" value={newQuestion} onChange={event => setNewQuestion(event.target.value)}/><button type="button" className="button-secondary shrink-0" onClick={addQuestion} aria-label="Add question"><Plus size={14}/></button></div>
                    {questions.map(item => <div key={item.key} className="flex flex-wrap items-center gap-3 rounded-lg bg-ink p-3"><span className="min-w-0 flex-1 text-xs">{item.label}</span><label className="flex items-center gap-1.5 text-[10px] text-muted"><input type="checkbox" checked={item.required} onChange={event => setQuestions(list => list.map(q => q.key === item.key ? { ...q, required: event.target.checked } : q))}/>Required</label><label className="flex items-center gap-1.5 text-[10px] text-amber"><input type="checkbox" checked={item.consequential} onChange={event => setQuestions(list => list.map(q => q.key === item.key ? { ...q, consequential: event.target.checked } : q))}/>Sensitive / consequential</label><button type="button" aria-label={`Remove ${item.label}`} onClick={() => setQuestions(list => list.filter(q => q.key !== item.key))} className="text-muted hover:text-rose-200"><Trash2 size={14}/></button></div>)}
                </section>
                <button type="submit" disabled={busy || loading || !jobId || !versionId} className="button-primary w-full">{busy ? 'Preparing drafts…' : 'Prepare application drafts'}</button>
                <p className="text-[10px] leading-5 text-muted">The agent can draft a cover letter and supported answers. It cannot submit to real employers; unsupported sites require manual action.</p>
            </form>

            <div className="space-y-5">
                {selected ? <section className="space-y-5 rounded-2xl border border-line bg-panel p-5 sm:p-6">
                    <div className="flex flex-wrap items-start justify-between gap-3"><div><h2 className="text-sm font-semibold">Review application</h2><p className="mt-1 text-[10px] text-muted">{selected.jobTitle} · {selected.company}</p></div><span className={`text-[9px] font-bold uppercase tracking-wide ${statusTone[selected.status] ?? 'text-muted'}`}>{selected.status.replaceAll('_', ' ')}</span></div>
                    <div className="grid gap-2 rounded-xl border border-line bg-ink p-3 text-[10px] text-muted"><p>Resume: <span className="text-white">{selected.resumeFileName}</span></p><p>Application URL: {selected.applicationUrl ? <a className="text-cyan underline" href={selected.applicationUrl} target="_blank" rel="noreferrer">Open job posting <ArrowUpRight className="inline" size={12}/></a> : 'Not provided'}</p><p>Cover-letter source: <span className="text-white">{selected.coverLetterSource ?? 'Not generated'}</span></p></div>
                    {selected.missingInformation?.length > 0 && <div className="space-y-1 rounded-xl border border-amber/20 bg-amber/[.04] p-3 text-[10px] text-amber">{selected.missingInformation.map((item, index) => <p key={index}>• {item}</p>)}</div>}
                    {selected.coverLetterDraft != null && <div><label htmlFor="auto-apply-letter" className="mb-2 block text-[10px] font-semibold text-muted">Editable cover-letter draft</label><textarea id="auto-apply-letter" className="field-input min-h-48 resize-y" maxLength={12000} value={draft} onChange={event => setDraft(event.target.value)}/><button type="button" disabled={busy || !draft.trim() || draft === selected.coverLetterDraft} onClick={() => void saveLetter()} className="button-secondary mt-2 !h-9 !px-3 !text-[10px]"><Save size={13}/>Save edited letter</button></div>}
                    {selected.answers?.map(question => <AnswerEditor key={question.key} question={question} value={answerInputs[question.key] ?? ''} remember={Boolean(remember[question.key])} disabled={busy} onChange={value => setAnswerInputs(items => ({ ...items, [question.key]: value }))} onRemember={value => setRemember(items => ({ ...items, [question.key]: value }))} onSave={confirm => void updateAnswer(question, confirm)}/>)}
                    <section className="space-y-3 rounded-xl border border-line p-4"><div><h3 className="text-xs font-semibold">Employer application fields</h3><p className="mt-1 text-[10px] leading-5 text-muted">Use the CareerPilot Autofill extension on the employer form to capture its visible fields, types, choices, and required markers. Verified profile/resume values are prefetched when labels match. Sensitive answers stay blank.</p></div><ApplicationFieldsEditor fields={applicationFields} disabled={busy || selected.status === 'SUBMITTED'} invalid={fieldsInvalid} onChange={fields => { setApplicationFields(fields); setFieldsDirty(true); }}/><div className="flex flex-wrap items-center justify-between gap-2"><span className="text-[9px] text-muted">{applicationFields.filter(field => field.required).length - missingApplicationFields(applicationFields).length}/{applicationFields.filter(field => field.required).length} required fields complete</span><button type="button" disabled={busy || !fieldsDirty || selected.status === 'SUBMITTED'} onClick={() => void saveApplicationFields()} className="button-secondary !h-9 !px-3 !text-[10px]"><Save size={13}/>Save application answers</button></div></section>
                    <div className="rounded-xl border border-line bg-ink p-4"><h3 className="text-xs font-semibold">Optional referral drafts</h3><p className="mt-1 text-[10px] leading-5 text-muted">Uses the existing LinkedIn/Instagram message templates. The recipient and relationship are not invented.</p><div className="mt-3 grid gap-2 sm:grid-cols-2"><input className="field-input" aria-label="Referral recipient name" placeholder="Recipient’s name" value={recipient} onChange={event => setRecipient(event.target.value)}/><input className="field-input" aria-label="Relevant experience for referral" placeholder="Relevant experience (optional)" value={fit} onChange={event => setFit(event.target.value)}/></div><button type="button" className="button-secondary mt-3 !h-9 !px-3 !text-[10px]" onClick={makeReferralDrafts}>Create referral drafts</button>{referralMessages && <div className="mt-3 space-y-3">{[['LinkedIn', referralMessages.linkedIn], ['Instagram', referralMessages.instagram]].map(([name, message]) => <div key={name}><label className="mb-1 block text-[10px] font-semibold">{name}</label><textarea className="field-input min-h-28 resize-y" aria-label={`${name} referral message`} value={message} onChange={event => setReferralMessages(current => ({ ...current, [name === 'LinkedIn' ? 'linkedIn' : 'instagram']: event.target.value }))}/><button type="button" className="button-secondary mt-1 !h-8 !px-3 !text-[10px]" onClick={() => void copy(message)}><Check size={12}/>Copy {name}</button></div>)}</div>}</div>
                    {selected.status === 'READY_FOR_REVIEW' && <label className="flex items-start gap-2 rounded-xl border border-line bg-ink p-3 text-[10px] leading-5"><input type="checkbox" checked={consent} onChange={event => setConsent(event.target.checked)} className="mt-1"/><span>I reviewed the job, resume, cover letter, and every answer. I approve this exact draft for the next step. This does not itself submit an application.</span></label>}
                    {selected.status === 'READY_FOR_REVIEW' && <button type="button" disabled={busy || !consent || fieldsDirty || missingApplicationFields(applicationFields).length > 0} onClick={() => void approve()} className="button-primary w-full"><Check size={14}/>Approve this exact draft</button>}
                    {selected.status === 'APPROVED' && <div className="space-y-2"><button type="button" disabled={busy || autofillBusy} onClick={() => void shareApprovedDraft()} className="button-primary w-full"><Check size={14}/>{autofillBusy ? 'Sharing approved draft…' : 'Send approved draft to browser autofill'}</button><p className="text-[9px] leading-5 text-muted">Autofill only fills supported empty fields after you click the extension. It skips sensitive questions and resume uploads, and never submits the form.</p>{autofillShared && <Link to={`/app/auto-apply/final/${selected.id}`} className="button-primary w-full"><ArrowUpRight size={14}/>Final Apply — review saved application</Link>}<button type="button" disabled={busy} onClick={() => void continueManually()} className="button-secondary w-full"><ArrowUpRight size={14}/>Continue to manual application</button></div>}
                    {selected.status === 'REQUIRES_MANUAL_ACTION' && <div role="status" className="rounded-xl border border-amber/20 bg-amber/[.04] p-3 text-[10px] leading-5 text-amber">{selected.resultMessage}{selected.applicationUrl && <a className="ml-1 underline" href={selected.applicationUrl} target="_blank" rel="noreferrer">Open application page</a>}</div>}
                    {(selected.status === 'FAILED' || (selected.status === 'NEEDS_INFORMATION' && selected.missingInformation?.some(item => !item.startsWith('Answer and confirm: ')))) && <div className="space-y-2"><p className="text-[10px] text-amber">{selected.resultMessage} If you have fixed the missing setup or resume issue, retry preparation.</p><button type="button" disabled={busy} onClick={() => void retryPreparation()} className="button-secondary w-full">{busy ? 'Retrying preparation…' : 'Retry preparation'}</button></div>}
                    <p className="text-[9px] leading-5 text-muted">Approval is tied to the exact current content and is cleared when answers or the letter change. No application is reported submitted by this manual step.</p>
                </section> : <div className="rounded-2xl border border-line bg-panel px-6 py-12 text-center text-xs text-muted"><FileText size={24} className="mx-auto"/><p className="mt-3 font-semibold text-white">Your preparation will appear here</p><p className="mt-1">Choose a job and reviewed resume, then prepare an application.</p></div>}

                <section className="rounded-2xl border border-line bg-panel p-5"><div className="flex items-center justify-between"><div><h2 className="text-sm font-semibold">Preparation sessions</h2><p className="mt-1 text-[10px] text-muted">Saved for your account</p></div><CircleAlert size={16} className="text-cyan"/></div>{loading ? <p className="py-6 text-center text-xs text-muted">Loading sessions…</p> : sessions.length ? <div className="mt-4 space-y-2">{sessions.map(item => <button type="button" key={item.id} onClick={() => void openSession(item.id)} className={`w-full rounded-xl border p-3 text-left transition ${selected?.id === item.id ? 'border-violet/40 bg-violet/[.06]' : 'border-line bg-ink hover:border-white/20'}`}><span className="flex items-start justify-between gap-2"><span className="text-xs font-semibold">{item.jobTitle} · {item.company}</span><span className={`text-[8px] font-bold ${statusTone[item.status] ?? 'text-muted'}`}>{item.status.replaceAll('_', ' ')}</span></span><span className="mt-1 block text-[9px] text-muted">{new Date(item.updatedAt).toLocaleString()}</span></button>)}</div> : <p className="py-6 text-center text-xs text-muted">No preparation sessions yet.</p>}</section>
                <Link to="/app/automation" className="block rounded-xl border border-line bg-panel p-4 text-[10px] leading-5 text-muted hover:border-cyan/30">Need the existing fictional-portal test? Open Automation Center. It does not contact real employers. <ArrowUpRight className="inline" size={12}/></Link>
            </div>
        </div>
    </main>;
}

function Field({ label, children }) { return <div><label className="mb-2 block text-[10px] font-semibold text-muted">{label}</label>{children}</div>; }

function AnswerEditor({ question, value, remember, disabled, onChange, onRemember, onSave }) {
    return <div className="rounded-xl border border-line bg-ink p-4"><label htmlFor={`answer-${question.key}`} className="block text-xs font-semibold">{question.label}{question.required ? ' *' : ''}</label><p className="mt-1 text-[9px] text-muted">{question.consequential ? 'Sensitive or consequential: AI will not draft this answer.' : question.answerSource?.includes('OPENAI') ? 'AI draft—verify every fact before confirming.' : question.answerSource === 'LOCAL_NEEDS_USER_INPUT' ? 'No AI provider configured; answer this yourself.' : question.draftReason}</p>{question.reusableAnswer && !question.confirmed && <button type="button" className="mt-2 text-[9px] text-cyan underline" onClick={() => onChange(question.reusableAnswer)}>Use your earlier confirmed answer as a draft (review and confirm)</button>}<textarea id={`answer-${question.key}`} className="field-input mt-2 min-h-24 resize-y" maxLength={2000} value={value} onChange={event => onChange(event.target.value)}/>{!question.consequential && <label className="mt-2 flex items-center gap-2 text-[9px] text-muted"><input type="checkbox" checked={remember} onChange={event => onRemember(event.target.checked)}/>Remember this confirmed answer for the same question</label>}<div className="mt-2 flex flex-wrap gap-2"><button type="button" disabled={disabled || !value.trim()} className="button-secondary !h-8 !px-3 !text-[9px]" onClick={() => onSave(false)}>Save draft</button><button type="button" disabled={disabled || !value.trim()} className="button-primary !h-8 !px-3 !text-[9px]" onClick={() => onSave(true)}>Confirm this answer</button>{question.confirmed && <span className="self-center text-[9px] text-mint">Confirmed by you</span>}</div></div>;
}
