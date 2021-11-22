import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { SignUpForm } from './SignUpForm';

describe('SignUpForm', () => {

  const mockOnSignUpRequestListener = jest.fn();

  afterEach(() => {
    mockOnSignUpRequestListener.mockClear;
  });

  it('Should display the Form', () => {
    const renderResult = setupComponent();
    expect(renderResult.container).toMatchSnapshot();
  });

  const setupComponent = (): RenderResult => {
    return render(<SignUpForm onSignUpRequestCreatedListener={mockOnSignUpRequestListener} />);
  };
});