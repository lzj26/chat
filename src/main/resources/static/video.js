let websocket;//wensocket连接对象
let local;//本地视频标签
let localvideo;//本地视频流,包括音频跟视频
let audioTrack;//记录用户的音频授权
let rtcs = {};//webrtc的连接对象map
let name;//输入的用户名
let userId;//用户名对应的id
let client_type;//客户端的类型
let type;//聊天的类型
//配置ice服务器用来内网穿透
const configuration = {iceServers: [{urls: 'stun:stun.l.google.com:19302'}]}; // ICE服务器配置，用于穿透NAT


//设置监听在页面完全加载后在获取元素
document.addEventListener('DOMContentLoaded', () => {
    //获取客户端类型
    client_type = getType();
    console.log('client_type=', client_type);
    //打开注册/登录弹窗保证先注册/登录
    showLogin().then(result => {

        local = document.getElementById("localvideo");
        if (!local) {
            console.error('视频标签未找到');
        }
        type = document.getElementById("send_type");//当前发送的类型
        //给这个选择框添加监听change 事件监听器
        type.addEventListener('change', function () {
            // 获取当前选中的值
            let selectedValue = this.value;
            console.log(selectedValue)
            //发送请求获取元素
            getAllElement(selectedValue);

        });
        //给好友框也添加change监听
        document.getElementById('target').addEventListener('change', function () {
            //先清空聊天框
            let chats = document.getElementById('recipient_message');
            chats.value = '';
            //再填充聊天信息
            console.log(this.value);
            getOldChats(this.value);
        })

        // //除了连接禁用其他的按钮
        // document.getElementById("send").disabled = true;
        // document.getElementById("send_img").disabled = true;
        document.getElementById("end").disabled = true;
        document.getElementById("openOrCloseVideo").disabled = true;
        document.getElementById("openOrCloseAudio").disabled = true;
        // document.getElementById("call").disabled = true;
    });


})


//获取本地视频流,传入参数决定是否开启摄像头,默认不开启
async function createLocalVideo(video) {
    console.log("请求媒体")
    //授权访问麦克风跟摄像头,navigator.mediaDevices提供了对用户媒体设备（如摄像头和麦克风）的访问接口
    if (!navigator.mediaDevices) {
        console.error('navigator.mediaDevices 没授权');
    }

    localvideo = await navigator.mediaDevices.getUserMedia({audio: true, video});
    console.log(localvideo)
    //根据用户选择来决定音频操作

    if (audioTrack === false) {
        console.log("关闭了麦克风")
        localvideo.getAudioTracks()[0].enabled = false;
    }
    //将本地视频流显示显示在页面上
    local.srcObject = localvideo;

    //设置视频描述
    document.getElementById('localvideo_name').textContent = name;
    console.log('设置了名字')
    //显示本地视频div组件
    document.getElementById('div_localvideo').style.display = 'block';
}

//控制是否开启麦克风
async function openCloseAudio(button) {
    //由关到开
    if (button.getAttribute("name") === "open") {
        //修改状态
        audioTrack = true;
        //启用麦克风
        localvideo.getAudioTracks()[0].enabled = true;

        //修改按钮文字跟name属性
        button.setAttribute("name", "close");
        button.textContent = "关闭麦克风";
    } else {
        //修改状态
        audioTrack = false;
        //启用麦克风
        localvideo.getAudioTracks()[0].enabled = false;
        //修改按钮文字跟name属性
        button.setAttribute("name", "open");
        button.textContent = "开启麦克风";
    }
}

//控制是否开启摄像头
async function openCloseVideo(button) {
    //由关到开
    if (button.getAttribute("name") === "open") {

        // await createLocalVideo(true)
        //更新轨道并重新连接
        await updateAndConnet(true);
        //修改按钮文字跟name属性
        button.setAttribute("name", "close");
        button.textContent = "关闭摄像头";
    } else {
        // await createLocalVideo(false)
        //更新轨道并重新连接
        await updateAndConnet(false);
        //修改按钮文字跟name属性
        button.setAttribute("name", "open");
        button.textContent = "开启摄像头";
    }
}

