import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { LogInForm } from './LogInForm';

describe('LogInForm', () => {

  const mockOnLogInFn = jest.fn();

  afterEach(() => {
    mockOnLogInFn.mockClear();
  });

  it('Should display the LogInForm', () => {
    const renderResult = setupComponent();
    expect(renderResult.container).toMatchSnapshot();
  });

  const setupComponent = (): RenderResult => {
    return render(
      <LogInForm 
        onLogIn={mockOnLogInFn} 
        isLoading={false}
      />
      );
  };

});