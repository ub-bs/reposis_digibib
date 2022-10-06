<template>
  <Modal v-if="showModal" :title="request.uuid" size="xl" scrollable @close="hide">
    <div v-if="errorCode" class="alert alert-danger" role="alert">
      {{ $t(`digibib.contact.frontend.manager.error.${errorCode}`) }}
    </div>
    <div v-if="infoCode" class="alert alert-success" role="alert">
      {{ $t(`digibib.contact.frontend.manager.info.${infoCode}`) }}
    </div>
    <div v-if="request.state < RequestState.Processed" class="alert alert-warning"
        role="alert">
      {{ $t(`digibib.contact.frontend.manager.warning.requestNotProcessed`) }}
    </div>
    <div class="form-row">
      <div class="form-group col-md-4">
        <label for="inputName">
          {{ $t('digibib.contact.frontend.manager.label.name') }}
        </label>
        <input type="text" class="form-control" id="inputName" v-model="request.name"
          disabled />
      </div>
      <div class="form-group col-md-5">
        <label for="inputEmail">
          {{ $t('digibib.contact.frontend.manager.label.email') }}
        </label>
        <input type="text" class="form-control" id="inputEmail" v-model="request.email"
          disabled />
      </div>
      <div class="form-group col-md-3">
        <label for="inputOrcid">
          {{ $t('digibib.contact.frontend.manager.label.orcid') }}
        </label>
        <input type="text" class="form-control" id="inputOrcid" v-model="request.orcid"
          disabled />
      </div>
    </div>
    <div class="form-group">
      <textarea class="form-control" rows="5" readonly v-model="request.message" />
    </div>
    <hr class="my-4" />
    <div class="form-group">
      <label for="inputComment">
        {{ $t('digibib.contact.frontend.manager.label.comment') }}
      </label>
      <div class="d-flex flex-row">
        <textarea id="inputComment" class="form-control" rows="4"
          :readonly="!commentEditMode || !editable" @blur="handleCommentCancel"
          v-model="request.comment" @click="handleCommentClick" />
        <div v-if="commentEditMode" class="d-flex flex-column pl-1">
          <div class="btn-group-vertical">
            <button class="btn btn-primary shadow-none" @mousedown="handleCommentSave"
                title="Save">
              <i class="fa fa-check"></i>
            </button>
            <button class="btn btn-primary shadow-none" title="Cancel">
              <i class="fa fa-xmark"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
    <hr class="my-4" />
    <h6 class="mb-2">
      {{ $t('digibib.contact.frontend.manager.label.recipients') }}
    </h6>
    <p>
      {{ $t('digibib.contact.frontend.manager.info.recipients') }}
    </p>
    <recipients-table />
    <template v-slot:footer>
      <div class="btn-group">
        <button type="button" class="btn btn-success" @click="forward"
            :disabled="forwardDisabled">
          {{ $t('digibib.contact.frontend.manager.button.forward') }}
        </button>
      </div>
    </template>
  </Modal>
  <ConfirmModal ref="confirmModal" />
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import ConfirmModal from './ConfirmModal.vue';
import Modal from './Modal.vue';
import RecipientsTable from './RecipientsTable.vue';
import { ActionTypes } from '../store/request/action-types';
import { RequestState } from '../utils';

const store = useStore();
const { t } = useI18n();
const showModal = ref(false);
const confirmModal = ref(null);
const request: Request = ref({});
const errorCode = computed(() => store.state.request.errorCode);
const infoCode = computed(() => store.state.request.infoCode);
const forwardDisabled = computed(() => {
  if (request.value.state === RequestState.Sending_Failed) {
    return false;
  }
  return request.value.state !== RequestState.Processed;
});
const forward = async () => {
  let ok = true;
  if (request.value.recipients.filter((r) => r.enabled === true).length === 0) {
    ok = await confirmModal.value.show({
      title: t('digibib.contact.frontend.manager.confirm.forwardNoRecipient.title'),
      message: t('digibib.contact.frontend.manager.confirm.forwardNoRecipient.message'),
    });
  }
  if (ok) {
    store.dispatch(`request/${ActionTypes.FORWARD_REQUEST}`);
  }
};
const editable = computed(() => {
  if (request.value.state < RequestState.Sending) {
    return true;
  }
  return request.value.state === RequestState.Sending_Failed;
});
const commentEditMode = ref(false);
let commentSave = '';
const handleCommentClick = () => {
  if (editable.value) {
    commentSave = request.value.comment;
    commentEditMode.value = true;
  }
};
const handleCommentSave = async () => {
  commentSave = request.value.comment;
  store.dispatch(`request/${ActionTypes.UPDATE_REQUEST}`, request.value);
  commentEditMode.value = false;
};
const handleCommentCancel = () => {
  if (request.value.comment !== commentSave) {
    request.value.comment = commentSave;
  }
  commentEditMode.value = false;
};
const show = () => {
  request.value = store.state.request.request;
  showModal.value = true;
};
const hide = () => {
  showModal.value = false;
};
defineExpose({ show, hide });
</script>
