import axios from "axios";

// Demo-only shared secret matching the backend's dev default
// (pulsequeue.security.api-key) — this dashboard is the "trusted internal
// caller" for the simulate/publish buttons, not a real credential a browser
// should ever hold in production. Override via VITE_API_KEY at build time.
const API_KEY = import.meta.env.VITE_API_KEY ?? "pulsequeue-dev-key-change-me";

export const api = axios.create({
  baseURL: "/api/v1",
  headers: {
    "X-API-Key": API_KEY,
  },
});
