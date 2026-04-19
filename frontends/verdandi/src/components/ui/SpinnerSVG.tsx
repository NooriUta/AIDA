export function SpinnerSVG({ size = 28 }: { size?: number }) {
  const half = size / 2;
  const r    = half - 3;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} fill="none"
      style={{ animation: 'spin 0.8s linear infinite' }}>
      <style>{`@keyframes spin { to { transform: rotate(360deg) } }`}</style>
      <circle cx={half} cy={half} r={r} stroke="var(--seer-border-2)" strokeWidth="2.5" />
      <path d={`M${half} ${half - r} A${r} ${r} 0 0 1 ${half + r} ${half}`}
        stroke="var(--seer-accent)" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  );
}
