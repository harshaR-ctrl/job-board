import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { useVerifyEmail } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { Mail, CheckCircle, XCircle, Loader } from 'lucide-react';

export default function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const verifyMutation = useVerifyEmail();
  const { user } = useAuth();
  const [status, setStatus] = useState('verifying');

  useEffect(() => {
    if (!token) {
      setStatus('no-token');
      return;
    }
    verifyMutation.mutateAsync(token)
      .then(() => {
        const stored = JSON.parse(localStorage.getItem('user') || '{}');
        stored.verified = true;
        localStorage.setItem('user', JSON.stringify(stored));
        setStatus('success');
      })
      .catch(() => setStatus('error'));
  }, [token, verifyMutation]);

  return (
    <div className="min-h-[80vh] flex items-center justify-center animate-fade-in">
      <div className="w-full max-w-md">
        <div className="glass-card p-8 text-center">
          {status === 'verifying' && (
            <>
              <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center">
                <Loader className="w-8 h-8 text-white animate-spin" />
              </div>
              <h2 className="text-2xl font-bold text-white mb-2">Verifying Email...</h2>
              <p className="text-dark-200">Please wait while we verify your email address.</p>
            </>
          )}

          {status === 'success' && (
            <>
              <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-green-500 to-emerald-500 flex items-center justify-center">
                <CheckCircle className="w-8 h-8 text-white" />
              </div>
              <h2 className="text-2xl font-bold text-white mb-2">Email Verified!</h2>
              <p className="text-dark-200 mb-6">Your email has been successfully verified.</p>
              <Link to={user?.role === 'EMPLOYER' ? '/employer/dashboard' : '/'} className="btn-primary inline-flex items-center gap-2">
                Go to Dashboard
              </Link>
            </>
          )}

          {status === 'error' && (
            <>
              <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-red-500 to-rose-500 flex items-center justify-center">
                <XCircle className="w-8 h-8 text-white" />
              </div>
              <h2 className="text-2xl font-bold text-white mb-2">Verification Failed</h2>
              <p className="text-dark-200 mb-6">The verification link is invalid or has expired.</p>
              <Link to="/login" className="btn-primary inline-flex items-center gap-2">
                Back to Login
              </Link>
            </>
          )}

          {status === 'no-token' && (
            <>
              <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-yellow-500 to-orange-500 flex items-center justify-center">
                <Mail className="w-8 h-8 text-white" />
              </div>
              <h2 className="text-2xl font-bold text-white mb-2">No Token Found</h2>
              <p className="text-dark-200 mb-6">The verification link is missing a token.</p>
              <Link to="/login" className="btn-primary inline-flex items-center gap-2">
                Back to Login
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
