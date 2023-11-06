declare global {
 interface Window {
   webApplicationBaseURL: string;
 }
}

export enum Origin {
  Manual = 'MANUAL',
  Fallback = 'FALLBACK',
}

export enum RequestState {
  Received = 0,
  Processing = 1,
  Processing_Failed = 2,
  Processed = 3,
  Sending = 4,
  Sending_Failed = 5,
  Sent = 6,
  Confirmed = 7,
}

export namespace RequestState {
  export function toString(state: RequestState): string {
    return RequestState[state];
  }
}

export type Recipient = {
  uuid: string | undefined;
  name: string;
  origin: Origin;
  mail: string;
  enabled: boolean;
  failed: Date;
  sent: Date;
  confirmed: Date;
}

export type Request = {
  uuid: string;
  name: string;
  email: string;
  orcid?: string;
  created: Date;
  state: RequestState;
  message: string;
  objectID: string;
  recipients: Recipient[];
}

export type JWT = {
  login_success: boolean;
  access_token: string;
  token_type: string;
}

export const getWebApplicationBaseURL = (): string | undefined => window.webApplicationBaseURL;

export const fetchI18n = async (webApplicationBaseURL: string) => (
  await fetch(`${webApplicationBaseURL}rsc/locale/translate/digibib.contact.frontend.*`)).json();

export const fetchJWT = async (webApplicationBaseURL: string): Promise<JWT> => (
  await fetch(`${webApplicationBaseURL}rsc/jwt?`)).json();

export const isWarmState = (state: RequestState): boolean => (RequestState.Processed === state
  || RequestState.Received === state);
