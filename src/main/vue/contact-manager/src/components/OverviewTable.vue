<template>
  <div class="row">
    <table class="table table-striped">
      <colgroup>
        <col style="width: 20%">
        <col style="width: 11%">
        <col style="width: 12%">
        <col style="width: 12%">
        <col style="width: 7%">
        <col style="width: 50%">
        <col style="width: 5%">
      </colgroup>
      <thead>
        <tr>
          <th scope="col">
            {{ $t('digibib.contact.frontend.manager.label.id') }}
          </th>
          <th scope="col">
            {{ $t('digibib.contact.frontend.manager.label.objectID') }}
          </th>
          <th scope="col">
            {{ $t('digibib.contact.frontend.manager.label.created') }}
          </th>
          <th scope="col">
            {{ $t('digibib.contact.frontend.manager.label.forwarded') }}
          </th>
          <th scope="col">
            {{ $t('digibib.contact.frontend.manager.label.state') }}
          </th>
          <th scope="col">
            {{ $t('digibib.contact.frontend.manager.label.requester') }}
          </th>
          <th scope="col">
          </th>
        </tr>
      </thead>
      <tbody v-if="requests">
        <tr v-for="(item, index) in requests" :key="item.id">
          <td class="align-middle">
            {{ item.id }}
          </td>
          <td class="align-middle">
            <a :href="'receive/' + item.objectId" target="_blank">
              {{ item.objectId }}
            </a>
          </td>
          <td class="align-middle">
            {{ new Date(item.created).toLocaleString() }}
          </td>
          <td class="align-middle">
            {{ item.forwarded != null ? new Date(item.forwarded).toLocaleString() : '-' }}
          </td>
          <td class="align-middle">
            {{ RequestState.toString(item.state) }}
          </td>
          <td class="align-middle">
            {{ item.body.name + ' (' + item.body.email + ')' }}
          </td>
          <td class="align-middle">
            <div class="btn-group">
              <button class="btn shadow-none pt-0 pb-0 pr-1 pl-2" @click="viewRequest(index)">
                <i class="fa fa-eye"></i>
              </button>
              <button class="btn shadow-none pt-0 pb-0 pl-1 pr-2" @click="doRemoveRequest(item.id)"
                :disabled="item.state > RequestState.PROCESSED">
                <i class="fa fa-trash"></i>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <div class="row">
    <div class="col d-flex justify-content-center">
      <Pagination :total-rows="totalCount" :per-page="perPage" :current-page="currentPage"
        @change="handlePageChange" />
    </div>
  </div>
  <RequestModal v-if="currentRequestIndex >= 0" @request-updated="onRequestUpdated"
    @close="onModalClosed" @recipients-updated="onRecipientsUpdated"
    :request="requests[currentRequestIndex]" />
  <ConfirmModal ref="confirmModal" />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import ConfirmModal from '@/components/ConfirmModal.vue';
import RequestModal from '@/components/request_modal/RequestModal.vue';
import Pagination from '@/components/Pagination.vue';
import { ContactPerson, Request, RequestState } from '@/utils';
import { fetchRequests, removeRequest } from '@/api/service';

const emit = defineEmits(['error']);
const { t } = useI18n();
const confirmModal = ref();
const perPage = 8;
const requests = ref<Array<Request>>([]);
const totalCount = ref(0);
const currentPage = ref(0);
const currentRequestIndex = ref(-1);

const fetch = async (page: number) => {
  const offset = page * perPage;
  const response = await fetchRequests(offset, perPage);
  requests.value = response.data;
  totalCount.value = Number(response.headers['x-total-count']);
};
onMounted(async () => {
  await fetch(currentPage.value);
});
const handlePageChange = async (page: number) => {
  currentPage.value = page;
  try {
    await fetch(page);
  } catch (error) {
    emit('error', error instanceof Error ? error.message : 'unknown');
  }
};
const viewRequest = (index: number) => {
  currentRequestIndex.value = index;
};
const doRemoveRequest = async (requestId: string) => {
  const ok = await confirmModal.value.show({
    title: t('digibib.contact.frontend.manager.confirm.deleteRequest.title'),
    message: t('digibib.contact.frontend.manager.confirm.deleteRequest.message', {
      requestID: requestId,
    }),
  });
  if (ok) {
    try {
      await removeRequest(requestId);
      requests.value = requests.value.filter((r) => requestId !== r.id);
    } catch (error) {
      emit('error', error instanceof Error ? error.message : 'unknown');
    }
  }
};
const onRequestUpdated = (request: Request): void => {
  const requestIndex = requests.value.findIndex((r) => request.id === r.id);
  Object.assign(requests.value[requestIndex], request);
};
const onRecipientsUpdated = (requestId: string, recipients: ContactPerson[]): void => {
  const requestIndex = requests.value.findIndex((r) => requestId === r.id);
  requests.value[requestIndex].contactPersons = recipients;
};
const onModalClosed = () => {
  currentRequestIndex.value = -1;
};
</script>
