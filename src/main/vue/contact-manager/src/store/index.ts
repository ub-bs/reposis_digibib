import { createStore, createLogger } from 'vuex';

import { RootState } from './types';
import request from './request';

export default createStore<RootState>({
  plugins: process.env.NODE_ENV === 'development' ? [createLogger()] : [],
  modules: {
    request,
  },
});
