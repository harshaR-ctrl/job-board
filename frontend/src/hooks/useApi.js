import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../api/axiosInstance';

// ─── Auth Hooks ───
export function useRegister() {
  return useMutation({
    mutationFn: (data) => axiosInstance.post('/auth/register', data).then(r => r.data),
  });
}

export function useLogin() {
  return useMutation({
    mutationFn: (data) => axiosInstance.post('/auth/login', data).then(r => r.data),
  });
}

// ─── Job Hooks ───
export function useJobs(params = {}) {
  const queryString = new URLSearchParams(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== '' && v !== null)
  ).toString();

  return useQuery({
    queryKey: ['jobs', params],
    queryFn: () => axiosInstance.get(`/jobs?${queryString}`).then(r => r.data),
  });
}

export function useJob(id) {
  return useQuery({
    queryKey: ['job', id],
    queryFn: () => axiosInstance.get(`/jobs/${id}`).then(r => r.data),
    enabled: !!id,
  });
}

export function useMyJobs(page = 0, size = 10) {
  return useQuery({
    queryKey: ['myJobs', page, size],
    queryFn: () => axiosInstance.get(`/jobs/employer/mine?page=${page}&size=${size}`).then(r => r.data),
  });
}

export function useCreateJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data) => axiosInstance.post('/jobs', data).then(r => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['myJobs'] });
      qc.invalidateQueries({ queryKey: ['jobs'] });
    },
  });
}

export function useUpdateJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => axiosInstance.put(`/jobs/${id}`, data).then(r => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['myJobs'] });
      qc.invalidateQueries({ queryKey: ['jobs'] });
    },
  });
}

export function useCloseJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => axiosInstance.patch(`/jobs/${id}/close`).then(r => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['myJobs'] });
      qc.invalidateQueries({ queryKey: ['jobs'] });
    },
  });
}

// ─── Application Hooks ───
export function useApplyToJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (jobId) => axiosInstance.post(`/applications/${jobId}`).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['myApplications'] }),
  });
}

export function useMyApplications() {
  return useQuery({
    queryKey: ['myApplications'],
    queryFn: () => axiosInstance.get('/applications/my').then(r => r.data),
  });
}

export function useJobApplications(jobId) {
  return useQuery({
    queryKey: ['jobApplications', jobId],
    queryFn: () => axiosInstance.get(`/applications/job/${jobId}`).then(r => r.data),
    enabled: !!jobId,
  });
}

export function useUpdateApplicationStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }) => axiosInstance.patch(`/applications/${id}/status`, { status }).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['jobApplications'] }),
  });
}

// ─── Candidate Profile Hooks ───
export function useProfile() {
  return useQuery({
    queryKey: ['profile'],
    queryFn: () => axiosInstance.get('/candidates/profile').then(r => r.data),
  });
}

export function useUpdateProfile() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data) => axiosInstance.put('/candidates/profile', data).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['profile'] }),
  });
}

export function useUploadResume() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (file) => {
      const formData = new FormData();
      formData.append('file', file);
      return axiosInstance.post('/candidates/profile/resume', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      }).then(r => r.data);
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['profile'] }),
  });
}

// ─── Application Withdrawal Hooks ───
export function useWithdrawApplication() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => axiosInstance.patch(`/applications/${id}/withdraw`).then(r => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['myApplications'] });
      qc.invalidateQueries({ queryKey: ['jobApplications'] });
    },
  });
}

// ─── Email Verification Hooks ───
export function useVerifyEmail() {
  return useMutation({
    mutationFn: (token) => axiosInstance.post(`/auth/verify-email?token=${token}`).then(r => r.data),
  });
}

export function useResendVerification() {
  return useMutation({
    mutationFn: (data) => axiosInstance.post('/auth/resend-verification', data).then(r => r.data),
  });
}

// ─── Password Reset Hooks ───
export function useForgotPassword() {
  return useMutation({
    mutationFn: (data) => axiosInstance.post('/auth/forgot-password', data).then(r => r.data),
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: (data) => axiosInstance.post('/auth/reset-password', data).then(r => r.data),
  });
}

// ─── Employer Profile Hooks ───
export function useEmployerProfile() {
  return useQuery({
    queryKey: ['employerProfile'],
    queryFn: () => axiosInstance.get('/employers/profile').then(r => r.data),
  });
}

export function useUpdateEmployerProfile() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data) => axiosInstance.put('/employers/profile', data).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['employerProfile'] }),
  });
}
