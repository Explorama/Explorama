import { playwrightLauncher } from '@web/test-runner-playwright';

export default {
  nodeResolve: false,

  // Use Chromium for testing (can also add firefox, webkit)
  browsers: [
    playwrightLauncher({ product: 'chromium' }),
  ],

  // Test timeout
  testsFinishTimeout: 120000,

  // Don't fail on console errors (ClojureScript can be verbose)
  filterBrowserLogs: (log) => {
    // Filter out warnings we don't care about
    return log.type === 'error';
  },
};
