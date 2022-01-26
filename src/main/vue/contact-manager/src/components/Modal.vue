<template>
  <transition name="modal">
    <div v-if="isVisible" class="modal-mask">
      <div class="modal-wrapper" @click="closeBackDrop">
        <div class="modal-dialog" :class="'modal-' + size"  role="document" @click.stop="">
          <div class="modal-content">
            <div class="modal-header">
              <slot name="header"></slot>
              <button v-if="!okOnly" type="button" class="close" aria-label="Close">
                <span aria-hidden="true" @click="close">&times;</span>
              </button>
            </div>
            <div class="modal-body">
              <slot></slot>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-primary" @click="emit('ok')">
                OK
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import {
  defineProps,
  defineEmits,
  defineExpose,
  ref,
} from 'vue';

interface Props {
  busy?: boolean,
  size: string,
  okOnly: boolean,
}
const props = defineProps<Props>();
const isVisible = ref(false);
const emit = defineEmits(['hide', 'hidden', 'show', 'shown', 'ok']);

function close(): void {
  if (!props.busy) {
    emit('hide');
    isVisible.value = false;
    emit('hidden');
  }
}

function closeBackDrop(): void {
  if (!props.okOnly) {
    close();
  }
}

function show(): void {
  emit('show');
  isVisible.value = true;
  emit('shown');
}

defineExpose({
  show,
});
</script>

<style>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  -webkit-transform: scale(1.02);
  transform: scale(1.02);
}
</style>
