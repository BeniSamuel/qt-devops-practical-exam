/** @type {import('next').NextConfig} */
const nextConfig = {
  // Produces .next/standalone — a minimal Node.js server for container images.
  output: 'standalone',
  reactStrictMode: true,
  async rewrites() {
    // Local development convenience only: forward /api/* to Spring Boot when API_PROXY_TARGET is set,
    // e.g. API_PROXY_TARGET=http://localhost:8080 npm run dev
    const target = process.env.API_PROXY_TARGET;
    return target ? [{ source: '/api/:path*', destination: `${target}/api/:path*` }] : [];
  },
};

export default nextConfig;
