import { MutationTree } from 'vuex';
import { State } from './state';
import { MutationTypes } from './mutation-types';
import { Request, RequestState } from '../../utils';

export type Mutations<S = State> = {
  [MutationTypes.SET_REQUESTS](state: S, payload: Request[]): void;
  [MutationTypes.SET_STATE](state: S, payload): void;
  [MutationTypes.ADD_RECIPIENT](state: S, payload): void;
  [MutationTypes.UPDATE_RECIPIENT](state: S, payload): void;
  [MutationTypes.REMOVE_RECIPIENT](state: S, payload): void;
  [MutationTypes.SET_TOTAL_COUNT](state: S, count: number): void;
  [MutationTypes.UPDATE_REQUEST](state: S, payload): void;
  [MutationTypes.REMOVE_REQUEST](state: S, payload: string): void;
}

export const mutations: MutationTree<State> & Mutations = {
  [MutationTypes.SET_REQUESTS](state: State, requests: Request[]): void {
    state.cache = {};
    requests.forEach((r) => {
      state.cache[r.uuid] = r;
    });
  },
  [MutationTypes.SET_STATE](state: State, payload): void {
    state.cache[payload.slug].state = payload.state;
  },
  [MutationTypes.UPDATE_REQUEST](state: State, payload): void {
    state.cache[payload.slug] = payload.request;
  },
  [MutationTypes.UPDATE_RECIPIENT](state: State, payload): void {
    const recipients = state.cache[payload.slug].recipients
      .filter((re) => re.uuid !== payload.recipientUUID);
    recipients.push(payload.recipient);
    state.cache[payload.slug].recipients = recipients;
  },
  [MutationTypes.ADD_RECIPIENT](state: State, payload): void {
    state.cache[payload.slug].recipients.push(payload.recipient);
  },
  [MutationTypes.REMOVE_RECIPIENT](state: State, payload): void {
    const recipients = state.cache[payload.slug].recipients
      .filter((re) => re.uuid !== payload.recipientUUID);
    state.cache[payload.slug].recipients = recipients;
  },
  [MutationTypes.SET_TOTAL_COUNT](state: State, count: number): void {
    state.totalCount = count;
  },
  [MutationTypes.REMOVE_REQUEST](state: State, slug: string): void {
    delete state.cache[slug];
  },
};
