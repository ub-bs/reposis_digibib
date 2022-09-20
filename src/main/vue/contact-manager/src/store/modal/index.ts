import {
  createStore,
  createLogger,
} from 'vuex';

import { State, state } from './state';
import { mutations } from './mutations';
import { actions } from './actions';
import { getters } from './getters';

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};
