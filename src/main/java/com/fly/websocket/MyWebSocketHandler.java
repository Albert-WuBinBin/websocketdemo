package com.fly.websocket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MyWebSocketHandler implements WebSocketHandler {
	

	//保存所有用户的session
	private static final Map<String,WebSocketSession> users=new HashMap<String,WebSocketSession>();
	
	//连接就绪时
	public void afterConnectionEstablished(WebSocketSession session)
			throws Exception {		
		String uid=session.getId();
		System.out.println("ID:"+uid+"---连接成功");
		if(users.get(uid)==null){
			users.put(uid,session);
		}
	}
	//处理信息
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
			throws Exception {
		Gson gson=new Gson();
		 // 将消息JSON格式通过Gson转换成Map
        // message.getPayload().toString() 获取消息具体内容
		Map<String,Object> map=gson.fromJson(message.getPayload().toString(),
				new TypeToken<Map<String,Object>>(){}.getType());
		System.out.println("handleMessage..."+message.getPayload());
		String msg=map.get("msg").toString();
		TextMessage textMessage=new TextMessage(msg,true);
		if(msg.contains("@")){
			sendMsgToOne(textMessage,session);
		}
		else{
			sendMsgToAllUsers(textMessage);
		}
	}
	/*
	 * 发给所有人
	 */
	private void sendMsgToAllUsers(WebSocketMessage<?> msg) throws Exception {
		Iterator<Entry<String, WebSocketSession>> iterator = users.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, WebSocketSession> entry = iterator.next();
			entry.getValue().sendMessage(msg);
		}
	}
	/*
	 * 发给某人
	 */
	private void sendMsgToOne(WebSocketMessage<?> msg,WebSocketSession session) throws Exception {
		String[] split = msg.getPayload().toString().split("@");
		Iterator<Entry<String, WebSocketSession>> iterator = users.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, WebSocketSession> entry = iterator.next();
			if(entry.getValue().getId().equals(split[1])){
				if(entry.getValue().isOpen()){
					entry.getValue().sendMessage(new TextMessage(split[0],true));
				}
			}
			
		}
		if(session.isOpen()){
			session.sendMessage(new TextMessage(split[0],true));
		}
	}
	// 关闭 连接时
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
			throws Exception {
		System.out.println("WebSocket:"+session.getId()+"---关闭连接");
        System.out.println("用户"+session.getId()+"已退出！");
        users.remove(session.getId());
        System.out.println("剩余在线用户"+users.size());
		
	}
	// 处理传输时异常
	public void handleTransportError(WebSocketSession session, Throwable arg1)
			throws Exception {
		if(session.isOpen()){
			session.close();
			users.remove(session.getId());
		}
		System.out.println("Id："+session.getId()+"------异常------");
		
	}

	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}

}
