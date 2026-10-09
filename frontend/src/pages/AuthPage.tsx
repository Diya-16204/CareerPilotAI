import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import type { UseFormRegisterReturn } from 'react-hook-form'
import { ArrowLeft, ArrowRight, Eye, EyeOff, Sparkles } from 'lucide-react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Brand } from '../components/Brand'
import { apiMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'

const loginSchema = z.object({ email: z.string().email('Enter a valid email address.'), password: z.string().min(8, 'Use at least 8 characters.') })
const registerSchema = z.object({ name: z.string().trim().min(2, 'Enter at least 2 characters.').max(80), email: z.string().email('Enter a valid email address.'), password: z.string().min(10, 'Use at least 10 characters.').max(72, 'Use no more than 72 characters.') })
type LoginForm = z.infer<typeof loginSchema>
type RegisterForm = z.infer<typeof registerSchema>

export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const isRegister = mode === 'register'
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const { signIn, signUp } = useAuth()
  const loginForm = useForm<LoginForm>({ resolver: zodResolver(loginSchema) })
  const registerForm = useForm<RegisterForm>({ resolver: zodResolver(registerSchema) })
  const submitting = isRegister ? registerForm.formState.isSubmitting : loginForm.formState.isSubmitting

  const submitLogin = loginForm.handleSubmit(async (values) => {
    setError('')
    try { await signIn(values.email, values.password); navigate((location.state as { from?: string } | null)?.from ?? '/app', { replace: true }) }
    catch (reason) { setError(apiMessage(reason)) }
  })
  const submitRegister = registerForm.handleSubmit(async (values) => {
    setError('')
    try { await signUp(values.name, values.email, values.password); navigate('/app', { replace: true }) }
    catch (reason) { setError(apiMessage(reason)) }
  })

  return <div className="min-h-screen bg-ink text-white lg:grid lg:grid-cols-[1fr_.9fr]">
    <div className="relative hidden overflow-hidden border-r border-white/[.06] bg-[#0d0f1b] p-12 lg:flex lg:flex-col"><div className="absolute -left-36 top-1/4 size-[500px] rounded-full bg-violet/10 blur-[130px]"/><div className="absolute bottom-0 right-0 size-[350px] rounded-full bg-cyan/[.07] blur-[120px]"/><div className="relative"><Brand /></div><div className="relative my-auto max-w-lg"><div className="eyebrow"><Sparkles size={13}/> Your next chapter starts here</div><h1 className="mt-7 font-display text-5xl font-semibold leading-[1.08] tracking-[-.065em]">Make room for<br/><span className="text-gradient">what’s next.</span></h1><p className="mt-6 max-w-md text-sm leading-7 text-[#a6abc0]">A calm, focused home for the work that moves your career forward — from shaping your story to finding your fit.</p><div className="mt-10 flex items-center gap-4 border-t border-white/[.08] pt-6"><div className="flex -space-x-2">{['S','M','A'].map((v,i)=><span key={v} className={`grid size-8 place-items-center rounded-full border-2 border-[#0d0f1b] text-[10px] font-bold ${['bg-violet/80','bg-cyan/70','bg-[#d69770]'][i]}`}>{v}</span>)}</div><span className="text-xs text-muted">Built for the journey, not just the job search.</span></div></div><p className="relative text-[10px] text-[#686d82]">© 2026 CareerPilot AI</p></div>
    <div className="flex min-h-screen flex-col px-5 py-6 sm:px-10 lg:px-16"><div className="flex items-center justify-between lg:justify-end"><Link to="/" className="inline-flex items-center gap-2 text-xs text-muted transition hover:text-white lg:hidden"><ArrowLeft size={15}/> Back home</Link><div className="hidden lg:block"/><span className="text-xs text-muted">{isRegister ? 'Already have an account?' : 'New to CareerPilot?'}</span><Link to={isRegister ? '/login' : '/register'} className="ml-3 text-xs font-semibold text-violet hover:text-[#afa5ff]">{isRegister ? 'Sign in' : 'Create account'}</Link></div>
      <div className="mx-auto my-auto w-full max-w-[400px] py-14"><div className="mb-9 lg:hidden"><Brand/></div><div className="eyebrow"><span className="size-1.5 rounded-full bg-mint"/>{isRegister ? 'Your path starts here' : 'Welcome back'}</div><h2 className="mt-5 font-display text-3xl font-bold tracking-[-.055em]">{isRegister ? 'Create your account' : 'Sign in to your space'}</h2><p className="mt-2 text-sm leading-6 text-muted">{isRegister ? 'One thoughtful step toward your next opportunity.' : 'Pick up right where your next chapter begins.'}</p>
        {error && <div role="alert" className="mt-6 rounded-xl border border-rose-400/20 bg-rose-400/[.07] px-4 py-3 text-xs leading-5 text-rose-200">{error}</div>}
        <form className="mt-8 space-y-5" onSubmit={isRegister ? submitRegister : submitLogin} noValidate>
          {isRegister && <Field label="Full name" name="name" placeholder="How should we call you?" register={registerForm.register('name')} error={registerForm.formState.errors.name?.message}/>}
          <Field label="Email address" name="email" type="email" placeholder="you@example.com" register={isRegister ? registerForm.register('email') : loginForm.register('email')} error={isRegister ? registerForm.formState.errors.email?.message : loginForm.formState.errors.email?.message}/>
          <div><div className="mb-2 flex items-center justify-between"><label htmlFor="password" className="text-xs font-semibold text-[#d1d3df]">Password</label>{!isRegister && <span className="text-[10px] text-muted">Secure sign-in</span>}</div><div className="relative"><input id="password" autoComplete={isRegister ? 'new-password' : 'current-password'} type={showPassword ? 'text' : 'password'} placeholder={isRegister ? 'At least 10 characters' : 'Enter your password'} className="field-input pr-11" {...(isRegister ? registerForm.register('password') : loginForm.register('password'))}/><button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-white" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={16}/> : <Eye size={16}/>}</button></div><p className="field-error">{isRegister ? registerForm.formState.errors.password?.message : loginForm.formState.errors.password?.message}</p></div>
          {isRegister && <p className="text-[10px] leading-5 text-muted">Use 10 or more characters. Your password is securely hashed by the service.</p>}
          <button disabled={submitting} type="submit" className="button-primary !mt-7 w-full">{submitting ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'} {!submitting && <ArrowRight size={16}/>}</button>
        </form>
        <p className="mt-6 text-center text-[10px] leading-5 text-[#777c91]">By continuing, you agree to use CareerPilot responsibly and keep your account secure.</p>
      </div><div className="flex justify-between border-t border-white/[.06] pt-5 text-[10px] text-[#686d82]"><Link to="/" className="hover:text-white">CareerPilot AI</Link><span>Your data belongs to you.</span></div></div>
  </div>
}

function Field({ label, name, type = 'text', placeholder, register, error }: { label: string; name: string; type?: string; placeholder: string; register: UseFormRegisterReturn; error?: string }) {
  return <div><label htmlFor={name} className="mb-2 block text-xs font-semibold text-[#d1d3df]">{label}</label><input id={name} type={type} autoComplete={name === 'email' ? 'email' : 'name'} placeholder={placeholder} className="field-input" {...register}/><p className="field-error">{error}</p></div>
}
