let urlPrefix='http://localhost:8089';

//发送请求获取列表
function getAllElement(type) {
    let url;
    if(type === 'Private'){
        //服务器测试,请求地址也通过域名拿吧
        // url="http://test-sami-ws.warmheart.top:8089/api/friend/getAllFriends"
        //本机测试
        url=urlPrefix+"/userFriend/view"
        console.log(url)
        //内网测试
        // url="https://192.168.2.190:8089/api/friend/getAllFriends"
    }

    else {
        //暂时不需要
        url="/grouped/get_all_groups"
    }

    console.log("name定义了吗:"+ name)
    //构建发送参数
    let data={
        userId : userId,
        status : '01'
    };
    console.log("发送的请求体"+JSON.stringify(data))
    //利用fetch发送get请求即可，
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json' //发送json的请求头
        },
        //注意用json格式化
        body: JSON.stringify(data)
    })
        //返回json格式数据
        .then(res=>res.json())
        .then(datas=>{
            console.log(datas)
            //填充第二个选择框
            let send_target=document.getElementById("target");
            //先清空原有的选择项
            send_target.options.length = 0;

            // 检查响应状态码
            if (datas.code === 200) {
                // 获取好友列表
                const friendList = datas.data.userLink;
                console.log(friendList)
                //遍历填充选择框
                if(friendList!=null){
                    friendList.forEach(function (friend){
                        console.log(friend)
                        let target=document.createElement('option');
                        //填充对方姓名跟id
                        target.value=friend.friendName+':'+friend.friendId;
                        target.text=friend.friendName;
                        send_target.appendChild(target)
                    })
                }

            } else {
                console.error(`Request failed with code: ${datas.code}, message: ${datas.message}`);
            }


        })
        .catch(error=>{
            throw error;
        })
    //发送请求获取对应的列表
}

//获取跟用户的聊天记录
function getOldChats(send_target){
    let url;
    let type=document.getElementById("send_type").value;
    if(type === 'Private'){
        //服务器测试,请求地址也通过域名拿吧
        // url="http://test-sami-ws.warmheart.top:8089/api/friend/getAllFriends"
        //本机测试
        url=urlPrefix+"/messageRecipients/getAllChats"
        //内网测试
        // url="https://192.168.2.190:8089/api/friend/getAllFriends"
    }
    else {
        //暂时不需要
        url="/grouped/get_all_groups"
    }

    //读取构建发送参数
    //切割获取当前的目标
    let split = send_target.split(':');
    let data={
        userName: name,
        userId: userId,
        friendName: split[0],
        friendId: split[1]
    };
    //使用fetch函数发送
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            if(response.code===200){
                console.log('获取了历史聊天')

                //展示历史聊天
                let chats=response.data.messages
                // console.log(chats)
                if(chats !=null){
                    chats.forEach(chat=>{
                        //对方的信息
                        if(chat.sendName!=name){
                            showMessage(chat.sendName+':'+chat.message,chat.sendTime)
                        }
                        else {
                            showMessage('自己:'+chat.message,chat.sendTime)
                        }
                    })
                }

            }

        })
        .catch(error=>{
            throw error;
        })
}

//查询用户
function friendUser(){
    //获取用户输入
    let userName=document.getElementById("friendName").value;
    if(userName.length>0){
        let url=urlPrefix+"/user/findUser"

        //构建发送参数
        let data={
            userName: userName
        };
        //使用fetch函数发送
        fetch(url,{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
            .then(res=>res.json())
            .then(response=>{
                console.log("收到了数据")
                console.log(response)
                if(response.code==200){
                    if(response.data.userLink!=null){
                        let users=response.data.userLink;
                        // console.log(users)
                        //获取表格
                        let table=document.getElementById("friendsTable");
                        //先清空表格内容
                        table.innerHTML='';
                        //往表格填充行
                        let tr=document.createElement('tr');
                        let td_id=document.createElement('th');
                        let td_name=document.createElement('th');
                        let td_mobile=document.createElement('th');
                        let td_deal=document.createElement('th');
                        td_id.textContent="用户id";
                        td_name.textContent="用户名";
                        td_mobile.textContent="用户邮箱";
                        td_deal.textContent="操作"
                        tr.appendChild(td_id)
                        tr.appendChild(td_name)
                        tr.appendChild(td_mobile)
                        tr.appendChild(td_deal)
                        table.appendChild(tr)
                        //遍历填充查询结果
                        users.forEach(user=>{
                            console.log(user)
                            //往表格填充行
                            let tr=document.createElement('tr');
                            let td_id=document.createElement('td');
                            let td_name=document.createElement('td');
                            let td_mobile=document.createElement('td');
                            td_id.textContent=user.id;
                            td_name.textContent=user.userName;
                            td_mobile.textContent=user.userEmail;
                            tr.appendChild(td_id)
                            tr.appendChild(td_name)
                            tr.appendChild(td_mobile)
                            //创建按钮
                            let button=document.createElement('button');
                            button.textContent='添加';
                            //添加自定义属性
                            button.setAttribute('user_id',user.id);
                            button.addEventListener('click',(event)=>{
                                //有参数的写法，传递按钮本身
                                addFriendApply(event.target)
                            });
                            tr.appendChild(button);
                            //把行放表格里面
                            table.appendChild(tr);
                        })

                        //显示查询结果弹窗
                        document.getElementById('friends').style.display = 'block';
                        document.getElementById('overlay').style.display = 'block';
                        //添加全局监听,点击弹窗外面关闭弹窗
                        document.addEventListener('click',friendsClose,true)
                    }
                    else {
                        alert("没有查询到用户")
                    }
                }
                else {
                    alert("发生错误")
                }



            })
            .catch(error=>{
                throw error;
            })
    }
    else {
        alert("没有输入")
    }



}
//关闭弹窗
function friendsClose(event){
    // 检查点击的目标是否是 friends 弹窗或其子元素
    if (!event.target.closest('#friends')) {
        document.getElementById('friends').style.display = 'none';
        document.getElementById('overlay').style.display = 'none';
        // 移除事件监听器
        document.removeEventListener('click', friendsClose, true);
    }
}

//发送添加好友申请的请求

function addFriendApply(button){
    let url=urlPrefix+"/userFriendApply/add";
    //读取构建发送参数
    //获取填充的id
    let id=button.getAttribute('user_id');
    console.log('所选择的用户id为'+id)
    console.log('自己的id为'+userId)
    let data={
        userId: userId,
        friendId: id
    };
    //使用fetch函数发送
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            alert(response.message);
        })
        .catch(error=>{
            throw error;
        })
}

