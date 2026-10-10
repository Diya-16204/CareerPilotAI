import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { authApi, setAccessToken } from '../api/client';
const AuthContext = createContext(null);
export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading] = useState(false);
    const establish = useCallback((result) => {
        setAccessToken(result.accessToken);
        setUser(result.user);
    }, []);
    const signIn = useCallback(async (email, password) => establish(await authApi.login({ email, password })), [establish]);
    const signUp = useCallback(async (name, email, password) => establish(await authApi.register({ name, email, password })), [establish]);
    const signOut = useCallback(() => { setAccessToken(null); setUser(null); }, []);
    const updateName = useCallback((name) => setUser((current) => current ? { ...current, name } : current), []);
    const value = useMemo(() => ({ user, loading, signIn, signUp, signOut, updateName }), [user, loading, signIn, signUp, signOut, updateName]);
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() {
    const context = useContext(AuthContext);
    if (!context)
        throw new Error('useAuth must be used within AuthProvider');
    return context;
}
