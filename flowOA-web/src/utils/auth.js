const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUser() {
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUser() {
  localStorage.removeItem(USER_KEY)
}

export function clearAuth() {
  removeToken()
  removeUser()
}

// Additional functions for warm-flow-ui integration
export function getFramework() {
  return 'VUE3'
}

export function setTokenName(tokenName) {
  localStorage.setItem('tokenName', tokenName)
}

export function getTokenName() {
  return localStorage.getItem('tokenName')
}

export function setFramework(framework) {
  localStorage.setItem('framework', framework)
}
