import { useEffect, useState } from 'react';
import { Activity, BriefcaseBusiness, Plus } from 'lucide-react';
import { apiMessage, careerApi } from '../api/client';
const statuses = ['SAVED', 'APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN'];
export function ApplicationsPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [title, setTitle] = useState('');
    const [company, setCompany] = useState('');
    useEffect(() => { careerApi.allApplications().then((page) => setItems(page.items)).catch((reason) => setError(apiMessage(reason))).finally(() => setLoading(false)); }, []);
    async function create(event) { event.preventDefault(); setBusy(true); setError(''); try {
        const item = await careerApi.createApplication({ title: title.trim(), company: company.trim(), status: 'SAVED' });
        setItems((current) => [item, ...current]);
        setTitle('');
        setCompany('');
        setShowForm(false);
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    async function update(item, status) { setError(''); setItems((current) => current.map((entry) => entry.id === item.id ? { ...entry, status } : entry)); try {
        const updated = await careerApi.updateApplicationStatus(item.id, status);
        setItems((current) => current.map((entry) => entry.id === item.id ? updated : entry));
    }
    catch (reason) {
        setItems((current) => current.map((entry) => entry.id === item.id ? item : entry));
        setError(apiMessage(reason));
    } }
    return <div><div className="eyebrow"><Activity size={13}/>Stay organized</div><div className="mt-3 flex flex-wrap items-end justify-between gap-4"><div><h1 className="font-display text-3xl font-bold tracking-[-.055em]">Application tracker</h1><p className="mt-2 text-sm text-muted">Keep every opportunity and next step in view. Status changes are saved to your account.</p></div><button onClick={() => setShowForm((value) => !value)} className="button-primary !h-10 !px-4 !text-xs"><Plus size={14}/>{showForm ? 'Close form' : 'Add application'}</button></div>
    {error && <div role="alert" className="mt-6 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs text-rose-200">{error}</div>}
    {showForm && <form onSubmit={create} className="mt-6 grid gap-4 rounded-2xl border border-line bg-panel p-5 sm:grid-cols-[1fr_1fr_auto] sm:items-end"><div><label htmlFor="application-title" className="mb-2 block text-xs font-semibold">Role title</label><input id="application-title" required maxLength={160} value={title} onChange={(event) => setTitle(event.target.value)} className="field-input" placeholder="e.g. Frontend Developer"/></div><div><label htmlFor="application-company" className="mb-2 block text-xs font-semibold">Company</label><input id="application-company" required maxLength={160} value={company} onChange={(event) => setCompany(event.target.value)} className="field-input" placeholder="Company name"/></div><button disabled={busy} className="button-primary !h-[46px]" type="submit">{busy ? 'Saving…' : 'Add to tracker'}</button></form>}
    <div className="mt-7 overflow-x-auto rounded-2xl border border-line bg-panel"><div className="grid min-w-[640px] grid-cols-[1.2fr_1fr_.75fr_.7fr] border-b border-line px-5 py-3 text-[9px] font-bold uppercase tracking-[.15em] text-muted"><span>Role</span><span>Company</span><span>Status</span><span>Last updated</span></div>{loading ? <div className="p-8 text-center text-xs text-muted">Loading applications…</div> : items.length ? <div className="min-w-[640px] divide-y divide-line">{items.map((item) => <div key={item.id} className="grid grid-cols-[1.2fr_1fr_.75fr_.7fr] items-center px-5 py-3.5"><span className="flex items-center gap-2 truncate text-xs font-semibold"><BriefcaseBusiness size={14} className="shrink-0 text-violet"/>{item.title}</span><span className="truncate text-xs text-muted">{item.company}</span><label className="sr-only" htmlFor={`status-${item.id}`}>Status for {item.title}</label><select id={`status-${item.id}`} value={item.status} onChange={(event) => void update(item, event.target.value)} className="field-input !h-8 !w-fit min-w-[120px] !py-0 !text-[10px]">{statuses.map((status) => <option key={status} value={status}>{status.toLowerCase().replace('_', ' ')}</option>)}</select><span className="text-[10px] text-muted">{new Date(item.updatedAt).toLocaleDateString()}</span></div>)}</div> : !error && <div className="px-6 py-14 text-center"><span className="mx-auto grid size-11 place-items-center rounded-xl bg-white/[.035] text-muted"><BriefcaseBusiness size={18}/></span><p className="mt-4 text-sm font-semibold">Nothing to track yet</p><p className="mt-1 text-xs text-muted">Add an application here or track a role from job discovery.</p></div>}</div>
    <p className="mt-5 text-center text-[10px] text-muted">Tracking an opportunity does not submit an application. Update its status as you move through the process.</p>
  </div>;
}
