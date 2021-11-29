import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { AppToolbar } from './AppToolbar';
import { ProfileContext, ProfileState } from '../../Context';
import { AppBar } from '@mui/material';
import { Profile } from '../../../Models';
import { MemoryRouter } from 'react-router';

describe('AppToolbar', () => {

  it('Should render A LogIn Button when the User is not logged-in', () => {
    const profileState: ProfileState = {
      getProfile: () => undefined,
      setCurrentProfile: (): void => {},
    };

    const renderResult = setupComponent(profileState);
    expect(renderResult.container).toMatchSnapshot();
  });

  it('Should render the UserName in the ToolBar when user is logged-in', () => {
    const profile: Profile = {
      firstName: 'Miguel',
      lastName: 'Chan',
      phoneNumber: '',
      profileId: '',
    };

    const profileState: ProfileState = {
      getProfile: () => profile,
      setCurrentProfile: (): void => {},
    };

    const renderResult = setupComponent(profileState);
    expect(renderResult.container).toMatchSnapshot();
  });

  const setupComponent = (profileState: ProfileState): RenderResult => {
    const Component = (
      <MemoryRouter>
        <ProfileContext.Provider value={profileState}>
          <AppBar>
            <AppToolbar />
          </AppBar>
        </ProfileContext.Provider>
      </MemoryRouter>
    );

    return render(Component);
  };

});