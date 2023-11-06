import { defineStore } from 'pinia';
import {
  addRecipient,
  forwardRequest,
  forwardRequestToRecipient,
  removeRecipient,
  updateRecipient,
  updateRequest,
} from '@/api/service';
import { Request, RequestState, Recipient } from '@/utils';

interface State {
  showModal: boolean
  request: Request | undefined
}

export const useRequestStore = defineStore('request', {
  state: (): State => ({
    showModal: false,
    request: undefined,
  }),
  getters: {
    getRecipientById(state) {
      return (id) => {
        if (state.request) {
          return state.request.recipients.find((r) => r.uuid === id);
        }
        return undefined;
      };
    },
  },
  actions: {
    async forwardRequest(): Promise<void> {
      if (this.request) {
        await forwardRequest(this.request.uuid);
        this.request.state = RequestState.Sending;
      }
    },
    async forwardRequestToRecipient(id: string): Promise<void> {
      if (this.request) {
        await forwardRequestToRecipient(this.request.uuid, id);
      }
      // TODO get recipient and reset sent and failed
    },
    async addRecipient(recipient: Recipient): Promise<void> {
      if (this.request) {
        const tmp = recipient;
        tmp.uuid = await addRecipient(this.request.uuid, recipient);
        this.request.recipients.push(tmp);
      }
    },
    async updateRecipient(id: string, recipient: Recipient): Promise<void> {
      if (this.request) {
        await updateRecipient(this.request.uuid, id, recipient);
        const index = this.request.recipients.findIndex((r) => r.uuid === recipient.uuid);
        if (index > -1) {
          this.request.recipients[index] = recipient;
        } else {
          throw new Error();
        }
      }
    },
    async removeRecipient(id: string): Promise<void> {
      if (this.request) {
        await removeRecipient(this.request.uuid, id);
        this.request.recipients = this.request.recipients.filter((r) => r.uuid !== id);
      }
    },
    async updateRequest(): Promise<void> {
      if (this.request) {
        await updateRequest(this.request.uuid, this.request);
      }
    },
  },
});
