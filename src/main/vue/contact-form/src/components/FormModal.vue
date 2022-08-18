<template>
  <modal :title="$t('digibib.contact.frontend.form.title')" @ok="handleSubmit" @close="emit('close')" :busy="busy"
      size="lg">
    <div class="row">
      <div class="col-12">
        <div v-if="showError" class="alert alert-danger" role="alert">
          {{ $t('digibib.contact.frontend.form.error') }}
        </div>
      </div>
    </div>
    <div class="row pb-4">
      <div class="col-12">
        {{ $t('digibib.contact.frontend.form.text') }}
      </div>
    </div>
    <!-- TODO mark required fields -->
    <form ref="form" @submit.stop.prevent="handleSubmit">
      <div class="form-row">
        <div class="col-6">
          <div class="form-group required">
            <label class="control-label" for="nameInput">
              {{ $t('digibib.contact.frontend.form.label.name') }}
            </label>
            <input type="text" class="form-control" id="nameInput" ref="nameInput" required
              :class="nameState === false ? 'is-invalid' : ''" v-model="contactRequest.name" />
            <div class="invalid-feedback" :class="nameState === false ? 'd-block' : ''">
              {{ $t('digibib.contact.frontend.form.validation.name') }}
            </div>
          </div>
        </div>
        <div class="col-6">
          <div class="form-group required">
            <label class="control-label" for="mailInput">
              {{ $t('digibib.contact.frontend.form.label.email') }}
            </label>
            <input type="email" class="form-control" id="mailInput" ref="mailInput" required
              :class="mailState === false ? 'is-invalid' : ''" v-model="contactRequest.email" />
            <div class="invalid-feedback" :class="mailState === false ? 'd-block' : ''">
              {{ $t('digibib.contact.frontend.form.validation.email') }}
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-12">
          <div class="form-group">
            <label for="orcidInput">
              {{ $t('digibib.contact.frontend.form.label.orcid') }}
            </label>
            <input type="text" class="form-control" id="orcidInput" ref="orcid"
              :class="orcidState === false ? 'is-invalid' : ''" v-model="contactRequest.orcid" />
            <div class="invalid-feedback" :class="orcidState === false ? 'd-block' : ''">
              {{ $t('digibib.contact.frontend.form.validation.orcid') }}
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-12">
          <div class="form-group required">
            <label class="control-label" for="messageInput">
              {{ $t('digibib.contact.frontend.form.label.message') }}
            </label>
            <textarea type="text" class="form-control" id="messageInput" rows="10" ref="messageInput"
              :class="messageState === false ? 'is-invalid' : ''" v-model="contactRequest.message"
              required />
            <div class="invalid-feedback" :class="messageState === false ? 'd-block' : ''">
              {{ $t('digibib.contact.frontend.form.validation.message') }}
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-12">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" v-model="contactRequest.sendCopy"
              id="sendCopyCheck">
            <label class="form-check-label" for="sendCopyCheck">
              {{ $t('digibib.contact.frontend.form.label.sendCopy') }}
            </label>
          </div>
        </div>
      </div>
      <input id="website" name="website" type="text" v-model="website"  />
    </form>
    <div class="row pt-1">
      <div class="col">
        <cage-captcha ref="captcha" :baseUrl="baseUrl + 'rsc/captchaCage'"/>
      </div>
    </div>
  </modal>
</template>

<script setup lang="ts">

import {
  defineEmits,
  defineProps,
  onMounted,
  ref,
} from 'vue';
import Modal from './Modal.vue';
import CageCaptcha from './CageCaptcha.vue';
import validateORCID from '../utils';

interface IContactRequest {
  name: string;
  email: string;
  orcid?: string;
  message: string;
  objectID: string;
  sendCopy: boolean;
}
const props = defineProps({
  baseUrl: String,
  objectId: String,
});
const emit = defineEmits(['close', 'success']);
const busy = ref(false);
const showError = ref(false);
const contactRequest: IContactRequest = ref({}) as IContactRequest;
const nameState: boolean = ref(null);
const nameInput = ref(null);
const mailInput = ref(null);
const messageInput = ref(null);
const mailState: boolean = ref(null);
const orcidState: boolean = ref(null);
const messageState: boolean = ref(null);
const website = ref('');
const captcha = ref(null);
const resetStates = () => {
  nameState.value = null;
  mailState.value = null;
  orcidState.value = null;
  messageState.value = null;
};
const resetForm = () => {
  busy.value = false;
  showError.value = false;
  resetStates();
  contactRequest.value = {} as IContactRequest;
  contactRequest.value.objectID = props.objectId;
};
onMounted(() => {
  resetForm();
});
const checkFormValidity = () => {
  if (!nameInput.value.checkValidity()) {
    nameState.value = false;
  }
  if (!mailInput.value.checkValidity()) {
    mailState.value = false;
  }
  if (!messageInput.value.checkValidity()) {
    messageState.value = false;
  }
  if (contactRequest.value.orcid && !validateORCID(contactRequest.value.orcid)) {
    orcidState.value = false;
  }
  return nameState.value === null && mailState.value === null && messageState.value === null
      && orcidState.value === null;
};
const handleSubmit = async () => {
  busy.value = true;
  resetStates();
  if (checkFormValidity() && website.value.length === 0) {
    const token = await captcha.value.verifyCaptcha();
    if (token) {
      try {
        const response = await fetch(`${props.baseUrl}rsc/contact`, {
          method: 'POST',
          headers: {
            Accept: 'application/json, text/plain, */*',
            'Content-Type': 'application/json',
            'X-Captcha': token,
          },
          body: JSON.stringify(contactRequest.value),
        });
        busy.value = false;
        if (response.ok) {
          emit('success');
        } else {
          showError.value = true;
        }
      } catch (error) {
        showError.value = true;
        busy.value = false;
      }
    }
  }
  busy.value = false;
};
</script>
<style>
input#website {
  display: none;
}
</style>
