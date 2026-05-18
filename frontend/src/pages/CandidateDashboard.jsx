import { useMyApplications, useWithdrawApplication } from '../hooks/useApi';
import { Link } from 'react-router-dom';
import { FileText, Building2, Calendar, ExternalLink, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CandidateDashboard() {
  const { data, isLoading, error } = useMyApplications();
  const withdrawMutation = useWithdrawApplication();
  const applications = data?.data || [];

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const statusStats = applications.reduce(
    (acc, app) => {
      acc[app.status] = (acc[app.status] || 0) + 1;
      return acc;
    },
    {}
  );

  return (
    <div className="animate-fade-in">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">My Applications</h1>
        <p className="text-gray-600 mt-1">Track the status of all your job applications</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
        {[
          { label: 'Total', value: applications.length, colorClass: 'text-primary-600' },
          { label: 'Shortlisted', value: statusStats.SHORTLISTED || 0, colorClass: 'text-blue-600' },
          { label: 'Hired', value: statusStats.HIRED || 0, colorClass: 'text-green-600' },
          { label: 'Pending', value: statusStats.APPLIED || 0, colorClass: 'text-yellow-600' },
          { label: 'Withdrawn', value: statusStats.WITHDRAWN || 0, colorClass: 'text-red-600' },
        ].map(({ label, value, colorClass }) => (
          <div key={label} className="glass-card p-5 text-center">
            <div className={`text-3xl font-bold ${colorClass}`}>
              {value}
            </div>
            <p className="text-sm text-gray-700 mt-1">{label}</p>
          </div>
        ))}
      </div>

      {/* Applications Table */}
      {isLoading ? (
        <div className="flex justify-center py-12">
          <div className="w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : error ? (
        <div className="text-center py-12 glass-card">
          <p className="text-gray-900">Failed to load applications. Please try again.</p>
        </div>
      ) : applications.length === 0 ? (
        <div className="text-center py-16 glass-card">
          <FileText className="w-12 h-12 mx-auto text-gray-400 mb-3" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No Applications Yet</h3>
          <p className="text-gray-900 mb-4">Start applying to jobs to see them here</p>
          <Link to="/" className="btn-primary inline-flex items-center gap-2">
            Browse Jobs
          </Link>
        </div>
      ) : (
        <div className="glass-card overflow-hidden">
          {/* Table Header */}
          <div className="hidden md:grid grid-cols-12 gap-4 p-4 border-b border-gray-200 text-sm font-medium text-gray-900 uppercase tracking-wider">
            <div className="col-span-4">Position</div>
            <div className="col-span-3">Company</div>
            <div className="col-span-2">Applied</div>
            <div className="col-span-2">Status</div>
            <div className="col-span-1"></div>
          </div>

          {/* Table Rows */}
          {applications.map((app, index) => (
            <div
              key={app.id}
              className="grid grid-cols-1 md:grid-cols-12 gap-4 p-4 border-b border-gray-200 last:border-0 hover:bg-primary-50 transition-colors animate-slide-up"
              style={{ animationDelay: `${index * 40}ms` }}
            >
              <div className="col-span-4 flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center border border-primary-300 shrink-0">
                  <FileText className="w-5 h-5 text-primary-600" />
                </div>
                <div className="min-w-0">
                  <p className="font-medium text-gray-900 truncate">{app.jobTitle}</p>
                </div>
              </div>

              <div className="col-span-3 flex items-center gap-2 text-gray-900">
                <Building2 className="w-4 h-4 text-gray-500 shrink-0" />
                <span className="truncate">{app.companyName}</span>
              </div>

              <div className="col-span-2 flex items-center gap-2 text-sm text-gray-900">
                <Calendar className="w-4 h-4 shrink-0" />
                {formatDate(app.appliedAt)}
              </div>

              <div className="col-span-2 flex items-center">
                <span className={`status-badge status-${app.status}`}>{app.status}</span>
              </div>

              <div className="col-span-1 flex items-center justify-end gap-2">
                {app.status === 'APPLIED' && (
                  <button
                    onClick={async () => {
                      try {
                        await withdrawMutation.mutateAsync(app.id);
                        toast.success('Application withdrawn');
                      } catch (err) {
                        toast.error(err.response?.data?.message || 'Failed to withdraw');
                      }
                    }}
                    className="p-2 rounded-lg bg-red-100 hover:bg-red-200 text-red-700 hover:text-red-800 transition-all"
                    title="Withdraw Application"
                  >
                    <XCircle className="w-4 h-4" />
                  </button>
                )}
                <Link
                  to={`/jobs/${app.jobId}`}
                  className="p-2 rounded-lg bg-gray-200 hover:bg-gray-300 text-gray-700 hover:text-gray-900 transition-all"
                  title="View Job"
                >
                  <ExternalLink className="w-4 h-4" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
