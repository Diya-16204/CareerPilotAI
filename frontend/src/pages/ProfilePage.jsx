import { useEffect, useState } from 'react';
import { KeyRound, Save, ShieldCheck, UserRound } from 'lucide-react';
import { apiMessage, careerApi } from '../api/client';
import { useAuth } from '../auth/AuthContext';
export function ProfilePage() {
    const { updateName } = useAuth();
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [headline, setHeadline] = useState('');
    const [location, setLocation] = useState('');
    const [summary, setSummary] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    const [notice, setNotice] = useState('');
    useEffect(() => { careerApi.profile().then((profile) => { setName(profile.name); setEmail(profile.email); setHeadline(profile.headline ?? ''); setLocation(profile.location ?? ''); setSummary(profile.summary ?? ''); }).catch((reason) => setError(apiMessage(reason))).finally(() => setLoading(false)); }, []);
    async function saveProfile(event) { event.preventDefault(); setBusy(true); setError(''); setNotice(''); try {
        const result = await careerApi.updateProfile({ name, headline, location, summary });
        setName(result.name);
        updateName(result.name);
        setNotice('Your profile is saved.');
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    async function savePassword(event) { event.preventDefault(); setError(''); setNotice(''); if (newPassword !== confirmPassword) {
        setError('The new passwords do not match.');
        return;
    } setBusy(true); try {
        await careerApi.changePassword(currentPassword, newPassword);
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
        setNotice('Your password has been changed. Sign in again if you use another device.');
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    return <div><div className="eyebrow"><UserRound size={13}/>Your account</div><h1 className="mt-3 font-display text-3xl font-bold tracking-[-.055em]">Profile & settings</h1><p className="mt-2 text-sm text-muted">Keep your details current and secure your account.</p>{error && <div role="alert" className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs text-rose-200">{error}</div>}{notice && <div role="status" className="mt-5 rounded-xl border border-mint/15 bg-mint/[.06] px-4 py-3 text-xs text-mint">{notice}</div>}
    {loading ? <div className="mt-7 rounded-2xl border border-line bg-panel p-8 text-center text-xs text-muted">Loading your profile…</div> : <div className="mt-7 grid gap-5 xl:grid-cols-[1.2fr_.8fr]"><form onSubmit={(event) => void saveProfile(event)} className="rounded-2xl border border-line bg-panel p-5 sm:p-6"><div className="flex items-center gap-3 border-b border-line pb-4"><span className="grid size-9 place-items-center rounded-xl bg-violet/10 text-violet"><UserRound size={16}/></span><div><h2 className="text-sm font-semibold">Personal profile</h2><p className="mt-1 text-[10px] text-muted">Use details you want available to career tools.</p></div></div><div className="mt-5 grid gap-4 sm:grid-cols-2"><div><label htmlFor="profile-name" className="mb-2 block text-[10px] font-semibold text-muted">Full name</label><input required minLength={2} maxLength={80} id="profile-name" value={name} onChange={(event) => setName(event.target.value)} className="field-input"/></div><div><label htmlFor="profile-email" className="mb-2 block text-[10px] font-semibold text-muted">Email address</label><input id="profile-email" value={email} disabled className="field-input opacity-60"/><p className="mt-1 text-[9px] text-muted">Email changes are not available yet.</p></div><div className="sm:col-span-2"><label htmlFor="profile-headline" className="mb-2 block text-[10px] font-semibold text-muted">Professional headline</label><input maxLength={180} id="profile-headline" value={headline} onChange={(event) => setHeadline(event.target.value)} className="field-input" placeholder="e.g. Early-career backend developer"/></div><div className="sm:col-span-2"><label htmlFor="profile-location" className="mb-2 block text-[10px] font-semibold text-muted">Location</label><input maxLength={160} id="profile-location" value={location} onChange={(event) => setLocation(event.target.value)} className="field-input" placeholder="City, region, or remote"/></div><div className="sm:col-span-2"><label htmlFor="profile-summary" className="mb-2 block text-[10px] font-semibold text-muted">Short summary</label><textarea maxLength={4000} id="profile-summary" value={summary} onChange={(event) => setSummary(event.target.value)} className="min-h-[130px] w-full resize-y rounded-xl border border-white/[.08] bg-[#0d0f19] p-4 text-xs leading-6 outline-none focus:border-violet/50" placeholder="A few sentences about the work you want to do…"/><p className="mt-1 text-right text-[9px] text-muted">{summary.length}/4,000</p></div></div><button disabled={busy} className="button-primary mt-5 !h-9 !px-4 !text-[10px]"><Save size={13}/>{busy ? 'Saving…' : 'Save profile'}</button></form>
      <div className="space-y-5"><form onSubmit={(event) => void savePassword(event)} className="rounded-2xl border border-line bg-panel p-5"><div className="flex items-center gap-3 border-b border-line pb-4"><span className="grid size-9 place-items-center rounded-xl bg-cyan/10 text-cyan"><KeyRound size={16}/></span><div><h2 className="text-sm font-semibold">Change password</h2><p className="mt-1 text-[10px] text-muted">Use 10 or more characters.</p></div></div><div className="mt-5 space-y-4">{[{ id: 'current-password', label: 'Current password', value: currentPassword, set: setCurrentPassword }, { id: 'new-password', label: 'New password', value: newPassword, set: setNewPassword }, { id: 'confirm-password', label: 'Confirm new password', value: confirmPassword, set: setConfirmPassword }].map((field) => <div key={field.id}><label htmlFor={field.id} className="mb-2 block text-[10px] font-semibold text-muted">{field.label}</label><input id={field.id} type="password" required minLength={field.id === 'current-password' ? 1 : 10} maxLength={72} value={field.value} onChange={(event) => field.set(event.target.value)} autoComplete={field.id === 'current-password' ? 'current-password' : 'new-password'} className="field-input"/></div>)}</div><button disabled={busy} className="button-primary mt-5 !h-9 !px-4 !text-[10px]" type="submit">Update password</button></form><div className="rounded-2xl border border-mint/10 bg-mint/[.025] p-5"><div className="flex gap-3"><ShieldCheck size={17} className="shrink-0 text-mint"/><div><h3 className="text-xs font-semibold">Token safety</h3><p className="mt-2 text-[10px] leading-5 text-muted">Access tokens stay in memory in this browser and expire after 15 minutes. This session does not use refresh tokens or server-side token revocation yet.</p></div></div></div></div></div>}
  </div>;
}
