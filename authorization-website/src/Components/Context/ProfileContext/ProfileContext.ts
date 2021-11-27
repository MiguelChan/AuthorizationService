import React from 'react';
import { Profile } from '../../../Models';

export type OnProfileFetched = (profile: Profile) => void;

export interface ProfileState {
  setCurrentProfile: (profile: Profile) => void;
  getProfile: () => Profile | undefined;
}

class ProfileStateClass implements ProfileState {

  private readonly PROFILE_KEY = 'ProfileKey';

  private profile: Profile | undefined;
  private readonly localStorage: Storage;

  constructor() {
    this.profile = undefined;
    this.localStorage = window.localStorage;

    this.setCurrentProfile = this.setCurrentProfile.bind(this);
    this.getProfile = this.getProfile.bind(this);

    if (this.localStorage[this.PROFILE_KEY] !== undefined) {
      this.profile = JSON.parse(this.localStorage[this.PROFILE_KEY]);
    }
  }

  public setCurrentProfile(profile: Profile): void {
    this.profile = profile;
    this.localStorage[this.PROFILE_KEY] = JSON.stringify(profile);
  }

  public getProfile(): Profile | undefined {
    return this.profile;
  }
}

export const initialState: ProfileState = new ProfileStateClass();

/**
 * Defines the Overall Profile Context for the Application.
 */
export const ProfileContext = React.createContext<ProfileState>(initialState);

