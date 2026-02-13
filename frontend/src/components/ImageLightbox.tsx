import { useEffect } from "react";

interface ImageLightboxProps {
  src: string;
  alt: string;
  onClose: () => void;
}

export function ImageLightbox({ src, alt, onClose }: ImageLightboxProps) {
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  return (
    <div className="fixed inset-0 z-50 bg-black/90 p-4" onClick={onClose}>
      <button
        type="button"
        className="tap absolute right-4 top-4 rounded-full bg-white/20 px-3 py-1 text-sm font-bold text-white"
        onClick={onClose}
      >
        ✕
      </button>
      <div className="flex h-full w-full items-center justify-center">
        <img
          src={src}
          alt={alt}
          className="max-h-full max-w-full rounded-2xl object-contain"
          onClick={(event) => event.stopPropagation()}
        />
      </div>
    </div>
  );
}
