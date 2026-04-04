
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        watch: {
            usePolling: true, // Forces Vite to notice saves even if OS signals fail
        },
        proxy: {
            // This redirects any frontend call to /api over to your Java server
            // Once this is containerized, we run into some localhost issues because
            // localhost usually refers to the current container.  Instead, we want to
            // link it to the container's name.
            // Also making this configurable to an environment variable in case it
            // needs to be changed later.
            '/api': {
                target: 'http://backend:8080',
                changeOrigin: true,
                secure: false,
            }
        },
        allowedHosts: [
            "frontend"
        ]
    }
})



/*
export default defineConfig({
    plugins: [react()],
    server: {
        watch: {
            usePolling: true, // This forces Vite to check files every X milliseconds
        },
    },
})

 */