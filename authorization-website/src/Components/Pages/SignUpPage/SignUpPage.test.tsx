import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
import axios from 'axios';
import Application from '../../App/App';

jest.mock('axios');
const post = axios.post as jest.Mock;
function setup() {
  window.history.replaceState({}, '', '/register');
  const view = render(<Application />);
  const values: { [label: string]: string } = { 'First name': 'Test', 'Last name': 'User', 'Phone number': '1234567890',
    'Email': 'new@example.com', 'Password': 'TestPass123!', 'Confirm password': 'TestPass123!' };
  Object.keys(values).forEach(label => fireEvent.change(view.getByLabelText(label), { target: { value: values[label] } }));
  return view;
}
describe('Public registration', () => {
  it('submits once, masks passwords and links to login after success', async () => {
    let resolve: (value: any) => void = () => {};
    post.mockReturnValueOnce(new Promise(done => { resolve = done; }));
    const info = jest.spyOn(console, 'info').mockImplementation(() => {});
    const view = setup();
    expect(view.getByLabelText('Password').getAttribute('type')).toBe('password');
    expect(view.getByLabelText('Confirm password').getAttribute('type')).toBe('password');
    fireEvent.click(view.getByText('Create Account'));
    await wait(() => expect(post).toHaveBeenCalledTimes(1));
    expect(post).toHaveBeenCalledWith('/api/sign-up', { firstName: 'Test', lastName: 'User', phoneNumber: '1234567890',
      emailAddress: 'new@example.com', password: 'TestPass123!' });
    expect(view.getByText('Creating account...').closest('button')).toBeDisabled();
    resolve({ data: { profileId: 'profile' } });
    await wait(() => expect(view.getByText('Account created. You can now log in.')).toBeInTheDocument());
    expect(view.getByText('Go to login').getAttribute('href')).toBe('/login');
    expect(info).not.toHaveBeenCalled();
    info.mockRestore();
  });
  it('shows errors and allows a successful retry', async () => {
    post.mockRejectedValueOnce(new Error('Conflict')).mockResolvedValueOnce({ data: { profileId: 'profile' } });
    const view = setup();
    fireEvent.click(view.getByText('Create Account'));
    await wait(() => expect(view.getByText(/Unable to create your account/)).toBeInTheDocument());
    fireEvent.click(view.getByText('Create Account'));
    await wait(() => expect(view.getByText('Account created. You can now log in.')).toBeInTheDocument());
    expect(post).toHaveBeenCalledTimes(2);
  });
  it('does not submit mismatched or absent confirmation', async () => {
    const view = setup();
    fireEvent.change(view.getByLabelText('Confirm password'), { target: { value: '' } });
    fireEvent.click(view.getByText('Create Account'));
    await wait(() => expect(view.getByText('Confirm your password')).toBeInTheDocument());
    fireEvent.change(view.getByLabelText('Confirm password'), { target: { value: 'Different123!' } });
    fireEvent.click(view.getByText('Create Account'));
    await wait(() => expect(view.getByText('Passwords must match')).toBeInTheDocument());
    expect(post).not.toHaveBeenCalled();
  });
});
