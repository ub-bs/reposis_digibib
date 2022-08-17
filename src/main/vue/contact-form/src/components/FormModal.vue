<template>
  <modal :title="$t('digibib.contact.frontend.form.title')" @ok="handleSubmit" @close="emit('close')">
    <div class="row">
      <div class="col-12">
        <div v-if="showAlert" class="alert alert-danger" role="alert">
          {{ alertMessage }}
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
            <input type="text" class="form-control" id="nameInput" ref="name" required
              :class="nameState === false ? 'is-invalid' : ''" v-model="contactRequest.name" />
            <div class="invalid-feedback" :class="nameState === false ? 'd-block' : ''">
              {{ $t('digibib.contact.frontend.form.validation.name') }}
            </div>
          </div>
        </div>
        <div class="col-6">
          <div class="form-group required">
            <label class="control-label" for="emailInput">
              {{ $t('digibib.contact.frontend.form.label.email') }}
            </label>
            <input type="email" class="form-control" id="emailInput" ref="email" required
              :class="emailState === false ? 'is-invalid' : ''" v-model="contactRequest.email" />
            <div class="invalid-feedback" :class="emailState === false ? 'd-block' : ''">
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
            <textarea type="text" class="form-control" id="messageInput" rows="10" ref="message"
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
        <cage-captcha ref="captcha" :baseUrl="baseUrl"/>
      </div>
    </div>
  </modal>
</template>

<script setup lang="ts">
import {
  computed,
  defineEmits,
  defineProps,
  onMounted,
  reactive,
  ref,
} from 'vue';
import Modal from './Modal.vue';
import CageCaptcha from './CageCaptcha.vue';

const props = defineProps({
  baseUrl: String,
  objectId: String,
});
const emit = defineEmits(['close', 'success']);
const close = () => emit('close');

interface IContactRequest {
  name: string;
  email: string;
  orcid?: string;
  message: string;
  objectID: string;
  sendCopy: boolean;
}

const I18N_PFEFIX = 'digibib.contact.frontend.form.';

const busy = ref(false);
const alertMessage = ref('');
const showAlert = computed(() => alertMessage.value.length > 0);
const contactRequest: IContactRequest = reactive({}) as IContactRequest;
const nameState: boolean = ref(null);
const emailState: boolean = ref(null);
const orcidState: boolean = ref(null);
const messageState: boolean = ref(null);
const website = ref('');
const captcha = ref(null);

const resetStates = () => {
  nameState.value = null;
  emailState.value = null;
  orcidState.value = null;
  messageState.value = null;
};

const resetForm = () => {
  busy.value = false;
  alertMessage.value = '';
  contactRequest.value = {} as IContactRequest;
  resetStates();
  contactRequest.objectID = props.objectId;
};

onMounted(() => {
  resetForm();
});

const checkFormValidity = () => true;

const handleSubmit = async () => {
  busy.value = true;
  resetStates();
  if (checkFormValidity() && website.value.length === 0) {
    const token = await captcha.value.verifyCaptcha();
    if (token.length !== 0) {
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
        if (!response.ok) {
          // alertMessage.value = t(`${I18N_PFEFIX}error`) as string;
        } else {
          emit('success');
        }
      } catch (error) {
        // alertMessage.value = t(`${I18N_PFEFIX}error`) as string;
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
