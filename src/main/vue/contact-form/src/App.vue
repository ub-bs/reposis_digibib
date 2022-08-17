<template>
  <div>
    <Teleport to="#btnContact" append>
      <a class="dropdown-item" role="menuitem" tabindex="-1" @click="showFormModal = true">
        {{ $t('digibib.contact.frontend.button.contact') }}
      </a>
    </Teleport>
    <confirmation-modal sendCopy v-if="showConfirmationModal" @close="showConfirmationModal = false" />
    <form-modal :objectId="objectId" :baseUrl="baseUrl" v-if="showFormModal"
        @close="showFormModal = false" @success="handleSuccess" />
  </div>
</template>
<script setup lang="ts">
import { defineProps, ref } from 'vue';
import ConfirmationModal from './components/ConfirmationModal.vue';
import FormModal from './components/FormModal.vue';

defineProps({
  baseUrl: {
    type: String,
    required: true,
  },
  objectId: {
    type: String,
    required: true,
  },
});
const showConfirmationModal = ref(false);
const showFormModal = ref(false);
const handleSuccess = () => {
  showFormModal.value = false;
  showConfirmationModal.value = true;
};
</script>
