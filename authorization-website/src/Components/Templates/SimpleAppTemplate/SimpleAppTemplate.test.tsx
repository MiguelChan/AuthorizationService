import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { SimpleAppTemplate } from './SimpleAppTemplate';

describe('SimpleAppTemplate', () => {
  it('Should display the Template', () => {
    const renderResult = setupComponent();  
    expect(renderResult.container).toMatchSnapshot();
  });

  const setupComponent = (): RenderResult => {
    return render(<SimpleAppTemplate />);
  }

});