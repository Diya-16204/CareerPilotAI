import { useEffect, useMemo, useState } from 'react';
import { Activity, Bot, CheckCircle2, CircleAlert, Clock3, ExternalLink, LoaderCircle, ShieldCheck } from 'lucide-react';
import { apiMessage, careerApi } from '../api/client';
const portals = [
    { key: 'PORTAL_A', label: 'Northstar', description: 'Standard application' },
    { key: 'PORTAL_B', label: 'Waypoint', description: 'Guided application wizard' },
    { key: 'PORTAL_C', label: 'Craftwork', description: 'Role-specific screening' },
];
const isActive = (status) => status === 'PENDING' || status === 'RUNNING';
export function AutomationPage() {
    const [resumes, setResumes] = useState([]);
    const [resume, setResume] = useState(null);
    const [resumeId, setResumeId] = useState('');
    const [versionId, setVersionId] = useState('');
    const [portal, setPortal] = useState('PORTAL_A');
    const [jobs, setJobs] = useState([]);
    const [jobKey, setJobKey] = useState('');
    const [answers, setAnswers] = useState({});
    const [education, setEducation] = useState('');
    const [experience, setExperience] = useState('');
    const [coverLetter, setCoverLetter] = useState('');
    const [consent, setConsent] = useState(false);
    const [runs, setRuns] = useState([]);
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    const [notice, setNotice] = useState('');
    const job = jobs.find((item) => item.key === jobKey) ?? null;
    const activeRuns = useMemo(() => runs.filter((run) => isActive(run.status)), [runs]);
    useEffect(() => {
        let current = true;
        Promise.allSettled([careerApi.resumes(), careerApi.automationRuns()])
            .then(([resumeResult, runResult]) => {
            if (!current)
                return;
            if (resumeResult.status === 'fulfilled') {
                const resumeItems = resumeResult.value;
                setResumes(resumeItems);
                setResumeId(resumeItems[0]?.id ?? '');
            }
            else {
                setError(apiMessage(resumeResult.reason));
            }
            if (runResult.status === 'fulfilled')
                setRuns(runResult.value.items);
            else if (resumeResult.status === 'fulfilled')
                setError(`Resume list loaded, but automation history could not be loaded: ${apiMessage(runResult.reason)}`);
        })
            .finally(() => { if (current)
            setLoading(false); });
        return () => { current = false; };
    }, []);
    useEffect(() => {
        let current = true;
        if (!resumeId) {
            setResume(null);
            setVersionId('');
            return;
        }
        careerApi.resume(resumeId)
            .then((detail) => {
            if (!current)
                return;
            setResume(detail);
            setVersionId((selected) => detail.versions.some((version) => version.id === selected) ? selected : detail.versions[0]?.id ?? '');
        })
            .catch((reason) => { if (current)
            setError(apiMessage(reason)); });
        return () => { current = false; };
    }, [resumeId]);
    useEffect(() => {
        let current = true;
        setJobs([]);
        setJobKey('');
        setAnswers({});
        setConsent(false);
        careerApi.portalJobs(portal.toLowerCase())
            .then((items) => {
            if (!current)
                return;
            setJobs(items);
            setJobKey(items[0]?.key ?? '');
        })
            .catch((reason) => { if (current)
            setError(apiMessage(reason)); });
        return () => { current = false; };
    }, [portal]);
    useEffect(() => {
        if (activeRuns.length === 0)
            return;
        let current = true;
        const timer = window.setInterval(() => {
            Promise.all(activeRuns.map((run) => careerApi.automationRun(run.id)))
                .then((updates) => {
                if (!current)
                    return;
                setRuns((existing) => updates.reduce((items, update) => items.map((item) => item.id === update.id ? update : item), existing));
            })
                .catch(() => { });
        }, 2000);
        return () => { current = false; window.clearInterval(timer); };
    }, [activeRuns]);
    async function refreshHistory() {
        const page = await careerApi.automationRuns();
        setRuns(page.items);
    }
    async function start(event) {
        event.preventDefault();
        if (!job || !versionId || !consent)
            return;
        setBusy(true);
        setError('');
        setNotice('');
        const input = {
            portalKey: portal, jobKey: job.key, resumeVersionId: versionId,
            education: education.trim(), experience: experience.trim(), coverLetter: coverLetter.trim(),
            answers: Object.fromEntries(job.questions.map((question) => [question, answers[question]?.trim() ?? ''])),
            consent: true,
        };
        try {
            const run = await careerApi.startAutomationRun(input, crypto.randomUUID());
            setRuns((existing) => [run, ...existing.filter((item) => item.id !== run.id)].slice(0, 50));
            setNotice('Run queued. This page will update after the fictional portal submission is independently verified.');
            setAnswers({});
            setConsent(false);
            await refreshHistory();
        }
        catch (reason) {
            setError(apiMessage(reason));
        }
        finally {
            setBusy(false);
        }
    }
    return <div>
    <div className="eyebrow"><Bot size={14}/> User-approved browser workflows</div>
    <div className="mt-3 flex flex-wrap items-end justify-between gap-4">
      <div><h1 className="font-display text-3xl font-bold tracking-[-.055em]">Automation center</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-muted">Prepare a fictional application, approve it, and follow the saved confirmation through verification.</p></div>
      <span className="rounded-full border border-cyan/15 bg-cyan/[.06] px-3 py-1.5 text-[10px] font-semibold uppercase tracking-wider text-cyan">Simulation only</span>
    </div>

    <div className="mt-6 flex gap-3 rounded-2xl border border-amber/20 bg-amber/[.04] p-4 text-xs leading-5 text-[#d7cba8]">
      <ShieldCheck size={16} className="mt-0.5 shrink-0 text-amber"/><p>Runs use a local browser against fictional CareerPilot portals only. No real employer is contacted. Review each answer and give explicit consent before a run starts.</p>
    </div>
    {error && <div role="alert" className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs leading-5 text-rose-200">{error}</div>}
    {notice && <div role="status" className="mt-5 rounded-xl border border-mint/20 bg-mint/[.06] px-4 py-3 text-xs leading-5 text-mint">{notice}</div>}

    <div className="mt-6 grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(340px,.85fr)]">
      <form onSubmit={(event) => void start(event)} className="space-y-5 rounded-2xl border border-line bg-panel p-5 sm:p-6">
        <div><h2 className="text-sm font-semibold">Prepare an application</h2><p className="mt-1 text-[10px] leading-5 text-muted">Only the resume version you select is attached to the test application.</p></div>
        <fieldset><legend className="mb-2 text-[10px] font-semibold text-muted">Choose a fictional portal</legend><div className="grid gap-2 sm:grid-cols-3">{portals.map((item) => <button key={item.key} type="button" onClick={() => setPortal(item.key)} aria-pressed={portal === item.key} className={`rounded-xl border p-3 text-left transition ${portal === item.key ? 'border-violet/40 bg-violet/[.08]' : 'border-line bg-ink hover:border-white/20'}`}><span className="block text-xs font-semibold">{item.label}</span><span className="mt-1 block text-[9px] text-muted">{item.description}</span></button>)}</div></fieldset>
        <div className="grid gap-4 sm:grid-cols-2">
          <div><label htmlFor="automation-job" className="mb-2 block text-[10px] font-semibold text-muted">Sample role</label><select id="automation-job" className="field-input" value={jobKey} onChange={(event) => { setJobKey(event.target.value); setAnswers({}); }} disabled={!jobs.length}>{jobs.map((item) => <option key={item.key} value={item.key}>{item.title} · {item.location}</option>)}</select></div>
          <div><label htmlFor="automation-resume" className="mb-2 block text-[10px] font-semibold text-muted">Your resume</label><select id="automation-resume" className="field-input" value={resumeId} onChange={(event) => setResumeId(event.target.value)} disabled={!resumes.length}><option value="">Select a resume</option>{resumes.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}</select></div>
        </div>
        {resume && <div><label htmlFor="automation-version" className="mb-2 block text-[10px] font-semibold text-muted">Resume version</label><select id="automation-version" className="field-input" value={versionId} onChange={(event) => setVersionId(event.target.value)}>{resume.versions.map((version) => <option key={version.id} value={version.id}>Version {version.versionNumber} · {version.originalFileName}</option>)}</select></div>}
        {portal === 'PORTAL_A' && <div><label htmlFor="automation-cover-letter" className="mb-2 block text-[10px] font-semibold text-muted">Cover letter</label><textarea id="automation-cover-letter" required maxLength={4000} rows={4} className="field-input !h-auto min-h-24 py-3" value={coverLetter} onChange={(event) => setCoverLetter(event.target.value)} placeholder="Add a truthful, role-specific note"/></div>}
        {portal === 'PORTAL_B' && <div className="grid gap-4"><div><label htmlFor="automation-education" className="mb-2 block text-[10px] font-semibold text-muted">Highest completed education</label><input id="automation-education" required maxLength={300} className="field-input" value={education} onChange={(event) => setEducation(event.target.value)}/></div><div><label htmlFor="automation-experience" className="mb-2 block text-[10px] font-semibold text-muted">Relevant experience</label><textarea id="automation-experience" required maxLength={2000} rows={3} className="field-input !h-auto py-3" value={experience} onChange={(event) => setExperience(event.target.value)}/></div></div>}
        {job?.questions.map((question, index) => <div key={question}><label htmlFor={`automation-answer-${index}`} className="mb-2 block text-[10px] font-semibold text-muted">{question}</label><textarea id={`automation-answer-${index}`} required maxLength={2000} rows={3} className="field-input !h-auto py-3" value={answers[question] ?? ''} onChange={(event) => setAnswers((current) => ({ ...current, [question]: event.target.value }))}/></div>)}
        {!loading && resumes.length === 0 && <div className="rounded-xl border border-amber/20 bg-amber/[.04] p-3 text-[10px] leading-5 text-[#d7cba8]">Upload and review a PDF or DOCX in <a className="text-cyan underline" href="/app/resumes">Resume studio</a> before starting a run.</div>}
        <label className="flex items-start gap-3 rounded-xl border border-line bg-ink p-4 text-[10px] leading-5 text-[#c9cad5]"><input type="checkbox" className="mt-1 accent-violet" checked={consent} onChange={(event) => setConsent(event.target.checked)}/><span>I have reviewed this application and approve submission to the selected simulated portal. I understand no real employer will receive it.</span></label>
        <button type="submit" disabled={busy || loading || !job || !versionId || !consent} className="button-primary w-full justify-center">{busy ? <><LoaderCircle size={15} className="animate-spin"/> Queueing run…</> : <><ShieldCheck size={15}/> Approve and start simulation</>}</button>
        {!jobs.length && !error && <p className="text-[10px] text-muted">Loading available simulation roles…</p>}
      </form>

      <section aria-labelledby="automation-history" className="rounded-2xl border border-line bg-panel p-5 sm:p-6">
        <div className="flex items-center justify-between gap-4"><div><h2 id="automation-history" className="text-sm font-semibold">Run history</h2><p className="mt-1 text-[10px] text-muted">Persisted verification results for your account</p></div><span className="grid size-9 place-items-center rounded-xl border border-white/[.07] bg-white/[.03] text-cyan"><Activity size={16}/></span></div>
        {loading ? <div className="py-12 text-center text-xs text-muted">Loading your automation history…</div> : runs.length === 0 ? <div className="mt-5 rounded-xl border border-dashed border-line px-5 py-12 text-center"><Clock3 size={22} className="mx-auto text-muted"/><p className="mt-3 text-xs font-semibold">No runs yet</p><p className="mt-1 text-[10px] leading-5 text-muted">Approved simulations will appear here with their verification result.</p></div> : <div className="mt-5 space-y-3">{runs.map((run) => <RunCard key={run.id} run={run}/>)}</div>}
      </section>
    </div>
    <p className="mt-5 text-[10px] leading-5 text-muted">A run is marked successful only after the API independently confirms that its owner-scoped submission is persisted. Interrupted or uncertain runs are reported as unverified.</p>
  </div>;
}
function RunCard({ run }) {
    const portalName = portals.find((portal) => portal.key === run.portalKey)?.label ?? run.portalKey;
    const statusStyle = run.status === 'VERIFIED_SUCCESS' ? 'border-mint/20 bg-mint/[.06] text-mint' : run.status === 'FAILED' || run.status === 'UNVERIFIED' ? 'border-rose-300/20 bg-rose-300/[.05] text-rose-200' : 'border-cyan/20 bg-cyan/[.05] text-cyan';
    return <article className="rounded-xl border border-line bg-ink p-4">
    <div className="flex flex-wrap items-start justify-between gap-3"><div><p className="text-xs font-semibold">{portalName} <span className="font-normal text-muted">· {run.jobKey}</span></p><p className="mt-1 text-[9px] text-muted">{new Date(run.createdAt).toLocaleString()}</p></div><span className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-[9px] font-semibold ${statusStyle}`}>{isActive(run.status) ? <LoaderCircle size={11} className="animate-spin"/> : run.status === 'VERIFIED_SUCCESS' ? <CheckCircle2 size={11}/> : run.status === 'FAILED' || run.status === 'UNVERIFIED' ? <CircleAlert size={11}/> : <Clock3 size={11}/>} {run.status.replaceAll('_', ' ')}</span></div>
    {run.message && <p className="mt-3 text-[10px] leading-5 text-muted">{run.message}</p>}
    {run.confirmationId && <div className="mt-3 rounded-lg border border-mint/10 bg-mint/[.03] p-3"><p className="text-[9px] uppercase tracking-wider text-muted">Verified submission ID</p><p className="mt-1 break-all font-mono text-[10px] text-mint">{run.confirmationId}</p></div>}
    <a href={`/simulation/${run.portalKey.toLowerCase()}`} className="mt-3 inline-flex items-center gap-1 text-[9px] font-semibold text-cyan hover:text-white">View simulated portal <ExternalLink size={11}/></a>
  </article>;
}
