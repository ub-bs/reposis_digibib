<template>
  <div>
    <Teleport to="#btnContact" append>
      <a class="dropdown-item" role="menuitem" tabindex="-1" @click="showConfirmationModal = true">
        {{ $t('digibib.contact.frontend.button.contact') }}
      </a>
    </Teleport>
    <confirmation-modal sendCopy v-if="showConfirmationModal" @close="showConfirmationModal = false" />
    <form-modal :objectId="props.objectId" :baseUrl="props.baseUrl" v-if="showFormModal"
        @close="showFormModal = false" @success="handleSuccess" />
  </div>
</template>
<script setup lang="ts">
import { defineProps, ref } from 'vue';
import ConfirmationModal from './components/ConfirmationModal.vue';
import FormModal from './components/FormModal.vue';

interface Props {
  baseUrl: string,
  objectId: string,
}
const props = defineProps<Props>();
const showConfirmationModal = ref(false);
const showFormModal = ref(false);
const handleSuccess = () => {
  showFormModal.value = false;
  showConfirmationModal.value = true;
};
</script>
