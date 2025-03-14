
var websocket;//wensocket连接对象
let local;//本地视频标签
let target;//对方标签
let localvideo;//本地视频流
let rtc;//webrtc的对象
let name;//输入的用户名
let target_name;//要发起通话的目标用户名
//配置ice服务器用来内网穿透
const configuration = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] }; // ICE服务器配置，用于穿透NAT
//设置监听在页面完全加载后在获取元素
document.addEventListener('DOMContentLoaded', () => {
    local = document.getElementById("localvideo");
    if (!local) {
        console.error('视频标签未找到');
    }
    target = document.getElementById('targetvideo');

    //除了连接/开启摄像头禁用其他的按钮
    document.getElementById("send").disabled = true;
    document.getElementById("send_img").disabled = true;
    document.getElementById("end").disabled = true;
} )

//获取本地视频流
async function createLocalVideo(){

    //授权访问麦克风跟摄像头,navigator.mediaDevices提供了对用户媒体设备（如摄像头和麦克风）的访问接口
    if (!navigator.mediaDevices) {
        console.error('navigator.mediaDevices 没授权');
    }

    localvideo=await navigator.mediaDevices.getUserMedia({ audio: true, video: true });
    console.log(localvideo)
    //将本地视频流显示显示在页面上
    local.srcObject = localvideo;
}


//发起通话
async function call(){
    // document.getElementById("call").disabled = true;

    //先开启摄像头
    await createLocalVideo();

    // 获取要通话的目标列表,以,拼接
    target_name=document.getElementById("target_names").value;
    console.log(target_name)
    //创建webrtc连接
    rtc=new RTCPeerConnection(configuration);
    // console.log(rtc)
    // 注册ICE候选者事件处理器,用于穿透NAT，确保两端可以建立连接。
    rtc.onicecandidate = event => {
        if (event.candidate) {
            // 将ICE候选者发送给目标,标识符为candidate,包裹为json字符串,只有这里需要，以为在接受时需要将其转化会对象
            //先假定写死，user1发起，user2接收
            sendSignalingMessage(name,target_name,`candidate:${JSON.stringify(event.candidate)}`);
        }
    };

    // 接收到远程视频流的操作,用来处理接收到的视频流
    rtc.ontrack = event => {
        console.log("接收到视频流")
        // 将远程音视频流显示在远程视频标签中
        target.srcObject = event.streams[0];
    };

    // 将本地媒体流的轨道添加到RTCPeerConnection对象中,
    localvideo.getTracks().forEach(track => rtc.addTrack(track, localvideo));

    // 创建offer,是一个sdp描述符，用来描述会话的参数
    const offer = await rtc.createOffer();
    // 设置为本地描述
    await rtc.setLocalDescription(offer);
    // 通过websocket发送offer,标识符就是offer
    sendSignalingMessage(name,target_name,`offer:${rtc.localDescription.sdp}`);


    //开启挂断按钮
    document.getElementById("call").disabled = true;
    document.getElementById("end").disabled = false;
}

//发送video消息,就是这里出错，因为写死了,当应答者成功接收然后发送answer描述还是发给自己
function sendSignalingMessage(name,target,message) {//带目标发送
    //message就是标识符:信息    采用原来的格式Private:username:target:message，就是message还是message,只不过包含标识符跟sdp描述
    //拼接
    let to_message="Private:"+name+":"+target+":"+message+":MESSAGE_DATA_END!";
    // console.log(to_message)
    //发送消息
    websocket.send(to_message);
}

//挂断函数
function end(){
    //关闭连接
    rtc.close();
    rtc=null;
    //清空视频标签内容
    local.srcObject=null;
    target.srcObject=null;

    //启动开启摄像头按钮
    document.getElementById("end").disabled = true;
    document.getElementById("start").disabled = true;
}

















