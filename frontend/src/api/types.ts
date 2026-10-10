export type User = { id: string; email: string; name: string }
export type AuthResponse = { user: User; accessToken: string; tokenType: 'Bearer'; expiresIn: number }
export type Job = {
  id: string; title: string; company: string; location: string; employmentType: string
  description: string; skills: string[]; source: string; sample: boolean; saved: boolean
}
export type ApplicationStatus = 'SAVED' | 'APPLIED' | 'SCREENING' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN'
export type Application = {
  id: string; title: string; company: string; status: ApplicationStatus
  appliedAt: string | null; updatedAt: string
}
export type Page<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number }
export type AnalyticsSummary = {
  totalApplications: number; interviews: number; offers: number; practiceSessions: number; answersSubmitted: number
  applicationsByStatus: Record<ApplicationStatus, number>; recentActivity: Array<{ id: string; description: string; occurredAt: string }>
}
export type ResumeVersion = {
  id: string; versionNumber: number; originalFileName: string; contentType: string
  extractedEmail: string | null; extractedPhone: string | null; skills: string[]
  reviewed: boolean; createdAt: string; extractedText: string
}
export type Resume = { id: string; title: string; activeVersionNumber: number; createdAt: string; updatedAt: string; versions: ResumeVersion[] }
export type ResumeSummary = { id: string; title: string; activeVersionNumber: number; createdAt: string; updatedAt: string; versionCount: number; latestFileName: string | null }
export type MatchEvidence = { skill: string; resumeEvidence: string | null; jobEvidence: string }
export type MatchResult = {
  id: string; resumeVersionId: string; jobId: string; score: number; formulaVersion: string
  requiredSkillCoverage: number; matchedSkills: MatchEvidence[]; missingSkills: MatchEvidence[]
  partialSkills: MatchEvidence[]; recommendations: string[]; explanation: string; createdAt: string
}
export type CoverLetter = { id: string; jobId: string; title: string; body: string; generationSource: string; createdAt: string; updatedAt: string }
export type InterviewQuestion = { id: string; topic: string; difficulty: string; prompt: string }
export type InterviewSession = { id: string; role: string; topic: string; difficulty: string; state: string; startedAt: string; endedAt: string | null; averageScore: number | null; answerCount: number }
export type InterviewAnswer = { id: string; questionId: string; ordinal: number; question: string; answer: string; contentCoverageScore: number; feedback: string; evaluationSource: string; modelAnswer: string; createdAt: string }
export type InterviewSessionDetail = { session: InterviewSession; answers: InterviewAnswer[] }
export type UserProfile = { id: string; email: string; name: string; headline: string | null; location: string | null; summary: string | null; updatedAt: string | null }
export type PortalJob = { key: string; title: string; department: string; location: string; description: string; requirements: string[]; questions: string[] }
export type PortalSubmission = { confirmationId: string; portalKey: string; jobKey: string; jobTitle: string; createdAt: string; reused: boolean }
export type PortalVerification = Omit<PortalSubmission, 'reused'> & { status: string }
export type AutomationRunStatus = 'PENDING' | 'RUNNING' | 'VERIFIED_SUCCESS' | 'FAILED' | 'UNVERIFIED'
export type AutomationRun = {
  id: string; portalKey: 'PORTAL_A' | 'PORTAL_B' | 'PORTAL_C'; jobKey: string
  status: AutomationRunStatus; confirmationId: string | null; message: string | null
  createdAt: string; startedAt: string | null; completedAt: string | null
}
export type StartAutomationRun = {
  portalKey: AutomationRun['portalKey']; jobKey: string; resumeVersionId: string
  education: string; experience: string; coverLetter: string
  answers: Record<string, string>; consent: true
}
