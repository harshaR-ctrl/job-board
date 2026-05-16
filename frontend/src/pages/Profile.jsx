import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useProfile, useUpdateProfile, useUploadResume } from '../hooks/useApi';
import toast from 'react-hot-toast';
import { User, Phone, Wrench, Clock, Upload, FileText, Save } from 'lucide-react';
import { useRef } from 'react';

const profileSchema = z.object({
  phone: z.string().max(20).optional().or(z.literal('')),
  skills: z.string().optional().or(z.literal('')),
  experienceYears: z.coerce.number().min(0).optional().or(z.literal('')),
});

export default function Profile() {
  const { data, isLoading } = useProfile();
  const updateProfileMutation = useUpdateProfile();
  const uploadResumeMutation = useUploadResume();
  const fileInputRef = useRef(null);

  const profile = data?.data;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(profileSchema),
    values: profile
      ? {
          phone: profile.phone || '',
          skills: profile.skills || '',
          experienceYears: profile.experienceYears || 0,
        }
      : undefined,
  });

  const onSubmit = async (formData) => {
    try {
      const response = await updateProfileMutation.mutateAsync(formData);
      if (response.success) {
        toast.success('Profile updated successfully!');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update profile');
    }
  };

  const handleResumeUpload = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      toast.error('File size must be less than 5MB');
      return;
    }

    try {
      const response = await uploadResumeMutation.mutateAsync(file);
      if (response.success) {
        toast.success('Resume uploaded successfully!');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to upload resume');
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
        <h1 className="text-3xl font-bold text-white">My Profile</h1>
        <p className="text-dark-200 mt-1">Manage your candidate profile and resume</p>
      </div>

      {/* Profile Info Card */}
      {profile && (
        <div className="glass-card p-6 mb-6 flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center text-white text-2xl font-bold shadow-lg">
            {profile.name?.[0]?.toUpperCase() || 'U'}
          </div>
          <div>
            <h2 className="text-xl font-semibold text-white">{profile.name}</h2>
            <p className="text-dark-200">{profile.email}</p>
          </div>
        </div>
      )}

      {/* Resume Upload */}
      <div className="glass-card p-6 mb-6">
        <h3 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
          <FileText className="w-5 h-5 text-primary-400" />
          Resume
        </h3>
        {profile?.resumeUrl ? (
          <div className="flex items-center gap-3 p-4 rounded-xl bg-dark-700/50 mb-4">
            <FileText className="w-5 h-5 text-green-400" />
            <span className="text-dark-100 text-sm flex-1">Resume uploaded: {profile.resumeUrl}</span>
          </div>
        ) : (
          <p className="text-dark-200 text-sm mb-4">No resume uploaded yet</p>
        )}
        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf,.doc,.docx"
          onChange={handleResumeUpload}
          className="hidden"
          id="resume-upload"
        />
        <button
          onClick={() => fileInputRef.current?.click()}
          disabled={uploadResumeMutation.isPending}
          className="btn-secondary flex items-center gap-2 text-sm"
        >
          {uploadResumeMutation.isPending ? (
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
          ) : (
            <Upload className="w-4 h-4" />
          )}
          {profile?.resumeUrl ? 'Replace Resume' : 'Upload Resume'}
        </button>
      </div>

      {/* Profile Form */}
      <div className="glass-card p-6">
        <h3 className="text-lg font-semibold text-white mb-6 flex items-center gap-2">
          <User className="w-5 h-5 text-primary-400" />
          Profile Information
        </h3>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label htmlFor="profile-phone" className="label-text flex items-center gap-2">
              <Phone className="w-4 h-4" /> Phone Number
            </label>
            <input
              id="profile-phone"
              type="tel"
              className="input-field"
              placeholder="+1-555-0123"
              {...register('phone')}
            />
            {errors.phone && <p className="mt-1 text-sm text-red-400">{errors.phone.message}</p>}
          </div>

          <div>
            <label htmlFor="profile-skills" className="label-text flex items-center gap-2">
              <Wrench className="w-4 h-4" /> Skills
            </label>
            <textarea
              id="profile-skills"
              rows={3}
              className="input-field resize-none"
              placeholder="Java, Spring Boot, React, PostgreSQL..."
              {...register('skills')}
            />
            {errors.skills && <p className="mt-1 text-sm text-red-400">{errors.skills.message}</p>}
          </div>

          <div>
            <label htmlFor="profile-experience" className="label-text flex items-center gap-2">
              <Clock className="w-4 h-4" /> Years of Experience
            </label>
            <input
              id="profile-experience"
              type="number"
              min={0}
              className="input-field"
              placeholder="3"
              {...register('experienceYears')}
            />
            {errors.experienceYears && <p className="mt-1 text-sm text-red-400">{errors.experienceYears.message}</p>}
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
