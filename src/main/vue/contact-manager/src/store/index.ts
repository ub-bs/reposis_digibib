import {
  createStore,
  createLogger,
} from 'vuex';

import { State, state } from './state';
import { mutations } from './mutations';
import { actions } from './actions';
import { getters } from './getters';
import modal from './modal';

const main = {
  state,
  mutations,
  actions,
  getters,
};

export default createStore<State>({
  plugins: process.env.NODE_ENV === 'development' ? [createLogger()] : [],
  modules: {
    main,
    modal,
  },
});
