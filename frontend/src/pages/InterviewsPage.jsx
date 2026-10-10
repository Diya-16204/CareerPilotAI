import { useEffect, useMemo, useState } from 'react';
import { ArrowRight, CheckCircle2, CircleHelp, Clock3, History, MessageSquareText, RotateCcw } from 'lucide-react';
import { apiMessage, careerApi } from '../api/client';
const topics = ['DSA', 'JAVA', 'OOP', 'DBMS', 'SQL', 'OPERATING_SYSTEMS', 'NETWORKS', 'REST_APIS', 'SPRING_BOOT', 'SYSTEM_DESIGN', 'BEHAVIORAL'];
const levels = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
export function InterviewsPage() {
    const [history, setHistory] = useState([]);
    const [available, setAvailable] = useState([]);
    const [session, setSession] = useState(null);
    const [answers, setAnswers] = useState([]);
    const [question, setQuestion] = useState(null);
    const [evaluation, setEvaluation] = useState(null);
    const [role, setRole] = useState('Software Engineer');
    const [topic, setTopic] = useState('JAVA');
    const [difficulty, setDifficulty] = useState('BEGINNER');
    const [answer, setAnswer] = useState('');
    const [busy, setBusy] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const topicsAvailable = useMemo(() => new Set(available.map((item) => item.topic)), [available]);
    const difficulties = useMemo(() => new Set(available.filter((item) => item.topic === topic).map((item) => item.difficulty)), [available, topic]);
    useEffect(() => { let active = true; Promise.all([careerApi.interviewHistory(), careerApi.interviewQuestions('', '')]).then(([sessions, questions]) => { if (active) {
        setHistory(sessions.items);
        setAvailable(questions);
        if (questions.length) {
            const first = questions[0];
            setTopic(first.topic);
            setDifficulty(first.difficulty);
        }
    } }).catch((reason) => { if (active)
        setError(apiMessage(reason)); }).finally(() => { if (active)
        setLoading(false); }); return () => { active = false; }; }, []);
    async function loadNext(id) { setBusy(true); setError(''); try {
        const next = await careerApi.nextInterviewQuestion(id);
        setQuestion(next);
        setEvaluation(null);
        setAnswer('');
        if (!next) {
            const detail = await careerApi.interviewSession(id);
            setSession(detail.session);
            setAnswers(detail.answers);
            setHistory((items) => [detail.session, ...items.filter((item) => item.id !== id)]);
        }
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    async function start() { setBusy(true); setError(''); setEvaluation(null); try {
        const created = await careerApi.startInterview(role, topic, difficulty);
        setSession(created);
        setAnswers([]);
        setHistory((items) => [created, ...items.filter((item) => item.id !== created.id)]);
        await loadNext(created.id);
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    async function submit(event) { event.preventDefault(); if (!session || !question)
        return; setBusy(true); setError(''); try {
        const result = await careerApi.answerInterview(session.id, question.id, answer);
        setEvaluation(result);
        setAnswers((items) => [...items, result]);
        setAnswer('');
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    async function resumeHistory(item) { setBusy(true); setError(''); try {
        const detail = await careerApi.interviewSession(item.id);
        setSession(detail.session);
        setAnswers(detail.answers);
        setQuestion(null);
        setEvaluation(detail.answers.at(-1) ?? null);
        if (detail.session.state === 'IN_PROGRESS')
            await loadNext(item.id);
    }
    catch (reason) {
        setError(apiMessage(reason));
    }
    finally {
        setBusy(false);
    } }
    const wordCount = answer.trim() ? answer.trim().split(/\s+/).length : 0;
    return <div><div className="eyebrow"><CircleHelp size={13}/>Practice with purpose</div><h1 className="mt-3 font-display text-3xl font-bold tracking-[-.055em]">Interview practice</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-muted">Work through seeded technical and behavioral questions, save your sessions, and review a transparent local content-coverage rubric.</p>{error && <div role="alert" className="mt-5 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs text-rose-200">{error}</div>}
    <div className="mt-7 grid gap-5 xl:grid-cols-[.68fr_1.32fr]"><section className="h-fit rounded-2xl border border-line bg-panel p-5"><div className="flex items-center gap-2"><span className="grid size-8 place-items-center rounded-lg bg-cyan/10 text-cyan"><MessageSquareText size={15}/></span><h2 className="text-xs font-semibold">Set up a practice session</h2></div><div className="mt-5 space-y-4"><div><label htmlFor="interview-role" className="mb-2 block text-[10px] font-semibold text-muted">Role you’re preparing for</label><input id="interview-role" value={role} maxLength={160} onChange={(event) => setRole(event.target.value)} className="field-input"/></div><div><label htmlFor="interview-topic" className="mb-2 block text-[10px] font-semibold text-muted">Topic</label><select id="interview-topic" value={topic} onChange={(event) => { const value = event.target.value; setTopic(value); const next = available.find((item) => item.topic === value); if (next)
        setDifficulty(next.difficulty); }} className="field-input">{topics.filter((value) => topicsAvailable.has(value)).map((value) => <option key={value} value={value}>{value.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase())}</option>)}</select></div><div><label htmlFor="interview-level" className="mb-2 block text-[10px] font-semibold text-muted">Difficulty</label><select id="interview-level" value={difficulty} onChange={(event) => setDifficulty(event.target.value)} className="field-input">{levels.filter((value) => difficulties.has(value)).map((value) => <option key={value} value={value}>{value.toLowerCase().replace(/^\w/, c => c.toUpperCase())}</option>)}</select></div><button disabled={busy || loading || !role.trim()} onClick={() => void start()} className="button-primary w-full">{busy ? 'Starting…' : 'Start practice'} <ArrowRight size={14}/></button><p className="text-[9px] leading-4 text-muted">No answer is sent to an external model. Code is not executed.</p></div>
      {history.length > 0 && <div className="mt-6 border-t border-line pt-4"><div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-widest text-muted"><History size={13}/>Recent sessions</div><div className="mt-3 max-h-64 space-y-1 overflow-auto">{history.map((item) => <button key={item.id} onClick={() => void resumeHistory(item)} className={`w-full rounded-lg px-3 py-2.5 text-left transition hover:bg-white/[.04] ${session?.id === item.id ? 'bg-violet/[.08]' : ''}`}><span className="block truncate text-[10px] font-semibold">{item.role} · {item.topic.replaceAll('_', ' ')}</span><span className="mt-1 flex items-center gap-1.5 text-[9px] text-muted"><Clock3 size={10}/>{new Date(item.startedAt).toLocaleDateString()} · {item.answerCount} answered · {item.state.toLowerCase()}</span></button>)}</div></div>}</section>
    <section className="min-h-[480px] rounded-2xl border border-line bg-panel p-5 sm:p-7">{loading ? <div className="grid min-h-[400px] place-items-center text-xs text-muted">Loading your practice history…</div> : !session ? <div className="grid min-h-[400px] place-items-center text-center"><div><span className="mx-auto grid size-12 place-items-center rounded-2xl border border-white/[.07] bg-white/[.025] text-muted"><CircleHelp size={19}/></span><p className="mt-4 text-sm font-semibold">A quiet place to practice out loud</p><p className="mt-1 max-w-sm text-xs leading-5 text-muted">Choose a topic and difficulty to begin. Your answers and session history are saved to your account.</p></div></div> : <><div className="flex flex-wrap items-start justify-between gap-3 border-b border-line pb-4"><div><span className="status-pill">{session.topic.replaceAll('_', ' ')} · {session.difficulty.toLowerCase()}</span><h2 className="mt-3 font-display text-lg font-bold">{session.role}</h2><p className="mt-1 text-[10px] text-muted">Question {answers.length + (question ? 1 : 0)} · {session.state.toLowerCase()}</p></div><span className="text-[10px] text-muted">{answers.length} answered</span></div>
      {question ? <><div className="mt-6 rounded-xl border border-violet/12 bg-violet/[.045] p-5"><p className="text-[9px] font-bold uppercase tracking-widest text-violet">Question {answers.length + 1}</p><p className="mt-3 text-sm leading-7 text-[#e3e4ee]">{question.prompt}</p></div><form onSubmit={(event) => void submit(event)} className="mt-5"><label htmlFor="practice-answer" className="mb-2 block text-[10px] font-semibold text-muted">Your answer</label><textarea id="practice-answer" required value={answer} onChange={(event) => setAnswer(event.target.value)} maxLength={12000} placeholder="Explain your reasoning in your own words…" className="min-h-[180px] w-full resize-y rounded-xl border border-white/[.08] bg-[#0d0f19] p-4 text-xs leading-6 text-white outline-none focus:border-violet/50"/><div className="mt-2 flex items-center justify-between"><span className="text-[9px] text-muted">{wordCount} words · {answer.length}/12,000</span><button disabled={busy || !answer.trim()} className="button-primary !h-9 !px-4 !text-[10px]">Submit answer <ArrowRight size={13}/></button></div></form></> : evaluation ? <div className="mt-6 space-y-4"><article className="rounded-xl border border-cyan/12 bg-cyan/[.035] p-5"><div className="flex items-center justify-between"><h3 className="text-xs font-semibold">Local rubric estimate</h3><span className="font-display text-xl font-bold text-cyan">{evaluation.contentCoverageScore}%</span></div><p className="mt-3 text-[11px] leading-6 text-[#c9ccdc]">{evaluation.feedback}</p><span className="mt-3 inline-flex rounded-full border border-white/[.08] px-2 py-1 text-[8px] text-muted">{evaluation.evaluationSource}</span></article><details className="rounded-xl border border-line"><summary className="cursor-pointer px-4 py-3 text-[10px] font-semibold">Compare with a sample answer</summary><p className="border-t border-line px-4 py-4 text-[11px] leading-6 text-muted">{evaluation.modelAnswer}</p></details><button disabled={busy} onClick={() => void loadNext(session.id)} className="button-primary !h-9 !px-4 !text-[10px]">Next question <ArrowRight size={13}/></button></div> : <div className="mt-6 rounded-xl border border-mint/10 bg-mint/[.035] p-5"><div className="flex items-center gap-2 text-xs font-semibold text-mint"><CheckCircle2 size={15}/>Session complete</div><p className="mt-2 text-[11px] leading-6 text-muted">You answered {answers.length} questions. Average local content coverage: {session.averageScore ?? '—'}%. Review the questions below and choose another topic when ready.</p><button onClick={() => { setSession(null); setQuestion(null); setEvaluation(null); }} className="mt-4 inline-flex items-center gap-2 text-[10px] font-semibold text-violet"><RotateCcw size={12}/> Start a new session</button></div>}
      {answers.length > 0 && <div className="mt-6 border-t border-line pt-4"><h3 className="text-[10px] font-bold uppercase tracking-widest text-muted">Answered questions</h3><div className="mt-3 space-y-2">{answers.map((item) => <div key={item.id} className="flex items-center gap-3 rounded-lg border border-white/[.05] px-3 py-2.5"><span className="grid size-6 place-items-center rounded-md bg-white/[.04] text-[9px] text-muted">{item.ordinal}</span><span className="min-w-0 flex-1 truncate text-[10px] text-[#c5c8d7]">{item.question}</span><span className="text-[9px] font-semibold text-cyan">{item.contentCoverageScore}%</span></div>)}</div></div>}
      <p className="mt-5 text-center text-[9px] leading-4 text-muted">This deterministic keyword-coverage heuristic is not an AI correctness judgment. Review the sample answer and verify technical accuracy; submitted code is never executed.</p></>}</section></div>
  </div>;
}
