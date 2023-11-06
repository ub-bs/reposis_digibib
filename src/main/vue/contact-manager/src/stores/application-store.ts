import { defineStore } from 'pinia';
import {
  fetchRequests,
  removeRequest,
} from '@/api/service';
import { Request } from '@/utils';

interface State {
  offset: number
  limit: number
  totalCount: number
  requests: Request[]
}

export const useApplicationStore = defineStore('application', {
  state: (): State => ({
    limit: 8,
    offset: 0,
    totalCount: 0,
    requests: [],
  }),
  actions: {
    async fetch(): Promise<void> {
      const response = await fetchRequests(this.offset, this.limit);
      this.requests = response.data;
      this.totalCount = parseInt(response.headers['x-total-count'], 10);
    },
    async removeRequest(id: string): Promise<void> {
      await removeRequest(id);
      if ((this.offset === 0) && (this.totalCount > this.limit)) {
        this.fetch();
      } else if ((this.offset >= this.limit) && (this.totalCount % this.limit === 1)) {
        this.offset -= this.limit;
        this.fetch();
      } else {
        this.requests = this.requests.filter((r) => r.uuid !== id);
        this.totalCount -= 1;
      }
    },
  },
});
