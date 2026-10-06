import React from 'react';
import { apiClient } from '../../../Clients';
import { SignUpRequest, SignUpResponse } from '../../../Models';

export type SignUpApiFn = (request: SignUpRequest) => Promise<SignUpResponse>;
export type OnSignUpRequestedListener = (firstName: string, lastName: string,
  phoneNumber: string, emailAddress: string, password: string) => void;
export interface SignUpState {
  isCreatingAccount: boolean;
  isProfileCreated: boolean;
  hasError: boolean;
  onSignUpRequested: OnSignUpRequestedListener;
}
export type UseSignUp = (signUpApiFn: SignUpApiFn) => SignUpState;

export function useSignUp(signUpApiFn: SignUpApiFn = apiClient.signUp): SignUpState {
  const [isCreatingAccount, setCreating] = React.useState(false);
  const [isProfileCreated, setCreated] = React.useState(false);
  const [hasError, setError] = React.useState(false);
  const pending = React.useRef(false);
  const mounted = React.useRef(true);
  React.useEffect(() => {
    mounted.current = true;
    return () => { mounted.current = false; };
  }, []);

  const onSignUpRequested: OnSignUpRequestedListener = (firstName, lastName, phoneNumber, emailAddress, password) => {
    if (pending.current) return;
    pending.current = true;
    setCreating(true);
    setError(false);
    setCreated(false);
    signUpApiFn({ firstName, lastName, phoneNumber, emailAddress, password })
      .then(response => {
        if (mounted.current) {
          const created = Boolean(response.profileId);
          setCreated(created);
          setError(!created);
        }
      })
      .catch(() => { if (mounted.current) setError(true); })
      .finally(() => {
        pending.current = false;
        if (mounted.current) setCreating(false);
      });
  };
  return { isCreatingAccount, isProfileCreated, hasError, onSignUpRequested };
}
