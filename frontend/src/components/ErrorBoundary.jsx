import { Component } from 'react';
import { AlertTriangle } from 'lucide-react';

/**
 * Error Boundary component to catch runtime errors and display a user-friendly message
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-gradient-to-br from-dark-800 to-dark-900 flex items-center justify-center p-4">
          <div className="glass-card p-8 max-w-md text-center">
            <AlertTriangle className="w-12 h-12 mx-auto text-red-500 mb-4" />
            <h1 className="text-2xl font-bold text-gray-900 mb-3">Oops! Something went wrong</h1>
            <p className="text-gray-600 mb-6">
              We encountered an unexpected error. Please try refreshing the page or contact support if the problem persists.
            </p>
            <div className="mb-6 p-4 bg-red-500/10 rounded-lg border border-red-500/20 text-left">
              <p className="text-sm text-red-600 font-mono break-words">
                {this.state.error?.message || 'Unknown error'}
              </p>
            </div>
            <button
              onClick={() => window.location.href = '/'}
              className="btn-primary w-full"
            >
              Return to Home
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
