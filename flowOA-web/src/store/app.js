import { defineStore } from 'pinia'

const useAppStore = defineStore('app', {
  state: () => ({
    appParams: null,
    token: null
  }),
  actions: {
    // Fetch URL params for designer integration
    fetchTokenName() {
      const urlParams = new URLSearchParams(window.location.search)
      const params = {}
      for (const [key, value] of urlParams.entries()) {
        if (value && value !== 'undefined') {
          params[key] = value
        }
      }
      this.appParams = params
      // SECURITY NOTE: Extracting tokens from URL params exposes them to browser history,
      // server access logs, and Referer headers. This is only for warm-flow-ui integration.
      // Consider using a more secure token exchange mechanism (e.g., POST body or sessionStorage).
      if (params.token) {
        this.token = params.token
        localStorage.setItem('token', params.token)
      }
    },
    // Get stored token
    getToken() {
      return this.token || localStorage.getItem('token')
    }
  },
  getters: {
    appParamsGetter: (state) => state.appParams
  }
})

export default useAppStore
