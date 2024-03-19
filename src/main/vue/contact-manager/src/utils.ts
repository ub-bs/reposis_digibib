export const ORIGIN_MANUAL = 'manual';

export const ORIGIN_FALLBACK = 'fallback';

export enum RequestState {
  RECEIVED = 0,
  PROCESSED = 10,
  FORWARDING = 20,
  FORWARDED = 30,
  CONFIRMED = 40,
}

export namespace RequestState {
  export function toString(state: RequestState): string {
    return RequestState[state];
  }
}

export type Forwarding = {
  date: Date;
  failed: Date;
  confirmed: Date;
}

export type ContactPerson = {
  name: string;
  email: string;
  origin: string;
  enabled: boolean;
  forwarding?: Forwarding;
}

export type RequestBody = {
  name: string;
  email: string;
  message: string;
  orcid?: string;
}

export type Request = {
  id: string;
  objectId: string;
  body: RequestBody;
  created: Date;
  forwarded: Date;
  state: RequestState;
  contactPersons: ContactPerson[];
  comment: string;
}

export type ErrorResponse = {
  errorCode: string;
}
