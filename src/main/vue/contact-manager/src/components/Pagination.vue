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

const props = defineProps({
  totalRows: {
    type: [Number, String],
    required: true,
  },
  currentPage: {
    type: [Number, String],
    required: true,
  },
  perPage: {
    type: [Number, String],
    required: true,
  },
});
const emit = defineEmits(['change']);
const pageCount = computed(() => Math.ceil(props.totalRows / props.perPage));
const pages = computed(() => {
  if (props.totalRows === 0) {
    return [0];
  }
  return Array.from(Array(pageCount.value).keys());
});
const jumpToPage = (page: number) => {
  if (page >= 0 && page < pageCount.value && page !== props.currentPage) {
    emit('change', page);
  }
};
</script>