//查询新的好友申请
function friendApply(){
    let url=urlPrefix+"/userFriendApply/friendView";
    //读取构建发送参数
    let data={
        userId: userId
    };
    //使用fetch函数发送
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            if(response.code===200){
                let applys=response.data.dataList;
                if(applys.length>0){

                    // console.log(users)
                    //获取表格
                    let table=document.getElementById("applysTable");
                    //先清空表格内容
                    table.innerHTML='';
                    //填充首行
                    //往表格填充行
                    let tr=document.createElement('tr');
                    let td_time=document.createElement('th');
                    let td_name=document.createElement('th');
                    let td_deal1=document.createElement('th');
                    let td_deal2=document.createElement('th');
                    td_time.textContent='邀请时间';
                    td_name.textContent='谁向你发来好友申请';
                    td_deal1.textContent='操作1';
                    td_deal2.textContent='操作2';
                    tr.appendChild(td_time)
                    tr.appendChild(td_name)
                    tr.appendChild(td_deal1)
                    tr.appendChild(td_deal2)

                    table.appendChild(tr);

                    //遍历填充申请记录
                    applys.forEach(apply=>{
                        //往表格填充行
                        let tr=document.createElement('tr');
                        let td_time=document.createElement('td');
                        let td_name=document.createElement('td');
                        let td_deal1=document.createElement('th');
                        let td_deal2=document.createElement('th');
                        td_time.textContent=apply.cstCreate;
                        td_name.textContent=apply.userName;
                        tr.appendChild(td_time)
                        tr.appendChild(td_name)
                        //创建同意按钮
                        let button=document.createElement('button');
                        button.textContent='同意好友申请';
                        //添加自定义属性
                        //申请记录的id
                        button.setAttribute('apply_id',apply.id);
                        //邀请人的id
                        button.setAttribute('user_id',apply.userId);
                        button.addEventListener('click',addFriend);
                        td_deal1.appendChild(button)
                        tr.appendChild(td_deal1);

                        //创建拒绝按钮
                        let button2=document.createElement('button');
                        button2.textContent='拒绝好友申请';
                        //添加自定义属性
                        //申请记录的id
                        button2.setAttribute('apply_id',apply.id);
                        button2.addEventListener('click',deleteFriendApply);
                        td_deal2.appendChild(button2);
                        tr.appendChild(td_deal2);
                        //把行放表格里面
                        table.appendChild(tr);
                    })

                    //显示查询结果弹窗
                    document.getElementById('applys').style.display = 'block';
                    document.getElementById('overlay').style.display = 'block';
                    //添加全局监听,点击弹窗外面关闭弹窗
                    document.addEventListener('click',applysClose,true)
                }
                else {
                    alert("没有新的好友申请");
                }
            }
        })
        .catch(error=>{
            throw error;
        })
}
//关闭弹窗
function applysClose(event){
    // 检查点击的目标是否是 applys 弹窗或其子元素
    if (!event.target.closest('#applys')) {
        document.getElementById('applys').style.display = 'none';
        document.getElementById('overlay').style.display = 'none';
        // 移除事件监听器
        document.removeEventListener('click',applysClose,true);
    }
}


//新增好友的请求
function addFriend(event){

    let url=urlPrefix+"/userFriend/add";
    //读取构建发送参数
    //获取对方id
    let friendId=event.target.getAttribute('user_id');
    //获取邀请记录的id
    let applyId=event.target.getAttribute('apply_id');
    console.log('邀请记录的id为'+applyId)
    console.log('对方id为'+friendId)
    console.log('自己的id为'+userId)
    let data={
        userId: userId,
        friendId: friendId,
        id: applyId
    };
    //使用fetch函数发送
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            if(response.code===200){
                alert(response.message);
            }
        })
        .catch(error=>{
            throw error;
        })
}

