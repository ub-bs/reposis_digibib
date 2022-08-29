<template>
  <div class="modal-mask">
    <div class="modal-wrapper" @click="close">
      <div class="modal-dialog" :class="style" role="document" @click.stop="">
        <div class="modal-content">
          <div class="modal-header">
            <slot name="title">
              <h5 class="modal-title">
                {{ title }}
              </h5>
              <button v-if="!okOnly" type="button" class="close" aria-label="Close">
                <span aria-hidden="true" @click="close">&times;</span>
              </button>
            </slot>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div class="modal-footer">
            <slot name="footer">
              <button type="button" class="btn btn-primary" @click="ok">
                {{ okTitle }}
              </button>
            </slot>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  title: String,
  okTitle: {
    type: String,
    default: 'OK',
  },
  okOnly: {
    type: Boolean,
    default: false,
  },
  busy: {
    type: Boolean,
    default: false,
  },
  size: {
    type: String,
    default: 'md',
  },
  scrollable: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(['close', 'ok']);
const close = () => {
  if (!props.busy) {
    emit('close');
  }
};
const ok = () => {
  if (!props.busy) {
    emit('ok');
  }
};
const style = computed(() => {
  let result = `modal-${props.size}`;
  if (props.scrollable) {
    result += ' modal-dialog-scrollable';
  }
  return result;
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
}
.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}
</style>
