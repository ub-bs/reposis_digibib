import { Request } from '../../utils';

export type State = {
  totalCount;
  cache: Record<string, Request>;
  modalData: Request | undefined;
}

export const state: State = {
  totalCount: 0,
  cache: {},
  modalData: undefined,
};
