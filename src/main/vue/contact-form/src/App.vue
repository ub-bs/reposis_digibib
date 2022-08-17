<template>
  <div>
    <Teleport to="#btnContact" append>
      <a class="dropdown-item" role="menuitem" tabindex="-1" @click="showFormModal = true">
        {{ $t('digibib.contact.frontend.button.contact') }}
      </a>
    </Teleport>
    <accept-modal sendCopy v-if="showAcceptModal" @close="showAcceptModal = false" />
    <form-modal :objectId="props.objectId" :baseUrl="props.baseUrl" v-if="showFormModal"
        @close="showFormModal = false" @success="handleSuccess" />
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import AcceptModal from './components/AcceptModal.vue';
import FormModal from './components/FormModal.vue';

interface Props {
  baseUrl: string,
  objectId: string,
}
const props = defineProps<Props>();
const showAcceptModal = ref(false);
const showFormModal = ref(false);
const handleSuccess = () => {
  showFormModal.value = false;
  showAcceptModal.value = true;
};
</script>

<style>
</style>
