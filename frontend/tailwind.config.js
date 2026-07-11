/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        canvas: "#0b0e14",
        panel: "#12161f",
        panelborder: "#232837",
        muted: "#8b93a7",
        accent: "#2dd4bf",
        status: {
          processed: "#34d399",
          failed: "#fbbf24",
          deadlettered: "#f87171",
          duplicate: "#8b93a7",
          received: "#60a5fa",
        },
      },
      fontFamily: {
        mono: ["'JetBrains Mono'", "ui-monospace", "SFMono-Regular", "Menlo", "monospace"],
      },
    },
  },
  plugins: [],
};
