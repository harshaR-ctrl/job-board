import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useResendVerification } from '../hooks/useApi';
import { Briefcase, LogOut, User, LayoutDashboard, Building2, AlertTriangle, Send } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Navbar() {
  const { user, logout, isEmployer, isCandidate } = useAuth();
  const resendMutation = useResendVerification();
  const navigate = useNavigate();

  const handleResendVerification = async () => {
    try {
      await resendMutation.mutateAsync({ email: user.email });
      toast.success('Verification email sent!');
    } catch {
      toast.error('Failed to resend verification email');
    }
  };

  return (
    <nav className="glass sticky top-0 z-50 border-b border-primary-200 shadow-sm">
      {user && !user.verified && (
        <div className="bg-yellow-50 border-b border-yellow-200 px-6 py-2 flex items-center justify-center gap-3 text-sm">
          <AlertTriangle className="w-4 h-4 text-yellow-600 shrink-0" />
          <span className="text-yellow-800">Please verify your email address.</span>
          <button
            onClick={handleResendVerification}
            disabled={resendMutation.isPending}
            className="text-yellow-300 hover:text-yellow-100 underline underline-offset-2 flex items-center gap-1"
          >
            <Send className="w-3 h-3" />
            {resendMutation.isPending ? 'Sending...' : 'Resend'}
          </button>
        </div>
      )}
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3 group">
          <div className="w-10 h-10 rounded-xl bg-primary-600 flex items-center justify-center shadow-lg shadow-primary-600/30 group-hover:shadow-primary-700/40 transition-all duration-300">
            <Briefcase className="w-5 h-5 text-white" />
          </div>
          <span className="text-xl font-bold gradient-text">JobBoard</span>
        </Link>

        {/* Nav Links */}
        <div className="flex items-center gap-4">
          <Link
            to="/"
            className="text-sm font-medium text-dark-700 hover:text-primary-600 transition-colors duration-200"
          >
            Browse Jobs
          </Link>

          {user ? (
            <>
              {isEmployer() && (
                <>
                  <Link
                    to="/employer/dashboard"
                    className="flex items-center gap-2 text-sm font-medium text-dark-700 hover:text-primary-600 transition-colors duration-200"
                  >
                    <LayoutDashboard className="w-4 h-4" />
                    Dashboard
                  </Link>
                  <Link
                    to="/employer/profile"
                    className="flex items-center gap-2 text-sm font-medium text-dark-700 hover:text-primary-600 transition-colors duration-200"
                  >
                    <Building2 className="w-4 h-4" />
                    Company
                  </Link>
                </>
              )}
              {isCandidate() && (
                <>
                  <Link
                    to="/candidate/dashboard"
                    className="flex items-center gap-2 text-sm font-medium text-dark-700 hover:text-primary-600 transition-colors duration-200"
                  >
                    <LayoutDashboard className="w-4 h-4" />
                    My Applications
                  </Link>
                  <Link
                    to="/profile"
                    className="flex items-center gap-2 text-sm font-medium text-dark-700 hover:text-primary-600 transition-colors duration-200"
                  >
                    <User className="w-4 h-4" />
                    Profile
                  </Link>
                </>
              )}

              <div className="h-6 w-px bg-gray-300 mx-2" />

              <div className="flex items-center gap-3">
                <div className="text-right hidden sm:block">
                  <p className="text-sm font-medium text-dark-900">{user.name}</p>
                  <p className="text-xs text-dark-500">{user.role}</p>
                </div>
                <button
                  onClick={logout}
                  className="p-2 rounded-lg bg-gray-200 hover:bg-gray-300 text-dark-700 hover:text-dark-900 transition-all duration-200"
                  title="Logout"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-secondary text-sm py-2 px-4">
                Login
              </Link>
              <Link to="/register" className="btn-primary text-sm py-2 px-4">
                Get Started
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
