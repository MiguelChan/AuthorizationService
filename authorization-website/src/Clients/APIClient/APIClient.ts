import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from "axios";
import { ProfileDto } from "../../Models";

class APIClient {

  private readonly API_URL = '/api';

  constructor() {
    axios.defaults.withCredentials = true;
    this.logIn = this.logIn.bind(this);
  }

  public logIn(username: string, password: string): Promise<ProfileDto> {
    return new Promise<ProfileDto>((accept, reject) => {
      const params = new URLSearchParams();
      params.append('username', username);
      params.append('password', password);

      const config: AxiosRequestConfig = {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
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
        console.info(error);
        reject(error);
      });
    });
  }
}

export const apiClient: APIClient = new APIClient();