//更新媒体轨道以及重新建立连接,传入参数决定是否获取视频
async function updateAndConnet(video) {
    console.log("视频权限更新")
    //先遍历删除webrtc的旧轨道跟停止当前视频流的轨道
    for (const key in rtcs) {
        //更新所有的webrtc连接对象的媒体轨道并且重新连接
        if (rtcs.hasOwnProperty(key)) {
            let rtc = rtcs[key];
            // 移除旧的所有轨道
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

    //再获取新视频流
    await createLocalVideo(video)

    //再遍历添加新轨道并且重新生成,设置,发送offer描述
    for (const key in rtcs) {
        if (rtcs.hasOwnProperty(key)) {
            let rtc = rtcs[key];
            // 将新的本地媒体流的轨道添加到RTCPeerConnection对象中,
            localvideo.getTracks().forEach(track => rtc.addTrack(track, localvideo));
            console.log("更新成功")
            // 创建offer,是一个sdp描述符，用来描述会话的参数
            const offer = rtc.createOffer();
            // 设置为本地描述,加await等待设置成功再获取
            await rtc.setLocalDescription(offer);
            // 通过websocket发送offer,标识符就是offer
            await sendSignalingMessage(userId, key, `offer:${rtc.localDescription.sdp}`);
            console.log("发送offer成功")
        }
    }


}


//发起通话
async function call() {
    // document.getElementById("call").disabled = true;
    // 获取要通话的目标列表,以,拼接
    let target_name = document.getElementById("target_names").value;
    console.log(target_name)
    if (target_name.length > 0) {
        //分情况，当本地媒体流不为空时就是中途加入好友的操作
        if (localvideo == null) {
            // 初始化时询问用户是否自动开启麦克风
            audioOpen().then(async (value) => {
                // 先获取本地视频流
                await createLocalVideo(false);

                //向服务器发送开始邀请通话消息,这里的群组先写死。注意拼接结束信息,VIDEO:start:userId:username:groupname:targetnames
                let message = "VIDEO:start:" + userId + ":" + name + ":" + "group1" + ":" + target_name + ":MESSAGE_DATA_END!";
                console.log(message)
                //发送
                if (websocket == null) console.log("连接为空")
                websocket.send(message);
                console.log("发送了邀请")

                //开启挂断按钮
                // document.getElementById("call").disabled = true;
                document.getElementById("end").disabled = false;
                document.getElementById("openOrCloseVideo").disabled = false;
                document.getElementById("openOrCloseAudio").disabled = false;
            });
        } else {//中途加人
            //向服务器发送开始邀请通话消息,这里的群组先写死。注意拼接结束信息
            let message = "VIDEO:start:" + userId + ":" + name + ":" + "group1" + ":" + target_name + ":MESSAGE_DATA_END!";
            console.log(message)
            //发送
            if (websocket == null) console.log("连接为空")
            websocket.send(message);
            console.log("发送了邀请")
        }
    } else {
        alert("目标用户未选择/输入");
    }

    // document.getElementById("openOrCloseAudio").disabled = true;

}

//接受通话邀请的函数,传入发起者名字
async function accept(startId) {
    //向服务器发送接受消息,这里的群组先写死。VIDEO:accept:startId:acceptId:acceptName
    let message = "VIDEO:accept:" + startId + ":" + userId + ":" + name + ":MESSAGE_DATA_END!";

    // 初始化时询问用户是否自动开启麦克风
    audioOpen().then(async (value) => {
        // 先开启摄像头
        await createLocalVideo(false);

        //发送,都要放then里面不然这边还没申请创建媒体流呢
        await websocket.send(message);

        //开启按钮
        document.getElementById("openOrCloseVideo").disabled = false;
        document.getElementById("openOrCloseAudio").disabled = false;
        document.getElementById("end").disabled = false;
    });


}


//新建webrtc连接的函数,传入目标的id
async function creatWebRtc(target) {

    //创建一个用来容纳该远程视频流的标签
    let remote =createVideo(target);

    //创建webrtc连接对象
    let rtc = new RTCPeerConnection(configuration);
    //将该连接对象添加到连接对象map中
    rtcs[target] = rtc;
    console.log("创建了rtcd对象，对方的id为"+target)
    // console.log(rtcs.size +" "+rtcs.length)
    // 接收到远程视频流的操作,用来处理接收到的视频流
    rtc.ontrack = event => {
        console.log("接收到视频流")
        //把远程视频流放入上面创建的视频标签里面
        remote.srcObject = event.streams[0];
    };

    // 将本地媒体流的轨道添加到RTCPeerConnection对象中,
    localvideo.getTracks().forEach(track => rtc.addTrack(track, localvideo));

    // 创建offer,是一个sdp描述符，用来描述会话的参数
    const offer = rtc.createOffer();
    // 设置为本地描述,加await等待设置成功再获取
    await rtc.setLocalDescription(offer);
    // 通过websocket发送offer,标识符就是offer
    await sendSignalingMessage(userId, target, `offer:${rtc.localDescription.sdp}`);

    console.log("发送了offer描述")
    //等发送了offe，目标那边先创建了webrtc连接对象之后再发送ice注册信息，不然那边接收到的注册信息哪对应的rtc时就是空的
    // 注册ICE候选者事件处理器,用于穿透NAT，确保两端可以建立连接。
    rtc.onicecandidate = event => {
        if (event.candidate) {
            // 将ICE候选者发送给目标,标识符为candidate,包裹为json字符串,只有这里需要，以为在接受时需要将其转化会对象
            //由name发送给target
            sendSignalingMessage(userId, target, `candidate:${JSON.stringify(event.candidate)}`);
        }
    };
    //注册rtc连接状态变化的监听
    clearVideo(rtc, remote,target);
}

//发送video消息,就是这里出错，因为写死了,当应答者成功接收然后发送answer描述还是发给自己
function sendSignalingMessage(userId, target, message) {//带目标发送
    //message就是标识符:信息    采用原来的格式Private:username:target:message，就是message还是message,只不过包含标识符跟sdp描述
    //拼接
    let to_message = "VIDEO:Private:" + userId + ":" + target + ":" + message + ":MESSAGE_DATA_END!";
    // console.log(to_message)
    //发送消息
    websocket.send(to_message);
}

//挂断函数
function end() {
    //清空本地视频标签内容并且隐藏本地视频标签
    local.srcObject = null;
    document.getElementById('div_localvideo').style.display = 'none';

    // 遍历对象清空视频流
    for (const key in rtcs) {
        //关闭所有webrtc的连接
        if (rtcs.hasOwnProperty(key)) {
            rtcs[key].close();
            //清空视频标签内容
            let elementById = document.getElementById("video_" + key);
            elementById.srcObject = null;
            let video = document.getElementById('video_container_'+key);
            //删除各个视频流的父div
            document.getElementById('video_show').removeChild(video);
        }
    }
    rtcs = null;

    //发送挂断信息
    hangUp();

    //清空视频流轨道
    closeTarks();

    //清空房间人数
    document.getElementById('sum').textContent='';
}


//websocket连接函数
async function connect() {

    // //获取并存储用户名
    // name = document.getElementById('name').value;
    // userId=1;



    //创建一个websocket的连接
    //  websocket=new WebSocket("wss://139.9.200.130:8443/ws");//指定服务端的IP地址
    // websocket=new WebSocket("wss://localhost:8443/ws");//指定本机的IP地址
    websocket=new WebSocket("ws://localhost:8443/ws");//指定本机的IP地址
    // websocket=new WebSocket("wss://192.168.2.190:8443/ws");//内网测试
    // websocket = new WebSocket("wss://test-sami-ws.warmheart.top:443/ws");//公司服务器


    // 当 WebSocket 连接成功时触发
    websocket.onopen = async function () {
        console.log("连接成功");
        //需要发送注册消息，但是服务器那边不需要进行返回登陆成功跟查询id的操作
        if (name.length > 0) {
            await websocket.send("REGISTER:" + name + ":" +userId+":"+ client_type + ":MESSAGE_DATA_END!");//发送注册信息,带上结束标识
            console.log("REGISTER:" + name + ":" +userId+":"+ client_type + ":MESSAGE_DATA_END!")
        } else {
            alert("用户名未知");
        }
        // //禁用连接，开启发送/通话
        // // document.getElementById("connect").disabled = true;
        // document.getElementById("send").disabled = false;
        // document.getElementById("send_img").disabled = false;
        // document.getElementById("openOrCloseAudio").disabled = false;
        // document.getElementById("call").disabled = false;

    }
    //当接收到信息时触发
    websocket.onmessage = async function (data) {
        let message = data.data;

        //分情况处理
        // //拿到用户id
        // if (message.startsWith("userid")) {
        //     //先把userId: 切割
        //     let value = data.data.substring(7);
        //     let i = value.indexOf(":");
        //     userId = value.substring(0, i);//来源消息
        //     console.log(userId);
        //     //原来的响应信息
        //     let text = value.substring(i + 1);//具体消息,可能是图片，文本,交换消息
        //     let time = new Date().toLocaleTimeString();
        //     showMessage(text, time);
        // }
        //请求通话的消息:request_call:群组name:发起人name:发起人id
         if (message.startsWith("request_call")) {
            let strings = message.split(":");
            //调用弹窗函数来看是否接受请求
            openPopUp(strings[1], strings[2], strings[3]);
        }
        //收到返回的房间参与者id列表信息,注意返回类似return_idLink:[hello, 你好],有空格
        else if (message.startsWith("return_idLink")) {
            //切割掉return_idLink:[
            let nameLink = message.substring(15, message.length - 1);
            console.log(nameLink);
            let name_link = nameLink.split(",");
            //遍历每个成员都新建一个webrtc连接
            for (let i = 0; i < name_link.length; i++) {
                //新建一个webrtc连接,等一个建立完再继续,注意清除前后空格
                let targetId = name_link[i].trim();
                //不是自己的id再建立webrtc连接
                if(targetId !==userId){
                    await creatWebRtc(targetId);
                    console.log("要建立连接的对方id为" + targetId);
                }
            }
        }
        //展示当前房间人数
        else if (message.startsWith("sum")) {
            showSum(message);
        }
        //是否同意继续视频聊天的信息
        else if (message.startsWith("continue")) {
            //调用弹函窗数
            continueOrNo();
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
                if (rtcs[fromId] == null) {
                    // console.log(value)
                    //当接收到一个通话请求时,本端也要进行跟那边一样的一系列操作，除此之外还要向通话发起者发送answer应答描述
                    // 创建一个新的RTCPeerConnection对象
                    rtc = new RTCPeerConnection(configuration);

                    //先放进webrtc的连接对象map，不然ice注册描述那边取不到对应的对象，异步的
                    rtcs[fromId] = rtc;
                    //拿到远程视频标签
                    remote=remote = document.getElementById("video_" + fromId);
                    console.log("创建了rtcd对象，对方的id为"+fromId)
                    //当页面中没有对方的视频标签时,就创建
                    if(remote ==null){
                        remote=createVideo(fromId)
                    }
                    //存在的情况下要显示其父div标签
                    document.getElementById("video_container_"+fromId).style.display = 'block';


                    // 将本地媒体流的轨道添加到RTCPeerConnection中
                    localvideo.getTracks().forEach(track => rtc.addTrack(track, localvideo));

                } else {//存在可能是更新媒体轨道的请求
                    //拿到已经存在的元素
                    rtc = rtcs[fromId];
                    remote = document.getElementById("video_" + fromId);
                }

                // 注册ICE候选者事件处理器,一样注册保证可以穿透内网进行连接
                rtc.onicecandidate = event => {
                    if (event.candidate) {
                        // 将ICE候选者发送给对端，也要用json包裹
                        sendSignalingMessage(userId, fromId, `candidate:${JSON.stringify(event.candidate)}`);
                    }
                };

                //注册rtc连接状态变化的监听
                clearVideo(rtc, remote,fromId);

                // 注册音视频轨道事件处理器
                rtc.ontrack = event => {
                    // 将远程音视频流显示在远程视频标签中
                    remote.srcObject = event.streams[0];
                };

                // 解析offer,并设置为远程描述
                const desc = new RTCSessionDescription({type: 'offer', sdp: offer});
                await rtc.setRemoteDescription(desc);


                // 创建应答（answer）,接收者跟发送的反过来，发起者是offer做本地，answer做远程，而接收者反过来
                const answer = await rtc.createAnswer();
                // 设置本地应答描述
                await rtc.setLocalDescription(answer);

                // 通过websocket发送应答
                sendSignalingMessage(userId, fromId, `answer:${rtc.localDescription.sdp}`);
                console.log("发送了answer描述")
            }
            //answer的描述
            else if (describe.startsWith("answer")) {
                console.log("收到answer描述")
                //根据id选择对应的webrtc连接
                let rtc = rtcs[fromId];
                //再切割
                let answer = describe.substring(7);//截取后面信息部分
                //解析answer并设置应答描述
                const desc = new RTCSessionDescription({type: 'answer', sdp: answer});
                await rtc.setRemoteDescription(desc);
                console.log("setRemoteDescription 成功")
            }
            //ice候选者的描述
            else {
                console.log("收到ice候选信息")
                //根据uid选择对应的webrtc连接
                let rtc = rtcs[fromId];
                if (rtc == null) {
                    console.log("ice注册这里拿到的rtc为空,targetuid为" + fromId + " rtcs的size为" + rtcs.length);
                }
                //再切割
                let ice = describe.substring(10);//截取后面信息部分

                // 解析并添加ICE候选者,要传入对象，所以要用JSON.parse()再转回来
                const candidate = new RTCIceCandidate(JSON.parse(ice));
                await rtc.addIceCandidate(candidate);
            }

        }
        //其他正常信息
        else {
            //切割消息
            let i = message.indexOf(":");
            if (i !== -1) {
                let header = message.substring(0, i);//来源消息

                let value = message.substring(i + 1);//具体消息,可能是图片，文本,交换消息

                if (value.startsWith("data:image/")) { //处理图片信息
                    document.getElementById("recipient_message").value = header;
                    let imgElement = document.getElementById('getimage');
                    imgElement.src = value;//显示图片
                    console.log('显示成功')
                } else {//处理文本信息
                    //将收到的文本信息展示在接收框
                    let time = new Date().toLocaleTimeString();
                    showMessage(message, time);
                    console.log(message);//返回的是一个MessageEvent 对象，调用data方法获取文本信息
                }
            } else {
                console.log("响应信息有问题");
            }
        }
    }

    //当连接关闭时触发的操作
    websocket.onclose = function () {

        console.log("连接关闭");
        let time = new Date().toLocaleTimeString();
        showMessage("连接关闭",time);
        //开启连接，关闭发送
        document.getElementById("send").disabled = true;
        document.getElementById("send_img").disabled = true;

        //在websocket连接关闭时也同时清空该设备的视频流资源，但是不发送挂断信号到服务器，使得用户可以重连
        //清空本地视频标签内容跟webrtc元素,参考挂断函数，但是不发送挂断信号,也发不啊
        if(local!=null){
            local.srcObject = null;
            //隐藏本地视频div组件
            document.getElementById('div_localvideo').style.display = 'none';
        }
       if(rtcs!=null){
           // 遍历对象清空视频流
           for (const key in rtcs) {
               //关闭所有webrtc的连接
               if (rtcs.hasOwnProperty(key)) {
                   rtcs[key].close();
                   //清空视频标签内容
                   let elementById = document.getElementById("video_" + key);
                   elementById.srcObject = null;

                   let video = document.getElementById('video_container_'+key);
                   //删除各个视频流的父div
                   document.getElementById('video_show').removeChild(video);
               }
           }
           rtcs = null;
       }

        //清空视频流轨道
        closeTarks();

        //清空房间人数
        document.getElementById('sum').textContent='';

    }

    //当发生错误时触发的操作
    websocket.onerror = function (data) {
        alert(data)
        console.log(data);
    }

}


//发送文本信息
function sendMessage() {
    //获取当前的发送类型
    let send_type = type.value;
    //获取当前的目标
    let send_target = document.getElementById("target").value;
    //获取输入框的输入
    let message = document.getElementById("message").value;
    if (message.length > 0) {
        //检查连接状态并发送信息
        if (websocket.readyState === WebSocket.OPEN) {//如果连接状态==打开
            console.log(send_type + ":" + send_target + ":" + message + ":MESSAGE_DATA_END!");
            //格式：Private:userName:userId:targetName:targetId:message
            let text = send_type + ":" + name + ":" + userId + ":" + send_target + ":" + message + ":MESSAGE_DATA_END!"
            websocket.send(text);//调用方法发送信息,带上结束标识
            document.getElementById("message").value = "";//清空输入框
        }
    } else {
        let time = new Date().toLocaleTimeString();
        showMessage('没有输入信息', time)
        console.log('没有输入信息')
    }
}

//发送图片信息
function sendImage() {
    //获取选择的图片
    let input = document.getElementById('image');
    if (input.files.length > 0) {
        let file = input.files[0];//选择第一个选中的图片发送
        // console.log(file);
        let reader = new FileReader();
        //这个一个触发器，当reader的读取完成之后触发的动作
        reader.onloadend = function () {
            if (websocket.readyState === WebSocket.OPEN) {//如果连接状态==打开

                console.log("准备发送")
                //分块发送
                let MaxSize = 65518;//设置64kB的块大小,注意算上18字节的结束标识符
                let array = [];   //设置存储分块的数组
                let len = reader.result.length;
                for (let i = 0; i < len; i += MaxSize) {//切割
                    //切割数据
                    array.push(reader.result.slice(i, i + MaxSize));
                }
                //分块发送
                //先读取并发送头部消息
                //获取当前的发送类型
                let send_type = type.value;
                //获取当前的目标,//格式：Private:userName:userId:targetName:targetId:message
                let send_target = document.getElementById("target").value;
                let header = send_type + ":" + name + ":" + userId + ":" + send_target + ":";
                console.log(header)
                websocket.send(header);//发送头部消息
                let arrayLen = array.length;
                for (let j = 0; j < arrayLen; j++) {//发送
                    console.log("发送分片：" + j);
                    //发送数据
                    websocket.send(array[j]);//base64编码之后发送的也是TextWebSocketFrame文本帧
                    // if(j===0) console.log(array[j])
                }
                //最后带着标识符发送最后一次
                //发送结束数据
                websocket.send(":MESSAGE_DATA_END!");//base64编码之后发送的也是TextWebSocketFrame文本帧

                console.log("发送成功")
            } else {
                console.log('未连接');
            }
        };
        //开始读取将选择的图片以base64的编码读取到reader中，注意触发器要写在前面！！！
        reader.readAsDataURL(file);
    } else {
        let time = new Date().toLocaleTimeString();
        showMessage('没有选择图片', time)
        console.log('没有选择图片');
    }
}

//打开是否接受弹窗
function openPopUp(groupName, startName, startId) {

    //修改描述
    let message;
    if (groupName !== "") {
        message = "来自群组:" + groupName + "的成员" + startName + "邀请你视频聊天";
    } else {
        message = startName + "邀请你视频聊天";
    }
    document.getElementById("describe").textContent = message;
    //显示弹窗
    document.getElementById('dialog').style.display = 'block';
    document.getElementById('overlay').style.display = 'block';

    //统一的后续操作，调用接受函数
    document.getElementById("accept").onclick = function () {
        hideDialog()
        accept(startId);
    }
    document.getElementById("refuse").onclick = function () {
        hideDialog()
    }

}

//关闭弹窗
function hideDialog() {
    document.getElementById('dialog').style.display = 'none';
    document.getElementById('overlay').style.display = 'none';
}

//打开请求麦克风弹窗
function audioOpen() {
    //首先前置操作函数要返回一个Promise对象
    return new Promise((resolve) => {//resolve就是后面的返回分支
        //显示弹窗
        document.getElementById('selectAudio').style.display = 'block';
        document.getElementById('overlay').style.display = 'block';

        //统一的后续操作，调用接受函数
        document.getElementById("yes").onclick = function () {
            audioTrack = true;
            console.log(audioTrack)
            //修改控制麦克风的状态,因为默认时开启，这里开启就要改为关闭
            let button = document.getElementById("openOrCloseAudio");
            //修改按钮文字跟name属性
            button.setAttribute("name", "close");
            button.textContent = "关闭麦克风";

            hideAudio()
            //最后调用resolve来说明这是成功的情况,true是传递给then里面操作的值
            //但是我这里是全局变量设置了，就不需要使用这个返回值
            resolve(true);
        }
        document.getElementById("no").onclick = function () {
            audioTrack = false;
            console.log(audioTrack)
            hideAudio()
            //另外一个成功的分支
            resolve(false);
        }
    })
}

//关闭弹窗
function hideAudio() {
    document.getElementById('selectAudio').style.display = 'none';
    document.getElementById('overlay').style.display = 'none';
}


//监听rtc连接对象状态，在连接断开时清空视频标签内容
function clearVideo(rtc, remote,target) {
    //注册rtc连接状态变化的监听
    rtc.oniceconnectionstatechange = function (event) {
        console.log("rtc的状态为:" + rtc.iceConnectionState)
        //实现在断开后清空视频标签内容
        if (rtc.iceConnectionState === 'disconnected') {
            //从webrtcmap中删除这个连接对象
            delete rtcs[target];
            rtc.close();//清空这个连接对象

            //清空对应的视频标签
            remote.srcObject = null;

            // 将其父div标签隐藏
           document.getElementById('video_container_'+target).style.display = 'none';

        }
    };
}

//展示消息
function showMessage(message, time) {
    let elementById = document.getElementById("recipient_message");
    elementById.value += time + '\n';
    elementById.value += message + '\n' + '\n';
    // 滚动到底部scrollHeight文本框总高度,clientHeight可视高度
    elementById.scrollTop = elementById.scrollHeight - elementById.clientHeight - 20;
}

//刚进来时的登录弹窗
function showLogin() {
    //使用Promise函数保证执行顺序，使得先注册
    return new Promise((resolve) => {
        //显示弹窗
        document.getElementById('login').style.display = 'block';
        document.getElementById('overlay').style.display = 'block';
        resolve(true);
    })

}

//检查输入用户名是否为空
function queryIsNull(input) {
    document.getElementById("button_login").disabled = input.value.trim() === '';
}

//控制操作说明图片的可见性
function explainOpen() {
    document.getElementById('explain').style.display = 'block';
    document.getElementById('overlay').style.display = 'block';
    //添加全局点击监听函数，点击就关闭弹窗,使用冒泡阶段true,适合在事件到达目标节点之前进行一些预处理操作，例如关闭弹窗。
    document.addEventListener('click', explainClose, true);
}

function explainClose() {
    document.getElementById('explain').style.display = 'none'
    document.getElementById('overlay').style.display = 'none';
    document.removeEventListener('click', explainClose, true);
}

//获取该客户端的类型
function getType() {
    //从user-Agent信息中获取
    let userAgent = navigator.userAgent;
    //手机可能的类型,不在数组的类型就是pc端
    let mobileType = ['Mobile', 'Android', 'iPhone', 'iPad', 'Windows Phone'];
    for (let i = 0; i < mobileType.length; i++) {
        //如果在数组里面说明是手机端
        if (userAgent.indexOf(mobileType[i].toLowerCase()) > -1) {
            return '手机'
        }
    }
    //不然就是电脑端
    return '电脑';
}

//打开是否继续弹窗
function continueOrNo() {
    document.getElementById('continue').style.display = 'block';
    document.getElementById('overlay').style.display = 'block';

    //同意的分支
    document.getElementById('result-container').addEventListener('click', () => {
        //关闭弹窗
        closeContinue()
        //调用同意继续的函数
        continueVideo();
    })
    //拒绝的分支
    document.getElementById('result-nocontainer').addEventListener('click', () => {
        closeContinue()
        //调用拒绝的函数
        hangUp();
    })
}

function closeContinue() {
    document.getElementById('continue').style.display = 'none';
    document.getElementById('overlay').style.display = 'none';
}

//同意继续通话就发送同意信息给服务器，格式 VIDEO:continue:userId:userName
function continueVideo() {

    // 初始化时询问用户是否自动开启麦克风
    audioOpen().then(async (value) => {
        // 先获取本地视频流
        await createLocalVideo(false);

        //向服务器发送同意继续视频的请求
        let message = "VIDEO:continue:" + userId + ":" + name + ":MESSAGE_DATA_END!";
        //发送
        if (websocket == null) console.log("连接为空")
        websocket.send(message);
        console.log("发送了同意继续视频的信息")

        //开启挂断按钮
        // document.getElementById("call").disabled = true;
        document.getElementById("end").disabled = false;
        document.getElementById("openOrCloseVideo").disabled = false;
        document.getElementById("openOrCloseAudio").disabled = false;
    });

}

//发送挂断信息的函数
function hangUp() {
    //向服务器发送挂断信号,VIDEO:leave:userId:userName
    let message = "VIDEO:leave:" + userId + ":" + name + ":MESSAGE_DATA_END!";
    if (websocket != null) {
        websocket.send(message);
    } else {
        console.log("连接为空")
    }
}

//展示人数
function showSum(message){
    let split = message.split(':');
    document.getElementById('sum').textContent=split[1];
}

//停止轨道
function closeTarks() {
    if(localvideo!=null){
        localvideo.getTracks().forEach(track => {
            //如果轨道在活跃就停止旧轨道
            if (track.readyState === 'live') {
                track.stop();
            }
        });
    }
}

//创建新视频标签,传入目标的id跟用户名
function createVideo(id) {
    //创建父div,类为video-container
    let video_container=document.createElement('div');
    video_container.setAttribute('class','video_container');
    video_container.setAttribute('id','video_container_'+id);
    //创建视频标签
    let remote = document.createElement("video");
    //设置id
    remote.setAttribute("id", "video_" + id);
    // 设置该video标签的属性
    remote.controls = true; // 显示播放器控制条
    remote.width = 320; //设置宽高
    remote.height = 240;
    remote.autoplay = true;//设置自动播放
    remote.muted = true;//设置静音

    //创建描述文本的子div
    let video_label=document.createElement('div');
    video_label.setAttribute('class','video_label');
    //设置id
    video_label.setAttribute('id',"video_" + id+"_name")


    //将视频跟描述添加到父div
    video_container.appendChild(remote);
    video_container.appendChild(video_label);

    //将父div添加到视频流展示区域
    let videoShow = document.getElementById('video_show');
    videoShow.appendChild(video_container);

    //通过document对象再拿到到该父div使其显示
    document.getElementById('video_container_'+id).style.display='block'

    //填充视频描述
    getNameById(id);

    //返回视频标签
    return remote;
}