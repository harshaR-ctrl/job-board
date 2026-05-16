import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useEmployerProfile, useUpdateEmployerProfile } from '../hooks/useApi';
import toast from 'react-hot-toast';
import { Building2, Globe, FileText, Save } from 'lucide-react';

const profileSchema = z.object({
  companyName: z.string().min(1, 'Company name is required').max(200),
  website: z.string().max(500).optional().or(z.literal('')),
  description: z.string().optional().or(z.literal('')),
});

export default function EmployerCompanyProfile() {
  const { data, isLoading } = useEmployerProfile();
  const updateProfileMutation = useUpdateEmployerProfile();

  const profile = data?.data;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(profileSchema),
    values: profile
      ? {
          companyName: profile.companyName || '',
          website: profile.website || '',
          description: profile.description || '',
        }
      : undefined,
  });

  const onSubmit = async (formData) => {
    try {
      const response = await updateProfileMutation.mutateAsync(formData);
      if (response.success) {
        toast.success('Company profile updated successfully!');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update profile');
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <div className="w-10 h-10 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto animate-fade-in">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-dark-900">Company Profile</h1>
        <p className="text-dark-900 mt-1">Manage your company information</p>
      </div>

      {/* Company Info Card */}
      {profile && (
        <div className="glass-card p-6 mb-6 flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-primary-100 flex items-center justify-center text-primary-700 text-2xl font-bold shadow-lg shadow-primary-200">
            {profile.companyName?.[0]?.toUpperCase() || 'C'}
          </div>
          <div>
            <h2 className="text-xl font-semibold text-dark-900">{profile.companyName}</h2>
            <p className="text-dark-900">{profile.email}</p>
          </div>
        </div>
      )}

      {/* Profile Form */}
      <div className="glass-card p-6">
        <h3 className="text-lg font-semibold text-dark-900 mb-6 flex items-center gap-2">
          <Building2 className="w-5 h-5 text-primary-400" />
          Company Information
        </h3>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label htmlFor="company-name" className="label-text flex items-center gap-2">
              <Building2 className="w-4 h-4" /> Company Name
            </label>
            <input
              id="company-name"
              type="text"
              className="input-field"
              placeholder="Acme Corp"
              {...register('companyName')}
            />
            {errors.companyName && <p className="mt-1 text-sm text-red-400">{errors.companyName.message}</p>}
          </div>

          <div>
            <label htmlFor="company-website" className="label-text flex items-center gap-2">
              <Globe className="w-4 h-4" /> Website
            </label>
            <input
              id="company-website"
              type="url"
              className="input-field"
              placeholder="https://acmecorp.com"
              {...register('website')}
            />
            {errors.website && <p className="mt-1 text-sm text-red-400">{errors.website.message}</p>}
          </div>

          <div>
            <label htmlFor="company-description" className="label-text flex items-center gap-2">
              <FileText className="w-4 h-4" /> Description
            </label>
            <textarea
              id="company-description"
              rows={4}
              className="input-field resize-none"
              placeholder="Tell candidates about your company, culture, and mission..."
              {...register('description')}
            />
            {errors.description && <p className="mt-1 text-sm text-red-400">{errors.description.message}</p>}
          </div>

          <button
            type="submit"
            disabled={updateProfileMutation.isPending}
            className="btn-primary w-full flex items-center justify-center gap-2"
          >
            {updateProfileMutation.isPending ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <Save className="w-4 h-4" />
                Save Profile
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
