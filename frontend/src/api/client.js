import axios from 'axios';
let accessToken = null;
export const setAccessToken = (token) => { accessToken = token; };
export const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1',
    timeout: 12_000,
    headers: { Accept: 'application/json' },
});
api.interceptors.request.use((config) => {
    if (accessToken)
        config.headers.Authorization = `Bearer ${accessToken}`;
    return config;
});
export const apiMessage = (error) => {
    if (axios.isAxiosError(error)) {
        if (!error.response)
            return 'CareerPilot could not reach the service. Check that the API is running and try again.';
        return error.response.data?.message ?? 'Something went wrong. Please try again.';
    }
    return 'Something went wrong. Please try again.';
};
export const authApi = {
    register: async (input) => (await api.post('/auth/register', input)).data,
    login: async (input) => (await api.post('/auth/login', input)).data,
    me: async () => (await api.get('/users/me')).data,
};
export const careerApi = {
    portalJobs: async (portal) => (await api.get(`/simulated-portals/${portal}/jobs`)).data,
    submitPortalApplication: async (portal, idempotencyKey, input) => (await api.post(`/simulated-portals/${portal}/submissions`, input, { headers: { 'Idempotency-Key': idempotencyKey } })).data,
    verifyPortalSubmission: async (confirmationId) => (await api.get(`/simulated-portals/submissions/${confirmationId}`)).data,
    automationRuns: async () => (await api.get('/automation-runs', { params: { page: 0, size: 50 } })).data,
    startAutomationRun: async (input, idempotencyKey) => (await api.post('/automation-runs', input, { headers: { 'Idempotency-Key': idempotencyKey } })).data,
    automationRun: async (id) => (await api.get(`/automation-runs/${id}`)).data,
    analytics: async () => (await api.get('/analytics/summary')).data,
    jobs: async (params) => (await api.get('/jobs', { params })).data,
    job: async (id) => (await api.get(`/jobs/${id}`)).data,
    importJob: async (input) => (await api.post('/jobs/import', input)).data,
    applications: async () => (await api.get('/applications', { params: { page: 0, size: 6, sort: 'updatedAt,desc' } })).data,
    allApplications: async () => (await api.get('/applications', { params: { page: 0, size: 100, sort: 'updatedAt,desc' } })).data,
    createApplication: async (input) => (await api.post('/applications', input)).data,
    updateApplicationStatus: async (id, status) => (await api.patch(`/applications/${id}/status`, { status })).data,
    saveJob: async (id) => (await api.put(`/jobs/${id}/saved`)).data,
    resumes: async () => (await api.get('/resumes')).data,
    resume: async (id) => (await api.get(`/resumes/${id}`)).data,
    uploadResume: async (file, title, resumeId) => {
        const form = new FormData();
        form.append('file', file);
        if (title?.trim())
            form.append('title', title.trim());
        if (resumeId)
            form.append('resumeId', resumeId);
        return (await api.post('/resumes', form)).data;
    },
    reviewResume: async (id, versionId, input) => (await api.put(`/resumes/${id}/versions/${versionId}/review`, input)).data,
    deleteResume: async (id) => (await api.delete(`/resumes/${id}`)).data,
    downloadResume: async (id, versionId) => (await api.get(`/resumes/${id}/versions/${versionId}/download`, { responseType: 'blob' })).data,
    analyzeResumeAts: async (resumeId, versionId, input) => (await api.post(`/resumes/${resumeId}/versions/${versionId}/ats-analyses`, input ?? {})).data,
    resumeAtsHistory: async (resumeId, versionId) => (await api.get(`/resumes/${resumeId}/versions/${versionId}/ats-analyses`)).data,
    matchResume: async (resumeVersionId, jobId) => (await api.post('/matches', { resumeVersionId, jobId })).data,
    coverLetters: async () => (await api.get('/cover-letters', { params: { page: 0, size: 20 } })).data,
    coverLetter: async (id) => (await api.get(`/cover-letters/${id}`)).data,
    generateCoverLetter: async (jobId, resumeVersionId, tone) => (await api.post('/cover-letters/draft', { jobId, resumeVersionId, tone })).data,
    saveCoverLetter: async (id, body) => (await api.put(`/cover-letters/${id}`, { body })).data,
    interviewQuestions: async (topic, difficulty) => (await api.get('/interviews/questions', { params: { topic, difficulty } })).data,
    startInterview: async (role, topic, difficulty) => (await api.post('/interviews/sessions', { role, topic, difficulty })).data,
    nextInterviewQuestion: async (sessionId) => (await api.get(`/interviews/sessions/${sessionId}/next`)).data,
    answerInterview: async (sessionId, questionId, answer) => (await api.post(`/interviews/sessions/${sessionId}/answers`, { questionId, answer })).data,
    interviewHistory: async () => (await api.get('/interviews/sessions', { params: { page: 0, size: 20 } })).data,
    interviewSession: async (id) => (await api.get(`/interviews/sessions/${id}`)).data,
    profile: async () => (await api.get('/users/me/profile')).data,
    updateProfile: async (input) => (await api.patch('/users/me/profile', input)).data,
    changePassword: async (currentPassword, newPassword) => (await api.post('/users/me/password', { currentPassword, newPassword })).data,
};
