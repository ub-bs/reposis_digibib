<template>
  <b-modal id="request-modal" v-if="store.showModal" :title="request.uuid" size="xl" scrollable>
    <div class="container-fluid">
      <div v-if="errorCode" class="alert alert-danger" role="alert">
        {{ tc(`error.${errorCode}`) }}
      </div>
      <div v-if="infoCode" class="alert alert-success" role="alert">
        {{ tc(`info.${infoCode}`) }}
      </div>
      <div v-if="request.state < RequestState.Processed" class="alert alert-warning"
          role="alert">
        {{ tc('warning.requestNotProcessed') }}
      </div>
      <div class="form-row">
        <div class="form-group col-md-4">
          <label for="inputName">
            {{ tc('label.name') }}
          </label>
          <input type="text" class="form-control" id="inputName" v-model="request.name" disabled />
        </div>
        <div class="form-group col-md-5">
          <label for="inputEmail">
            {{ tc('label.email') }}
          </label>
          <input type="text" class="form-control" id="inputEmail" v-model="request.email"
            disabled />
        </div>
        <div class="form-group col-md-3">
          <label for="inputOrcid">
            {{ tc('label.orcid') }}
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
          {{ tc('label.comment') }}
        </label>
        <div class="d-flex flex-row">
          <textarea id="inputComment" class="form-control" rows="4"
            :readonly="!commentEditMode || !store.isEditable" @blur="cancelCommentEdit"
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
        {{ tc('label.recipients') }}
      </h6>
      <p>
        {{ tc('info.recipients') }}
      </p>
      <recipients-table @action-started="resetInfoError" @error="handleError" @info="handleInfo" />
    </div>
    <template v-slot:modal-footer>
      <div class="btn-group">
        <button type="button" class="btn btn-success" @click="forwardRequest"
            :disabled="!store.isReadyToForward">
          {{ tc('button.forward') }}
        </button>
      </div>
    </template>
  </b-modal>
</template>
<script setup lang="ts">
import {
  Component,
  computed,
  getCurrentInstance,
  ref,
} from 'vue';
import { useI18n } from 'vue-i18n';
import { BModal, BvModal } from 'bootstrap-vue';
import { getI18nKey, RequestState } from '@/utils';
import { useRequestStore } from '@/stores/request-modal-store';
import RecipientsTable from './recipients/RecipientsTable.vue';

const store = useRequestStore();
const request = computed(() => store.request);
const { t } = useI18n();
const tc = (value: string, obj?) => t(getI18nKey(value), obj);
const commentEditMode = ref(false);
let commentSave = '';
const errorCode = ref();
const infoCode = ref();
const instance: Component = getCurrentInstance();
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
const startCommentEdit = () => {
  if (store.isEditable) {
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
  try {
    await store.updateRequest(request.value.uuid, request.value);
  } catch (error) {
    handleError(error instanceof Error ? error.message : 'unknown');
    request.value.comment = commentSave;
  } finally {
    commentEditMode.value = false;
  }
};
const forwardRequest = async () => {
  const bvModal = instance.ctx._bv__modal as BvModal;
  const value = await bvModal.msgBoxConfirm(tc('confirm.forwardRecipient.message'), {
    title: tc('confirm.forwardRecipient.title'),
  });
  if (value) {
    try {
      await store.forwardRequest(request.value.uuid);
      handleInfo('forward');
    } catch (error) {
      handleError(error instanceof Error ? error.message : 'unknown');
    }
  }
};
</script>
