import { useEffect, useRef, useState } from 'react';
import { Check, Copy, Download, Linkedin, MessageCircle, Send, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { apiMessage, careerApi } from '../api/client';

const relationshipOpeners = {
    known: 'It was great connecting with you recently.',
    mutual: 'I found your profile through our shared network.',
    new: 'I came across your profile while learning about the team at {company}.',
};

function personalize(opener, company) {
    return opener.replace('{company}', company);
}

function createMessages({ recipient, role, company, jobId, jobUrl, resumeUrl, relationship, fit, sender }) {
    const opener = personalize(relationshipOpeners[relationship], company);
    const fitSentence = fit.trim() ? `My experience with ${fit.trim()} seems relevant to the role.` : '';
    const references = [
        `Job ID: ${jobId}`,
        jobUrl.trim() ? `Job posting: ${jobUrl.trim()}` : '',
        resumeUrl.trim() ? `Resume: ${resumeUrl.trim()}` : '',
    ].filter(Boolean).join('\n');
    const linkedIn = [
        `Hi ${recipient},`,
        `${opener} I’m preparing to apply for the ${role} position at ${company}.`,
        fitSentence,
        references,
        `If you feel my background could be a fit, would you be comfortable referring me? I’d be happy to share my resume and the job link. No pressure if you’re unable to.`,
        `Thank you for considering it,\n${sender || 'Your name'}`,
    ].filter(Boolean).join('\n\n');
    const instagram = [
        `Hi ${recipient}! ${opener}`,
        `I’m interested in the ${role} role at ${company}.${fitSentence ? ` ${fitSentence}` : ''}`,
        references,
        `If you’re comfortable, could you refer me or point me to the right person? I can send my resume and the role link. No worries if not — thank you!`,
        sender || 'Your name',
    ].filter(Boolean).join('\n\n');
    return { linkedIn, instagram };
}

export function ReferralPage() {
    const { user } = useAuth();
    const [savedJobs, setSavedJobs] = useState([]);
    const [jobsLoading, setJobsLoading] = useState(true);
    const [resumes, setResumes] = useState([]);
    const [resumeDetail, setResumeDetail] = useState(null);
    const [resumeLoading, setResumeLoading] = useState(true);
    const [resumeBusy, setResumeBusy] = useState(false);
    const [resumeId, setResumeId] = useState('');
    const [resumeVersionId, setResumeVersionId] = useState('');
    const [form, setForm] = useState({ recipient: '', jobId: '', jobUrl: '', resumeUrl: '', relationship: 'new', fit: '' });
    const [messages, setMessages] = useState(null);
    const [error, setError] = useState('');
    const [copied, setCopied] = useState('');

    useEffect(() => {
        let active = true;
        careerApi.jobs({ page: 0, size: 100 }).then(page => {
            if (!active) return;
            const saved = page.items.filter(job => job.saved);
            setSavedJobs(saved);
            setForm(current => ({ ...current, jobId: saved[0]?.id ?? '' }));
        }).catch(reason => {
            if (active) setError(apiMessage(reason));
        }).finally(() => {
            if (active) setJobsLoading(false);
        });
        return () => { active = false; };
    }, []);

    useEffect(() => {
        let active = true;
        careerApi.resumes().then(async items => {
            if (!active) return;
            setResumes(items);
            const first = items[0];
            if (first) {
                const detail = await careerApi.resume(first.id);
                if (!active) return;
                setResumeId(first.id);
                setResumeDetail(detail);
                setResumeVersionId(detail.versions.find(version => version.versionNumber === detail.activeVersionNumber)?.id ?? detail.versions[0]?.id ?? '');
            }
        }).catch(reason => {
            if (active) setError(apiMessage(reason));
        }).finally(() => {
            if (active) setResumeLoading(false);
        });
        return () => { active = false; };
    }, []);

    function update(field, value) {
        setForm(current => ({ ...current, [field]: value }));
        if (field === 'jobId') setMessages(null);
    }

    async function chooseResume(id) {
        setResumeId(id);
        setResumeDetail(null);
        setResumeVersionId('');
        setMessages(null);
        if (!id) return;
        setResumeBusy(true);
        setError('');
        try {
            const detail = await careerApi.resume(id);
            setResumeDetail(detail);
            setResumeVersionId(detail.versions.find(version => version.versionNumber === detail.activeVersionNumber)?.id ?? detail.versions[0]?.id ?? '');
        } catch (reason) {
            setError(apiMessage(reason));
        } finally {
            setResumeBusy(false);
        }
    }

    async function downloadSelectedResume() {
        const version = resumeDetail?.versions.find(item => item.id === resumeVersionId);
        if (!resumeId || !version) return;
        setResumeBusy(true);
        setError('');
        try {
            const blob = await careerApi.downloadResume(resumeId, resumeVersionId);
            const url = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = version.originalFileName;
            anchor.click();
            window.setTimeout(() => URL.revokeObjectURL(url), 1000);
        } catch (reason) {
            setError(apiMessage(reason));
        } finally {
            setResumeBusy(false);
        }
    }

    function generate() {
        const job = savedJobs.find(item => item.id === form.jobId);
        if (!form.recipient.trim() || !job) {
            setError('Add the person’s name and choose one of your saved jobs first.');
            setMessages(null);
            return;
        }
        for (const [label, value] of [['Job posting link', form.jobUrl], ['Resume link', form.resumeUrl]]) {
            if (value.trim()) {
                try {
                    const url = new URL(value.trim());
                    if (!['http:', 'https:'].includes(url.protocol)) throw new Error('Unsupported protocol');
                } catch {
                    setError(`${label} must be a valid http:// or https:// link.`);
                    setMessages(null);
                    return;
                }
            }
        }
        setError('');
        setCopied('');
        setMessages(createMessages({ ...form, role: job.title, company: job.company, sender: user?.name?.trim() }));
    }

    async function copy(platform) {
        try {
            await navigator.clipboard.writeText(messages[platform]);
            setCopied(platform);
            window.setTimeout(() => setCopied(current => current === platform ? '' : current), 1800);
        } catch {
            setError('Clipboard access is unavailable. Select and copy the message manually.');
        }
    }

    return <div>
        <div className="eyebrow"><Sparkles size={13}/>Make a thoughtful introduction</div>
        <h1 className="mt-3 font-display text-3xl font-bold tracking-[-.055em]">Referral message studio</h1>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-muted">Create separate, editable referral requests for LinkedIn and Instagram. Keep the details honest, add the job link yourself, and give the person room to say no.</p>

        {error && <div role="alert" className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs text-rose-200">{error}</div>}

        <div className="mt-7 grid gap-5 xl:grid-cols-[.7fr_1.3fr]">
            <section className="h-fit rounded-2xl border border-line bg-panel p-5">
                <div className="flex items-center gap-2"><span className="grid size-8 place-items-center rounded-lg bg-violet/10 text-violet"><MessageCircle size={15}/></span><h2 className="text-xs font-semibold">A little context</h2></div>
                <div className="mt-5 space-y-4">
                    <div><label htmlFor="referral-recipient" className="mb-2 block text-[10px] font-semibold text-muted">Who are you messaging? *</label><input id="referral-recipient" className="field-input" maxLength={80} placeholder="Their name" value={form.recipient} onChange={event => update('recipient', event.target.value)}/></div>
                    <div><label htmlFor="referral-job" className="mb-2 block text-[10px] font-semibold text-muted">Which saved job is this referral for? *</label><select id="referral-job" className="field-input" value={form.jobId} disabled={jobsLoading || savedJobs.length === 0} onChange={event => update('jobId', event.target.value)}><option value="">{jobsLoading ? 'Loading saved jobs…' : savedJobs.length ? 'Choose a saved job' : 'No saved jobs yet'}</option>{savedJobs.map(job => <option key={job.id} value={job.id}>{job.title} · {job.company}</option>)}</select>{!jobsLoading && savedJobs.length === 0 && <p className="mt-2 text-[10px] leading-5 text-muted">Save a role first from <Link className="font-semibold text-violet hover:text-white" to="/app/jobs">Explore jobs</Link>, then it will appear here.</p>}</div>
                    {savedJobs.find(job => job.id === form.jobId) && <div className="grid gap-3 rounded-xl border border-white/[.07] bg-white/[.025] p-3 sm:grid-cols-2">
                        <ReadOnlyField label="Job ID" value={savedJobs.find(job => job.id === form.jobId).id}/>
                        <ReadOnlyField label="Job role" value={savedJobs.find(job => job.id === form.jobId).title}/>
                        <ReadOnlyField label="Company" value={savedJobs.find(job => job.id === form.jobId).company}/>
                        <ReadOnlyField label="Location" value={savedJobs.find(job => job.id === form.jobId).location}/>
                    </div>}
                    <div><label htmlFor="referral-job-url" className="mb-2 block text-[10px] font-semibold text-muted">Job posting link (optional)</label><input id="referral-job-url" type="url" className="field-input" maxLength={1000} placeholder="https://company.com/jobs/…" value={form.jobUrl} onChange={event => update('jobUrl', event.target.value)}/></div>
                    <div><label htmlFor="referral-resume" className="mb-2 block text-[10px] font-semibold text-muted">Resume to attach</label><select id="referral-resume" className="field-input" value={resumeId} disabled={resumeLoading || resumeBusy || resumes.length === 0} onChange={event => void chooseResume(event.target.value)}><option value="">{resumeLoading ? 'Loading your resumes…' : resumes.length ? 'Choose a resume' : 'No uploaded resumes'}</option>{resumes.map(resume => <option key={resume.id} value={resume.id}>{resume.title} · {resume.latestFileName}</option>)}</select>
                        {resumeDetail?.versions?.length > 1 && <div className="mt-3"><label htmlFor="referral-resume-version" className="mb-2 block text-[10px] font-semibold text-muted">Resume version</label><select id="referral-resume-version" className="field-input" value={resumeVersionId} disabled={resumeBusy} onChange={event => { setResumeVersionId(event.target.value); setMessages(null); }}>
                            {resumeDetail.versions.map(version => <option key={version.id} value={version.id}>Version {version.versionNumber} · {version.originalFileName}</option>)}
                        </select></div>}
                        {resumeId && resumeVersionId && <button type="button" disabled={resumeBusy} onClick={() => void downloadSelectedResume()} className="button-secondary mt-3 !h-9 !px-3 !text-[10px]"><Download size={13}/>{resumeBusy ? 'Preparing…' : 'Download this resume to attach'}</button>}
                        <p className="mt-1.5 text-[9px] leading-4 text-muted">Download the selected file, then attach it yourself in LinkedIn or Instagram. CareerPilot cannot send DMs or attach files to those platforms.</p>
                    </div>
                    <div><label htmlFor="referral-resume-url" className="mb-2 block text-[10px] font-semibold text-muted">Public resume link (optional)</label><input id="referral-resume-url" type="url" className="field-input" maxLength={1000} placeholder="https://drive.google.com/…" value={form.resumeUrl} onChange={event => update('resumeUrl', event.target.value)}/><p className="mt-1.5 text-[9px] leading-4 text-muted">Only use a link the recipient is allowed to access; the private CareerPilot download requires your login.</p></div>
                    <div><label htmlFor="referral-relationship" className="mb-2 block text-[10px] font-semibold text-muted">How do you know them?</label><select id="referral-relationship" className="field-input" value={form.relationship} onChange={event => update('relationship', event.target.value)}><option value="new">We haven’t met yet</option><option value="mutual">We share a network</option><option value="known">We’ve spoken before</option></select></div>
                    <div><label htmlFor="referral-fit" className="mb-2 block text-[10px] font-semibold text-muted">Relevant experience (optional)</label><textarea id="referral-fit" className="field-input min-h-24 resize-y" maxLength={300} placeholder="e.g. Java, Spring Boot, and building REST APIs" value={form.fit} onChange={event => update('fit', event.target.value)}/></div>
                    <button type="button" onClick={generate} className="button-primary w-full"><Send size={14}/>Generate both messages</button>
                    <p className="text-[9px] leading-4 text-muted">Job ID, role, company, and location come from your saved job. Drafts are generated on this device using templates; nothing is sent automatically or stored by this page.</p>
                </div>
            </section>

            <div className="space-y-5">
                <MessageCard title="LinkedIn message" hint="Professional and concise" icon={<Linkedin size={15}/>} value={messages?.linkedIn} copied={copied === 'linkedIn'} onCopy={() => void copy('linkedIn')} onChange={value => setMessages(current => ({ ...current, linkedIn: value }))}/>
                <MessageCard title="Instagram DM" hint="Warmer and shorter" icon={<MessageCircle size={15}/>} value={messages?.instagram} copied={copied === 'instagram'} onCopy={() => void copy('instagram')} onChange={value => setMessages(current => ({ ...current, instagram: value }))}/>
                {!messages && <div className="rounded-2xl border border-line bg-panel px-6 py-8 text-center text-xs text-muted">Fill in the details and generate both drafts. You can edit each message before copying.</div>}
            </div>
        </div>
    </div>;
}

function MessageCard({ title, hint, icon, value, copied, onCopy, onChange }) {
    const textarea = useRef(null);
    useEffect(() => {
        if (!textarea.current) return;
        textarea.current.style.height = 'auto';
        textarea.current.style.height = `${textarea.current.scrollHeight}px`;
    }, [value]);

    return <section className="rounded-2xl border border-line bg-panel p-5 sm:p-6">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line pb-4"><div className="flex items-center gap-3"><span className="grid size-9 place-items-center rounded-xl bg-violet/10 text-violet">{icon}</span><div><h2 className="text-sm font-semibold">{title}</h2><p className="mt-1 text-[10px] text-muted">{hint}</p></div></div><button type="button" disabled={!value} onClick={onCopy} className="button-secondary !h-9 !px-3 !text-[10px]"><span className="inline-flex items-center gap-1.5">{copied ? <Check size={13}/> : <Copy size={13}/>} {copied ? 'Copied' : 'Copy message'}</span></button></div>
        {value ? <><label htmlFor={`message-${title.replaceAll(' ', '-').toLowerCase()}`} className="sr-only">Edit {title}</label><textarea ref={textarea} id={`message-${title.replaceAll(' ', '-').toLowerCase()}`} value={value} onChange={event => onChange(event.target.value)} maxLength={2000} rows={1} className="mt-4 min-h-44 w-full resize-none overflow-hidden rounded-xl border border-white/[.07] bg-[#0d0f19] p-4 text-xs leading-6 text-[#e4e4ee] outline-none focus:border-violet/50"/><p className="mt-2 text-right text-[9px] text-muted">{value.length}/2,000 characters</p></> : <p className="mt-5 text-xs text-muted">Your {title} draft will appear here.</p>}
    </section>;
}

function ReadOnlyField({ label, value }) {
    return <div className="min-w-0"><span className="block text-[9px] font-semibold uppercase tracking-wide text-muted">{label}</span><span className="mt-1 block truncate text-[10px] text-white/85" title={value}>{value}</span></div>;
}
