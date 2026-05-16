import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useJobs } from '../hooks/useApi';
import { Search, MapPin, DollarSign, Briefcase, ChevronLeft, ChevronRight } from 'lucide-react';

export default function JobBoard() {
  const [filters, setFilters] = useState({
    title: '',
    location: '',
    minSalary: '',
    maxSalary: '',
    page: 0,
    size: 9,
  });

  const [searchInput, setSearchInput] = useState('');
  const [locationInput, setLocationInput] = useState('');

  const { data, isLoading, error } = useJobs(filters);
  const jobs = data?.data?.content || [];
  const totalPages = data?.data?.totalPages || 0;

  const handleSearch = (e) => {
    e.preventDefault();
    setFilters((prev) => ({
      ...prev,
      title: searchInput,
      location: locationInput,
      page: 0,
    }));
  };

  const handlePageChange = (newPage) => {
    setFilters((prev) => ({ ...prev, page: newPage }));
  };

  const formatSalary = (amount) => {
    if (!amount) return null;
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(amount);
  };

  return (
    <div className="animate-fade-in">
      {/* Hero Section */}
      <div className="text-center mb-12">
        <h1 className="text-5xl font-extrabold text-dark-900 mb-4 leading-tight">
          Find Your <span className="gradient-text">Dream Job</span>
        </h1>
        <p className="text-lg text-dark-900 max-w-2xl mx-auto">
          Discover opportunities from top companies. Your next career move starts here.
        </p>
      </div>

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="glass-card p-6 mb-8">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-dark-400" />
            <input
              id="search-title"
              type="text"
              placeholder="Search by job title, keyword..."
              className="input-field pl-12"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
            />
          </div>
          <div className="flex-1 relative">
            <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-dark-400" />
            <input
              id="search-location"
              type="text"
              placeholder="Location..."
              className="input-field pl-12"
              value={locationInput}
              onChange={(e) => setLocationInput(e.target.value)}
            />
          </div>
          <div className="relative w-full md:w-40">
            <DollarSign className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-dark-400" />
            <input
              id="search-min-salary"
              type="number"
              placeholder="Min Salary"
              className="input-field pl-12"
              value={filters.minSalary}
              onChange={(e) => setFilters((prev) => ({ ...prev, minSalary: e.target.value, page: 0 }))}
            />
          </div>
          <button type="submit" className="btn-primary whitespace-nowrap">
            <Search className="w-4 h-4 inline mr-2" />
            Search
          </button>
        </div>
      </form>

      {/* Results */}
      {isLoading ? (
        <div className="flex justify-center py-20">
          <div className="w-10 h-10 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : error ? (
        <div className="text-center py-20">
          <p className="text-dark-900">Failed to load jobs. Please try again.</p>
        </div>
      ) : jobs.length === 0 ? (
        <div className="text-center py-20">
          <Briefcase className="w-16 h-16 mx-auto text-dark-400 mb-4" />
          <h3 className="text-xl font-semibold text-dark-900 mb-2">No Jobs Found</h3>
          <p className="text-dark-900">Try adjusting your search filters</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {jobs.map((job, index) => (
              <Link
                key={job.id}
                to={`/jobs/${job.id}`}
                className="glass-card p-6 block animate-slide-up"
                style={{ animationDelay: `${index * 60}ms` }}
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="w-12 h-12 rounded-xl bg-primary-100 flex items-center justify-center border border-primary-300">
                    <Briefcase className="w-6 h-6 text-primary-600" />
                  </div>
                  <span className={`status-badge status-${job.status}`}>{job.status}</span>
                </div>

                <h3 className="text-lg font-semibold text-dark-900 mb-1 line-clamp-1">{job.title}</h3>
                <p className="text-sm text-primary-600 font-medium mb-3">{job.companyName}</p>

                <div className="flex items-center gap-4 text-sm text-dark-900 mb-4">
                  <span className="flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5" />
                    {job.location}
                  </span>
                </div>

                <p className="text-sm text-dark-900 line-clamp-2 mb-4">{job.description}</p>

                {(job.salaryMin || job.salaryMax) && (
                  <div className="flex items-center gap-1 text-sm font-medium text-green-700">
                    <DollarSign className="w-3.5 h-3.5" />
                    {formatSalary(job.salaryMin)} — {formatSalary(job.salaryMax)}
                  </div>
                )}
              </Link>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-4">
              <button
                onClick={() => handlePageChange(filters.page - 1)}
                disabled={filters.page === 0}
                className="btn-secondary py-2 px-4 flex items-center gap-1 disabled:opacity-30"
              >
                <ChevronLeft className="w-4 h-4" /> Prev
              </button>
              <span className="text-sm text-dark-900">
                Page {filters.page + 1} of {totalPages}
              </span>
              <button
                onClick={() => handlePageChange(filters.page + 1)}
                disabled={filters.page >= totalPages - 1}
                className="btn-secondary py-2 px-4 flex items-center gap-1 disabled:opacity-30"
              >
                Next <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
