export function LoadingDots() {
  return (
    <div style={{ display: 'flex', gap: '5px', alignItems: 'center' }}>
      <style>{`
        @keyframes dotPulse {
          0%, 80%, 100% { opacity: 0.2; transform: scale(0.75); }
          40%            { opacity: 1;   transform: scale(1);    }
        }
      `}</style>
      {[0, 1, 2].map((i) => (
        <div key={i} style={{
          width: 5, height: 5, borderRadius: '50%',
          background: 'var(--seer-accent)',
          animation: `dotPulse 1.4s ease-in-out ${i * 0.22}s infinite`,
        }} />
      ))}
    </div>
  );
}
