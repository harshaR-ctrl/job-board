import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  useMyJobs,
  useCreateJob,
  useCloseJob,
  useJobApplications,
  useUpdateApplicationStatus,
} from '../hooks/useApi';
import toast from 'react-hot-toast';
import { Plus, X, Users, Briefcase, MapPin, DollarSign, XCircle } from 'lucide-react';

const jobSchema = z.object({
  title: z.string().min(1, 'Title is required').max(200),
  description: z.string().min(1, 'Description is required'),
  location: z.string().min(1, 'Location is required').max(200),
  salaryMin: z.coerce.number().min(0).optional().or(z.literal('')),
  salaryMax: z.coerce.number().min(0).optional().or(z.literal('')),
});

export default function EmployerDashboard() {
  const [activeTab, setActiveTab] = useState('listings');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [selectedJobId, setSelectedJobId] = useState(null);
  const [page, setPage] = useState(0);

  const { data: jobsData, isLoading: jobsLoading } = useMyJobs(page, 10);
  const createJobMutation = useCreateJob();
  const closeJobMutation = useCloseJob();

  const jobs = jobsData?.data?.content || [];
  const totalPages = jobsData?.data?.totalPages || 0;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(jobSchema),
  });

  const onCreateJob = async (data) => {
    try {
      const payload = {
        ...data,
        salaryMin: data.salaryMin || null,
        salaryMax: data.salaryMax || null,
      };
      const response = await createJobMutation.mutateAsync(payload);
      if (response.success) {
        toast.success('Job created successfully!');
        setShowCreateForm(false);
        reset();
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create job');
    }
  };

  const handleCloseJob = async (jobId) => {
    try {
      await closeJobMutation.mutateAsync(jobId);
      toast.success('Job closed successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to close job');
    }
  };

  return (
    <div className="animate-fade-in">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-dark-900">Employer Dashboard</h1>
          <p className="text-gray-600 mt-1">Manage your job listings and applicants</p>
        </div>
        <button
          onClick={() => setShowCreateForm(true)}
          className="btn-primary flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          Post New Job
        </button>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6">
        {[
          { key: 'listings', label: 'My Listings', icon: Briefcase },
          { key: 'applicants', label: 'Applicants', icon: Users },
        ].map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => setActiveTab(key)}
            className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
              activeTab === key
                ? 'bg-primary-600 text-white shadow-lg shadow-primary-900/30'
                : 'bg-dark-600 text-gray-600 hover:bg-dark-500'
            }`}
          >
            <Icon className="w-4 h-4" />
            {label}
          </button>
        ))}
      </div>

      {/* Create Job Modal */}
      {showCreateForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
          <div className="glass-card p-8 w-full max-w-lg mx-4 animate-scale-in">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-gray-900">Create Job Listing</h2>
              <button onClick={() => setShowCreateForm(false)} className="text-gray-600 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSubmit(onCreateJob)} className="space-y-4">
              <div>
                <label htmlFor="job-title" className="label-text">Job Title</label>
                <input id="job-title" className="input-field" placeholder="e.g. Senior React Developer" {...register('title')} />
                {errors.title && <p className="mt-1 text-sm text-red-400">{errors.title.message}</p>}
              </div>
              <div>
                <label htmlFor="job-desc" className="label-text">Description</label>
                <textarea id="job-desc" rows={4} className="input-field resize-none" placeholder="Describe the role, requirements..." {...register('description')} />
                {errors.description && <p className="mt-1 text-sm text-red-400">{errors.description.message}</p>}
              </div>
              <div>
                <label htmlFor="job-location" className="label-text">Location</label>
                <input id="job-location" className="input-field" placeholder="e.g. Remote, San Francisco, CA" {...register('location')} />
                {errors.location && <p className="mt-1 text-sm text-red-400">{errors.location.message}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="job-min-salary" className="label-text">Min Salary ($)</label>
                  <input id="job-min-salary" type="number" className="input-field" placeholder="80000" {...register('salaryMin')} />
                </div>
                <div>
                  <label htmlFor="job-max-salary" className="label-text">Max Salary ($)</label>
                  <input id="job-max-salary" type="number" className="input-field" placeholder="120000" {...register('salaryMax')} />
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="submit" disabled={createJobMutation.isPending} className="btn-primary flex-1">
                  {createJobMutation.isPending ? 'Creating...' : 'Create Job'}
                </button>
                <button type="button" onClick={() => setShowCreateForm(false)} className="btn-secondary">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Tab Content */}
      {activeTab === 'listings' && (
        <>
          {jobsLoading ? (
            <div className="flex justify-center py-12">
              <div className="w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : jobs.length === 0 ? (
            <div className="text-center py-16 glass-card">
              <Briefcase className="w-12 h-12 mx-auto text-gray-400 mb-3" />
              <p className="text-gray-600">No job listings yet. Create your first posting!</p>
            </div>
          ) : (
            <div className="space-y-4">
              {jobs.map((job) => (
                <div key={job.id} className="glass-card p-6 flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">{job.title}</h3>
                      <span className={`status-badge status-${job.status}`}>{job.status}</span>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-gray-600">
                      <span className="flex items-center gap-1"><MapPin className="w-3.5 h-3.5" /> {job.location}</span>
                      {job.salaryMin && (
                        <span className="flex items-center gap-1">
                          <DollarSign className="w-3.5 h-3.5" />
                          {Number(job.salaryMin).toLocaleString()} – {Number(job.salaryMax).toLocaleString()}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <button
                      onClick={() => {
                        setSelectedJobId(job.id);
                        setActiveTab('applicants');
                      }}
                      className="btn-secondary py-2 px-4 text-sm flex items-center gap-1"
                    >
                      <Users className="w-4 h-4" /> Applicants
                    </button>
                    {job.status === 'OPEN' && (
                      <button
                        onClick={() => handleCloseJob(job.id)}
                        className="btn-danger py-2 px-4 text-sm flex items-center gap-1"
                      >
                        <XCircle className="w-4 h-4" /> Close
                      </button>
                    )}
                  </div>
                </div>
              ))}

              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-4 pt-4">
                  <button
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="btn-secondary py-2 px-4 text-sm disabled:opacity-30"
                  >
                    Previous
                  </button>
                  <span className="text-sm text-gray-600">Page {page + 1} of {totalPages}</span>
                  <button
                    onClick={() => setPage((p) => p + 1)}
                    disabled={page >= totalPages - 1}
                    className="btn-secondary py-2 px-4 text-sm disabled:opacity-30"
                  >
                    Next
                  </button>
                </div>
              )}
            </div>
          )}
        </>
      )}

      {activeTab === 'applicants' && (
        <ApplicantsPanel
          selectedJobId={selectedJobId}
          onSelectJob={setSelectedJobId}
          jobs={jobs}
        />
      )}
    </div>
  );
}

function ApplicantsPanel({ selectedJobId, onSelectJob, jobs }) {
  const { data, isLoading } = useJobApplications(selectedJobId);
  const updateStatusMutation = useUpdateApplicationStatus();

  const applicants = data?.data || [];

  const handleStatusChange = async (appId, newStatus) => {
    try {
      await updateStatusMutation.mutateAsync({ id: appId, status: newStatus });
      toast.success(`Status updated to ${newStatus}`);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update status');
    }
  };

  return (
    <div>
      {/* Job Selector */}
      <div className="mb-6">
        <label htmlFor="applicants-job-select" className="label-text">Select Job Listing</label>
        <select
          id="applicants-job-select"
          className="input-field"
          value={selectedJobId || ''}
          onChange={(e) => onSelectJob(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">— Select a job —</option>
          {jobs.map((job) => (
            <option key={job.id} value={job.id}>
              {job.title} ({job.status})
            </option>
          ))}
        </select>
      </div>

      {!selectedJobId ? (
        <div className="text-center py-12 glass-card">
          <Users className="w-12 h-12 mx-auto text-gray-400 mb-3" />
          <p className="text-gray-600">Select a job listing to view its applicants</p>
        </div>
      ) : isLoading ? (
        <div className="flex justify-center py-12">
          <div className="w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : applicants.length === 0 ? (
        <div className="text-center py-12 glass-card">
          <Users className="w-12 h-12 mx-auto text-gray-400 mb-3" />
          <p className="text-gray-600">No applicants for this listing yet</p>
        </div>
      ) : (
        <div className="space-y-3">
          {applicants.map((app) => (
            <div key={app.id} className="glass-card p-5 flex items-center justify-between">
              <div>
                <p className="font-semibold text-gray-900">{app.candidateName}</p>
                <p className="text-sm text-gray-600">{app.candidateEmail}</p>
                <p className="text-xs text-gray-300 mt-1">Applied: {new Date(app.appliedAt).toLocaleDateString()}</p>
              </div>
              <div className="flex items-center gap-3">
                <span className={`status-badge status-${app.status}`}>{app.status}</span>
                <select
                  className="input-field py-2 px-3 text-sm w-40"
                  value={app.status}
                  onChange={(e) => handleStatusChange(app.id, e.target.value)}
                >
                  <option value="APPLIED">Applied</option>
                  <option value="SHORTLISTED">Shortlisted</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="HIRED">Hired</option>
                </select>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
