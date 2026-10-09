import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import { authApi, setAccessToken } from '../api/client'
import type { User } from '../api/types'

type AuthContextValue = {
  user: User | null; loading: boolean; signIn: (email: string, password: string) => Promise<void>
  signUp: (name: string, email: string, password: string) => Promise<void>; signOut: () => void; updateName: (name: string) => void
}
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading] = useState(false)
  const establish = useCallback((result: { accessToken: string; user: User }) => {
    setAccessToken(result.accessToken)
    setUser(result.user)
  }, [])
  const signIn = useCallback(async (email: string, password: string) => establish(await authApi.login({ email, password })), [establish])
  const signUp = useCallback(async (name: string, email: string, password: string) => establish(await authApi.register({ name, email, password })), [establish])
  const signOut = useCallback(() => { setAccessToken(null); setUser(null) }, [])
  const updateName = useCallback((name: string) => setUser((current) => current ? { ...current, name } : current), [])
  const value = useMemo(() => ({ user, loading, signIn, signUp, signOut, updateName }), [user, loading, signIn, signUp, signOut, updateName])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