//连接函数
async function connect() {

    //创建一个websocket的连接
    //  websocket=new WebSocket("wss://124.220.215.198:8443/ws");//指定服务端的IP地址
    websocket=new WebSocket("wss://124.220.215.198:8443/ws");//指定本机的IP地址

    // 当 WebSocket 连接成功时触发
    websocket.onopen=  async function(){
        console.log("连接成功");
        //获取用户名
          name=document.getElementById('name').value;
        await websocket.send("REGISTER:"+name+":MESSAGE_DATA_END!");//发送注册信息,带上结束标识
        console.log("REGISTER:"+name+":MESSAGE_DATA_END!")

        //开启发送
        document.getElementById("send").disabled = false;
        document.getElementById("send_img").disabled = false;
    }
    //当接收到信息时触发
    websocket.onmessage=async function(data){
        // alert("hello3")
        //切割消息
        let i=data.data.indexOf(":");
        if(i!==-1){
            let header=data.data.substring(0, i);//来源消息

            let message=data.data.substring(i+1);//具体消息,可能是图片，文本
            // console.log(header);//先说明来源
            // console.log(message)

            //分情况处理，可能是video信息

            //offersdp描述，要返回应答信息
            if(message.startsWith("offer"))
            {

                console.log("这是offer的信息")
                //再切割
                let value =message.substring(6);//截取后面信息部分
                // console.log(value)
                //当接收到一个通话请求时,本端也要进行跟那边一样的一系列操作，除此之外还要向通话发起者发送answer应答描述
                // 创建一个新的RTCPeerConnection对象
                rtc = new RTCPeerConnection(configuration);

                // 注册ICE候选者事件处理器,一样注册保证可以穿透内网进行连接
                rtc.onicecandidate = event => {
                    if (event.candidate) {

                        //切割出发起者的用户名
                        target_name=header.substring(5);
                        console.log(name +" "+ target_name);
                        // 将ICE候选者发送给对端，也要用json包裹
                        sendSignalingMessage(name,target_name,`candidate:${JSON.stringify(event.candidate)}`);
                    }
                };

                // 注册音视频轨道事件处理器
                rtc.ontrack = event => {
                    // 将远程音视频流显示在远程视频标签中
                    target.srcObject = event.streams[0];
                };

                // 解析offer,并设置为远程描述
                const desc = new RTCSessionDescription({ type: 'offer', sdp: value });
                await rtc.setRemoteDescription(desc);
                // 将本地媒体流的轨道添加到RTCPeerConnection中
                localvideo.getTracks().forEach(track => rtc.addTrack(track, localvideo));

                // 创建应答（answer）,接收者跟发送的反过来，发起者是offer做本地，answer做远程，而接收者反过来
                const answer = await rtc.createAnswer();
                // 设置本地应答描述
                await rtc.setLocalDescription(answer);

                // 通过websocket发送应答

                target_name=header.substring(5);
                sendSignalingMessage(name,target_name,`answer:${rtc.localDescription.sdp}`);
            }
            //接收到应答描述,即发起者会收到
            else if(message.startsWith("answer"))
            {
                console.log("这是answer的信息")
                //再切割
                let value =message.substring(7);//截取后面信息部分
                //解析answer并设置应答描述
                const desc = new RTCSessionDescription({ type: 'answer', sdp: value });
                await rtc.setRemoteDescription(desc);
                console.log("setRemoteDescription 成功")
            }
            //ice候选者的描述，用于穿透内网实现点对点的连接
            else if(message.startsWith("candidate"))
            {
                console.log("这是candidate的信息")
                //再切割
                let value =message.substring(10);//截取后面信息部分

                // 解析并添加ICE候选者,要传入对象，所以要用JSON.parse()再转回来
                const candidate = new RTCIceCandidate(JSON.parse(value));
                await rtc.addIceCandidate(candidate);
            }
            else {//文本/图片信息
                if (message.startsWith("data:image/")) { //处理图片信息
                    document.getElementById("recipient_message").value = header;
                    let imgElement = document.getElementById('getimage');
                    imgElement.src =message;//显示图片
                    console.log('显示成功')
                }
                else {//处理文本信息
                    //将收到的文本信息展示在接收框
                    document.getElementById("recipient_message").value = data.data;
                    console.log(message);//返回的是一个MessageEvent 对象，调用data方法获取文本信息
                }
            }

        }
        else {
            console.log("响应信息有问题");
        }

    }
    //当连接关闭时触发的操作
    websocket.onclose=function(){
        console.log("连接关闭");

        // document.getElementById("connect").disabled = false;
    }

    //当发生错误时触发的操作
    websocket.onerror=function(data){
        alert(data)
        console.log(data);
    }


}


    //发送文本信息
    function sendMessage(){
        //获取输入框的输入
        var message=document.getElementById("message").value;
        if(message.length>0){
            //检查连接状态并发送信息
            if(websocket.readyState === WebSocket.OPEN){//如果连接状态==打开
                websocket.send(message+":MESSAGE_DATA_END!");//调用方法发送信息,带上结束标识
                document.getElementById("message").value="";//清空输入框
            }
        }
        else {
            console.log('没有输入信息')
        }
    }

    //发送图片信息
    function sendImage() {
        //获取选择的图片
        var input = document.getElementById('image');
        if (input.files.length > 0) {
            var file = input.files[0];//选择第一个选中的图片发送
            // console.log(file);
            var reader = new FileReader();
            //这个一个触发器，当reader的读取完成之后触发的动作
            reader.onloadend = function() {
                if(websocket.readyState === WebSocket.OPEN){//如果连接状态==打开
                    console.log("准备发送")
                    //分块发送
                    var MaxSize=65518;//设置64kB的块大小,注意算上18字节的结束标识符
                    var array=[];   //设置存储分块的数组
                    var len=reader.result.length;
                    for(var i=0;i<len;i+=MaxSize){//切割
                        //切割数据
                        array.push(reader.result.slice(i,i+MaxSize));
                    }
                    //分块发送
                    //先读取并发送头部消息
                    let header=document.getElementById("message").value
                    console.log(header)
                    websocket.send(header);//头部消息，可以改成读取拼接
                    var arrayLen=array.length;
                    for(var j=0;j<arrayLen-1;j++){//发送
                        //发送数据
                        websocket.send(array[j]);//base64编码之后发送的也是TextWebSocketFrame文本帧
                    }
                    //最后带着标识符发送最后一次
                    //发送结束数据
                    websocket.send(array[j]+":MESSAGE_DATA_END!");//base64编码之后发送的也是TextWebSocketFrame文本帧

                    console.log("发送成功")
                } else {
                    console.log('未连接');
                }
            };
            //开始读取将选择的图片以base64的编码读取到reader中，注意触发器要写在前面！！！
            reader.readAsDataURL(file);
        } else {
            console.log('没有选择图片');
        }
    }
