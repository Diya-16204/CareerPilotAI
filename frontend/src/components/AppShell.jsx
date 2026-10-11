import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Activity, BriefcaseBusiness, ChevronDown, CircleHelp, FileText, LayoutDashboard, LogOut, Menu, Settings2, UserRound, X, PenLine, UserCog, ChartNoAxesCombined, Workflow } from 'lucide-react';
import { Brand } from './Brand';
import { useAuth } from '../auth/AuthContext';
const links = [
    { to: '/app', label: 'Overview', icon: LayoutDashboard, end: true },
    { to: '/app/jobs', label: 'Explore jobs', icon: BriefcaseBusiness },
    { to: '/app/resumes', label: 'Resume studio', icon: FileText },
    { to: '/app/cover-letters', label: 'Cover letters', icon: PenLine },
    { to: '/app/applications', label: 'Applications', icon: Activity },
    { to: '/app/automation', label: 'Automation center', icon: Workflow },
    { to: '/app/profile', label: 'Profile & settings', icon: UserCog },
];
export function AppShell() {
    const { user, signOut } = useAuth();
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);
    const [userMenuOpen, setUserMenuOpen] = useState(false);
    return <div className="min-h-screen bg-ink text-white">
    <aside className={`fixed inset-y-0 left-0 z-40 flex w-[248px] flex-col overflow-hidden border-r border-line bg-[#0d0f1a] px-5 py-6 transition-transform lg:translate-x-0 ${open ? 'translate-x-0' : '-translate-x-full'}`}>
      <div className="flex shrink-0 items-center justify-between"><Brand /><button className="icon-button lg:hidden" aria-label="Close menu" onClick={() => setOpen(false)}><X size={18}/></button></div>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain pb-4">
        <div className="mt-12 px-3 text-[10px] font-bold uppercase tracking-[.2em] text-[#696f87]">Workspace</div>
        <nav className="mt-3 space-y-1" aria-label="Main navigation">{links.map(({ to, label, icon: Icon, end }) => <NavLink key={to} to={to} end={end} onClick={() => setOpen(false)} className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}><Icon size={17} strokeWidth={1.8}/><span>{label}</span>{label === 'Applications' && <span className="ml-auto rounded-full bg-white/[.06] px-2 py-0.5 text-[10px] text-muted">Live</span>}</NavLink>)}</nav>
        <div className="mt-9 px-3 text-[10px] font-bold uppercase tracking-[.2em] text-[#696f87]">Your toolkit</div>
        <div className="mt-3 space-y-1"><NavLink to="/app/interviews" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}><CircleHelp size={17}/><span>Interview practice</span></NavLink><NavLink to="/app/analytics" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}><ChartNoAxesCombined size={17}/><span>Analytics</span></NavLink><p className="px-3 pt-3 text-[9px] font-bold uppercase tracking-[.16em] text-[#696f87]">Test-only portals</p><NavLink to="/simulation/portal_a" className="nav-link"><BriefcaseBusiness size={16}/><span>Standard application</span></NavLink><NavLink to="/simulation/portal_b" className="nav-link"><BriefcaseBusiness size={16}/><span>Application wizard</span></NavLink><NavLink to="/simulation/portal_c" className="nav-link"><BriefcaseBusiness size={16}/><span>Role-specific application</span></NavLink></div>
      </div>
      <div className="shrink-0">
        <div className="rounded-2xl border border-white/[.07] bg-white/[.025] p-4"><div className="flex items-center gap-2 text-xs font-semibold"><span className="size-1.5 rounded-full bg-mint"/>CareerPilot is ready</div><p className="mt-2 text-[11px] leading-5 text-muted">Your progress will appear here as you build your career journey.</p></div>
        <div className="relative mt-3">
          {userMenuOpen && <div id="account-menu" className="absolute bottom-full left-0 right-0 z-50 mb-2 rounded-xl border border-white/10 bg-[#171a2b] p-1.5 shadow-xl">
            <button className="flex w-full items-center gap-2 rounded-lg px-3 py-2.5 text-left text-xs font-medium text-white/85 transition hover:bg-white/[.06]" onClick={() => { setUserMenuOpen(false); setOpen(false); navigate('/app/profile'); }}><UserCog size={15}/><span>Profile &amp; settings</span></button>
            <button className="flex w-full items-center gap-2 rounded-lg px-3 py-2.5 text-left text-xs font-medium text-rose-300 transition hover:bg-rose-400/10" onClick={() => { setUserMenuOpen(false); signOut(); navigate('/login'); }}><LogOut size={15}/><span>Log out</span></button>
          </div>}
          <button className="flex w-full items-center gap-3 rounded-xl p-2.5 text-left transition hover:bg-white/[.04]" aria-label="Open account menu" aria-expanded={userMenuOpen} aria-controls="account-menu" onClick={() => setUserMenuOpen(value => !value)}><span className="grid size-9 place-items-center rounded-full border border-white/10 bg-gradient-to-br from-violet/30 to-cyan/20 text-sm font-bold">{user?.name?.charAt(0).toUpperCase() ?? <UserRound size={16}/>}</span><span className="min-w-0 flex-1"><span className="block truncate text-xs font-semibold">{user?.name}</span><span className="block truncate text-[10px] text-muted">{user?.email}</span></span><ChevronDown size={15} className={`text-muted transition-transform ${userMenuOpen ? 'rotate-180' : ''}`}/></button>
        </div>
      </div>
    </aside>
    {open && <button className="fixed inset-0 z-30 bg-black/60 lg:hidden" aria-label="Close navigation overlay" onClick={() => setOpen(false)}/>}
    <div className="min-h-screen lg:pl-[248px]"><header className="sticky top-0 z-20 flex h-[70px] items-center justify-between border-b border-line/80 bg-ink/90 px-5 backdrop-blur-xl sm:px-8"><div className="flex items-center gap-3"><button className="icon-button lg:hidden" aria-label="Open menu" onClick={() => setOpen(true)}><Menu size={18}/></button><span className="text-xs text-muted">{new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'long', day: 'numeric' }).format(new Date())}</span><span className="hidden size-1 rounded-full bg-line sm:block"/><span className="hidden text-xs text-muted sm:block">A clearer path starts here</span></div><span className="grid size-8 place-items-center rounded-xl border border-white/[.07] bg-white/[.03] text-muted" aria-label="CareerPilot settings coming soon"><Settings2 size={16}/></span></header><main className="mx-auto max-w-[1400px] px-5 py-8 sm:px-8 lg:px-10"><Outlet /></main></div>
  </div>;
}
