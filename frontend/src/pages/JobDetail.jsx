import { useParams, useNavigate } from 'react-router-dom';
import { useJob, useApplyToJob } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { MapPin, DollarSign, Calendar, Building2, ArrowLeft, Send, Clock } from 'lucide-react';

export default function JobDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isCandidate } = useAuth();
  const { data, isLoading, error } = useJob(id);
  const applyMutation = useApplyToJob();

  const job = data?.data;

  const formatSalary = (amount) => {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const handleApply = async () => {
    if (!user) {
      toast.error('Please log in to apply');
      navigate('/login');
      return;
    }
    try {
      const response = await applyMutation.mutateAsync(id);
      if (response.success) {
        toast.success('Application submitted successfully!');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to apply');
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <div className="w-10 h-10 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (error || !job) {
    return (
      <div className="text-center py-20">
        <p className="text-dark-200">Job not found.</p>
        <button onClick={() => navigate('/')} className="btn-primary mt-4">
          Back to Jobs
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto animate-fade-in">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-dark-200 hover:text-white transition-colors mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back
      </button>

      {/* Job Header Card */}
      <div className="glass-card p-8 mb-6">
        <div className="flex items-start justify-between mb-6">
          <div>
            <div className="flex items-center gap-3 mb-3">
              <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-primary-600/20 to-accent-600/20 flex items-center justify-center border border-primary-500/20">
                <Building2 className="w-7 h-7 text-primary-400" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-dark-900">{job.title}</h1>
                <p className="text-primary-400 font-medium">{job.companyName}</p>
              </div>
            </div>
          </div>
          <span className={`status-badge status-${job.status}`}>{job.status}</span>
        </div>

        {/* Job Meta */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div className="flex items-center gap-3 p-4 rounded-xl bg-dark-700/50">
            <MapPin className="w-5 h-5 text-primary-400" />
            <div>
              <p className="text-xs text-dark-200">Location</p>
              <p className="text-sm font-medium text-white">{job.location}</p>
            </div>
          </div>
          <div className="flex items-center gap-3 p-4 rounded-xl bg-dark-700/50">
            <DollarSign className="w-5 h-5 text-green-400" />
            <div>
              <p className="text-xs text-dark-200">Salary Range</p>
              <p className="text-sm font-medium text-white">
                {formatSalary(job.salaryMin)} – {formatSalary(job.salaryMax)}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-3 p-4 rounded-xl bg-dark-700/50">
            <Calendar className="w-5 h-5 text-accent-400" />
            <div>
              <p className="text-xs text-dark-200">Posted</p>
              <p className="text-sm font-medium text-white">{formatDate(job.postedAt)}</p>
            </div>
          </div>
          <div className="flex items-center gap-3 p-4 rounded-xl bg-dark-700/50">
            <Clock className="w-5 h-5 text-yellow-400" />
            <div>
              <p className="text-xs text-dark-200">Updated</p>
              <p className="text-sm font-medium text-white">{formatDate(job.updatedAt)}</p>
            </div>
          </div>
        </div>

        {/* Apply Button */}
        {isCandidate() && job.status === 'OPEN' && (
          <button
            onClick={handleApply}
            disabled={applyMutation.isPending}
            className="btn-primary flex items-center gap-2"
          >
            {applyMutation.isPending ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <Send className="w-4 h-4" />
                Apply Now
              </>
            )}
          </button>
        )}

        {!user && job.status === 'OPEN' && (
          <button
            onClick={() => navigate('/login')}
            className="btn-primary flex items-center gap-2"
          >
            <Send className="w-4 h-4" />
            Login to Apply
          </button>
        )}
      </div>

      {/* Job Description */}
      <div className="glass-card p-8">
        <h2 className="text-xl font-semibold text-dark-900 mb-4">Job Description</h2>
        <div className="prose prose-invert max-w-none">
          {job.description.split('\n').map((paragraph, i) => (
            <p key={i} className="text-dark-700 leading-relaxed mb-4">
              {paragraph}
            </p>
          ))}
        </div>
      </div>
    </div>
  );
}
