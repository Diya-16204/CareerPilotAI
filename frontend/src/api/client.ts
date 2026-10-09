import axios from 'axios'
import type { AnalyticsSummary, Application, AuthResponse, CoverLetter, InterviewAnswer, InterviewQuestion, InterviewSession, InterviewSessionDetail, Job, MatchResult, Page, PortalJob, PortalSubmission, PortalVerification, Resume, ResumeSummary, ResumeVersion, User, UserProfile } from './types'

let accessToken: string | null = null
export const setAccessToken = (token: string | null) => { accessToken = token }

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1',
  timeout: 12_000,
  headers: { Accept: 'application/json' },
})
api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

export const apiMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    if (!error.response) return 'CareerPilot could not reach the service. Check that the API is running and try again.'
    return (error.response.data as { message?: string })?.message ?? 'Something went wrong. Please try again.'
  }
  return 'Something went wrong. Please try again.'
}

export const authApi = {
  register: async (input: { name: string; email: string; password: string }) => (await api.post<AuthResponse>('/auth/register', input)).data,
  login: async (input: { email: string; password: string }) => (await api.post<AuthResponse>('/auth/login', input)).data,
  me: async () => (await api.get<User>('/users/me')).data,
}
export const careerApi = {
  portalJobs: async (portal: string) => (await api.get<PortalJob[]>(`/simulated-portals/${portal}/jobs`)).data,
  submitPortalApplication: async (portal: string, idempotencyKey: string, input: { jobKey: string; applicantName: string; applicantEmail: string; education: string | null; experience: string | null; coverLetter: string | null; resumeFileName: string; resumeBase64: string; answers: Record<string,string>; consent: boolean }) => (await api.post<PortalSubmission>(`/simulated-portals/${portal}/submissions`, input, { headers: { 'Idempotency-Key': idempotencyKey } })).data,
  verifyPortalSubmission: async (confirmationId: string) => (await api.get<PortalVerification>(`/simulated-portals/submissions/${confirmationId}`)).data,
  analytics: async () => (await api.get<AnalyticsSummary>('/analytics/summary')).data,
  jobs: async (params: { q?: string; page?: number; size?: number }) => (await api.get<Page<Job>>('/jobs', { params })).data,
  job: async (id: string) => (await api.get<Job>(`/jobs/${id}`)).data,
  importJob: async (input: { title: string; company: string; location: string; employmentType: string; description: string; skills: string[] }) => (await api.post<Job>('/jobs/import', input)).data,
  applications: async () => (await api.get<Page<Application>>('/applications', { params: { page: 0, size: 6, sort: 'updatedAt,desc' } })).data,
  allApplications: async () => (await api.get<Page<Application>>('/applications', { params: { page: 0, size: 100, sort: 'updatedAt,desc' } })).data,
  createApplication: async (input: { jobId?: string; title: string; company: string; status: Application['status'] }) => (await api.post<Application>('/applications', input)).data,
  updateApplicationStatus: async (id: string, status: Application['status']) => (await api.patch<Application>(`/applications/${id}/status`, { status })).data,
  saveJob: async (id: string) => (await api.put(`/jobs/${id}/saved`)).data,
  resumes: async () => (await api.get<ResumeSummary[]>('/resumes')).data,
  resume: async (id: string) => (await api.get<Resume>(`/resumes/${id}`)).data,
  uploadResume: async (file: File, title?: string, resumeId?: string) => {
    const form = new FormData(); form.append('file', file); if (title?.trim()) form.append('title', title.trim()); if (resumeId) form.append('resumeId', resumeId)
    return (await api.post<Resume>('/resumes', form)).data
  },
  reviewResume: async (id: string, versionId: string, input: { email: string | null; phone: string | null; skills: string[] }) =>
    (await api.put<ResumeVersion>(`/resumes/${id}/versions/${versionId}/review`, input)).data,
  deleteResume: async (id: string) => (await api.delete(`/resumes/${id}`)).data,
  downloadResume: async (id: string, versionId: string) => (await api.get<Blob>(`/resumes/${id}/versions/${versionId}/download`, { responseType: 'blob' })).data,
  matchResume: async (resumeVersionId: string, jobId: string) => (await api.post<MatchResult>('/matches', { resumeVersionId, jobId })).data,
  coverLetters: async () => (await api.get<Page<CoverLetter>>('/cover-letters', { params: { page: 0, size: 20 } })).data,
  coverLetter: async (id: string) => (await api.get<CoverLetter>(`/cover-letters/${id}`)).data,
  generateCoverLetter: async (jobId: string, resumeVersionId: string, tone: string) => (await api.post<CoverLetter>('/cover-letters/draft', { jobId, resumeVersionId, tone })).data,
  saveCoverLetter: async (id: string, body: string) => (await api.put<CoverLetter>(`/cover-letters/${id}`, { body })).data,
  interviewQuestions: async (topic: string, difficulty: string) => (await api.get<InterviewQuestion[]>('/interviews/questions', { params: { topic, difficulty } })).data,
  startInterview: async (role: string, topic: string, difficulty: string) => (await api.post<InterviewSession>('/interviews/sessions', { role, topic, difficulty })).data,
  nextInterviewQuestion: async (sessionId: string) => (await api.get<InterviewQuestion | null>(`/interviews/sessions/${sessionId}/next`)).data,
  answerInterview: async (sessionId: string, questionId: string, answer: string) => (await api.post<InterviewAnswer>(`/interviews/sessions/${sessionId}/answers`, { questionId, answer })).data,
  interviewHistory: async () => (await api.get<Page<InterviewSession>>('/interviews/sessions', { params: { page: 0, size: 20 } })).data,
  interviewSession: async (id: string) => (await api.get<InterviewSessionDetail>(`/interviews/sessions/${id}`)).data,
  profile: async () => (await api.get<UserProfile>('/users/me/profile')).data,
  updateProfile: async (input: { name: string; headline: string; location: string; summary: string }) => (await api.patch<UserProfile>('/users/me/profile', input)).data,
  changePassword: async (currentPassword: string, newPassword: string) => (await api.post('/users/me/password', { currentPassword, newPassword })).data,
}
