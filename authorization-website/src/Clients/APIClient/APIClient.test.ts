import axios, { AxiosError, AxiosResponse } from 'axios';
import { ProfileDto, SignUpRequest, SignUpResponse } from '../../Models';
import { apiClient } from './APIClient';

jest.mock('axios');

describe('APIClient', () => {

  const mockPostFn = axios.post as jest.Mock;

  afterEach(() => {
    mockPostFn.mockClear();
  });

  describe('logIn', () => {

    const buildMockXmlHttpRequest = (responseUrl: string): XMLHttpRequest => {
      return {
        responseURL: responseUrl,
      } as any;
    };

    const buildMockAxiosRepsonse = (profileDto: ProfileDto, xmlHttpRequest: XMLHttpRequest): AxiosResponse<ProfileDto> => {
      return {
        data: profileDto,
        request: xmlHttpRequest,
      } as any;
    };

    it('Should log the User In', () => {
      const expectedUsername = 'Username';
      const expectedPassword = 'Password';

      const expectedParams = new URLSearchParams();
      expectedParams.append('username', expectedUsername);
      expectedParams.append('password', expectedPassword);

      const profileDto: ProfileDto = {
        profile: {} as any,
      };

      const httpRequest = buildMockXmlHttpRequest("None");
      const axiosResponse = buildMockAxiosRepsonse(profileDto, httpRequest);

      mockPostFn.mockResolvedValueOnce(axiosResponse);

      return apiClient.logIn(expectedUsername, expectedPassword)
        .then((response: ProfileDto) => {
          expect(response).toEqual(profileDto);

          expect(mockPostFn).toHaveBeenCalledWith('/login', expectedParams, expect.anything());
        });
    });

    it('Should reject the response when the URL contains an error', () => {
      const expectedUsername = 'Username';
      const expectedPassword = 'Password';

      const expectedParams = new URLSearchParams();
      expectedParams.append('username', expectedUsername);
      expectedParams.append('password', expectedPassword);

      const profileDto: ProfileDto = {
        profile: {} as any,
      };

      const httpRequest = buildMockXmlHttpRequest("login?error");
      const axiosResponse = buildMockAxiosRepsonse(profileDto, httpRequest);

      mockPostFn.mockResolvedValueOnce(axiosResponse);

      return apiClient.logIn(expectedUsername, expectedPassword)
        .catch(() => {
          expect(mockPostFn).toHaveBeenCalledWith('/login', expectedParams, expect.anything());
        });
    });

    it('Should reject when there is an error', () => {
      const expectedUsername = 'Username';
      const expectedPassword = 'Password';

      const expectedParams = new URLSearchParams();
      expectedParams.append('username', expectedUsername);
      expectedParams.append('password', expectedPassword);

      const axiosError: AxiosError = {} as any;

      mockPostFn.mockRejectedValueOnce(axiosError);

      return apiClient.logIn(expectedUsername, expectedPassword)
        .catch(() => {
          expect(mockPostFn).toHaveBeenCalledWith('/login', expectedParams, expect.anything());
        });
    });
  });

  describe('signUp', () => {
    const FULL_URL = '/api/sign-up';

    const buildAxiosResponse = (response: SignUpResponse): AxiosResponse<SignUpResponse> => {
      return {
        data: response,
      } as any;
    };

    it('Should sign the user up', () => {
      const signUpRequest: SignUpRequest = {
        firstName: 'firstName',
        emailAddress: 'emailAddress',
        lastName: 'lastName',
        password: 'password',
        phoneNumber: 'phoneNumber',
      };

      const expectedResponse: SignUpResponse = {
        profileId: 'SomeSome',
      };

      const axiosResponse = buildAxiosResponse(expectedResponse);
      mockPostFn.mockResolvedValueOnce(axiosResponse);

      return apiClient.signUp(signUpRequest).then((response: SignUpResponse) => {
        expect(response).toEqual(expectedResponse);
        expect(mockPostFn).toHaveBeenCalledWith(FULL_URL, signUpRequest);
      });
    });

    it('Should gracefully handle errors', () => {
      const signUpRequest: SignUpRequest = {
        firstName: 'firstName',
        emailAddress: 'emailAddress',
        lastName: 'lastName',
        password: 'password',
        phoneNumber: 'phoneNumber',
      };

      const axiosError: Error = new Error('SomeSome');

      mockPostFn.mockRejectedValueOnce(axiosError);

      return apiClient.signUp(signUpRequest).catch((error: any) => {
        expect(error).toEqual(axiosError);
        expect(mockPostFn).toHaveBeenCalledWith(FULL_URL, signUpRequest);
      });
    });
  });

});