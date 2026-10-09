import { useEffect, useState } from 'react'
import { Bookmark, MapPin, Search, SlidersHorizontal, Target, BriefcaseBusiness, Plus, X } from 'lucide-react'
import { Link } from 'react-router-dom'
import { careerApi, apiMessage } from '../api/client'
import type { Job } from '../api/types'

export function JobsPage() {
  const [query, setQuery] = useState('')
  const [jobs, setJobs] = useState<Job[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState<string | null>(null)
  const [tracking, setTracking] = useState<string | null>(null)
  const [notice, setNotice] = useState('')
  const [importOpen, setImportOpen] = useState(false)
  const [importing, setImporting] = useState(false)
  const [draft, setDraft] = useState({ title: '', company: '', location: '', employmentType: 'FULL_TIME', description: '', skills: '' })
  async function load(search: string) {
    setLoading(true); setError('')
    try { const page = await careerApi.jobs({ q: search || undefined, page: 0, size: 12 }); setJobs(page.items); setTotal(page.totalItems) }
    catch (reason) { setError(apiMessage(reason)) }
    finally { setLoading(false) }
  }
  useEffect(() => { void load('') }, [])
  async function toggleSave(job: Job) {
    setSaving(job.id)
    try { await careerApi.saveJob(job.id); setJobs((items) => items.map((item) => item.id === job.id ? { ...item, saved: !item.saved } : item)) }
    catch (reason) { setError(apiMessage(reason)) }
    finally { setSaving(null) }
  }
  async function trackJob(job: Job) {
    setTracking(job.id); setNotice(''); setError('')
    try { await careerApi.createApplication({ jobId: job.id, title: job.title, company: job.company, status: 'SAVED' }); setNotice(`${job.title} added to your application tracker.`) }
    catch (reason) { setError(apiMessage(reason)) }
    finally { setTracking(null) }
  }
  async function importJob(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); setImporting(true); setNotice(''); setError('')
    try {
      const job = await careerApi.importJob({ ...draft, skills: draft.skills.split(',').map((skill) => skill.trim()).filter(Boolean) })
      setJobs((items) => [job, ...items]); setTotal((count) => count + 1); setDraft({ title: '', company: '', location: '', employmentType: 'FULL_TIME', description: '', skills: '' }); setImportOpen(false); setNotice('Your job description was added to your private opportunities.')
    } catch (reason) { setError(apiMessage(reason)) }
    finally { setImporting(false) }
  }
  return <div><div className="eyebrow"><span className="size-1.5 rounded-full bg-cyan"/>Find your fit</div><div className="mt-3 flex flex-wrap items-end justify-between gap-4"><div><h1 className="font-display text-3xl font-bold tracking-[-.055em]">Explore opportunities</h1><p className="mt-2 text-sm text-muted">A thoughtful shortlist of roles to help you move forward.</p></div><span className="text-xs text-muted">{loading ? 'Loading…' : `${total} roles`}</span></div>
    <div className="mt-7 flex flex-wrap gap-2"><form className="flex min-w-[260px] flex-1 gap-2" onSubmit={(event) => { event.preventDefault(); void load(query) }}><div className="relative flex-1"><Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted"/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Try a role, skill, or company" className="field-input !h-12 !pl-11" aria-label="Search jobs"/></div><button className="button-primary !h-12 !px-5" type="submit">Search</button><button className="icon-button !h-12 !w-12" type="button" disabled title="More filters are coming soon" aria-label="More filters"><SlidersHorizontal size={17}/></button></form><button className="button-secondary !h-12" type="button" onClick={() => setImportOpen((value) => !value)}>{importOpen ? <X size={15}/> : <Plus size={15}/>} Add job description</button></div>
    {importOpen && <form onSubmit={(event) => void importJob(event)} className="mt-4 grid gap-3 rounded-2xl border border-violet/20 bg-panel p-5 sm:grid-cols-2" aria-label="Add a job description"><h2 className="font-display text-sm font-bold sm:col-span-2">Add a job description</h2><input required maxLength={160} className="field-input" placeholder="Role title" value={draft.title} onChange={(e)=>setDraft({...draft,title:e.target.value})}/><input required maxLength={160} className="field-input" placeholder="Company" value={draft.company} onChange={(e)=>setDraft({...draft,company:e.target.value})}/><input required maxLength={160} className="field-input" placeholder="Location" value={draft.location} onChange={(e)=>setDraft({...draft,location:e.target.value})}/><select className="field-input" value={draft.employmentType} onChange={(e)=>setDraft({...draft,employmentType:e.target.value})}><option value="FULL_TIME">Full time</option><option value="PART_TIME">Part time</option><option value="CONTRACT">Contract</option><option value="INTERNSHIP">Internship</option><option value="TEMPORARY">Temporary</option></select><textarea required maxLength={10000} rows={5} className="field-input sm:col-span-2" placeholder="Paste the job description" value={draft.description} onChange={(e)=>setDraft({...draft,description:e.target.value})}/><input className="field-input sm:col-span-2" placeholder="Skills, separated by commas" value={draft.skills} onChange={(e)=>setDraft({...draft,skills:e.target.value})}/><p className="text-[10px] leading-5 text-muted sm:col-span-2">This stores the text you provide in your private account. CareerPilot does not scrape job sites.</p><div className="sm:col-span-2"><button disabled={importing} className="button-primary">{importing?'Saving…':'Save job description'}</button></div></form>}
    {error && <div className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs leading-5 text-rose-200" role="alert">{error}</div>}{notice&&<div className="mt-5 rounded-xl border border-mint/15 bg-mint/[.06] px-4 py-3 text-xs text-mint" role="status">{notice}</div>}
    {loading ? <div className="mt-8 grid gap-4 md:grid-cols-2 xl:grid-cols-3" aria-label="Loading jobs">{[1,2,3,4,5,6].map((n)=><div key={n} className="h-52 animate-pulse rounded-2xl border border-line bg-panel/70"/>)}</div> : jobs.length ? <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">{jobs.map((job)=><article key={job.id} className="flex min-h-[250px] flex-col rounded-2xl border border-line bg-panel p-5 transition hover:-translate-y-0.5 hover:border-violet/25"><div className="flex items-start justify-between gap-3"><span className="grid size-10 place-items-center rounded-xl bg-gradient-to-br from-violet/15 to-cyan/10 font-display text-sm font-bold text-[#d9d4ff]">{job.company.slice(0,1)}</span><button disabled={saving===job.id} onClick={()=>void toggleSave(job)} className={`icon-button !h-8 !w-8 ${job.saved?'!text-violet':''}`} aria-label={job.saved?'Unsave job':'Save job'} title={job.saved?'Saved':'Save job'}><Bookmark size={15} fill={job.saved?'currentColor':'none'}/></button></div><h2 className="mt-4 font-display text-base font-bold tracking-tight">{job.title}</h2><p className="mt-1 text-xs text-[#c2c4d2]">{job.company}</p><p className="mt-2 flex items-center gap-1.5 text-[10px] text-muted"><MapPin size={12}/>{job.location} <span className="mx-1 text-line">·</span>{job.employmentType}</p><p className="mt-3 line-clamp-2 text-[11px] leading-5 text-muted">{job.description}</p><div className="mt-auto flex flex-wrap items-center gap-2 pt-4"><span className="rounded-full border border-white/[.08] px-2.5 py-1 text-[9px] text-muted">{job.source}</span>{job.sample && <span className="rounded-full bg-amber/10 px-2.5 py-1 text-[9px] font-semibold text-amber">Sample listing</span>}<div className="flex w-full items-center justify-between border-t border-white/[.06] pt-3"><button type="button" disabled={tracking===job.id} onClick={()=>void trackJob(job)} className="inline-flex h-8 items-center gap-1.5 rounded-lg border border-white/[.08] px-2.5 text-[9px] font-semibold text-[#d0d2df] transition hover:border-violet/30 hover:text-white"><BriefcaseBusiness size={12}/>{tracking===job.id?'Adding…':'Track this role'}</button><Link to={`/app/matches/new?jobId=${encodeURIComponent(job.id)}`} className="inline-flex h-8 items-center gap-1.5 rounded-lg border border-violet/20 bg-violet/[.07] px-2.5 text-[9px] font-semibold text-[#c9c1ff] transition hover:bg-violet/15"><Target size={12}/> Match resume</Link></div></div></article>)}</div> : !error && <div className="mt-8 rounded-2xl border border-line bg-panel px-6 py-14 text-center"><span className="mx-auto grid size-12 place-items-center rounded-2xl bg-white/[.035] text-muted"><Search size={20}/></span><p className="mt-4 text-sm font-semibold">No matching roles just yet</p><p className="mt-2 text-xs text-muted">Try a different search or check back as more opportunities are added.</p></div>}
    <p className="mt-6 text-center text-[10px] text-muted">Listings are sourced from user submissions and labeled sample data. CareerPilot does not scrape restricted sites.</p>
  </div>
}
