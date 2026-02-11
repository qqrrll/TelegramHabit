/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#111827",
        mist: "#e7edf7",
        glow: "#74b9ff",
        peach: "#ffb36a"
      },
      boxShadow: {
        float: "0 12px 35px rgba(17, 24, 39, 0.12)"
      }
    }
  },
  plugins: []
};
