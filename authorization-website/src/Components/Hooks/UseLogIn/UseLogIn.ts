import React from 'react';
import { 
  apiClient,
} from '../../../Clients';
import { 
  Profile, 
  ProfileDto,
} from '../../../Models';

type OnLogInRequested = (username: string, password: string) => void;
export type LogInApiMethod = (username: string, password: string) => Promise<ProfileDto>;

export interface UseLogInState {
  onLogInRequested: OnLogInRequested;
  isLoggingIn: boolean;
  currentProfile: Profile | undefined;
  hasError: boolean;
}

export type UseLogIn = (loginApiMethod?: LogInApiMethod) => UseLogInState;

type LogInRequest = {
  username: string;
  password: string;
}

export function useLogIn(loginApiMethod: LogInApiMethod = apiClient.logIn): UseLogInState {

  const [logInRequest, setLogInRequest] = React.useState<LogInRequest>();
  const [requestLogIn, setRequestLogIn] = React.useState<boolean>(false);
  const [hasError, setHasError] = React.useState<boolean>(false);
  const [profile, setProfile] = React.useState<Profile>();

  React.useEffect(() => {
    if (!requestLogIn) {
      return;
    }

    loginApiMethod(logInRequest!.username, logInRequest!.password)
      .then((profileDto: ProfileDto) => {
        setHasError(false);
        setProfile(profileDto.profile);
      }).catch((error: any) => {
        setHasError(true);
      }).finally(() => {
        setRequestLogIn(false);
        setLogInRequest(undefined);
      });
  }, [requestLogIn, loginApiMethod, logInRequest]);

  const onLogInRequested = (username: string, password: string): void => {
    setLogInRequest({
      username,
      password,
    });
    setRequestLogIn(true);
  };

  return {
    onLogInRequested,
    isLoggingIn: requestLogIn,
    currentProfile: profile,
    hasError,
  }
}