import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { ProtectedRoute } from './components/ProtectedRoute';
import { ApplicationsPage } from './pages/ApplicationsPage';
import { AuthPage } from './pages/AuthPage';
import { DashboardPage } from './pages/DashboardPage';
import { JobsPage } from './pages/JobsPage';
import { LandingPage } from './pages/LandingPage';
import { MatchPage } from './pages/MatchPage';
import { ResumesPage } from './pages/ResumesPage';
import { CoverLetterPage } from './pages/CoverLetterPage';
import { ReferralPage } from './pages/ReferralPage';
import { InterviewsPage } from './pages/InterviewsPage';
import { ProfilePage } from './pages/ProfilePage';
import { SimulatedPortalPage } from './pages/SimulatedPortalPage';
import { AutomationPage } from './pages/AutomationPage';
const AnalyticsPage = lazy(() => import('./pages/AnalyticsPage').then((module) => ({ default: module.AnalyticsPage })));
function NotFound() { return <div className="grid min-h-screen place-items-center bg-ink px-6 text-center text-white"><div><p className="font-display text-6xl font-bold text-violet">404</p><h1 className="mt-4 text-xl font-semibold">This page took another path.</h1><p className="mt-2 text-sm text-muted">Let’s get you back on track.</p><a className="button-primary mx-auto mt-6" href="/">Return home</a></div></div>; }
export function App() {
    return <Routes><Route path="/" element={<LandingPage />}/><Route path="/simulation/:portal" element={<SimulatedPortalPage />}/><Route path="/login" element={<AuthPage mode="login"/>}/><Route path="/register" element={<AuthPage mode="register"/>}/><Route element={<ProtectedRoute />}><Route path="/app" element={<AppShell />}><Route index element={<DashboardPage />}/><Route path="jobs" element={<JobsPage />}/><Route path="resumes" element={<ResumesPage />}/><Route path="cover-letters" element={<CoverLetterPage />}/><Route path="referrals" element={<ReferralPage />}/><Route path="interviews" element={<InterviewsPage />}/><Route path="automation" element={<AutomationPage />}/><Route path="analytics" element={<Suspense fallback={<div className="py-16 text-center text-sm text-muted">Loading analytics…</div>}><AnalyticsPage /></Suspense>}/><Route path="applications" element={<ApplicationsPage />}/><Route path="profile" element={<ProfilePage />}/><Route path="settings" element={<ProfilePage />}/><Route path="matches/new" element={<MatchPage />}/></Route></Route><Route path="/404" element={<NotFound />}/><Route path="*" element={<Navigate to="/404" replace/>}/></Routes>;
}
