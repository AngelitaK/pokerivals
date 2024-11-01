/** @type {import('next').NextConfig} */

const nextConfig = {
    async headers() {
      return [
        {
          // Match all API routes
          source: '/(.*)',
          headers: [
            { key: 'Cross-Origin-Opener-Policy', value: 'same-origin-allow-popups' },
            { key: 'Cross-Origin-Embedder-Policy', value: 'credentialless' },
          ],
        },
      ];
    },
  };

export default nextConfig;