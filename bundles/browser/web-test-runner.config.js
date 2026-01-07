export default {
  files: 'out/test/index.html',
  nodeResolve: true,

  // Use Playwright with chromium browser
  browsers: ['chromium'],

  // Optional: coverage configuration
  coverageConfig: {
    report: true,
    reportDir: 'coverage',
    threshold: {
      statements: 0,
      branches: 0,
      functions: 0,
      lines: 0
    }
  },

  // Timeout for tests (in milliseconds)
  testsStartTimeout: 60000,
  testsFinishTimeout: 120000,

  // Preserve symlinks (useful for monorepos)
  preserveSymlinks: true
};
