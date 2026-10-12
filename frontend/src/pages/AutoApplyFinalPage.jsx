import { useEffect, useState } from 'react';
import { ArrowLeft, ArrowUpRight, CheckCircle2, LoaderCircle, ShieldCheck } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { apiMessage, careerApi } from '../api/client';
import { ApplicationFieldsEditor, missingApplicationFields } from './autoApplyFields.jsx';

export function AutoApplyFinalPage() {
  const { sessionId } = useParams();
  const [session, setSession] = useState(null);
  const [fields, setFields] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [invalid, setInvalid] = useState(false);
  const [consent, setConsent] = useState(false);
  const [error, setError] = useState('');
  const [submission, setSubmission] = useState(null);

  useEffect(() => {
    let active = true;
    careerApi.autoApplySession(sessionId).then(async data => {
      if (!active) return;
      setSession(data);
      setFields(data.applicationFields ?? []);
      if (data.status === 'SUBMITTED') {
        try { setSubmission(await careerApi.autoApplyDemoSubmission(sessionId)); }
        catch { setSubmission({ reference: null, alreadySubmitted: true, submittedAt: data.updatedAt }); }
      }
    }).catch(reason => { if (active) setError(apiMessage(reason)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [sessionId]);

  const missing = missingApplicationFields(fields);
  const statusAllowsApply = session?.status === 'APPROVED';

  async function submitDemo() {
    setInvalid(true); setError('');
    if (missing.length) { setError(`Return to Auto Apply and complete: ${missing.map(field => field.label).join(', ')}`); return; }
    if (!consent) { setError('Confirm that you want to submit this to the CareerPilot fictional demo portal.'); return; }
    setBusy(true);
    try {
      const result = await careerApi.submitAutoApplyDemo(session.id, fields, true);
      setSubmission(result);
      setSession(await careerApi.autoApplySession(session.id));
    } catch (reason) { setError(apiMessage(reason)); }
    finally { setBusy(false); }
  }

  if (loading) return <main className="min-h-screen bg-[#edf1f6] p-8 text-slate-700"><LoaderCircle className="mx-auto animate-spin"/></main>;
  if (!session) return <main className="min-h-screen bg-[#edf1f6] p-8 text-slate-900"><div className="mx-auto max-w-xl rounded-xl bg-white p-8 shadow"><h1 className="text-xl font-bold">Application unavailable</h1><p role="alert" className="mt-3 text-sm text-rose-700">{error || 'This application session could not be loaded.'}</p><Link to="/app/auto-apply" className="mt-5 inline-flex items-center gap-2 text-sm text-indigo-700"><ArrowLeft size={16}/>Return to Auto Apply</Link></div></main>;

  return <main className="min-h-screen bg-[#edf1f6] px-4 py-8 text-slate-800 sm:px-8">
    <div className="mx-auto max-w-4xl">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4"><Link to="/app/auto-apply" className="inline-flex items-center gap-2 text-sm font-medium text-indigo-700"><ArrowLeft size={16}/>Back to Auto Apply</Link><span className="rounded-full border border-slate-300 bg-white px-3 py-1 text-xs font-semibold uppercase tracking-wide text-slate-600">Employer application review</span></div>
      <header className="rounded-2xl bg-[#18243a] px-6 py-8 text-white shadow-lg sm:px-10"><p className="text-xs font-bold uppercase tracking-[.2em] text-cyan-200">Application portal</p><h1 className="mt-3 text-3xl font-bold">{session.jobTitle}</h1><p className="mt-2 text-sm text-slate-300">{session.company}{session.location ? ` · ${session.location}` : ''}</p></header>
      <div className="mt-6 grid gap-5 md:grid-cols-[minmax(0,1fr)_280px]">
        <section className="space-y-5 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-8">
          {error && <p role="alert" className="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">{error}</p>}
          {missing.length > 0 && <div className="rounded-lg border border-amber-300 bg-amber-50 p-4 text-sm text-amber-900"><p className="font-semibold">Required information is missing</p><p className="mt-1">Go back to Auto Apply and complete: {missing.map(field => field.label).join(', ')}.</p></div>}
          {!statusAllowsApply && !submission && <p className="rounded-lg border border-amber-300 bg-amber-50 p-4 text-sm text-amber-900">This session is not approved for submission. Return to Auto Apply, review the latest saved information, and approve it first.</p>}
          <div><h2 className="text-lg font-semibold">Your application</h2><p className="mt-1 text-sm text-slate-500">These values were loaded from your saved Auto Apply session. Review them here; you will not be asked to re-enter the same details.</p></div>
          <div className="grid gap-3 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm sm:grid-cols-2"><p><span className="font-semibold">Resume:</span> {session.resumeFileName}</p><p><span className="font-semibold">Cover letter:</span> {session.coverLetterDraft ? 'Included in application draft' : 'Not provided'}</p></div>
          {session.coverLetterDraft && <details className="rounded-xl border border-slate-200 p-4"><summary className="cursor-pointer text-sm font-semibold">Review cover letter</summary><p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-700">{session.coverLetterDraft}</p></details>}
          <ApplicationFieldsEditor fields={fields} disabled={Boolean(submission) || session.status === 'SUBMITTED'} invalid={invalid} onChange={setFields}/>
          {session.answers?.length > 0 && <section className="space-y-3 rounded-xl border border-slate-200 p-4"><h3 className="text-sm font-semibold">Application questions</h3>{session.answers.map(answer => <div key={answer.key} className="rounded-lg bg-slate-50 p-3"><p className="text-sm font-medium">{answer.label}{answer.required ? ' *' : ''}</p><p className="mt-1 whitespace-pre-wrap text-sm text-slate-700">{answer.confirmed ? answer.confirmedAnswer : answer.draftAnswer || 'No answer saved'}</p>{!answer.confirmed && <p className="mt-1 text-xs text-amber-800">This is an unconfirmed draft. Return to Auto Apply to review and confirm it.</p>}</div>)}</section>}
          {!submission && <label className="flex items-start gap-3 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm"><input type="checkbox" className="mt-1" checked={consent} onChange={event => setConsent(event.target.checked)}/><span>I reviewed these answers and want CareerPilot to record this application in its fictional demo portal. No real employer will receive it.</span></label>}
          {submission ? <div role="status" className="rounded-xl border border-emerald-300 bg-emerald-50 p-5 text-emerald-900"><div className="flex items-center gap-2 font-semibold"><CheckCircle2 size={20}/>Application completed</div><p className="mt-2 text-sm">CareerPilot marked this role as <strong>Applied</strong> in your application tracker.</p><p className="mt-2 text-sm">Reference: <code className="font-mono">{submission.reference ?? 'Already submitted for this session'}</code></p><Link to="/app/applications" className="mt-3 inline-flex items-center gap-2 text-sm font-semibold text-indigo-800 underline">View application tracker <ArrowUpRight size={15}/></Link><p className="mt-2 text-xs">This is a CareerPilot demo record only; no real employer received an application.</p></div> : <button type="button" disabled={busy || !statusAllowsApply || missing.length > 0 || !consent} onClick={() => void submitDemo()} className="inline-flex min-h-12 w-full items-center justify-center gap-2 rounded-lg bg-indigo-700 px-5 text-sm font-semibold text-white hover:bg-indigo-800 disabled:cursor-not-allowed disabled:opacity-50"><ShieldCheck size={17}/>{busy ? 'Submitting…' : 'Apply Now — CareerPilot demo'}</button>}
        </section>
        <aside className="h-fit space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"><h2 className="font-semibold">Real employer website?</h2><p className="text-sm leading-6 text-slate-600">Opening a company site or filling browser fields does not submit an application. Review every field and use the employer’s own submit button. CareerPilot will not bypass CAPTCHA, MFA, or site restrictions.</p>{session.applicationUrl && <a href={session.applicationUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-700">Open employer application <ArrowUpRight size={15}/></a>}<p className="border-t border-slate-200 pt-4 text-xs leading-5 text-slate-500">Your selected resume: {session.resumeFileName}. Browser form upload controls must be handled by you.</p></aside>
      </div>
    </div>
  </main>;
}
