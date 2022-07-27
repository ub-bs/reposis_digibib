<template>
  <nav>
    <ul class="pagination">
      <li class="page-item" :class="currentPage === 0 ? 'disabled' : ''"
          @click="jumpToPage(currentPage - 1)">
        <a class="page-link" href="#">
          {{ $t('digibib.contact.frontend.manager.button.previous') }}
        </a>
      </li>
      <li v-for="page in pages" :key="page" class="page-item"
          :class="page === currentPage ? 'active' : ''" @click="jumpToPage(page)">
        <a class="page-link" href="#">
          {{ page + 1 }}
        </a>
      </li>
      <li class="page-item" :class="currentPage === pageCount - 1 ? 'disabled' : ''"
          @click="jumpToPage(currentPage + 1)">
        <a class="page-link" href="#">
          {{ $t('digibib.contact.frontend.manager.button.next') }}
        </a>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const pageCount = computed(() => Math.ceil(store.state.totalRows / store.state.perPage));
const currentPage = computed(() => store.state.currentPage);
const totalRows = computed(() => store.state.totalRows);
const pages = computed(() => {
  if (totalRows.value === 0) {
    return [0];
  }
  return Array.from(Array(pageCount.value).keys());
});
const jumpToPage = (page: number) => {
  if (page >= 0 && page < pageCount.value && page !== currentPage.value) {
    store.commit('setCurrentPage', page);
    store.dispatch('fetchData');
  }
};
</script>
