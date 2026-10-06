import type { Meta, StoryObj } from '@storybook/react-vite';
import { ProfileContext } from '../../Context';
import { ProfilePage } from './ProfilePage';

const meta: Meta<typeof ProfilePage> = {
  title: 'Components/Pages/ProfilePage',
  component: ProfilePage,
  decorators: [(Story) => (
    <ProfileContext.Provider value={{
      getProfile: () => ({ profileId: 'example', firstName: 'Example', lastName: 'Account', phoneNumber: '1234567890' }),
      setCurrentProfile: () => undefined,
    }}><Story /></ProfileContext.Provider>
  )],
};
export default meta;
export const Primary: StoryObj<typeof ProfilePage> = {};
