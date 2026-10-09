import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { ArrowLeft, ArrowRight, BadgeCheck, BriefcaseBusiness, FileUp, ShieldCheck } from 'lucide-react'
import { apiMessage, careerApi } from '../api/client'
import type { PortalJob } from '../api/types'

type PortalKey = 'PORTAL_A' | 'PORTAL_B' | 'PORTAL_C'
const portalConfig: Record<PortalKey,{name:string; tagline:string; accent:string}> = {
  PORTAL_A:{name:'Northstar Careers Simulation',tagline:'A standard application flow',accent:'border-cyan/30'},
  PORTAL_B:{name:'Waypoint Talent Simulation',tagline:'A guided multi-step application',accent:'border-violet/30'},
  PORTAL_C:{name:'Craftwork Careers Simulation',tagline:'A role-specific application',accent:'border-mint/30'},
}
const inputClass='field-input'

export function SimulatedPortalPage() {
  const { portal: routePortal } = useParams()
  const portal=(routePortal?.toUpperCase() ?? 'PORTAL_A') as PortalKey
  const config=portalConfig[portal]
  const [jobs,setJobs]=useState<PortalJob[]>([])
  const [job,setJob]=useState<PortalJob|null>(null)
  const [stage,setStage]=useState(0)
  const [name,setName]=useState(''); const [email,setEmail]=useState('')
  const [education,setEducation]=useState(''); const [experience,setExperience]=useState('')
  const [coverLetter,setCoverLetter]=useState('')
  const [file,setFile]=useState<File|null>(null); const [answers,setAnswers]=useState<Record<string,string>>({})
  const [consent,setConsent]=useState(false); const [busy,setBusy]=useState(false)
  const [idempotencyKey]=useState(()=>crypto.randomUUID())
  const [error,setError]=useState(''); const [message,setMessage]=useState('')
  const [confirmation,setConfirmation]=useState<string|null>(null)

  useEffect(()=>{ if(!config){setError('Unknown simulated career portal.');return} void careerApi.portalJobs(portal).then(setJobs).catch(reason=>setError(apiMessage(reason))) },[portal,config])
  if(!config)return <main className="grid min-h-screen place-items-center bg-ink p-6 text-white"><p>Unknown simulated portal.</p></main>

  const wizard=portal==='PORTAL_B'
  const review=stage===(wizard?3:1)
  const currentQuestion=job?.questions??[]
  async function next(event:React.FormEvent<HTMLFormElement>){event.preventDefault();setError('');if(stage<(wizard?2:0))setStage(stage+1);else setStage(wizard?3:1)}
  async function submit(){
    if(!job||!file){setError('Choose a job and attach a PDF or DOCX resume before submitting.');return}
    if(!consent){setError('Confirm that you want to submit this simulated application.');return}
    if(currentQuestion.some((question)=>!answers[question]?.trim())){setError('Answer each screening question before submission.');return}
    setBusy(true);setError('');setMessage('')
    try{
      const encoded=await new Promise<string>((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(String(reader.result).split(',')[1]??'');reader.onerror=()=>reject(new Error('Resume could not be read.'));reader.readAsDataURL(file)})
      const saved=await careerApi.submitPortalApplication(portal,idempotencyKey,{jobKey:job.key,applicantName:name,applicantEmail:email,education:wizard?education:null,experience:wizard?experience:null,coverLetter:portal==='PORTAL_A'?coverLetter:null,resumeFileName:file.name,resumeBase64:encoded,answers,consent})
      const verified=await careerApi.verifyPortalSubmission(saved.confirmationId)
      if(verified.confirmationId!==saved.confirmationId||verified.portalKey!==portal||verified.jobKey!==job.key||verified.status!=='PERSISTED')throw new Error('The portal returned a confirmation, but the independent record check did not match.')
      setConfirmation(verified.confirmationId)
    }catch(reason){setError(reason instanceof Error&&reason.name==='Error'?reason.message:apiMessage(reason))}
    finally{setBusy(false)}
  }

  if(confirmation)return <main className="min-h-screen bg-ink px-5 py-12 text-white"><section className="mx-auto max-w-xl rounded-3xl border border-mint/20 bg-panel p-8 text-center"><span className="mx-auto grid size-14 place-items-center rounded-2xl bg-mint/10 text-mint"><BadgeCheck/></span><p className="eyebrow mt-5 justify-center"><span className="size-1.5 rounded-full bg-mint"/>Simulation complete</p><h1 className="mt-3 font-display text-3xl font-bold">Application verified</h1><p className="mt-3 text-sm text-muted">This is a local career portal simulation. No real employer was contacted.</p><div className="mt-6 rounded-xl border border-line bg-ink p-4 text-left"><p className="text-[10px] uppercase tracking-wider text-muted">Persisted confirmation ID</p><p className="mt-2 break-all font-mono text-sm text-mint">{confirmation}</p><p className="mt-2 text-xs text-muted">{config.name} · {job?.title}</p></div><a href="/" className="button-secondary mx-auto mt-6">Return to CareerPilot</a></section></main>

  return <main className="min-h-screen bg-[#0a0c14] px-4 py-8 text-white sm:px-8"><div className="mx-auto max-w-3xl"><div className="flex flex-wrap items-center justify-between gap-4"><a href="/" className="font-display text-lg font-bold tracking-tight">CareerPilot <span className="text-violet">AI</span></a><span className="rounded-full border border-amber/20 bg-amber/[.07] px-3 py-1.5 text-[10px] font-bold uppercase tracking-[.16em] text-amber">Simulation · No real employer</span></div>
    <section className={`mt-8 rounded-3xl border ${config.accent} bg-panel p-6 sm:p-9`}><p className="eyebrow"><span className="size-1.5 rounded-full bg-cyan"/>{config.name}</p><h1 className="mt-3 font-display text-3xl font-bold tracking-tight">{config.tagline}</h1><p className="mt-2 text-sm text-muted">A test-only portal. Submissions are stored locally for verification.</p>
      {!job&&<div className="mt-8"><h2 className="text-sm font-semibold">Select a sample opening</h2><div className="mt-3 grid gap-3">{jobs.map((item)=><button key={item.key} type="button" onClick={()=>setJob(item)} className="rounded-2xl border border-line bg-ink p-5 text-left transition hover:border-violet/40"><span className="flex items-center gap-2 text-[10px] font-semibold uppercase tracking-wider text-cyan"><BriefcaseBusiness size={13}/>{item.department} · {item.location}</span><span className="mt-2 block font-display text-lg font-bold">{item.title}</span><span className="mt-2 block text-xs leading-5 text-muted">{item.description}</span><span className="mt-3 block text-[10px] text-muted">Requirements: {item.requirements.join(' · ')}</span></button>)}</div></div>}
      {job&&<><div className="mt-6 flex items-center justify-between border-b border-line pb-4"><div><p className="text-[10px] uppercase tracking-wider text-muted">Selected simulation role</p><p className="mt-1 font-semibold">{job.title} <span className="font-normal text-muted">· {job.location}</span></p></div><button type="button" onClick={()=>{setJob(null);setStage(0)}} className="text-xs text-violet">Change role</button></div>
      {wizard&&<div className="mt-5 flex gap-2" aria-label="Application progress">{['Profile','Background','Screening','Review'].map((label,index)=><span key={label} className={`h-1 flex-1 rounded-full ${index<=stage?'bg-violet':'bg-white/10'}`} title={label}/>)}</div>}
      {!review?<form className="mt-6 space-y-4" onSubmit={(event)=>void next(event)}>
        {(!wizard||stage===0)&&<><h2 className="font-display text-lg font-bold">{wizard?'Profile details':'Contact information'}</h2><label className="block text-xs text-muted">Full name<input required maxLength={120} autoComplete="name" className={`${inputClass} mt-2`} value={name} onChange={(e)=>setName(e.target.value)}/></label><label className="block text-xs text-muted">Email address<input required type="email" maxLength={254} autoComplete="email" className={`${inputClass} mt-2`} value={email} onChange={(e)=>setEmail(e.target.value)}/></label>{portal==='PORTAL_A'&&<label className="block text-xs text-muted">Cover letter<textarea required maxLength={4000} rows={6} className={`${inputClass} mt-2`} placeholder="Tell us why you are interested in this role" value={coverLetter} onChange={(e)=>setCoverLetter(e.target.value)}/></label>}</>}
        {wizard&&stage===1&&<><h2 className="font-display text-lg font-bold">Education and experience</h2><label className="block text-xs text-muted">Highest completed education<input required maxLength={300} className={`${inputClass} mt-2`} value={education} onChange={(e)=>setEducation(e.target.value)}/></label><label className="block text-xs text-muted">Relevant experience<textarea required maxLength={2000} rows={4} className={`${inputClass} mt-2`} value={experience} onChange={(e)=>setExperience(e.target.value)}/></label></>}
        {(!wizard||stage===2)&&<><h2 className="font-display text-lg font-bold">{portal==='PORTAL_C'?'Job-specific screening':portal==='PORTAL_A'?'Resume attachment':'Resume and screening'}</h2><label className="flex cursor-pointer items-center gap-3 rounded-xl border border-line bg-ink p-4 text-xs text-muted"><FileUp size={17} className="text-violet"/><span className="flex-1">Attach resume (PDF/DOCX, max 5 MB)<input required type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" className="mt-2 block w-full text-[10px]" onChange={(e)=>setFile(e.target.files?.[0]??null)}/></span></label>{currentQuestion.map((question,index)=><label key={question} className="block text-xs text-muted">{wizard&&<span className="mr-1 text-violet">Step {index+1}.</span>}{question}<textarea required maxLength={2000} rows={3} className={`${inputClass} mt-2`} value={answers[question]??''} onChange={(e)=>setAnswers({...answers,[question]:e.target.value})}/></label>)}</>}
        <div className="flex justify-between pt-3">{wizard&&stage>0?<button type="button" className="button-secondary" onClick={()=>setStage(stage-1)}><ArrowLeft size={15}/>Back</button>:<span/>}<button type="submit" className="button-primary">Review application<ArrowRight size={15}/></button></div>
      </form>:<div className="mt-6 space-y-5"><h2 className="font-display text-lg font-bold">Review your application</h2><div className="grid gap-3 rounded-2xl border border-line bg-ink p-5 text-xs sm:grid-cols-2"><p><span className="text-muted">Applicant</span><br/><span className="mt-1 inline-block font-semibold">{name}</span></p><p><span className="text-muted">Email</span><br/><span className="mt-1 inline-block font-semibold">{email}</span></p><p><span className="text-muted">Role</span><br/><span className="mt-1 inline-block font-semibold">{job.title}</span></p><p><span className="text-muted">Resume</span><br/><span className="mt-1 inline-block font-semibold">{file?.name}</span></p>{wizard&&<><p><span className="text-muted">Education</span><br/><span className="mt-1 inline-block">{education}</span></p><p className="sm:col-span-2"><span className="text-muted">Experience</span><br/><span className="mt-1 inline-block">{experience}</span></p></>}{portal==='PORTAL_A'&&<p className="sm:col-span-2"><span className="text-muted">Cover letter</span><br/><span className="mt-1 inline-block whitespace-pre-wrap">{coverLetter}</span></p>}{currentQuestion.map((question)=><p key={question} className="sm:col-span-2"><span className="text-muted">{question}</span><br/><span className="mt-1 inline-block">{answers[question]}</span></p>)}</div><label className="flex items-start gap-3 rounded-xl border border-amber/20 bg-amber/[.04] p-4 text-xs leading-5 text-[#d7cba8]"><input type="checkbox" checked={consent} onChange={(e)=>setConsent(e.target.checked)} className="mt-1 accent-violet"/><span>I approve submission to this simulated portal. I understand this test does not contact a real employer.<br/><span className="inline-flex items-center gap-1 text-[10px] text-muted"><ShieldCheck size={12}/>Resume data is stored in the local test database for verification.</span></span></label><div className="flex justify-between"><button type="button" className="button-secondary" onClick={()=>setStage(wizard?2:0)}><ArrowLeft size={15}/>Edit</button><button type="button" disabled={busy||!consent} onClick={()=>void submit()} className="button-primary">{busy?'Submitting…':'Confirm simulated submission'}</button></div></div>}
      </>}{error&&<p className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.06] p-3 text-xs text-rose-200" role="alert">{error}</p>}{message&&<p role="status" className="mt-4 text-xs text-mint">{message}</p>}</section><p className="mt-5 text-center text-[10px] text-muted">{config.name} is a fictional demonstration website created for automated testing.</p></div></main>
}
