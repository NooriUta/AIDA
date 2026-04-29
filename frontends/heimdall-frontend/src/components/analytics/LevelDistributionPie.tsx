/**
 * UA-06: Level distribution widget — INFO/WARN/ERROR event breakdown.
 * Simple three-bar display matching the HEIMDALL badge color scheme.
 */
import type { LevelDistribution } from '../../api/analytics';

interface Props {
  distribution: LevelDistribution;
}

export function LevelDistributionPie({ distribution }: Props) {
  const total = distribution.info + distribution.warn + distribution.error;

  const rows: { label: string; value: number; cls: string }[] = [
    { label: 'INFO',  value: distribution.info,  cls: 'badge-info'    },
    { label: 'WARN',  value: distribution.warn,  cls: 'badge-warn'    },
    { label: 'ERROR', value: distribution.error, cls: 'badge-err'     },
  ];

  return (
    <div className="level-dist" data-testid="level-dist">
      {rows.map(({ label, value, cls }) => (
        <div key={label} className="level-dist-row">
          <span className={`badge ${cls}`}>{label}</span>
          <div className="level-dist-bar-wrap">
            <div
              className={`level-dist-bar ${cls}`}
              style={{ width: total > 0 ? `${(value / total) * 100}%` : '0%' }}
            />
          </div>
          <span className="level-dist-count">{value.toLocaleString()}</span>
        </div>
      ))}
      <div className="level-dist-total">
        {total.toLocaleString()} events (24 h window)
      </div>
    </div>
  );
}
