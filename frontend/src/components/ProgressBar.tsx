export function unfinishedShare(problemCount: number, initialCount: number) {
  if (problemCount <= 0 || initialCount <= 0) {
    return 0;
  }
  return (problemCount / initialCount) * 100;
}

export function ProgressBar({ value, unfinished = 0 }: { value: number; unfinished?: number }) {
  const done = Math.max(0, Math.min(100, value));
  const missed = Math.max(0, Math.min(100 - done, unfinished));
  return (
    <div className="progress-track" role="progressbar" aria-valuenow={Math.round(done)} aria-valuemin={0} aria-valuemax={100}>
      {done > 0 ? <div className="progress-fill" style={{ width: `${done}%` }} /> : null}
      {missed > 0 ? <div className="progress-fill unfinished" style={{ width: `${missed}%` }} /> : null}
    </div>
  );
}
