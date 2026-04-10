import { Component, type ErrorInfo, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

interface Props {
  children: ReactNode;
  /** Optional key — changing it resets the boundary (e.g. on route change). */
  resetKey?: string;
}

interface State {
  error: Error | null;
}

/**
 * Catches render-time errors in any child tree and shows a fallback UI
 * instead of crashing the entire application.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // eslint-disable-next-line no-console
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  componentDidUpdate(prevProps: Props) {
    // Reset when the parent changes resetKey (e.g. navigation).
    if (this.state.error && prevProps.resetKey !== this.props.resetKey) {
      this.setState({ error: null });
    }
  }

  private handleRetry = () => this.setState({ error: null });

  render() {
    if (this.state.error) {
      return <ErrorFallback error={this.state.error} onRetry={this.handleRetry} />;
    }
    return this.props.children;
  }
}

// ── Fallback UI (functional, so it can use hooks like useTranslation) ────────

function ErrorFallback({ error, onRetry }: { error: Error; onRetry: () => void }) {
  const { t } = useTranslation();

  return (
    <div role="alert" style={{
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      width: '100%', height: '100%', minHeight: 200,
      flexDirection: 'column', gap: 16, padding: 32,
    }}>
      <div style={{
        width: 40, height: 40, borderRadius: '50%',
        background: 'color-mix(in srgb, var(--seer-accent, #D4922A) 15%, transparent)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: 20,
      }}>
        !
      </div>

      <span style={{ fontSize: 13, color: 'var(--t2, #888)', textAlign: 'center', maxWidth: 420 }}>
        {t('errorBoundary.message', 'Something went wrong while rendering this view.')}
      </span>

      {import.meta.env.DEV && (
        <pre style={{
          fontSize: 11, color: 'var(--t3, #666)', background: 'var(--bg1, #1a1a1a)',
          padding: '8px 12px', borderRadius: 6, maxWidth: 500, overflow: 'auto',
          whiteSpace: 'pre-wrap', wordBreak: 'break-word',
        }}>
          {error.message}
        </pre>
      )}

      <button
        onClick={onRetry}
        style={{
          padding: '6px 18px', fontSize: 12, borderRadius: 6,
          border: '1px solid var(--bd, #333)', background: 'var(--bg1, #222)',
          color: 'var(--t1, #ccc)', cursor: 'pointer',
        }}
      >
        {t('errorBoundary.retry', 'Try again')}
      </button>
    </div>
  );
}
