import { Compass } from 'lucide-react';
import { Link } from 'react-router-dom';
export function Brand({ light = false }) {
    return <Link to="/" className="inline-flex items-center gap-2.5" aria-label="CareerPilot home">
    <span className="grid size-9 place-items-center rounded-xl bg-violet text-white shadow-glow"><Compass size={19} strokeWidth={2.2}/></span>
    <span className={`font-display text-[16px] font-extrabold tracking-[-.04em] ${light ? 'text-white' : 'text-white'}`}>careerpilot<span className="text-violet">.</span></span>
  </Link>;
}
