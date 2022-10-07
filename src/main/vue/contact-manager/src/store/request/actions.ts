import { ActionContext, ActionTree } from 'vuex';
import { RootState } from '../types';
import { State } from './state';
import { Request, Recipient, RequestState } from '../../utils';
import { ActionTypes } from './action-types';
import { MutationTypes } from './mutation-types';
import { Mutations } from './mutations';
import { Getters } from './getters';
import {
  getRequests,
  removeRequest,
  forwardRequest,
  forwardRequestToRecipient,
  updateRequest,
  addRecipient,
  updateRecipient,
  removeRecipient,
} from '../../api/service';

type AugmentedActionContext = {
  commit<K extends keyof Mutations>(
    key: K,
    payload: Parameters<Mutations[K]>[1],
  ): ReturnType<Mutations[K]>
  getters<K extends keyof Getters>(
    key: K,
    payload: Parameters<Getters[K]>[1],
  ): ReturnType<Getters[K]>
} & Omit<ActionContext<State, RootState>, 'commit'>

export interface Actions {
  [ActionTypes.FETCH]({ state, commit }: AugmentedActionContext, payload): void,
  [ActionTypes.FORWARD_REQUEST]({ state, commit }: AugmentedActionContext, payload: string): void,
  [ActionTypes.FORWARD_REQUEST_TO_RECIPIENT]({ state }: AugmentedActionContext, payload): void,
  [ActionTypes.ADD_RECIPIENT]({ state, commit }: AugmentedActionContext, payload): Promise<void>,
  [ActionTypes.UPDATE_RECIPIENT]({ state, commit }: AugmentedActionContext, payload): void,
  [ActionTypes.REMOVE_RECIPIENT]({ state, commit }: AugmentedActionContext, payload): void,
  [ActionTypes.UPDATE_REQUEST]({ state }: AugmentedActionContext, payload): void,
  [ActionTypes.REMOVE_REQUEST]({ state }: AugmentedActionContext, payload: string): void,
}

export const actions: ActionTree<State, RootState> & Actions = {
  async [ActionTypes.FETCH]({ state, commit }, { offset, limit }): Promise<void> {
    const response = await getRequests(offset, limit);
    commit(MutationTypes.SET_REQUESTS, response.data);
    commit(MutationTypes.SET_TOTAL_COUNT, Number(response.headers['x-total-count']));
  },
  async [ActionTypes.FORWARD_REQUEST]({ state, commit }, slug: string): Promise<void> {
    await forwardRequest(slug);
    commit(MutationTypes.SET_STATE, { slug, state: RequestState.Sending });
  },
  async [ActionTypes.FORWARD_REQUEST_TO_RECIPIENT]({ state }, { slug, recipientUUID })
    : Promise<void> {
    await forwardRequestToRecipient(slug, recipientUUID);
    // TODO get recipient and reset sent and failed
  },
  async [ActionTypes.ADD_RECIPIENT]({ state, commit }, { slug, recipient }): Promise<void> {
    const r = recipient;
    const response = await addRecipient(slug, recipient);
    r.uuid = response.headers.location.split('/').pop();
    commit(MutationTypes.ADD_RECIPIENT, { slug, recipient: r });
  },
  async [ActionTypes.UPDATE_RECIPIENT]({ state, commit }, { slug, recipientUUID, recipient }):
    Promise<void> {
    await updateRecipient(slug, recipientUUID, recipient);
    commit(MutationTypes.UPDATE_RECIPIENT, { slug, recipientUUID, recipient });
  },
  async [ActionTypes.REMOVE_RECIPIENT]({ state, commit }, { slug, recipientUUID }): Promise<void> {
    await removeRecipient(slug, recipientUUID);
    commit(MutationTypes.REMOVE_RECIPIENT, { slug, recipientUUID });
  },
  async [ActionTypes.UPDATE_REQUEST]({ state, commit }, { slug, request }): Promise<void> {
    await updateRequest(slug, request);
    commit(MutationTypes.UPDATE_REQUEST, { slug, request });
  },
  async [ActionTypes.REMOVE_REQUEST]({ state, commit }, slug): Promise<void> {
    await removeRequest(slug);
    commit(MutationTypes.REMOVE_REQUEST, slug);
  },
};
