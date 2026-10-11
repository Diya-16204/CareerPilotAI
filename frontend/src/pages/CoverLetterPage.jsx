import { useEffect, useState } from 'react';
import { Copy, Download, FileText, PenLine, RefreshCw, Save } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import { apiMessage, careerApi } from '../api/client';
export function CoverLetterPage() {
    const [params] = useSearchParams();
    const requestedJobId = params.get('jobId');
    const [jobs, setJobs] = useState([]);
    const [jobId, setJobId] = useState(requestedJobId ?? '');
    const [resumes, setResumes] = useState([]);
    const [resumeId, setResumeId] = useState('');
    const [versions, setVersions] = useState([]);
    const [versionId, setVersionId] = useState('');
    const [letters, setLetters] = useState([]);
    const [letter, setLetter] = useState(null);
    const [body, setBody] = useState('');
    const [tone, setTone] = useState('professional');
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    const [notice, setNotice] = useState('');
    useEffect(() => { let active = true; Promise.all([careerApi.jobs({ page: 0, size: 100 }), careerApi.resumes(), careerApi.coverLetters()]).then(async ([jobPage, files, letterPage]) => { if (!active)
        return; setJobs(jobPage.items); setJobId(jobPage.items.some((job) => job.id === requestedJobId) ? requestedJobId : jobPage.items[0]?.id ?? ''); setResumes(files); setLetters(letterPage.items); const chosen = files[0]; if (chosen) {
        setResumeId(chosen.id);
        const detail = await careerApi.resume(chosen.id);
        if (active) {
            setVersions(detail.versions);
            setVersionId(detail.versions.find((v) => v.versionNumber === detail.activeVersionNumber)?.id ?? detail.versions[0]?.id ?? '');
        }
    } }).catch((reason) => { if (active)
        setError(apiMessage(reason)); }).finally(() => { if (active)
        setLoading(false); }); return () => { active = false; }; }, [requestedJobId]);
    async function chooseResume(id) { setResumeId(id); setVersions([]); setVersionId(''); if (!id)
        return; try {
        const detail = await careerApi.resume(id);
        setVersions(detail.versions);
        setVersionId(detail.versions.find((v) => v.versionNumber === detail.activeVersionNumber)?.id ?? detail.versions[0]?.id ?? '');
    }
    catch (reason) {
        setError(apiMessage(reason));
    } }
    async function generate() { if (!jobId || !versionId) {
        setError('Choose a job and a resume version first.');
        return;
    } setBusy(true); setError(''); setNotice(''); try {
        const draft = await careerApi.generateCoverLetter(jobId, versionId, tone);
        setLetter(draft);
        setBody(draft.body);
        setLetters((current) => [draft, ...current.filter((item) => item.id !== draft.id)]);
        setNotice(`Draft created using ${draft.generationSource === 'LOCAL_TEMPLATE' ? 'a local template' : 'the configured provider'}. Edit and verify every detail before using it.`);
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    async function openLetter(item) { try {
        const detail = await careerApi.coverLetter(item.id);
        setLetter(detail);
        setBody(detail.body);
        setJobId(detail.jobId);
        setNotice('');
    }
    catch (reason) {
        setError(apiMessage(reason));
    } }
    async function save() { if (!letter)
        return; setBusy(true); setError(''); try {
        const updated = await careerApi.saveCoverLetter(letter.id, body);
        setLetter(updated);
        setLetters((current) => current.map((item) => item.id === updated.id ? updated : item));
        setNotice('Your edits are saved to your account.');
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    async function copy() { try {
        await navigator.clipboard.writeText(body);
        setNotice('Draft copied to clipboard.');
    }
    catch {
        setError('Clipboard access is unavailable in this browser.');
    } }
    function download() { const blob = new Blob([body], { type: 'text/plain;charset=utf-8' }); const url = URL.createObjectURL(blob); const anchor = document.createElement('a'); anchor.href = url; anchor.download = `${letter?.title ?? 'careerpilot-cover-letter'}.txt`; anchor.click(); window.setTimeout(() => URL.revokeObjectURL(url), 1000); }
    return <div><div className="eyebrow"><PenLine size={13}/>A thoughtful first draft</div><h1 className="mt-3 font-display text-3xl font-bold tracking-[-.055em]">Cover letter studio</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-muted">Create an editable draft using only your resume skills and the selected role. The local fallback is clearly labeled; verify all details before sending.</p>
    {error && <div role="alert" className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs text-rose-200">{error}</div>}{notice && <div role="status" className="mt-5 rounded-xl border border-mint/15 bg-mint/[.06] px-4 py-3 text-xs text-mint">{notice}</div>}
    <div className="mt-7 grid gap-5 xl:grid-cols-[.65fr_1.35fr]"><section className="h-fit rounded-2xl border border-line bg-panel p-5"><div className="flex items-center gap-2"><span className="grid size-8 place-items-center rounded-lg bg-violet/10 text-violet"><FileText size={15}/></span><h2 className="text-xs font-semibold">Draft settings</h2></div><div className="mt-5 space-y-4"><div><label htmlFor="letter-job" className="mb-2 block text-[10px] font-semibold text-muted">Opportunity</label><select id="letter-job" value={jobId} onChange={(event) => setJobId(event.target.value)} className="field-input">{!jobs.length && <option value="">No jobs available</option>}{jobs.map((job) => <option key={job.id} value={job.id}>{job.title} · {job.company}</option>)}</select></div><div><label htmlFor="letter-resume" className="mb-2 block text-[10px] font-semibold text-muted">Resume</label><select id="letter-resume" value={resumeId} onChange={(event) => void chooseResume(event.target.value)} className="field-input">{!resumes.length && <option value="">Upload a resume first</option>}{resumes.map((resume) => <option key={resume.id} value={resume.id}>{resume.title}</option>)}</select></div><div><label htmlFor="letter-version" className="mb-2 block text-[10px] font-semibold text-muted">Resume version</label><select id="letter-version" value={versionId} onChange={(event) => setVersionId(event.target.value)} className="field-input">{versions.map((version) => <option key={version.id} value={version.id}>Version {version.versionNumber} · {version.originalFileName}</option>)}</select></div><div><label htmlFor="letter-tone" className="mb-2 block text-[10px] font-semibold text-muted">Tone</label><select id="letter-tone" value={tone} onChange={(event) => setTone(event.target.value)} className="field-input"><option value="professional">Professional</option><option value="warm">Warm</option><option value="direct">Direct</option></select></div><button disabled={busy || loading || !jobId || !versionId} onClick={() => void generate()} className="button-primary w-full"><RefreshCw size={14}/>{busy ? 'Working…' : letter ? 'Generate a new draft' : 'Generate draft'}</button><p className="text-[9px] leading-4 text-muted">Local mode uses a deterministic template, not a language model. No provider API key is configured.</p></div>
      {letters.length > 0 && <div className="mt-6 border-t border-line pt-4"><h3 className="text-[10px] font-bold uppercase tracking-widest text-muted">Saved drafts</h3><div className="mt-3 max-h-64 space-y-1 overflow-auto">{letters.map((item) => <button key={item.id} onClick={() => void openLetter(item)} className={`w-full rounded-lg px-3 py-2.5 text-left transition hover:bg-white/[.04] ${letter?.id === item.id ? 'bg-violet/[.08]' : ''}`}><span className="block truncate text-[10px] font-semibold">{item.title}</span><span className="mt-1 block text-[9px] text-muted">{new Date(item.updatedAt).toLocaleDateString()} · {item.generationSource === 'LOCAL_TEMPLATE' ? 'Local template' : 'AI provider'}</span></button>)}</div></div>}</section>
      <section className="rounded-2xl border border-line bg-panel p-5 sm:p-7">{letter ? <><div className="flex flex-wrap items-start justify-between gap-3 border-b border-line pb-4"><div><span className="status-pill">{letter.generationSource === 'LOCAL_TEMPLATE' ? 'Local template' : 'Provider generated'}</span><h2 className="mt-3 font-display text-lg font-bold">{letter.title}</h2><p className="mt-1 text-[10px] text-muted">Last saved {new Date(letter.updatedAt).toLocaleString()}</p></div><div className="flex gap-2"><button onClick={() => void copy()} className="icon-button" title="Copy draft" aria-label="Copy draft"><Copy size={15}/></button><button onClick={download} className="icon-button" title="Download draft" aria-label="Download draft"><Download size={15}/></button></div></div><label htmlFor="letter-body" className="sr-only">Edit cover letter</label><textarea id="letter-body" value={body} onChange={(event) => setBody(event.target.value)} maxLength={12000} className="mt-5 min-h-[420px] w-full resize-y rounded-xl border border-white/[.07] bg-[#0d0f19] p-5 text-sm leading-7 text-[#e4e4ee] outline-none focus:border-violet/50"/><div className="mt-3 flex flex-wrap items-center justify-between gap-3"><p className="text-[9px] text-muted">{body.length}/12,000 characters · Check names, facts, and role details before using.</p><button disabled={busy} onClick={() => void save()} className="button-primary !h-9 !px-4 !text-[10px]"><Save size={13}/>{busy ? 'Saving…' : 'Save edits'}</button></div></> : <div className="grid min-h-[420px] place-items-center text-center"><div><span className="mx-auto grid size-12 place-items-center rounded-2xl border border-white/[.07] bg-white/[.025] text-muted"><PenLine size={19}/></span><p className="mt-4 text-sm font-semibold">Your draft will appear here</p><p className="mt-1 text-xs text-muted">Choose a role and reviewed resume version to begin.</p></div></div>}</section></div>
  </div>;
}
