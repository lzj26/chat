// 创建 Vuex Store存储全局变量

import {createStore} from "vuex";
//在服务器将8081的请求转发到本地8080即可
export const path='https://www.luoposhan.tech:8081';//这样，const表示不变，let表示可变
// export const path='http://localhost:8080';//这样，const表示不变，let表示可变

const store = createStore({
    state: {
        sharedVariable: '这是一个共享变量',
        userName: null,
        userId: null,
        ws: null,//websocket连接对象
        heartbeatInterval: null, //websocket连接对象的心跳检测定时器
        chats  : {},//每个用户的消息列表对象
        currentUserId: null, // 当前聊天对象的用户ID
        url: 'wss://www.luoposhan.tech:8444/ws', //websocket连接建立的地址
        // url: 'ws://localhost:8443/ws',
        audio: false, //消息提示音,
        video: false, //通话铃声
        calling: null, //标识通话状态
        callId: null,  //通话的对象id
        rtcs: {}, //webrtc的连接对象集合
        videoStreams: {}, //视频流,包括音频跟视频
        configuration : {iceServers: [{urls: 'stun:stun.l.google.com:19302'},
                {
                    urls: 'turn:139.9.200.130:3478?transport=udp',
                    username: 'test',
                    credential: 'test'
                },
                {
                    urls: 'turn:139.9.200.130:3478?transport=tcp',
                    username: 'test',
                    credential: 'test'
                }
                ],
        iceTransportPolicy: 'all',
        bundlePolicy: 'balanced',
        rtcpMuxPolicy: 'require',
        iceCandidatePoolSize: 0,}, // ICE服务器配置，用于穿透NAT

        cachedCandidates : {}, //ice候选者对象列表，每个rtc对象都缓存

    },
    //同步修改
    mutations: {
        DELETE_cachedCandidates(state, id){
            delete state.cachedCandidates[id];
        },
        ADD_cachedCandidates_listener (state, {id,ice}) {
            state.cachedCandidates[id].push(ice);
        },
        ADD_cachedCandidates(state,id){
            state.cachedCandidates[id] =[];
        },
        SET_heartbeatInterval(state,value){
            state.heartbeatInterval=value;
        },
        DELETE_VIDEO(state,target){
            delete state.videoStreams[target];
        },
        DELETE_RTCS(state,target){
            delete state.rtcs[target];
        },
        CLEAN_VIDEO(state){
          state.videoStreams={};
        },
        CLEAN_RTCS(state){
          state.rtcs={};
        },
        SET_CALLID(state, value){
            state.callId=value;
        },
        ADD_RTC(state,{userId,rtc}){//添加rtc
            state.rtcs[userId]=rtc;
        },
        ADD_VIDEO(state, {userId,video}){//添加视频流
            state.videoStreams[userId]=video;
        },
        CHANGE_CALLING(state,value){
            state.calling=value
        },
        setCurrentUserId(state, value) {
            state.currentUserId = value;
        },
        cleanCurrentUserId(state) {
            state.currentUserId = null;
        },
        setSharedVariable(state, value) {
            state.sharedVariable = value;
        },
        setUserName(state, value) {
            state.userName = value;
        },
        setUserId(state, value) {
            state.userId = value;
        },
        SET_CURRENT_USER_ID(state, userId) {//设置当前聊天用户id
            state.currentUserId = userId;
        },
        SET_WEBSOCKET(state,ws){
            state.ws=ws
        },
        SET_CHATS(state,chats){//初始化聊天列表
            state.chats=chats
        },
        SET_AUDIO(state){//改变状态
            state.audio=!state.audio
        },
        CHANGE_VIDEO(state,value){
           state.video=value
        },
        //加载历史聊天消息
        SET_CHAT_HISTORY(state, {userId, history}) {
            if (!state.chats[userId]) {//新增用户的对象
                state.chats[userId]={ messages: [], unread: 0 };
            }
            state.chats[userId].messages = history;
            state.chats[userId].unread = 0; // 加载历史后重置未读消息计数
        },
        //清除未读信息
        CLEAR_UNREAD_COUNT(state, userId) {
            if (state.chats[userId]) {
                state.chats[userId].unread = 0;
            }
        }
    },
    //异步修改
    actions: {
        async ADD_CHAT_MESSAGE({ commit,state,dispatch  }, {userId,type, message}) {//添加与特定用户的聊天记录
            console.log("chats:")
            console.log(state.chats)
            if (!state.chats[userId]) {//如果该条消息的发生者不存在就调用set方法新增
                //进入等待
                console.log("等待信息获取完成")
                await dispatch('getUserInfo',{userId:userId,type:type,message:message});
            }
            else {
                //往指定用户的聊天信息列表添加消息
                console.log("添加了信息")
                state.chats[userId].messages.push(message);
                if (state.currentUserId===null || userId !== state.currentUserId) {
                    state.chats[userId].unread += 1; // 如果不是当前聊天对象，则增加未读消息计数
                }
            }

        },
        updateSharedVariable({ commit,state }, value) {
            commit('setSharedVariable', value);
        },
        //初始化websocket连接
       async initWebSocket({ commit,state,dispatch }) {//其中调用的同步部分用{}包括起来
            const ws = new WebSocket(state.url);//建立
            commit('SET_WEBSOCKET', ws);//调用同步方法赋值

            // 当 WebSocket 连接成功时触发
            ws.onopen = async function () {
                console.log("连接成功");
                //需要发送注册消息，但是服务器那边不需要进行返回登陆成功跟查询id的操作
                if (state.userName.length > 0) {
                    await ws.send("REGISTER:" + state.userName+ ":" +state.userId+ ":MESSAGE_DATA_END!");//发送注册信息,带上结束标识
                    console.log("REGISTER:" + state.userName+ ":" +state.userId+ ":MESSAGE_DATA_END!")
                } else {
                    alert("用户名未知");
                }

                //开启心跳检测
                dispatch('setupHeartbeat',ws);

            }
            //当接收到信息时触发
            ws.onmessage = async function (data) {

                let message = data.data;

                //分情况处理
                //请求通话的消息:request_call:群组name:发起人name:发起人id
                if (message.startsWith("request_call")) {
                    console.log("收到了邀请:"+message)
                    let strings = message.split(":");
                    //将通话铃声改为true
                   await commit('CHANGE_VIDEO',true)
                    //提示信息
                   await commit('setSharedVariable',strings[2]+'邀请你语音通话')
                    //记录对方id
                    console.log("id为"+strings[3])
                  await commit('SET_CALLID',strings[3])
                    console.log("初始化完成")
                }
                else if(message.startsWith("heartbeat-ack")){//心跳检测
                    console.log('心跳响应正常');
                }
                //收到返回的房间参与者id列表信息,注意返回类似return_idLink:[hello, 你好],有空格
                else if (message.startsWith("return_idLink")) {
                    //先检查state中有无本地视频流
                    if(!state.videoStreams[state.userId]){
                        //没有就先创建，//等待视频获取成功
                       await dispatch('createLocalVideo',false);
                    }
                    //切割掉return_idLink:[
                    let nameLink = message.substring(15, message.length - 1);
                    console.log(nameLink);
                    let name_link = nameLink.split(",");
                    //遍历每个成员都新建一个webrtc连接
                    for (let i = 0; i < name_link.length; i++) {
                        //新建一个webrtc连接,等一个建立完再继续,注意清除前后空格
                        let targetId = name_link[i].trim();
                        //不是自己的id再建立webrtc连接
                        if(targetId !==state.userId){
                            //创建webrtc连接
                           await dispatch('creatWebRtc',targetId);
                            console.log("要建立连接的对方id为" + targetId);
                        }
                    }
                }
                //展示当前房间人数
                else if (message.startsWith("sum")) {
                    // showSum(message);
                }
                //是否同意继续视频聊天的信息
                else if (message.startsWith("continue")) {
                    //调用弹函窗数
                    // continueOrNo();
                }
                else if(message.startsWith("accept")){
                    //改变通话状态为等待连接中
                    commit('CHANGE_CALLING','connecting');
                    //将audio铃声关闭
                    commit('CHANGE_VIDEO',false);
                    console.log(message)
                }
                //控制信息
                else if (message.startsWith("control")) {
                    //先切割掉control:
                    let value = message.substring(8);
                    //再查找第一个:的位置
                    let i = value.indexOf(":");
                    let fromId = value.substring(0, i);
                    console.log("对方id为" + fromId)
                    let describe = value.substring(i + 1);//描述

                    //offersdp描述，要返回应答信息,接收到的消息格式
                    if (describe.startsWith("offer")) {
                        console.log("收到了offer描述")
                        //再切割出offer描述,这个别放分支
                        let offer = describe.substring(6);//截取后面信息部分
                        //当前线路的webrtc连接对象
                        let rtc;
                        //当前然远程视频流的视频标签
                        let remote;
                        //不存在就新建
                        if (state.rtcs[fromId] == null) {
                            // console.log(value)
                            //当接收到一个通话请求时,本端也要进行跟那边一样的一系列操作，除此之外还要向通话发起者发送answer应答描述
                            // 创建一个新的RTCPeerConnection对象
                            rtc = new RTCPeerConnection(state.configuration);

                            //调用同步方法将该连接对象添加到连接对象map中
                            commit('ADD_RTC',{userId:fromId,rtc:rtc});
                            console.log("创建了rtcd对象，对方的id为"+fromId)

                            // //拿到远程视频标签
                            // remote=remote = document.getElementById("video_" + fromId);
                            //
                            // //当页面中没有对方的视频标签时,就创建
                            // if(remote ==null){
                            //     remote=createVideo(fromId)
                            // }
                            // //存在的情况下要显示其父div标签
                            // document.getElementById("video_container_"+fromId).style.display = 'block';

                            //先检查state中有无本地视频流
                            if(!state.videoStreams[state.userId]){
                                //没有就先创建，//等待视频获取成功
                                await dispatch('createLocalVideo',false);
                            }

                            let localvideo=state.videoStreams[state.userId];
                            // 将本地媒体流的轨道添加到RTCPeerConnection中
                           await localvideo.getTracks().forEach(track => rtc.addTrack(track, localvideo));

                        } else {//存在可能是更新媒体轨道的请求
                            //拿到已经存在的元素
                            rtc = state.rtcs[fromId];
                        }

                        // 注册音视频轨道事件处理器
                        rtc.ontrack =  event => {
                            // 将远程音视频流显示在远程视频标签中
                            console.log("接收到视频流，对方id为："+fromId)
                            //添加视频流
                            commit('ADD_VIDEO',{userId:fromId,video:event.streams[0]})
                            //将通话状态改为通话中
                            // commit('CHANGE_CALLING','calling');
                        };

                        // 解析offer,并设置为远程描述
                        const desc = new RTCSessionDescription({type: 'offer', sdp: offer});
                        await rtc.setRemoteDescription(desc);
                        console.log("设置远程描述成功")
                        //判断有无未设置的ice缓存
                        if(state.cachedCandidates[fromId] && state.cachedCandidates[fromId].length>0 ){
                            state.cachedCandidates[fromId].forEach(ice => {

                                const candidate = new RTCIceCandidate(JSON.parse(ice));
                                 rtc.addIceCandidate(candidate)
                                     .catch(e => console.error('Error adding cached ICE candidate:', e));
                                console.log("设置了缓存的ice")
                            });
                            //清空ice候选信息
                            commit('DELETE_cachedCandidates',fromId)
                            console.log("清空了ice缓存")
                        }


                        // 创建应答（answer）,接收者跟发送的反过来，发起者是offer做本地，answer做远程，而接收者反过来
                        const answer = rtc.createAnswer();
                        // 设置本地应答描述
                        await rtc.setLocalDescription(answer);

                        // 通过websocket发送应答
                       await dispatch('sendSignalingMessage',{target:fromId, message:`answer:${rtc.localDescription.sdp}`});
                        console.log("发送了answer描述")

                        //注册rtc连接状态变化的监听
                        rtc.oniceconnectionstatechange = function (event) {
                            console.log("rtc的状态为:" + rtc.iceConnectionState)
                            //实现在断开后清空视频标签内容
                            if (rtc.iceConnectionState === 'disconnected') {
                                //从webrtcmap中删除这个连接对象
                                commit('DELETE_RTCS',fromId)
                                rtc.close();//清空这个连接对象
                                //如果存在视频流也删除
                                if(state.videoStreams[fromId]){
                                    commit('DELETE_VIDEO',fromId)
                                }
                            }
                        };

                        // 注册ICE候选者事件处理器,一样注册保证可以穿透内网进行连接
                        rtc.onicecandidate =  event => {
                            if (event.candidate) {
                                // 将ICE候选者发送给对端，也要用json包裹
                                dispatch('sendSignalingMessage',{target:fromId, message:`candidate:${JSON.stringify(event.candidate)}`});
                            }
                        };

                    }
                    //answer的描述
                    else if (describe.startsWith("answer")) {
                        console.log("收到answer描述")
                        //根据id选择对应的webrtc连接
                        let rtc = state.rtcs[fromId];
                        //再切割
                        let answer = describe.substring(7);//截取后面信息部分
                        //解析answer并设置应答描述
                        const desc = new RTCSessionDescription({type: 'answer', sdp: answer});
                        await rtc.setRemoteDescription(desc);
                        console.log("setRemoteDescription 成功")

                        // //判断有无未设置的ice缓存
                        // if(state.cachedCandidates[fromId] && state.cachedCandidates[fromId].length>0 ){
                        //     state.cachedCandidates[fromId].forEach(ice => {
                        //
                        //         const candidate = new RTCIceCandidate(JSON.parse(ice));
                        //          rtc.addIceCandidate(candidate);
                        //         console.log("设置了缓存的ice")
                        //     });
                        //     //清空ice候选信息
                        //     commit('DELETE_cachedCandidates',fromId)
                        // }
                    }
                    //ice候选者的描述
                    else {
                        console.log("收到ice候选信息")
                        //根据uid选择对应的webrtc连接
                        let rtc = state.rtcs[fromId];
                        if (rtc == null) {
                            console.log("ice注册这里拿到的rtc为空,targetuid为" + fromId + " rtcs的size为" + rtcs.length);
                        }
                        //再切割
                        let ice = describe.substring(10);//截取后面信息部分
                        //先判断远程描述有无设置
                        if (!rtc.currentRemoteDescription) {
                            console.log("未设置远程描述")
                            // 如果还未设置远程描述，则缓存ICE候选者
                            if(!state.cachedCandidates[fromId]){//没有缓存列表先建立
                                console.log('建立了ice缓存列表')
                                commit('ADD_cachedCandidates',fromId);
                            }
                            //往对应的列表添加ice候选,未转化
                            commit('ADD_cachedCandidates_listener',{id: fromId,ice:ice});
                            console.log("缓存ice成功")
                        } else {
                            // 如果已经设置了远程描述，则直接添加ICE候选者
                            // 解析并添加ICE候选者,要传入对象，所以要用JSON.parse()再转回来
                            const candidate = new RTCIceCandidate(JSON.parse(ice));
                            await rtc.addIceCandidate(candidate);
                            console.log("设置了ice")
                        }

                    }

                }
                //其他正常信息
                else {
                    //切割消息
                    let infos = splitAndKeepRemaining(message,":",4);
                    if (infos.length===5) {
                        let type=infos[1];//类型
                        let souceName=infos[2];//来源的姓名
                        let souceId=infos[3];//来源id

                        let value = infos[4];//具体消息,可能是图片，文本消息

                        //创建一条消息的对象
                        let messageInfo=null;
                        if (value.startsWith("data:image/")) { //处理图片信息
                            messageInfo={ text: value, time: new Date(), own: false, type:'IMAGE'}
                        } else {//处理文本信息
                            messageInfo={ text: value, time: new Date(), own: false, type:'TEXT'}
                        }
                        //往state的对应用户的消息列表添加记录
                        let mytype= type==='private'?'01':'02';
                       await dispatch('ADD_CHAT_MESSAGE',{userId: souceId,type: mytype,message: messageInfo})

                        console.log('audio:'+state.audio)
                        //消息提示音改变状态
                        commit('SET_AUDIO')
                        console.log('audio:'+state.audio)
                    } else {
                        console.log("响应信息有问题:"+message);
                    }

                }

            }


            ws.onerror = (error) => console.error('WebSocket error:', error);
            ws.onclose = () => {
                console.log('WebSocket connection closed');
                //清空websocket连接
                commit('SET_WEBSOCKET',null);
                //清空心跳检测
                clearInterval(state.heartbeatInterval)
                commit('SET_heartbeatInterval',null);


            };
        },
        //获取本地视频流
        createLocalVideo({ commit, state }, video) {
            console.log("请求媒体");
            if (!navigator.mediaDevices) {
                console.error('navigator.mediaDevices 没授权');
                return Promise.reject(new Error('navigator.mediaDevices is not available'));
            }

            // 返回一个 Promise 来处理异步操作,以便获取到真正的视频流
            return navigator.mediaDevices.getUserMedia({ audio: true, video })
                .then(localvideo => {
                    console.log("获取的视频流对象为")
                    console.log(localvideo);
                    // 添加视频流到 state
                    commit('ADD_VIDEO', { userId: state.userId, video: localvideo });
                    console.log("添加了本地视频流")
                    return localvideo; // 返回获取到的流以便后续操作
                })
                .catch(error => {
                    console.error('Error accessing media devices.', error);
                    throw error; // 抛出错误，让调用者处理
                });
        },
        //创建webrtc连接,传入对方id
            async creatWebRtc({ commit,state,dispatch},target) {
            //创建webrtc连接对象
            let rtc = new RTCPeerConnection(state.configuration);
            //调用同步方法将该连接对象添加到连接对象map中
            commit('ADD_RTC',{userId:target,rtc:rtc});
            console.log("创建了rtcd对象，对方的id为"+target)

            // 接收到远程视频流的操作,用来处理接收到的视频流
            rtc.ontrack = event => {
                console.log("接收到视频流，对方id为："+target)
                //添加视频流
                commit('ADD_VIDEO',{userId:target,video:event.streams[0]})
                //改变通话状态为通话中
                // commit('CHANGE_CALLING','calling');
            };

            // 将本地媒体流的轨道添加到RTCPeerConnection对象中,
            let localStream=state.videoStreams[state.userId];
            // console.log("拿到的本地视频流为")
            // console.log(localStream)
            let tracks=localStream.getTracks();
           await tracks.forEach((track) => rtc.addTrack(track, localStream));
            console.log("本地视频流设置完成")
            // 创建offer,是一个sdp描述符，用来描述会话的参数
            const offer = rtc.createOffer();
            // 设置为本地描述,加await等待设置成功再获取
             await rtc.setLocalDescription(offer);
            // 通过websocket发送offer,标识符就是offer
           await dispatch('sendSignalingMessage',{target:target, message:`offer:${rtc.localDescription.sdp}`});

            console.log("发送了offer描述")
            //等发送了offe，目标那边先创建了webrtc连接对象之后再发送ice注册信息，不然那边接收到的注册信息哪对应的rtc时就是空的
            //注册rtc连接状态变化的监听
                rtc.oniceconnectionstatechange = function (event) {
                    console.log("rtc的状态为:" + rtc.iceConnectionState)
                    //实现在断开后清空视频标签内容
                    if (rtc.iceConnectionState === 'disconnected') {
                        //从webrtcmap中删除这个连接对象
                        commit('DELETE_RTCS',target)
                        rtc.close();//清空这个连接对象
                        //如果存在视频流也删除
                        if(state.videoStreams[target]){
                            commit('DELETE_VIDEO',target)
                        }
                    }
                };
                // 注册ICE候选者事件处理器,用于穿透NAT，确保两端可以建立连接。
                rtc.onicecandidate =  event => {
                    if (event.candidate) {
                        // 将ICE候选者发送给目标,标识符为candidate,包裹为json字符串,只有这里需要，以为在接受时需要将其转化会对象
                        //由name发送给target
                        dispatch('sendSignalingMessage',{target:target, message:`candidate:${JSON.stringify(event.candidate)}`});
                        console.log("发送了ice注册信息")
                    }
                };
        },
        sendSignalingMessage({ commit,state },{ target,message}){
            //message就是标识符:信息    采用原来的格式Private:userid:targetid:message，就是message还是message,只不过包含标识符跟sdp描述
            let to_message = "VIDEO:Private:" + state.userId + ":" + target + ":" + message + ":MESSAGE_DATA_END!";
            //调用websocket发送
            if(state.ws && state.ws.readyState === WebSocket.OPEN){
                state.ws.send(to_message)
                console.log("发送了:"+message)
            }
            else {
                alert("与服务器连接丢失，websocket连接断开");
            }
        },
        //清除rtcs连接的函数,挂断函数
        cleanRtcs({ commit,state }){
            for (const key in state.rtcs) {
                //关闭所有webrtc的连接
                if (state.rtcs.hasOwnProperty(key)) {
                    //关闭连接
                    state.rtcs[key].close();
                }
            }
            //清空rtcs连接
            commit('CLEAN_RTCS')

            //清除本地视频流
            if(state.videoStreams[state.userId]){
                let localvideo=state.videoStreams[state.userId];
                    localvideo.getTracks().forEach(track => {
                        //如果轨道在活跃就停止旧轨道
                        if (track.readyState === 'live') {
                            track.stop();
                        }
                    });
            }
            commit('CLEAN_VIDEO')
        },

        //更新视频流获取的函数
        //更新媒体轨道以及重新建立连接,传入参数决定是否获取视频
        async updateAnddConnet({ commit,state,dispatch},video) {
            console.log("视频权限更新")
            //先遍历删除webrtc的旧轨道跟停止当前视频流的轨道
            for (const key in state.rtcs) {
                //更新所有的webrtc连接对象的媒体轨道并且重新连接
                if (state.rtcs.hasOwnProperty(key)) {
                    let rtc = state.rtcs[key];
                    // 移除旧的所有轨道
                    let localvideo=state.videoStreams[state.userId];
                    localvideo.getTracks().forEach(track => {
                        //如果轨道在活跃就停止旧轨道
                        if (track.readyState === 'live') {
                            track.stop();
                        }
                        //当检测到的轨道类型跟已经添加到rtc对象的轨道类型一样就删除rtc对象中的轨道
                        const sender = rtc.getSenders().find(s => s.track?.kind === track.kind);
                        if (sender) {
                            //移除
                            rtc.removeTrack(sender);
                            console.log("移除了一个轨道")
                        }
                    });
                }
            }
            console.log("移除了旧轨道")

            //再获取新视频流
            await dispatch('createLocalVideo',video)

            //再遍历添加新轨道并且重新生成,设置,发送offer描述
            for (const key in state.rtcs) {
                if (state.rtcs.hasOwnProperty(key)) {
                    let rtc = state.rtcs[key];
                    // 将新的本地媒体流的轨道添加到RTCPeerConnection对象中,
                    let localvideo=state.videoStreams[state.userId];
                    localvideo.getTracks().forEach(track => rtc.addTrack(track, localvideo));
                    console.log("更新成功")
                    // 创建offer,是一个sdp描述符，用来描述会话的参数
                    const offer = rtc.createOffer();
                    // 设置为本地描述,加await等待设置成功再获取
                    await rtc.setLocalDescription(offer);
                    // 通过websocket发送offer,标识符就是offer
                   await dispatch('sendSignalingMessage',{target:key, message:`offer:${rtc.localDescription.sdp}`});
                   console.log("发送更新的offer描述成功")
                }
            }

        },
        //心跳检测，就是每间隔30s发一条消息
        setupHeartbeat({state,commit},ws) {
            // 发送心跳,开启定时器
           let heartbeatInterval = setInterval(() => {
                if (ws.readyState === WebSocket.OPEN) {
                    ws.send('heartbeat:MESSAGE_DATA_END!');
                    console.log("发送了心跳检测")
                }
                else {
                    console.log('websocket连接断开')
                }
            }, 30000) // 30秒一次
            //存储
            commit('SET_heartbeatInterval',heartbeatInterval);
        },
        //获取用户信息
        async getUserInfo({state,commit},{userId,type,message}){
            console.log("当前没有该用户的聊天列表，正在新增")
            //先发送请求查询用户的头像，姓名,
            let  data={
                type:type,
                userId:userId
            }
            console.log("发送的请求体")
            console.log(data)
            fetch(path+"/user/getInfo",{
                method: "POST",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response=>response.json())
                .then(res=>{
                    if(res.code === 200 && res.data !=null){
                        console.log("收到了返回结果："+res.data)
                        //Vue3支持直接修改对象
                        state.chats[userId]={
                            messages: [],
                            unread: 0,
                            type: type,//区分私法，群聊
                            avatar: res.data.avatar,
                            name: res.data.name
                        };
                        //往指定用户的聊天信息列表添加消息
                        console.log("添加了信息")
                        state.chats[userId].messages.push(message);
                        if (state.currentUserId===null || userId !== state.currentUserId) {
                            state.chats[userId].unread += 1; // 如果不是当前聊天对象，则增加未读消息计数
                        }
                    }
                    else {
                        state.chats[userId]={
                            messages: [],
                            unread: 0,
                            type: type,//区分私法，群聊
                        };
                    }
                })
                .catch(error=>{
                    throw error;
                })


        },

    },
    //获取
    getters: {
        getSharedVariable: state => state.sharedVariable,

        getUserName (state){
            return state.userName;
        },
        getUserId :state => state.userId,
        getMessageMap: state=>state.chats,
        getCurrentUserId: state=>state.currentUserId,
        getWs: state=>state.ws,
        getAudio: state=>state.audio,
        getVideo: state=>state.video,
        getCalling: state=>state.calling,
        getCallId: state=>state.callId,
        getVideoStreams: state=>state.videoStreams,
    }
});

//切割字符串函数
function splitAndKeepRemaining(str, separator, n) {

    const result = [];

    let remaining = str;

    for (let i = 0; i < n; i++) {

        const index = remaining.indexOf(separator);

        if (index === -1) break;

        result.push(remaining.substring(0, index));

        remaining = remaining.substring(index + separator.length);

    }

    result.push(remaining);

    return result;

}

export default store;