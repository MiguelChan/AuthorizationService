import { test, expect } from '@playwright/test';

test('ignores a forged persisted profile while the server denies the session', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('ProfileKey', JSON.stringify({
    profileId: 'forged', firstName: 'Forged', lastName: 'Identity', phoneNumber: '1234567890',
  })));
  await page.goto('/');
  expect((await page.request.get('/api/profile')).status()).toBe(401);
  await expect(page.getByText('Welcome to the Auth-Hub', { exact: true })).toBeVisible();
  await expect(page.getByText('Welcome back, Forged Identity!')).toHaveCount(0);
  expect(await page.evaluate(() => localStorage.getItem('ProfileKey'))).toBeNull();
});

test('malformed legacy storage does not crash the application', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.addInitScript(() => localStorage.setItem('ProfileKey', '{broken json'));
  await page.goto('/');
  await expect(page.getByText('Welcome to the Auth-Hub', { exact: true })).toBeVisible();
  expect(errors).toEqual([]);
});

test('registers once, retries login, restores the server session and clears it after logout', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  const email = `browser-${Date.now()}@example.com`;
  const password = 'Aa1!BrowserFixture';
  const request = { firstName: 'Browser', lastName: 'Fixture', phoneNumber: '1234567890', emailAddress: email, password };
  await page.goto('/register');
  expect((await page.request.post('/api/sign-up', { data: request })).status()).toBe(403);
  let signupRequests = 0;
  page.on('request', request => {
    if (request.url().endsWith('/api/sign-up') && request.method() === 'POST') signupRequests++;
  });
  await page.getByLabel('First name', { exact: true }).fill(request.firstName);
  await page.getByLabel('Last name', { exact: true }).fill(request.lastName);
  await page.getByLabel('Phone number', { exact: true }).fill(request.phoneNumber);
  await page.getByLabel('Email', { exact: true }).fill(email);
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByLabel('Confirm password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Create Account', exact: true }).click();
  await expect(page.getByText('Account created. You can now log in.')).toBeVisible();
  expect(signupRequests).toBe(1);
  await page.getByRole('link', { name: 'Go to login' }).click();
  await page.getByLabel('Email', { exact: true }).fill(email);
  await page.getByLabel('Password', { exact: true }).fill('WrongPass123!');
  await page.getByRole('button', { name: 'Log In', exact: true }).click();
  await expect(page.getByText('Invalid username or password')).toBeVisible();
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Log In', exact: true }).click();
  await expect(page.getByText('Welcome back, Browser Fixture!')).toBeVisible();
  await page.goto('/');
  await expect(page.getByText('Browser Fixture', { exact: true })).toBeVisible();
  await page.reload();
  await expect(page.getByText('Welcome back, Browser Fixture!')).toBeVisible();
  expect((await page.request.get('/api/profile')).status()).toBe(200);
  expect(await page.evaluate(() => localStorage.getItem('ProfileKey'))).toBeNull();
  const cookies = await page.context().cookies();
  const session = cookies.find(cookie => cookie.name === 'JSESSIONID');
  expect(session?.httpOnly).toBe(true);
  expect(session?.sameSite).toBe('Strict');
  const edit = { firstName: 'Updated', lastName: 'Fixture', phoneNumber: '1234567890' };
  expect((await page.request.put('/api/profile', { data: edit })).status()).toBe(403);
  const csrf = await (await page.request.get('/api/csrf')).json();
  expect((await page.request.put('/api/profile', { data: edit, headers: { [csrf.headerName]: csrf.token } })).status()).toBe(200);
  await page.evaluate(() => window.dispatchEvent(new Event('focus')));
  await expect(page.getByText('Welcome back, Updated Fixture!')).toBeVisible();
  const logoutCsrf = await (await page.request.get('/api/csrf')).json();
  expect((await page.request.post('/logout', { headers: { [logoutCsrf.headerName]: logoutCsrf.token }, maxRedirects: 0 })).status()).toBe(204);
  expect((await page.request.get('/api/profile')).status()).toBe(401);
  await page.evaluate(() => window.dispatchEvent(new Event('focus')));
  await expect(page.getByText('Welcome to the Auth-Hub', { exact: true })).toBeVisible();
  await expect(page.getByText('Welcome back, Updated Fixture!')).toHaveCount(0);
  expect(errors).toEqual([]);
});

test('rejects unconfigured origins for preflight, real requests and browser access', async ({ page, baseURL }) => {
  const allowedOrigin = 'http://localhost:3000';
  const allowed = await page.request.fetch('/api/sign-up', { method: 'OPTIONS', headers: {
    Origin: allowedOrigin, 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': 'content-type',
  } });
  expect(allowed.status()).toBe(200);
  expect(allowed.headers()['access-control-allow-origin']).toBe(allowedOrigin);
  expect(allowed.headers()['access-control-allow-credentials']).toBe('true');
  const origin = 'http://localhost:1';
  const preflight = await page.request.fetch('/api/sign-up', { method: 'OPTIONS', headers: {
    Origin: origin, 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': 'content-type',
  } });
  expect(preflight.status()).toBe(403);
  expect(preflight.headers()['access-control-allow-origin']).toBeUndefined();
  expect((await page.request.post('/api/sign-up', { headers: { Origin: origin }, data: {} })).status()).toBe(403);
  expect((await page.request.get('/api/profile', { headers: { Origin: origin } })).status()).toBe(403);
  // A routed local page supplies an actual browser origin without contacting a remote host.
  await page.route(origin + '/**', route => route.fulfill({ contentType: 'text/html', body: '<!doctype html><title>Local origin fixture</title>' }));
  await page.goto(origin + '/');
  const observedRequest = page.waitForRequest(request => request.url() === baseURL + '/api/profile');
  const result = await page.evaluate(async target => {
    try {
      await fetch(target + '/api/profile', { credentials: 'include' });
      return 'readable';
    } catch {
      return 'blocked';
    }
  }, baseURL!);
  expect((await (await observedRequest).allHeaders()).origin).toBe(origin);
  expect(result).toBe('blocked');
});
