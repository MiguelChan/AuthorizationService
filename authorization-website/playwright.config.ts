import { defineConfig } from '@playwright/test';

const baseURL = process.env.AUTH_BROWSER_BASE_URL;
if (!baseURL || !/^http:\/\/127\.0\.0\.1:\d+$/.test(baseURL)) {
  throw new Error('Run tools/validation/browser.py with its owned loopback fixture');
}
export default defineConfig({
  testDir: './browser-tests',
  workers: 1,
  fullyParallel: false,
  retries: 0,
  timeout: 20000,
  globalTimeout: 120000,
  expect: { timeout: 5000 },
  reporter: 'list',
  use: { baseURL, channel: 'chrome', headless: true, trace: 'off', screenshot: 'off', video: 'off' },
});
