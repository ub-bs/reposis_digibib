import { Request } from '../../utils';

export type State = {
  totalCount;
  cache: Record<string, Request>;
}

export const state: State = {
  totalCount: 0,
  cache: {},
};
