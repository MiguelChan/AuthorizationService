import React from 'react';
import { apiClient } from '../../../Clients';
import { SignUpRequest, SignUpResponse } from '../../../Models';

export type SignUpApiFn = (signUpRequest: SignUpRequest) => Promise<SignUpResponse>;

export type OnSignUpRequestedListener = (
  firstName: string, 
  lastName: string, 
  phoneNumber: string, 
  emailAddress: string, 
  password: string,
) => void;

export interface SignUpState {
  isCreatingAccount: boolean;
  isProfileCreated: boolean;
  hasError: boolean;
  onSignUpRequested: OnSignUpRequestedListener;
}

export type UseSignUp = (signUpApiFn: SignUpApiFn) => SignUpState;

export function useSignUp(signUpApiFn: SignUpApiFn = apiClient.signUp): SignUpState {
  const [isCreatingAccount, setIsCreatingAccount] = React.useState<boolean>(false);
  const [signUpRequest, setSignUpRequest] = React.useState<SignUpRequest>();
  const [isProfileCreated, setProfileCreated] = React.useState<boolean>(false);
  const [hasError, setError] = React.useState<boolean>(false);

  React.useEffect(() => {
    if (!isCreatingAccount) {
      return;
    }

    signUpApiFn(signUpRequest!).then((response: SignUpResponse) => {
      setProfileCreated(response.profileId !== undefined && response.profileId !== null);
    }).catch((error: any) => {
      setProfileCreated(false);
      setError(true);
    }).finally(() => {
      setIsCreatingAccount(false);
      setSignUpRequest(undefined);
    });
  }, [signUpRequest, isCreatingAccount]);

  const onSignUpRequested = (
    firstName: string, 
    lastName: string, 
    phoneNumber: string, 
    emailAddress: string, 
    password: string,
  ): void => {
    setSignUpRequest({
      firstName,
      lastName,
      emailAddress,
      phoneNumber,
      password,
    });
    setIsCreatingAccount(true);
  };

  return {
    isCreatingAccount,
    hasError,
    isProfileCreated,
    onSignUpRequested,
  };
}