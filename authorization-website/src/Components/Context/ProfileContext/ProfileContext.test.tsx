import React from 'react';
import { act, renderHook, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import { apiClient } from '../../../Clients';
import { ProfileDto } from '../../../Models';
import { ProfileContext, ProfileProvider } from './ProfileContext';

vi.mock('../../../Clients', () => ({ apiClient: { getProfile: vi.fn() } }));
const getProfile = vi.mocked(apiClient.getProfile);
const profile = { profileId: 'server', firstName: 'Server', lastName: 'Identity', phoneNumber: '1234567890' };
const wrapper = ({ children }: React.PropsWithChildren) => <ProfileProvider>{children}</ProfileProvider>;
const mount = () => renderHook(() => React.useContext(ProfileContext), { wrapper });

beforeEach(() => {
  vi.resetAllMocks();
  localStorage.clear();
});

test('ignores forged legacy identity and removes stored personal data on a denied session', async () => {
  localStorage.setItem('ProfileKey', JSON.stringify(profile));
  getProfile.mockRejectedValue(new Error('401'));
  const { result } = mount();
  expect(result.current.getProfile()).toBeUndefined();
  await waitFor(() => expect(result.current.isCheckingSession).toBe(false));
  expect(result.current.getProfile()).toBeUndefined();
  expect(localStorage.getItem('ProfileKey')).toBeNull();
});

test('malformed stored data cannot crash initialization', async () => {
  localStorage.setItem('ProfileKey', '{broken');
  getProfile.mockRejectedValue(new Error('401'));
  const { result } = mount();
  await waitFor(() => expect(result.current.isCheckingSession).toBe(false));
  expect(result.current.getProfile()).toBeUndefined();
});

test('loads the authenticated server profile and clears it after session loss on focus', async () => {
  getProfile.mockResolvedValueOnce({ profile }).mockRejectedValueOnce(new Error('401'));
  const { result } = mount();
  await waitFor(() => expect(result.current.getProfile()).toEqual(profile));
  act(() => window.dispatchEvent(new Event('focus')));
  await waitFor(() => expect(result.current.getProfile()).toBeUndefined());
  expect(result.current.isCheckingSession).toBe(false);
});

test('does not let a delayed failed startup probe overwrite a successful login', async () => {
  let reject!: (error: Error) => void;
  getProfile.mockImplementation(() => new Promise<ProfileDto>((_, fail) => { reject = fail; }));
  const { result } = mount();
  const signal = getProfile.mock.calls[0][0]!;
  act(() => result.current.setCurrentProfile(profile));
  expect(signal.aborted).toBe(true);
  await act(async () => reject(new Error('old 401')));
  expect(result.current.getProfile()).toEqual(profile);
  expect(result.current.isCheckingSession).toBe(false);
  expect(localStorage.getItem('ProfileKey')).toBeNull();
});

test('bounds focus probes to one pending request and aborts it on unmount', () => {
  getProfile.mockImplementation(() => new Promise<ProfileDto>(() => undefined));
  const { unmount } = mount();
  act(() => {
    window.dispatchEvent(new Event('focus'));
    window.dispatchEvent(new Event('focus'));
  });
  expect(getProfile).toHaveBeenCalledTimes(1);
  const signal = getProfile.mock.calls[0][0]!;
  unmount();
  expect(signal.aborted).toBe(true);
});

test('continues session validation when browser storage is restricted', async () => {
  vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => { throw new Error('restricted'); });
  getProfile.mockResolvedValue({ profile });
  const { result } = mount();
  await waitFor(() => expect(result.current.getProfile()).toEqual(profile));
});

test('restores session validation during StrictMode setup/cleanup/setup', async () => {
  getProfile.mockResolvedValue({ profile });
  const { result } = renderHook(() => React.useContext(ProfileContext), {
    wrapper: ({ children }) => <React.StrictMode><ProfileProvider>{children}</ProfileProvider></React.StrictMode>,
  });
  await waitFor(() => expect(result.current.getProfile()).toEqual(profile));
  expect(getProfile.mock.calls[0][0]?.aborted).toBe(true);
  expect(result.current.isCheckingSession).toBe(false);
});
