<template>
  <b-modal id="request-modal" v-if="request !== undefined" :title="request.uuid" size="xl"
    scrollable>
    <div class="container-fluid">
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
            :readonly="!commentEditMode || !editable" @blur="cancelCommentEdit"
            v-model="request.comment" @click="startCommentEdit" />
          <div v-if="commentEditMode" class="d-flex flex-column pl-1">
            <div class="btn-group-vertical">
              <button class="btn btn-primary shadow-none" @mousedown="updateComment"
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
      <recipients-table @action-started="resetInfoError" @error="handleError" @info="handleInfo"
        :request="request" />
    </div>
    <template v-slot:modal-footer>
      <div class="btn-group">
        <button type="button" class="btn btn-success" @click="forwardRequest"
            :disabled="forwardDisabled">
          {{ $t('digibib.contact.frontend.manager.button.forward') }}
        </button>
      </div>
    </template>
  </b-modal>
</template>
<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import { BModal, BvModal } from 'bootstrap-vue';
import RecipientsTable from './RecipientsTable.vue';
import { ActionTypes } from '../store/request/action-types';
import { RequestState } from '../utils';

const store = useStore();
const request = computed(() => store.state.request.modalData);
const { t } = useI18n();
const commentEditMode = ref(false);
let commentSave = '';
const errorCode = ref();
const infoCode = ref();
// eslint-disable-next-line
const instance = (getCurrentInstance() as any);
const forwardDisabled = computed(() => {
  if (request.value.state === RequestState.Sending_Failed) {
    return false;
  }
  return request.value.state !== RequestState.Processed;
});
const resetInfoError = () => {
  errorCode.value = null;
  infoCode.value = null;
};
const handleInfo = (code) => {
  infoCode.value = code;
};
const handleError = (code) => {
  errorCode.value = code;
};
const editable = computed(() => {
  if (request.value.state < RequestState.Sending) {
    return true;
  }
  return request.value.state === RequestState.Sending_Failed;
});
const startCommentEdit = () => {
  if (editable.value) {
    commentSave = request.value.comment;
    commentEditMode.value = true;
  }
};
const cancelCommentEdit = () => {
  if (request.value.comment !== commentSave) {
    request.value.comment = commentSave;
  }
  commentEditMode.value = false;
};
const updateComment = async () => {
  errorCode.value = null;
  commentSave = request.value.comment;
  try {
    await store.dispatch(`request/${ActionTypes.UPDATE_REQUEST}`, {
      slug: request.value.uuid,
      request: request.value,
    });
  } catch (error) {
    handleError(error instanceof Error ? error.message : 'unknown');
  } finally {
    commentEditMode.value = false;
  }
};
const forwardRequest = async () => {
  if (request.value.recipients.filter((r) => r.enabled === true).length > 0) {
    const bvModal = instance.ctx._bv__modal as BvModal;
    bvModal.msgBoxConfirm(t('digibib.contact.frontend.manager.confirm.forwardNoRecipient.message'), {
      title: t('digibib.contact.frontend.manager.confirm.forwardNoRecipient.title'),
    }).then((value) => {
      if (value) {
        store.dispatch(`request/${ActionTypes.FORWARD_REQUEST}`, request.value.uuid);
        handleInfo('forward');
      }
    }).catch((error) => {
      handleError(error instanceof Error ? error.message : 'unknown');
    });
  }
};
</script>
