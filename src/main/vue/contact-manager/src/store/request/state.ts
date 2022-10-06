import { Request } from '../../utils';

export type State = {
  request: Request | undefined;
  errorCode: string | undefined;
  infoCode: string | undefined;
}

export const state: State = {
  request: undefined,
  errorCode: undefined,
  infoCode: undefined,
};
