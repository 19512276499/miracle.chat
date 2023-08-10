package com.miracle.chat;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {

       /**
        * 创建SSE
        * @param uid
        * @return
        */
       SseEmitter createSse(String uid);

       /**
        * 关闭SSE
        * @param uid
        */
       void closeSse(String uid);

       /**
        * 客户端发送消息到服务端
        * @param uid
        * @param chatRequest
        */
       ChatResponse sseChat(String uid, ChatRequest chatRequest);




       String send (String content);
}
