import { createStore, createLogger } from 'vuex';

import { RootState } from './types';
import request from './request';
import main from './main';

export default createStore<RootState>({
  plugins: process.env.NODE_ENV === 'development' ? [createLogger()] : [],
  modules: {
    main,
    request,
  },
});
