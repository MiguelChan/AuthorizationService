import React from 'react';
import {
  renderHook,
  RenderHookResult,
  act,
} from '@testing-library/react-hooks';
import { 
  LogInApiMethod,
  useLogIn,
  UseLogInState,
} from './UseLogIn';
import { 
  Profile, ProfileDto,
} from '../../../Models';

describe('UseLogin', () => {

  const mockApiFn = jest.fn();

  const mockProps: HookProps = {
    logInApiFn: mockApiFn,
  };

  interface HookProps {
    logInApiFn: LogInApiMethod,
  }

  afterEach(() => {
    mockApiFn.mockClear();
  });

  it('Should have an initial state set', () => {
    const hookResult = setupHook(mockProps);
    
    const useLogInState: UseLogInState = hookResult.result.current;

    expect(useLogInState.currentProfile).toBeUndefined();
    expect(useLogInState.isLoggingIn).toBeFalsy();
    expect(useLogInState.onLogInRequested).not.toBeNull();
  });

  it('Should perform a LogIn', async () => {
    const expectedUsername = '12345';
    const expectedPassword = 'password';

    const hookResult = setupHook(mockProps);

    const {
      waitFor,
      waitForNextUpdate,
    } = hookResult;

    const useLogInState: UseLogInState = hookResult.result.current;

    const expectedProfile: Profile = {
      firstName: 'FirstName',
      lastName: 'lastName',
      phoneNumber: 'PhoneNumber',
      profileId: '123123',
    };

    const profileDto: ProfileDto = {
      profile: expectedProfile,
    };

    mockApiFn.mockResolvedValueOnce(profileDto);

    act(() => {
      useLogInState.onLogInRequested(expectedUsername, expectedPassword);
    });
    await waitForNextUpdate();

    expect(mockApiFn).toHaveBeenCalledWith(expectedUsername, expectedPassword);
    
    const {
      currentProfile,
      isLoggingIn,
      hasError,
    } = hookResult.result.current;
    expect(currentProfile).toEqual(expectedProfile);
    expect(isLoggingIn).toBeFalsy();
    expect(hasError).toBeFalsy();
  });

  const setupHook = (props: HookProps): RenderHookResult<HookProps, UseLogInState> => {
    return renderHook<HookProps, UseLogInState>((props: HookProps) => {
      return useLogIn(props.logInApiFn);
    }, {
      initialProps: props,
    });
  }
});