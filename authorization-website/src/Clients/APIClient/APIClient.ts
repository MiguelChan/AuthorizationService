import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from "axios";
import { ProfileDto, SignUpRequest, SignUpResponse } from "../../Models";

class APIClient {

  private readonly API_URL = '/api';

  constructor() {
    axios.defaults.withCredentials = true;
    this.logIn = this.logIn.bind(this);
    this.signUp = this.signUp.bind(this);
  }

  public async logIn(username: string, password: string): Promise<ProfileDto> {
    const csrfHeaders = await this.csrfHeaders();
    return new Promise<ProfileDto>((accept, reject) => {
      const params = new URLSearchParams();
      params.append('username', username);
      params.append('password', password);

      const config: AxiosRequestConfig = {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          ...csrfHeaders,
        },
      };

      axios.post('/login', params, config)
      .then((result: AxiosResponse<ProfileDto>) => {
        const request: XMLHttpRequest = result.request;
        if (request.responseURL.includes("login?error")) {
          reject();
          return;
        }
        accept(result.data);
      })
      .catch((error: AxiosError) => {
        reject(error);
      });
    });
  }

  public async signUp(signUpRequest: SignUpRequest): Promise<SignUpResponse> {
    const csrfHeaders = await this.csrfHeaders();
    const url = '/sign-up';
    return new Promise<SignUpResponse>((accept, reject) => {
      const fullUrl = `${this.API_URL}${url}`;

      axios.post(fullUrl, signUpRequest, { headers: csrfHeaders })
      .then((response: AxiosResponse<SignUpResponse>) => {
        accept(response.data);
      })
      .catch((error: AxiosError) => {
        reject(error);
      });
    });
  }
  private async csrfHeaders(): Promise<Record<string, string>> {
    const response = await axios.get<{token: string; headerName: string}>('/api/csrf');
    return { [response.data.headerName]: response.data.token };
  }
}

export const apiClient: APIClient = new APIClient();
