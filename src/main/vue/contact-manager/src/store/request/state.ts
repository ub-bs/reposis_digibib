import { Request } from '../../utils';

export type State = {
  requests: Request[];
  totalCount;
  cache: Record<string, Request>;
}

export const state: State = {
  requests: [],
  totalCount: 0,
  cache: {},
};
