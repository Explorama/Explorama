const { test, expect } = require('@playwright/test');

// Playwright resolves fixtures by regex-parsing the test function signature for
// an object destructuring pattern, which ClojureScript does not emit. This file
// owns that signature so specs can be written in cljs.
globalThis.self = globalThis;

require('./out-test/specs.js');

// Global post-condition, wired once so every current and future spec is
// covered without editing any of them: a crashed component can still
// satisfy visibility/text/count assertions (it renders a div with text),
// so specs alone can't catch it. pageerror is collected for the whole
// spec run, not just its end, so a transient crash that later unmounts
// still fails the check.
for (const spec of global.explorama_e2e.specs) {
  test(spec.name, async ({ page }) => {
    const pageErrors = [];
    page.on('pageerror', (err) => pageErrors.push(err));

    await spec.run(page, expect);

    expect(pageErrors.map((err) => err.message)).toEqual([]);
    await expect(page.getByText('Component crashed')).toHaveCount(0);
  });
}
