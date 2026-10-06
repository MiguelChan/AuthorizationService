import { vi } from 'vitest';
import React from 'react';
import {
  renderHook,
  RenderHookResult,
  act,
  waitFor,
} from '@testing-library/react';
import { SignUpApiFn, SignUpState, useSignUp } from './UseSignUp';
import { SignUpRequest, SignUpResponse } from '../../../Models';

interface HookProps {
  signUpApiFn?: SignUpApiFn;
}

describe('UseSignUp', () => {

  const mockApiFn = vi.fn();

  afterEach(() => {
    mockApiFn.mockClear();
  });

  it('Should return an initial state', () => {
    const { 
      result,
     } = setupHook({
      signUpApiFn: mockApiFn,
    });

    const state: SignUpState = result.current;

    expect(state.onSignUpRequested).not.toBeNull();
    expect(state.isProfileCreated).toBeFalsy();
    expect(state.isCreatingAccount).toBeFalsy();
    expect(state.hasError).toBeFalsy();
  });

  it('Should call the SignUpApiFn when requested', async () => {
    const expectedFirstName = 'SomeSome';
    const expectedLastname = 'Some';
    const expectedPhone = 'ThisIsPhone';
    const expectedMail = 'Mail';
    const expectedPassword = 'password';

    const expectedRequest: SignUpRequest = {
      firstName: expectedFirstName,
      lastName: expectedLastname,
      phoneNumber: expectedPhone,
      emailAddress: expectedMail,
      password: expectedPassword,
    };

    const expectedResponse: SignUpResponse = {
      profileId: 'AProfileId',
    };

    mockApiFn.mockResolvedValueOnce(expectedResponse);

    const {
      result,
    } = setupHook({
      signUpApiFn: mockApiFn,
    });

    const {
      onSignUpRequested,
    } = result.current;
    act(() => {
      onSignUpRequested(expectedFirstName, expectedLastname, expectedPhone, expectedMail, expectedPassword);
    });
    await waitFor(() => expect(result.current.isCreatingAccount).toBe(false));
    
    expect(mockApiFn).toHaveBeenCalledWith(expectedRequest);
    expect(result.current.isProfileCreated).toBeTruthy();
    expect(result.current.hasError).toBeFalsy();
    expect(result.current.isCreatingAccount).toBeFalsy();
  });

  it('completes one registration under the application StrictMode mount cycle', async () => {
    mockApiFn.mockResolvedValueOnce({ profileId: 'created' });
    const { result } = renderHook<SignUpState, HookProps>(props => useSignUp(props.signUpApiFn), {
      initialProps: { signUpApiFn: mockApiFn },
      wrapper: ({ children }) => React.createElement(React.StrictMode, null, children),
    });
    act(() => {
      result.current.onSignUpRequested('First', 'Last', '1234567890', 'strict@example.com', 'TestPass123!');
      result.current.onSignUpRequested('First', 'Last', '1234567890', 'strict@example.com', 'TestPass123!');
    });
    await waitFor(() => expect(result.current.isProfileCreated).toBe(true));
    expect(result.current.isCreatingAccount).toBe(false);
    expect(mockApiFn).toHaveBeenCalledTimes(1);
  });

  it('Should handle errors gracefully', async () => {
    const expectedFirstName = 'SomeSome';
    const expectedLastname = 'Some';
    const expectedPhone = 'ThisIsPhone';
    const expectedMail = 'Mail';
    const expectedPassword = 'password';

    const expectedRequest: SignUpRequest = {
      firstName: expectedFirstName,
      lastName: expectedLastname,
      phoneNumber: expectedPhone,
      emailAddress: expectedMail,
      password: expectedPassword,
    };

    const expectedError: Error = new Error('SomeSome');
    mockApiFn.mockRejectedValueOnce(expectedError);

    const {
      result,
    } = setupHook({
      signUpApiFn: mockApiFn,
    });

    const {
      onSignUpRequested,
    } = result.current;
    act(() => {
      onSignUpRequested(expectedFirstName, expectedLastname, expectedPhone, expectedMail, expectedPassword);
    });
    await waitFor(() => expect(result.current.isCreatingAccount).toBe(false));
    
    expect(mockApiFn).toHaveBeenCalledWith(expectedRequest);
    expect(result.current.isProfileCreated).toBeFalsy();
    expect(result.current.hasError).toBeTruthy();
    expect(result.current.isCreatingAccount).toBeFalsy();
  });

  const setupHook = (inputProps: HookProps): RenderHookResult<SignUpState, HookProps> => {
    return renderHook<SignUpState, HookProps>((props) => {
      return useSignUp(props.signUpApiFn);
    }, {
      initialProps: {
        signUpApiFn: inputProps.signUpApiFn,
      },
    });
  };

});