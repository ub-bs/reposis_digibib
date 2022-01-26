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
              <button type="button" class="btn btn-primary" @click="$emit('ok')">
                OK
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';

@Options({
  props: {
    busy: Boolean,
    size: String,
    okOnly: Boolean,
  },
})
export default class Modal extends Vue {
  private busy: string;

  private size: string;

  private okOnly: boolean;

  private isVisible = false;

  private closeBackDrop(): void {
    if (!this.okOnly) {
      this.close();
    }
  }

  public close(): void {
    if (!this.busy) {
      this.$emit('hide');
      this.isVisible = false;
      this.$emit('hidden');
    }
  }

  public show(): void {
    this.$emit('show');
    this.isVisible = true;
    this.$emit('shown');
  }
}
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
