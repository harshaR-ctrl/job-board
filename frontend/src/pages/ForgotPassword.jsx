import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForgotPassword } from '../hooks/useApi';
import toast from 'react-hot-toast';
import { Mail, ArrowLeft, Send } from 'lucide-react';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const forgotMutation = useForgotPassword();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await forgotMutation.mutateAsync({ email });
      setSent(true);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to send reset email');
    }
  };

  if (sent) {
    return (
      <div className="max-w-md mx-auto mt-20 animate-fade-in">
        <div className="glass-card p-8 text-center">
          <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-primary-600 flex items-center justify-center">
            <Mail className="w-8 h-8 text-white" />
          </div>
          <h2 className="text-2xl font-bold text-dark-900 mb-2">Check Your Email</h2>
          <p className="text-dark-900 mb-6">
            If an account with that email exists, we've sent a password reset link.
          </p>
          <Link to="/login" className="btn-primary inline-flex items-center gap-2">
            <ArrowLeft className="w-4 h-4" /> Back to Login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto mt-20 animate-fade-in">
      <div className="glass-card p-8">
        <div className="text-center mb-8">
          <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-primary-600 flex items-center justify-center">
            <Mail className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-dark-900">Forgot Password?</h1>
          <p className="text-dark-900 mt-2">Enter your email and we'll send you a reset link</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label htmlFor="fp-email" className="label-text">Email Address</label>
            <input
              id="fp-email"
              type="email"
              className="input-field"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <button
            type="submit"
            disabled={forgotMutation.isPending}
            className="btn-primary w-full flex items-center justify-center gap-2"
          >
            {forgotMutation.isPending ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <Send className="w-4 h-4" /> Send Reset Link
              </>
            )}
          </button>
        </form>

        <div className="mt-6 text-center">
          <Link to="/login" className="text-sm text-primary-600 hover:text-primary-700 flex items-center justify-center gap-1">
            <ArrowLeft className="w-4 h-4" /> Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
}
