<template>
  <transition name="modal">
    <div class="modal-mask">
      <div class="modal-wrapper" @click="close">
        <div class="modal-dialog modal-lg" role="document" @click.stop="">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">
                {{ $t('digibib.contact.frontend.form.title') }}
              </h5>
              <button type="button" class="close" aria-label="Close">
                <span aria-hidden="true" @click="close">&times;</span>
              </button>
            </div>
            <div class="modal-body">
              <div>AAAA</div>
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
                  <div class="d-flex flex-column float-right" style="width: 225px">
                    <div class="d-flex flex-row">
                      <img style="width: 200px; height: auto;" :src="captchaUrl"
                          alt="graphical access code" />
                      <div class="d-flex flex-column justify-content-md-center"
                          style="width: 20px; margin-left: 5px;">
                        <i @click="shuffleCaptcha" class="fa fa-refresh" aria-hidden="true"></i>
                        <i class="fas fa-volume-up "></i>
                      </div>
                    </div>
                    <div class="pt-2">
                      <input type="text" v-model="captchaSecret" class="form-control-sm form-control w-100"
                        :class="captchaState === false ? 'is-invalid' : ''" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-primary" @click="handleSubmit">
                OK
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
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

const emit = defineEmits(['close', 'success']);
const close = () => emit('close');

interface Props {
  baseUrl: string,
  objectId: string,
}

interface IContactRequest {
  name: string;
  email: string;
  orcid?: string;
  message: string;
  objectID: string;
  sendCopy: boolean;
}

const I18N_PFEFIX = 'digibib.contact.frontend.form.';

const props = defineProps<Props>();
const captchaUrl = ref('');
const busy = ref(false);
const alertMessage = ref('');
const showAlert = computed(() => alertMessage.value.length > 0);
const contactRequest: IContactRequest = reactive({}) as IContactRequest;
const nameState: boolean = ref(null);
const emailState: boolean = ref(null);
const orcidState: boolean = ref(null);
const messageState: boolean = ref(null);
const captchaState: boolean = ref(null);
const captchaSecret = ref('');
const website = ref('');

const shuffleCaptcha = () => {
  captchaUrl.value = `${props.baseUrl}rsc/captchaCage?${Date.now()}`;
};

const resetStates = () => {
  nameState.value = null;
  emailState.value = null;
  orcidState.value = null;
  messageState.value = null;
  captchaState.value = null;
};

const resetForm = () => {
  busy.value = false;
  alertMessage.value = '';
  contactRequest.value = {} as IContactRequest;
  resetStates();
  shuffleCaptcha();
  captchaSecret.value = '';
  contactRequest.objectID = props.objectId;
};

onMounted(() => {
  resetForm();
});

const verifyCaptcha = async () => {
  const response = await fetch(`${props.baseUrl}rsc/captchaCage/userverify`, {
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain',
    },
    body: captchaSecret,
  });
  if (response.ok) {
    const result = await response.json();
    if (result.verified === true && result.verifiedToken.length !== 0) {
      return result.verifiedToken;
    }
  }
  return '';
};

const checkFormValidity = () => true;

const handleSubmit = async () => {
  busy.value = true;
  resetStates();
  if (checkFormValidity() && website.value.length === 0) {
    const token = await verifyCaptcha();
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
    } else {
      captchaState.value = false;
      shuffleCaptcha();
    }
  } else {
    busy.value = false;
  }
};
</script>
<style>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  -webkit-transform: scale(1.02);
  transform: scale(1.02);
}
input#website {
  display: none;
}
.captcha-container {
  width: 200px;
}
</style>