//查询自己发起的好友邀请
function myApply(){
    let url=urlPrefix+"/userFriendApply/userView";
    //读取构建发送参数
    let data={
        userId: userId
    };
    //使用fetch函数发送
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            if(response.code===200){
                let applys=response.data.dataList;
                if(applys.length>0){

                    // console.log(users)
                    //获取表格
                    let table=document.getElementById("myApplysTable");
                    //先清空表格内容
                    table.innerHTML='';
                    //填充首行
                    //往表格填充行
                    let tr=document.createElement('tr');
                    let td_time=document.createElement('th');
                    let td_name=document.createElement('th');
                    td_time.textContent='邀请时间';
                    td_name.textContent='向谁发起邀请';
                    tr.appendChild(td_time)
                    tr.appendChild(td_name)
                    table.appendChild(tr);

                    //遍历填充申请记录
                    applys.forEach(apply=>{
                        //往表格填充行
                        let tr=document.createElement('tr');
                        let td_time=document.createElement('td');
                        let td_name=document.createElement('td');
                        td_time.textContent=apply.cstCreate;
                        td_name.textContent=apply.friendName;
                        tr.appendChild(td_time)
                        tr.appendChild(td_name)
                        //把行放表格里面
                        table.appendChild(tr);
                    })
                    //显示查询结果弹窗
                    document.getElementById('myApplys').style.display = 'block';
                    document.getElementById('overlay').style.display = 'block';
                    //添加全局监听,点击弹窗外面关闭弹窗
                    document.addEventListener('click',myApplysClose,true)
                }
                else {
                    alert("没有发起好友申请");
                }
            }
        })
        .catch(error=>{
            throw error;
        })
}
//关闭弹窗
function myApplysClose(event){
    // 检查点击的目标是否是 applys 弹窗或其子元素
    if (!event.target.closest('#myApplys')) {
        document.getElementById('myApplys').style.display = 'none';
        document.getElementById('overlay').style.display = 'none';
        // 移除事件监听器
        document.removeEventListener('click',myApplysClose,true);
    }
}

//删除好友申请的请求
function deleteFriendApply(event){
    let url=urlPrefix+"/userFriendApply/delete";
    //读取构建发送参数
    //获取邀请记录的id
    let applyId=event.target.getAttribute('apply_id');
    console.log('邀请记录的id为'+applyId)

    let data={

        id: applyId
    };
    //使用fetch函数发送
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            if(response.code===200){
                alert(response.message);
            }
        })
        .catch(error=>{
            throw error;
        })
}

//根据id获取名字并填充视频描述
function getNameById(id){
    //请求路径
    let url=urlPrefix+"/user/getNameById";

    //构建请求参数
    let data={
        userId: id
    };

    //发送请求
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            if(response.code===200){
                if(response.data != null){
                    //填充视频描述
                    document.getElementById("video_" + id+"_name").textContent=response.data.userName;
                }
            }
            else {
                alert(response.message);
            }
        })
        .catch(error=>{
            throw error;
        })
}

//获取验证码的函数
function getCode(){
    //请求路径
    let url=urlPrefix+"/getCode";
    //获取邮箱
    let email=document.getElementById("email").value;

    //构建请求参数
    let data={
       email: email
    };

    //发送请求
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            alert(response.message);
        })
        .catch(error=>{
            throw error;
        })
}

//注册函数
function register(){
    //请求路径
    let url=urlPrefix+"/register";
    //获取参数
    let email=document.getElementById("email").value;
    let code=document.getElementById("code").value;
    let userName=document.getElementById("userName").value;
    let password=document.getElementById("password").value;
    //构建请求参数
    let data={
        userName:userName,
        code:code,
        password:password,
        email: email
    };

    //发送请求
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            alert(response.message);
        })
        .catch(error=>{
            throw error;
        })
}

//登录函数
function login(){
    //请求路径
    let url=urlPrefix+"/login";
    //获取参数
    let userName=document.getElementById("user").value;
    let password=document.getElementById("pass").value;
    //构建请求参数
    let data={
        userName:userName,
        password:password
    };

    //发送请求
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res=>res.json())
        .then(response=>{
            if(response.code===200){
                //赋值
                name=response.data.userName;
                userId=response.data.userId;
                let time = new Date().toLocaleTimeString();
                showMessage("登陆成功",time)
                alert("登录成功")
                //隐藏弹窗
                document.getElementById('login').style.display = 'none';
                document.getElementById('overlay').style.display = 'none';

                //建立wensocket连接
               connect()
                //填充
                //初始化先填充好友的
                getAllElement('Private');
            }
            else {
                alert(response.message);
            }

        })
        .catch(error=>{
            throw error;
        })
}

//登录-->注册
function toRegister(){
    document.getElementById('login').style.display = 'none';
    document.getElementById('register').style.display = 'block';
}
//注册-->登录
function toLogin(){
    document.getElementById('login').style.display = 'block';
    document.getElementById('register').style.display = 'none';
}

