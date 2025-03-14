<script >
  import login  from "@/components/login.vue";
  import regis from "./components/regis.vue";
  import index from "./components/index.vue";
  import store from "@/assets/store.js";
  import Confirm from "./components/confirm.vue"

  export default {
    // 注册子组件,使用 components 选项注册组件
    components: {
      login,
      regis,
      index,
      Confirm
    },
    data(){
      return{
        confirm: false
      };
    },
    methods:{
      //重复播放音效
      callMany() {
        let call = this.$refs.call;
        call.play().catch(error => {
          console.warn('播放声音失败:', error);
        });
        //监听结束，再重新播放
        call.addEventListener('ended', () => {
          call.currentTime = 0; // 重置当前时间为0，以便从头开始播放
          call.play().catch(error => {
            console.warn('播放声音失败:', error);
          });
        }); // 使用{ once: true }确保事件监听器仅触发一次
      },
      //停止播放音效
      endcall(){
        let call = this.$refs.call;
        call.pause();//停止
        call.currentTime = 0;
        if(this.calling===null){//如果通话状态都不对就
          this.$refs.callend.play().catch(error => {
            console.warn('播放声音失败:', error);
          });
        }

      },
      //向对方发送同意通话并跳转通话页面
      sendCallInfo(){
        //关闭弹窗
        this.confirm=false;

        //向服务器发送接受消息。VIDEO:accept:startId:acceptId:acceptName
        let message = "VIDEO:accept:" + this.callId + ":" + this.userId + ":" + this.name + ":MESSAGE_DATA_END!";
        //发送
        if(this.websocket && this.websocket.readyState === WebSocket.OPEN){
          this.websocket.send(message);
          console.log("同意了"+this.callId+"的邀请信息")

          //更改通话状态为等待连接中
          store.commit('CHANGE_CALLING','connecting')
          //关闭video铃声
          store.commit('CHANGE_VIDEO',false);

          //拿名字
          let callName=this.sharedVariable.split('邀请你语音通话')[0];
          //跳转通话组件
          this.$router.push({
            name: 'videoCall',
            //对方id，对方名字，
            query: { id: this.callId,name:callName,type:'01' }
          })
        }
        else {
          alert("与服务器连接丢失，websocket连接断开");
        }

      },
      //拒绝的分支，重置信息、
      refuse(){
        //关闭弹窗
        this.confirm=false;
        //播放断开声音
        // this.endcall();
        //重置信息
        //将通话铃声改为false
        store.commit('CHANGE_VIDEO',false)
        //清除对方id
        store.commit('SET_CALLID',null)
        console.log("重置完成")
      },

    },
    watch: {
      //当变量audio发生改变就触发
      audio(newaudio){//括号就是新值
        if(newaudio){//如果有新消息就触发
          this.$refs.notificationSound.play().catch(error => {
            console.warn('播放声音失败:', error);
          });
          //改变状态
          store.commit('SET_AUDIO')
        }
      },
      video(newcall){
        if(newcall){
          console.log("监听到了变为true")
          //响声音
          this.callMany();
        }
        else {//如果音效不在并且不在通话状态就播放
          this.endcall();
        }
      },
      callId(newcallId){
        console.log("对方id为"+newcallId)
        if(newcallId !==null ){

          this.confirm=true;
        }
      },
    },
    computed:{
      audio(){//变量名就是audio
        return store.getters.getAudio;
      },
      video(){
        console.log("video为："+store.getters.getVideo)
        return store.getters.getVideo
      },
      calling(){
        return store.getters.getCalling
      },
      callId(){
        console.log("callid为："+store.getters.getCallId)
        return store.getters.getCallId
      },
      sharedVariable(){
        return store.getters.getSharedVariable
      },
      userId(){
        return store.getters.getUserId
      },
      name(){
        return store.getters.getUserName
      },
      websocket(){
        return store.getters.getWs
      },
    }
  }
</script>

<template>
  <div id="app">
<!--    //路由的牵出位置-->
      <RouterView>
      </RouterView>
  </div>
  <audio id="notificationSound" ref="notificationSound" preload="auto">
    <source src="/mp3/y2394.mp3" type="audio/mpeg">
    Your browser does not support the audio element.
  </audio>
  <audio id="call" ref="call" preload="auto">
    <source src="/mp3/接听.mp3" type="audio/mpeg">
    Your browser does not support the audio element.
  </audio>
  <audio id="callend" ref="callend" preload="auto">
    <source src="/mp3/挂断.mp3" type="audio/mpeg">
    Your browser does not support the audio element.
  </audio>
  <Confirm  :visible="confirm" :message="sharedVariable"
            @confirm="sendCallInfo"
            @cancel="refuse"/>
</template>

<style scoped>
#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
</style>
