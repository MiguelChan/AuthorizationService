export interface Profile {
  profileId: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
}

export interface ProfileDto {
  profile: Profile;
}