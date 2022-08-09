<!-- TODO may outsource form groups and add custom validation -->
<template>
  <div>
    <Teleport to="#btnContact" append>
      <a class="dropdown-item" role="menuitem" tabindex="-1" @click="showModal">
        {{ $t('digibib.contact.frontend.button.contact') }}
      </a>
    </Teleport>
    <modal ref="acceptedModal" size="lg" @ok="hideAcceptedModal" ok-only="true" >
      <template #header>
        <h5 class="modal-title">
          {{ $t('digibib.contact.frontend.form.title') }}
        </h5>
      </template>
      <div>
        Die Anfrage wurde erfolgreich aufgenommen und wird nun an die Beteiligten weitergeleitet.
      </div>
      <div v-if="contactRequest.sendCopy">
        Sie erhalten in Kürze eine Bestätigungsmail.
      </div>
      <div>
        Sofern die Anfrage längst unbeantwortet bleibt, kontaktieren Sie bitte unseren Support.
      </div>
    </modal>
    <modal ref="modal" size="lg" @show="resetForm" @ok="handleSubmit" :busy=busy>
      <template #header>
        <h5 class="modal-title">
          {{ $t('digibib.contact.frontend.form.title') }}
        </h5>
      </template>
      <div v-if="showAlert" class="alert alert-danger" role="alert">
        {{ alertMessage }}
      </div>
      <div class="pb-4">
        {{ $t('digibib.contact.frontend.form.text') }}
      </div>
      <form ref="form" @submit.stop.prevent="handleSubmit">
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
        <div class="form-check">
          <input class="form-check-input" type="checkbox" v-model="contactRequest.sendCopy"
            id="sendCopyCheck">
          <label class="form-check-label" for="sendCopyCheck">
            {{ $t('digibib.contact.frontend.form.label.sendCopy') }}
          </label>
        </div>
        <input id="website" name="website" type="text" v-model="website"  />
      </form>
    </modal>
  </div>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import { useI18n } from 'vue-i18n';
import Modal from './components/Modal.vue';
import validateORCID from './utils';

interface IContactRequest {
  name: string;
  email: string;
  orcid?: string;
  message: string;
  sendCopy: boolean;
}

const I18N_PFEFIX = 'digibib.contact.frontend.form.';

@Options({
  components: {
    Modal,
  },
  props: {
    baseUrl: String,
    objectId: String,
  },
})
export default class ContactForm extends Vue {
  private readonly baseUrl: string;

  private readonly objectId: string;

  private busy = false;

  private alertMessage = '';

  private get showAlert(): boolean {
    return this.alertMessage.length > 0;
  }

  private contactRequest: IContactRequest = {} as IContactRequest;

  private nameState: boolean = null;

  private emailState: boolean = null;

  private orcidState: boolean = null;

  private messageState: boolean = null;

  private website = '';

  private setup() {
    const { t } = useI18n();
    return t;
  }

  private showModal(): void {
    (this.$refs.modal as Modal).show();
  }

  private hideModal(): void {
    (this.$refs.modal as Modal).close();
  }

  private showAcceptedModal(): void {
    (this.$refs.acceptedModal as Modal).show();
  }

  private hideAcceptedModal(): void {
    (this.$refs.acceptedModal as Modal).close();
  }

  private resetForm(): void {
    this.busy = false;
    this.alertMessage = '';
    this.contactRequest = {} as IContactRequest;
    this.resetStates();
  }

  private resetStates(): void {
    this.nameState = null;
    this.emailState = null;
    this.orcidState = null;
    this.messageState = null;
  }

  private checkFormValidity(): boolean {
    if (!(this.$refs.name as Vue & { checkValidity: () => boolean }).checkValidity()) {
      this.nameState = false;
    }
    if (!(this.$refs.email as Vue & { checkValidity: () => boolean }).checkValidity()) {
      this.emailState = false;
    }
    if (!(this.$refs.message as Vue & { checkValidity: () => boolean }).checkValidity()) {
      this.messageState = false;
    }
    if (this.contactRequest.orcid && !validateORCID(this.contactRequest.orcid)) {
      this.orcidState = false;
    }
    return this.nameState === null && this.emailState === null && this.messageState === null
        && this.orcidState === null;
  }

  private async handleSubmit(): Promise<void> {
    this.busy = true;
    this.resetStates();
    if (this.checkFormValidity() && this.website.length === 0) {
      try {
        const response = await fetch(`${this.baseUrl}api/v2/objects/${this.objectId}/contacts`, {
          method: 'POST',
          headers: {
            Accept: 'application/json, text/plain, */*',
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.contactRequest),
        });
        this.busy = false;
        if (!response.ok) {
          this.alertMessage = this.$t(`${I18N_PFEFIX}error`) as string;
        } else {
          this.$nextTick(() => {
            this.hideModal();
            this.showAcceptedModal();
          });
        }
      } catch (error) {
        this.alertMessage = this.$t(`${I18N_PFEFIX}error`) as string;
      }
    } else {
      this.busy = false;
    }
  }
}
</script>
<style>
input#website {
  display: none;
}
</style>
