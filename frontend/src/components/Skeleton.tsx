export function SkeletonCard({ className = "" }: { className?: string }) {
  return (
    <div className={`glass-card animate-pulse p-4 ${className}`}>
      <div className="h-4 w-28 rounded bg-slate-200/80" />
      <div className="mt-3 h-3 w-40 rounded bg-slate-200/70" />
      <div className="mt-4 h-2.5 w-full rounded bg-slate-200/70" />
    </div>
  );
}

export function SkeletonList({ rows = 3 }: { rows?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, idx) => (
        <SkeletonCard key={idx} />
      ))}
    </div>
  );
}

export function SkeletonProfile() {
  return (
    <div className="space-y-3">
      <div className="glass-card animate-pulse p-4">
        <div className="mx-auto h-24 w-24 rounded-full bg-slate-200/80" />
        <div className="mx-auto mt-3 h-4 w-36 rounded bg-slate-200/80" />
        <div className="mx-auto mt-2 h-3 w-24 rounded bg-slate-200/70" />
      </div>
      <div className="glass-card animate-pulse p-4">
        <div className="h-3 w-20 rounded bg-slate-200/80" />
        <div className="mt-3 grid grid-cols-2 gap-2">
          <div className="h-10 rounded-2xl bg-slate-200/70" />
          <div className="h-10 rounded-2xl bg-slate-200/70" />
        </div>
      </div>
    </div>
  );
}
