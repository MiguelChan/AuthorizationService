export interface SignUpRequest {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  emailAddress: string;
  password: string;
}

export interface SignUpResponse {
  profileId?: string;
  message?: string;
}