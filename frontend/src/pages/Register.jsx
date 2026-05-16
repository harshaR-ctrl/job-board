import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useRegister } from '../hooks/useApi';
import toast from 'react-hot-toast';
import { UserPlus, Mail, Lock, User, Building2, Globe } from 'lucide-react';

const registerSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters').max(100),
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters').max(100),
  role: z.enum(['CANDIDATE', 'EMPLOYER'], { required_error: 'Please select a role' }),
  companyName: z.string().optional(),
  website: z.string().optional(),
});

export default function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const registerMutation = useRegister();

  const {
    register: reg,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: { role: 'CANDIDATE' },
  });

  const selectedRole = watch('role');

  const onSubmit = async (data) => {
    try {
      const response = await registerMutation.mutateAsync(data);
      if (response.success) {
        login(response.data);
        toast.success('Account created successfully!');
        navigate(data.role === 'EMPLOYER' ? '/employer/dashboard' : '/');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center animate-fade-in">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-primary-600 flex items-center justify-center shadow-xl shadow-primary-600/30">
            <UserPlus className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-dark-900 mb-2">Create Account</h1>
          <p className="text-dark-900">Join the platform and start your journey</p>
        </div>

        {/* Form */}
        <div className="glass-card p-8">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            {/* Role Selector */}
            <div>
              <label className="label-text">I am a</label>
              <div className="grid grid-cols-2 gap-3">
                {['CANDIDATE', 'EMPLOYER'].map((role) => (
                  <label
                    key={role}
                    className={`flex items-center justify-center gap-2 px-4 py-3 rounded-xl border cursor-pointer transition-all duration-200 ${
                      selectedRole === role
                        ? 'border-primary-500 bg-primary-50 text-primary-700'
                        : 'border-gray-300 bg-white text-dark-700 hover:border-gray-400'
                    }`}
                  >
                    <input
                      type="radio"
                      value={role}
                      className="sr-only"
                      {...reg('role')}
                    />
                    {role === 'CANDIDATE' ? <User className="w-4 h-4" /> : <Building2 className="w-4 h-4" />}
                    <span className="text-sm font-medium">{role === 'CANDIDATE' ? 'Candidate' : 'Employer'}</span>
                  </label>
                ))}
              </div>
              {errors.role && <p className="mt-1 text-sm text-red-400">{errors.role.message}</p>}
            </div>

            <div>
              <label htmlFor="name" className="label-text">Full Name</label>
              <div className="relative">
                <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-dark-400" />
                <input
                  id="name"
                  type="text"
                  placeholder="John Doe"
                  className="input-field pl-11"
                  {...reg('name')}
                />
              </div>
              {errors.name && <p className="mt-1 text-sm text-red-400">{errors.name.message}</p>}
            </div>

            <div>
              <label htmlFor="reg-email" className="label-text">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-dark-400" />
                <input
                  id="reg-email"
                  type="email"
                  placeholder="you@example.com"
                  className="input-field pl-11"
                  {...reg('email')}
                />
              </div>
              {errors.email && <p className="mt-1 text-sm text-red-400">{errors.email.message}</p>}
            </div>

            <div>
              <label htmlFor="reg-password" className="label-text">Password</label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-dark-400" />
                <input
                  id="reg-password"
                  type="password"
                  placeholder="••••••••"
                  className="input-field pl-11"
                  {...reg('password')}
                />
              </div>
              {errors.password && <p className="mt-1 text-sm text-red-400">{errors.password.message}</p>}
            </div>

            {/* Employer-specific fields */}
            {selectedRole === 'EMPLOYER' && (
              <>
                <div>
                  <label htmlFor="companyName" className="label-text">Company Name</label>
                  <div className="relative">
                    <Building2 className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-dark-400" />
                    <input
                      id="companyName"
                      type="text"
                      placeholder="Acme Corp"
                      className="input-field pl-11"
                      {...reg('companyName')}
                    />
                  </div>
                </div>
                <div>
                  <label htmlFor="website" className="label-text">Website (optional)</label>
                  <div className="relative">
                    <Globe className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-dark-400" />
                    <input
                      id="website"
                      type="url"
                      placeholder="https://acme.com"
                      className="input-field pl-11"
                      {...reg('website')}
                    />
                  </div>
                </div>
              </>
            )}

            <button
              type="submit"
              disabled={registerMutation.isPending}
              className="btn-primary w-full flex items-center justify-center gap-2"
            >
              {registerMutation.isPending ? (
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <>
                  <UserPlus className="w-4 h-4" />
                  Create Account
                </>
              )}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-dark-900 text-sm">
              Already have an account?{' '}
              <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
