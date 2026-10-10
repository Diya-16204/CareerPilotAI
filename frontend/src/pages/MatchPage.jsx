import { useEffect, useState } from 'react';
import { ArrowLeft, ArrowRight, Check, CircleHelp, Minus, Target } from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';
import { apiMessage, careerApi } from '../api/client';
export function MatchPage() {
    const [params] = useSearchParams();
    const jobId = params.get('jobId') ?? '';
    const [job, setJob] = useState(null);
    const [resumes, setResumes] = useState([]);
    const [versionList, setVersionList] = useState([]);
    const [resumeId, setResumeId] = useState('');
    const [versionId, setVersionId] = useState('');
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    useEffect(() => { let active = true; Promise.all([careerApi.job(jobId), careerApi.resumes()]).then(async ([found, files]) => { if (!active)
        return; setJob(found); setResumes(files); const selected = files[0]; if (selected) {
        setResumeId(selected.id);
        const details = await careerApi.resume(selected.id);
        if (active) {
            setVersionList(details.versions);
            setVersionId(details.versions.find((v) => v.versionNumber === details.activeVersionNumber)?.id ?? details.versions[0]?.id ?? '');
        }
    } }).catch((reason) => { if (active)
        setError(apiMessage(reason)); }).finally(() => { if (active)
        setLoading(false); }); return () => { active = false; }; }, [jobId]);
    async function chooseResume(id) { setResumeId(id); setResult(null); setVersionId(''); setVersionList([]); if (!id)
        return; try {
        const detail = await careerApi.resume(id);
        setVersionList(detail.versions);
        setVersionId(detail.versions.find((v) => v.versionNumber === detail.activeVersionNumber)?.id ?? detail.versions[0]?.id ?? '');
    }
    catch (reason) {
        setError(apiMessage(reason));
    } }
    async function runMatch() { if (!job || !versionId)
        return; setBusy(true); setError(''); try {
        setResult(await careerApi.matchResume(versionId, job.id));
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    if (loading)
        return <div className="p-8 text-center text-xs text-muted">Preparing your match…</div>;
    return <div><Link to="/app/jobs" className="inline-flex items-center gap-2 text-xs text-muted hover:text-white"><ArrowLeft size={14}/> Back to jobs</Link>{error && <div role="alert" className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs text-rose-200">{error}</div>}{job && <><div className="mt-5 flex items-start gap-4"><span className="grid size-12 shrink-0 place-items-center rounded-2xl border border-violet/15 bg-violet/[.08] font-display text-lg font-bold text-[#d9d4ff]">{job.company.slice(0, 1)}</span><div><div className="eyebrow"><Target size={13}/>Explainable skills match</div><h1 className="mt-2 font-display text-2xl font-bold tracking-[-.05em]">{job.title}</h1><p className="mt-1 text-xs text-muted">{job.company} · {job.location}</p></div></div><section className="mt-7 rounded-2xl border border-line bg-panel p-5 sm:p-6"><div className="grid gap-4 sm:grid-cols-2"><div><label htmlFor="match-resume" className="mb-2 block text-xs font-semibold">Resume</label><select id="match-resume" value={resumeId} onChange={(event) => void chooseResume(event.target.value)} className="field-input">{!resumes.length && <option value="">No resumes uploaded</option>}{resumes.map((resume) => <option key={resume.id} value={resume.id}>{resume.title}</option>)}</select></div><div><label htmlFor="match-version" className="mb-2 block text-xs font-semibold">Resume version</label><select id="match-version" value={versionId} onChange={(event) => { setVersionId(event.target.value); setResult(null); }} className="field-input">{versionList.map((version) => <option key={version.id} value={version.id}>Version {version.versionNumber} · {version.originalFileName}</option>)}</select></div></div>{!resumes.length ? <div className="mt-5 rounded-xl border border-white/[.06] bg-white/[.02] p-4"><p className="text-xs font-semibold">Add your experience first</p><p className="mt-1 text-[10px] text-muted">Upload a resume to compare its reviewed skills with this role.</p><Link to="/app/resumes" className="mt-3 inline-flex items-center gap-1 text-[10px] font-semibold text-violet">Open resume studio <ArrowRight size={12}/></Link></div> : <button disabled={!versionId || busy} onClick={() => void runMatch()} className="button-primary mt-5">{busy ? 'Comparing skills…' : result ? 'Refresh match' : 'Compare resume to role'} <ArrowRight size={14}/></button>}</section>
      {result && <section className="mt-5 space-y-5"><div className="grid gap-4 lg:grid-cols-[.68fr_1.32fr]"><article className="rounded-2xl border border-violet/15 bg-gradient-to-br from-violet/[.1] to-panel p-6"><div className="flex items-center justify-between"><span className="text-[10px] font-bold uppercase tracking-[.16em] text-muted">Skills coverage</span><CircleHelp size={15} className="text-muted"/></div><div className="mt-4 flex items-end gap-2"><span className="font-display text-6xl font-extrabold tracking-[-.07em] text-white">{result.score}</span><span className="mb-2 text-sm text-muted">/100</span></div><div className="mt-4 h-2 overflow-hidden rounded-full bg-white/[.07]"><div className="h-full rounded-full bg-gradient-to-r from-violet to-cyan" style={{ width: `${result.score}%` }}/></div><p className="mt-4 text-[10px] leading-5 text-muted">{result.explanation}</p><p className="mt-3 text-[9px] text-[#747a90]">Formula: {result.formulaVersion}</p></article><article className="rounded-2xl border border-line bg-panel p-5"><h2 className="text-sm font-semibold">Required skills</h2><p className="mt-1 text-[10px] text-muted">Compare what the job lists with what your resume currently shows.</p><div className="mt-4 grid gap-2 sm:grid-cols-2">{result.matchedSkills.map((skill) => <div key={skill.skill} className="flex items-start gap-2 rounded-xl border border-mint/10 bg-mint/[.035] p-3"><Check size={14} className="mt-0.5 shrink-0 text-mint"/><div><p className="text-[11px] font-semibold">{skill.skill}</p><p className="mt-1 text-[9px] leading-4 text-muted">{skill.resumeEvidence}</p></div></div>)}{result.missingSkills.map((skill) => <div key={skill.skill} className="flex items-start gap-2 rounded-xl border border-amber/10 bg-amber/[.025] p-3"><Minus size={14} className="mt-0.5 shrink-0 text-amber"/><div><p className="text-[11px] font-semibold">{skill.skill}</p><p className="mt-1 text-[9px] leading-4 text-muted">Not detected in the selected resume version.</p></div></div>)}{!result.matchedSkills.length && !result.missingSkills.length && <p className="text-xs text-muted">This job does not list structured skills.</p>}</div></article></div>{result.recommendations.length > 0 && <article className="rounded-2xl border border-line bg-panel p-5"><h2 className="text-sm font-semibold">Ways to strengthen the evidence</h2><p className="mt-1 text-[10px] text-muted">Only add details that are true for your experience.</p><ul className="mt-4 space-y-3">{result.recommendations.map((item) => <li key={item} className="flex gap-3 text-[11px] leading-5 text-[#c8cada]"><span className="mt-2 size-1.5 shrink-0 rounded-full bg-violet"/>{item}</li>)}</ul></article>}<p className="text-center text-[9px] text-muted">A skills coverage indicator, not a prediction of hiring outcomes. Matching uses the selected resume version’s saved skill list.</p></section>}</>}{!job && !error && <div className="mt-8 text-sm text-muted">Job not found.</div>}</div>;
}
