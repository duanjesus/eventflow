/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        status: {
          processed: "#16a34a",
          failed: "#d97706",
          deadlettered: "#dc2626",
          duplicate: "#64748b",
          received: "#2563eb",
        },
      },
    },
  },
  plugins: [],
};
