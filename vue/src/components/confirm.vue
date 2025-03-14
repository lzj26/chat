<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-content">
      <p>{{ message }}</p>
      <div class="button-group">
        <button @click="onConfirm">{{ yes }}</button>
        <button @click="onCancel">{{ no }}</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  //props的属性可以被父组件绑定，从而父组件可以传递数据进来
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    message: {
      type: String,
      default: '您确定要执行此操作吗？'
    },
    yes:{
      type: String,
      default: '确定'
    },
    no:{
      type: String,
      default: '取消'
    }
  },
  methods: {
    onConfirm() {
      //this.$emit('confirm')子组件传递触发事件 ,在父组件引用那里监听@confirm就行
      this.$emit('confirm');
    },
    onCancel() {

      this.$emit('cancel');
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.button-group button {
  margin: 5px;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
}

.button-group button:first-child {
  background-color: #28a745;
  color: white;
}

.button-group button:last-child {
  background-color: #dc3545;
  color: white;
}

.button-group button:hover {
  opacity: 0.9;
}
</style>