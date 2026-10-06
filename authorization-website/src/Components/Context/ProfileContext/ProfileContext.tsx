import React from 'react';
import { apiClient } from '../../../Clients';
import { Profile } from '../../../Models';

export type OnProfileFetched = (profile: Profile) => void;
export interface ProfileState {
  setCurrentProfile: (profile: Profile) => void;
  getProfile: () => Profile | undefined;
  isCheckingSession?: boolean;
}

export const initialState: ProfileState = {
  setCurrentProfile: () => undefined,
  getProfile: () => undefined,
};
export const ProfileContext = React.createContext<ProfileState>(initialState);

/** Display identity only from the server session; never hydrate it from storage. */
export const ProfileProvider: React.FunctionComponent<React.PropsWithChildren> = ({ children }) => {
  const [profile, setProfile] = React.useState<Profile>();
  const [isCheckingSession, setCheckingSession] = React.useState(true);
  const revision = React.useRef(0);
  const request = React.useRef<AbortController>();

  const cancelPending = React.useCallback(() => {
    request.current?.abort();
    request.current = undefined;
  }, []);

  const setCurrentProfile = React.useCallback((current: Profile) => {
    // A successful login must win over any older startup/focus probe.
    revision.current++;
    cancelPending();
    setProfile(current);
    setCheckingSession(false);
  }, [cancelPending]);

  React.useEffect(() => {
    let active = true;
    try {
      window.localStorage.removeItem('ProfileKey');
    } catch {
      // Restricted browser storage must not prevent cookie-based authentication.
    }
    const refresh = () => {
      if (request.current) return;
      const currentRevision = ++revision.current;
      const controller = new AbortController();
      request.current = controller;
      setCheckingSession(true);
      const isCurrent = () => active && !controller.signal.aborted && revision.current === currentRevision;
      apiClient.getProfile(controller.signal)
        .then(response => { if (isCurrent()) setProfile(response.profile); })
        .catch(() => { if (isCurrent()) setProfile(undefined); })
        .finally(() => {
          if (isCurrent()) {
            request.current = undefined;
            setCheckingSession(false);
          }
        });
    };
    refresh();
    window.addEventListener('focus', refresh);
    return () => {
      active = false;
      cancelPending();
      window.removeEventListener('focus', refresh);
    };
  }, [cancelPending]);

  const getProfile = React.useCallback(() => profile, [profile]);
  const value = React.useMemo(() => ({ getProfile, setCurrentProfile, isCheckingSession }),
    [getProfile, setCurrentProfile, isCheckingSession]);
  return <ProfileContext.Provider value={value}>{children}</ProfileContext.Provider>;
};
