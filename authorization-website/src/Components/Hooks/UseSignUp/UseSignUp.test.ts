import React from 'react';
import {
  renderHook,
  RenderHookResult,
  act,
} from '@testing-library/react-hooks';
import { SignUpApiFn, SignUpState, useSignUp } from './UseSignUp';
import { SignUpRequest, SignUpResponse } from '../../../Models';

interface HookProps {
  signUpApiFn?: SignUpApiFn;
}

describe('UseSignUp', () => {

  const mockApiFn = jest.fn();

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
      waitForNextUpdate,
    } = setupHook({
      signUpApiFn: mockApiFn,
    });

    const {
      onSignUpRequested,
    } = result.current;
    act(() => {
      onSignUpRequested(expectedFirstName, expectedLastname, expectedPhone, expectedMail, expectedPassword);
    });
    await waitForNextUpdate();
    
    expect(mockApiFn).toHaveBeenCalledWith(expectedRequest);
    expect(result.current.isProfileCreated).toBeTruthy();
    expect(result.current.hasError).toBeFalsy();
    expect(result.current.isCreatingAccount).toBeFalsy();
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
      waitForNextUpdate,
    } = setupHook({
      signUpApiFn: mockApiFn,
    });

    const {
      onSignUpRequested,
    } = result.current;
    act(() => {
      onSignUpRequested(expectedFirstName, expectedLastname, expectedPhone, expectedMail, expectedPassword);
    });
    await waitForNextUpdate();
    
    expect(mockApiFn).toHaveBeenCalledWith(expectedRequest);
    expect(result.current.isProfileCreated).toBeFalsy();
    expect(result.current.hasError).toBeTruthy();
    expect(result.current.isCreatingAccount).toBeFalsy();
  });

  const setupHook = (inputProps: HookProps): RenderHookResult<HookProps, SignUpState> => {
    return renderHook<HookProps, SignUpState>((props) => {
      return useSignUp(props.signUpApiFn);
    }, {
      initialProps: {
        signUpApiFn: inputProps.signUpApiFn,
      },
    });
  };

});