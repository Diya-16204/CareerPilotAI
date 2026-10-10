import { useEffect, useMemo, useState } from 'react';
import { Download, FileText, History, Plus, Trash2, UploadCloud } from 'lucide-react';
import { apiMessage, careerApi } from '../api/client';
export function ResumesPage() {
    const [items, setItems] = useState([]);
    const [selectedId, setSelectedId] = useState('');
    const [resume, setResume] = useState(null);
    const [selectedVersionId, setSelectedVersionId] = useState('');
    const [file, setFile] = useState(null);
    const [title, setTitle] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [skills, setSkills] = useState('');
    const [busy, setBusy] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const versions = resume?.versions ?? [];
    const currentVersion = useMemo(() => versions.find((version) => version.id === selectedVersionId) ?? versions[0] ?? null, [versions, selectedVersionId]);
    async function refresh(nextId) {
        const next = await careerApi.resumes();
        setItems(next);
        const id = nextId ?? selectedId;
        if (id && next.some((entry) => entry.id === id)) {
            const detail = await careerApi.resume(id);
            setResume(detail);
            setSelectedId(id);
            setSelectedVersionId(detail.versions[0]?.id ?? '');
        }
        else if (!next.length) {
            setSelectedId('');
            setResume(null);
        }
        else if (!id || !next.some((entry) => entry.id === id)) {
            const detail = await careerApi.resume(next[0].id);
            setResume(detail);
            setSelectedId(next[0].id);
            setSelectedVersionId(detail.versions[0]?.id ?? '');
        }
    }
    useEffect(() => { let active = true; careerApi.resumes().then(async (result) => { if (!active)
        return; setItems(result); if (result[0]) {
        const detail = await careerApi.resume(result[0].id);
        if (active) {
            setResume(detail);
            setSelectedId(result[0].id);
            setSelectedVersionId(detail.versions[0]?.id ?? '');
        }
    } }).catch((reason) => { if (active)
        setError(apiMessage(reason)); }).finally(() => { if (active)
        setLoading(false); }); return () => { active = false; }; }, []);
    useEffect(() => { if (currentVersion) {
        setEmail(currentVersion.extractedEmail ?? '');
        setPhone(currentVersion.extractedPhone ?? '');
        setSkills(currentVersion.skills.join(', '));
    } }, [currentVersion]);
    async function upload(event) {
        event.preventDefault();
        if (!file) {
            setError('Choose a PDF or DOCX file first.');
            return;
        }
        setBusy(true);
        setError('');
        try {
            const result = await careerApi.uploadResume(file, title, selectedId || undefined);
            setFile(null);
            setTitle('');
            setResume(result);
            setSelectedId(result.id);
            setSelectedVersionId(result.versions[0]?.id ?? '');
            await refresh(result.id);
        }
        catch (reason) {
            setError(apiMessage(reason));
        }
        finally {
            setBusy(false);
        }
    }
    async function saveReview(event) {
        event.preventDefault();
        if (!resume || !currentVersion)
            return;
        setBusy(true);
        setError('');
        try {
            await careerApi.reviewResume(resume.id, currentVersion.id, { email: email.trim() || null, phone: phone.trim() || null, skills: skills.split(',').map((skill) => skill.trim()).filter(Boolean) });
            await refresh(resume.id);
        }
        catch (reason) {
            setError(apiMessage(reason));
        }
        finally {
            setBusy(false);
        }
    }
    async function download() {
        if (!resume || !currentVersion)
            return;
        try {
            const blob = await careerApi.downloadResume(resume.id, currentVersion.id);
            const url = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = currentVersion.originalFileName;
            anchor.click();
            URL.revokeObjectURL(url);
        }
        catch (reason) {
            setError(apiMessage(reason));
        }
    }
    async function remove() {
        if (!resume || !window.confirm(`Delete “${resume.title}” and its versions? This cannot be undone.`))
            return;
        setBusy(true);
        setError('');
        try {
            await careerApi.deleteResume(resume.id);
            setResume(null);
            setSelectedId('');
            await refresh('');
        }
        catch (reason) {
            setError(apiMessage(reason));
        }
        finally {
            setBusy(false);
        }
    }
    return <div><div className="eyebrow"><FileText size={13}/>Your experience, clearly told</div><div className="mt-3 flex flex-wrap items-end justify-between gap-4"><div><h1 className="font-display text-3xl font-bold tracking-[-.055em]">Resume studio</h1><p className="mt-2 max-w-xl text-sm leading-6 text-muted">Upload a PDF or DOCX, review what was extracted, and keep each version organized.</p></div><span className="text-xs text-muted">{items.length} {items.length === 1 ? 'resume' : 'resumes'}</span></div>
    {error && <div className="mt-6 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs leading-5 text-rose-200" role="alert">{error}</div>}
    <form onSubmit={upload} className="mt-7 grid gap-4 rounded-2xl border border-line bg-panel p-5 sm:grid-cols-[1.1fr_.8fr_auto] sm:items-end"><div><label htmlFor="resume-file" className="mb-2 block text-xs font-semibold">Choose a resume</label><input id="resume-file" type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" onChange={(event) => setFile(event.target.files?.[0] ?? null)} className="field-input !h-auto min-h-[46px] cursor-pointer py-3 file:mr-3 file:rounded-md file:border-0 file:bg-violet/15 file:px-2.5 file:py-1.5 file:text-[10px] file:font-semibold file:text-[#c8c0ff]"/><p className="mt-1 text-[10px] text-muted">PDF or DOCX · max 5 MB · stored privately</p></div><div><label htmlFor="resume-title" className="mb-2 block text-xs font-semibold">Resume name <span className="text-muted">(optional)</span></label><input id="resume-title" value={title} onChange={(event) => setTitle(event.target.value)} maxLength={160} placeholder="e.g. Product designer" className="field-input"/></div><button type="submit" disabled={busy} className="button-primary !h-[46px]">{busy ? <span>Processing…</span> : <><UploadCloud size={15}/> {selectedId ? 'Upload new version' : 'Upload resume'}</>}</button></form>
    {items.length > 1 && <div className="mt-5 flex flex-wrap items-center gap-3"><label htmlFor="resume-choice" className="text-xs font-semibold text-[#d1d3df]">Review resume</label><select id="resume-choice" value={selectedId} onChange={async (event) => { const id = event.target.value; setSelectedId(id); try {
        const data = await careerApi.resume(id);
        setResume(data);
        setSelectedVersionId(data.versions[0]?.id ?? '');
    }
    catch (reason) {
        setError(apiMessage(reason));
    } }} className="field-input !h-10 max-w-sm">{items.map((item) => <option key={item.id} value={item.id}>{item.title} · {item.versionCount} {item.versionCount === 1 ? 'version' : 'versions'}</option>)}</select></div>}
    {loading ? <div className="mt-8 rounded-2xl border border-line bg-panel p-10 text-center text-xs text-muted">Loading your resumes…</div> : resume && currentVersion ? <div className="mt-6 grid gap-5 xl:grid-cols-[.76fr_1.24fr]"><section className="rounded-2xl border border-line bg-panel p-5"><div className="flex items-start justify-between gap-4"><div><p className="text-[10px] font-bold uppercase tracking-[.15em] text-muted">Resume version</p><h2 className="mt-2 font-display text-lg font-bold">{resume.title}</h2><p className="mt-1 text-[10px] text-muted">Version {currentVersion.versionNumber} · {currentVersion.originalFileName}</p></div><span className={`status-pill ${currentVersion.reviewed ? '!border-mint/20 !bg-mint/10 !text-mint' : ''}`}>{currentVersion.reviewed ? 'Reviewed' : 'Needs review'}</span></div>
      {versions.length > 1 && <div className="mt-5 border-t border-line pt-4"><label htmlFor="version-choice" className="mb-2 flex items-center gap-2 text-[10px] font-semibold text-muted"><History size={13}/>Version history</label><select id="version-choice" value={currentVersion.id} onChange={(event) => setSelectedVersionId(event.target.value)} className="field-input !h-10">{versions.map((version) => <option key={version.id} value={version.id}>Version {version.versionNumber} · {new Date(version.createdAt).toLocaleDateString()}</option>)}</select></div>}
      <div className="mt-5 grid grid-cols-2 gap-3"><div className="rounded-xl border border-white/[.06] bg-white/[.02] p-3"><p className="text-[9px] uppercase tracking-widest text-muted">Extracted email</p><p className="mt-2 truncate text-xs font-medium">{currentVersion.extractedEmail || 'Not found'}</p></div><div className="rounded-xl border border-white/[.06] bg-white/[.02] p-3"><p className="text-[9px] uppercase tracking-widest text-muted">Extracted phone</p><p className="mt-2 truncate text-xs font-medium">{currentVersion.extractedPhone || 'Not found'}</p></div></div>
      <div className="mt-4"><p className="text-[10px] font-semibold text-muted">Detected skills</p><div className="mt-2 flex flex-wrap gap-1.5">{currentVersion.skills.length ? currentVersion.skills.map((skill) => <span key={skill} className="rounded-full border border-white/[.08] bg-white/[.03] px-2.5 py-1 text-[9px] text-[#d1d3df]">{skill}</span>) : <span className="text-[10px] text-muted">No skills detected. Add skills you can verify below.</span>}</div></div>
      <div className="mt-5 flex flex-wrap gap-2 border-t border-line pt-4"><button type="button" onClick={() => void download()} className="button-quiet !h-9 rounded-lg border border-white/[.08] px-3 text-[10px]"><Download size={13}/> Download original</button><button type="button" disabled={busy} onClick={() => void remove()} className="button-quiet !h-9 rounded-lg border border-rose-300/10 px-3 text-[10px] text-rose-200"><Trash2 size={13}/> Delete resume</button></div></section>
      <div className="space-y-5"><form onSubmit={saveReview} className="rounded-2xl border border-line bg-panel p-5"><div><h2 className="text-sm font-semibold">Review extracted details</h2><p className="mt-1 text-[10px] leading-5 text-muted">Parsing can miss or misread facts. Correct these details before using them for matching.</p></div><div className="mt-5 grid gap-4 sm:grid-cols-2"><div><label htmlFor="review-email" className="mb-2 block text-[10px] font-semibold text-muted">Email</label><input id="review-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Add or correct email" className="field-input"/></div><div><label htmlFor="review-phone" className="mb-2 block text-[10px] font-semibold text-muted">Phone</label><input id="review-phone" value={phone} onChange={(event) => setPhone(event.target.value)} placeholder="Add or correct phone" className="field-input"/></div><div className="sm:col-span-2"><label htmlFor="review-skills" className="mb-2 block text-[10px] font-semibold text-muted">Skills <span className="font-normal">(comma separated)</span></label><input id="review-skills" value={skills} onChange={(event) => setSkills(event.target.value)} placeholder="Java, React, SQL" className="field-input"/><p className="mt-1 text-[9px] text-muted">Only include skills you can support with real experience.</p></div></div><button type="submit" disabled={busy} className="button-primary mt-5 !h-9 !px-4 !text-[10px]">Save reviewed details</button></form>
      <details className="rounded-2xl border border-line bg-panel"><summary className="cursor-pointer list-none px-5 py-4 text-xs font-semibold marker:hidden">Extracted text preview <span className="ml-2 text-[10px] font-normal text-muted">Review for parsing errors</span></summary><pre className="max-h-80 overflow-auto whitespace-pre-wrap border-t border-line px-5 py-4 font-sans text-[10px] leading-5 text-[#b8bbcb]">{currentVersion.extractedText}</pre></details></div></div> : !error && <div className="mt-7 rounded-2xl border border-dashed border-line bg-panel/50 px-6 py-14 text-center"><span className="mx-auto grid size-12 place-items-center rounded-2xl border border-white/[.07] bg-white/[.025] text-violet"><Plus size={20}/></span><p className="mt-4 text-sm font-semibold">Start with the story you already have</p><p className="mx-auto mt-2 max-w-sm text-xs leading-5 text-muted">Upload a text-based PDF or DOCX. We’ll extract editable details and keep the original file private.</p></div>}
    <p className="mt-6 text-[10px] leading-5 text-muted">Uploaded files stay outside public web folders and are only available to your account. Never upload a document you do not have permission to store.</p>
  </div>;
}
