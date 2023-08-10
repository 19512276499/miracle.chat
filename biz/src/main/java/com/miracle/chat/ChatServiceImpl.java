package com.miracle.chat;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.miracle.config.LocalCache;
import com.miracle.domain.OpenAiResponse;
import com.miracle.listener.OpenAISSEEventSourceListener;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService{


    private final OpenAiStreamClient openAiStreamClient;

    public ChatServiceImpl(OpenAiStreamClient openAiStreamClient) {
        this.openAiStreamClient = openAiStreamClient;
    }

    @Override
    public SseEmitter createSse(String uid) {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);

        //完成后回调
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束连接...................", uid);
            LocalCache.CACHE.remove(uid);
        });

        //超时回调
        sseEmitter.onTimeout(() -> {
            log.info("[{}]连接超时...................", uid);
        });

        //异常回调
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info("[{}]连接异常,{}", uid, throwable.toString());
                        sseEmitter.send(SseEmitter.event()
                                .id(uid)
                                .name("发生异常！")
                                .data(Message.builder().content("发生异常请重试！").build())
                                .reconnectTime(3000));
                        LocalCache.CACHE.put(uid, sseEmitter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        try {
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocalCache.CACHE.put(uid, sseEmitter);
        log.info("[{}]创建sse连接成功！", uid);
        return sseEmitter;
    }

    @Override
    public void closeSse(String uid) {

        SseEmitter sse = (SseEmitter) LocalCache.CACHE.get(uid);

        if (sse != null) {
            //通知客户端当前服务端已经完成会话，不会再像客户端发送任何信息了
            sse.complete();
            //移除
            LocalCache.CACHE.remove(uid);
        }
        log.info("Sse手动关闭的链接");

    }

    @Override
    public ChatResponse sseChat(String uid, ChatRequest chatRequest) {
        if (StrUtil.isBlank(chatRequest.getMsg())) {
            log.info("参数异常，msg为null", uid);
            throw new BaseException("参数异常，msg不能为空~");
        }
        String messageContext = (String) LocalCache.CACHE.get("msg" + uid);
        List<Message> messages = new ArrayList<>();


        //不为空证明之前有对话存在了已经
        if (StrUtil.isNotBlank(messageContext)) {

            messages = JSONUtil.toList(messageContext, Message.class);

            //限制长度，最多保存10个会话
            if (messages.size() >= 10) {
                messages = messages.subList(1, 10);
            }

            //组装历史对话
            Message currentMessage = Message.builder().content(chatRequest.getMsg()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            //没有历史对话直接添加
            Message currentMessage = Message.builder().content(chatRequest.getMsg()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }

        //从本地缓存拿到对话
        SseEmitter sseEmitter = (SseEmitter) LocalCache.CACHE.get(uid);

        if (sseEmitter == null) {
            log.info("聊天消息推送失败uid:[{}],没有创建连接，请重试。", uid);
            throw new BaseException("聊天消息推送失败uid:[{}],没有创建连接，请重试。~");
        }
        OpenAISSEEventSourceListener openAIEventSourceListener = new OpenAISSEEventSourceListener(sseEmitter);

        ChatCompletion completion = ChatCompletion
                .builder()
                .messages(messages)
                .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                .build();

        openAiStreamClient.streamChatCompletion(completion, openAIEventSourceListener);

        LocalCache.CACHE.put("msg" + uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        ChatResponse response = new ChatResponse();
        response.setQuestionTokens(completion.tokens());
        return response;
    }

    @Override
    public String send(String content) {


        // 设置你的 OpenAI API 密钥
        String apiKey = "";

        // 设置 API 端点
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        // 设置请求参数
        String model = "gpt-3.5-turbo";
        String role = "system";

        double temperature = 0.7;


        // 创建 JSON 请求载荷
        String jsonPayload = String.format("{\"model\": \"%s\", \"messages\": [{\"role\": \"%s\", \"content\": \"%s\"}], \"temperature\": %s}",
                model, role, content, temperature);

        String responseJson ="";

        try {
            // 创建 HttpClient 实例
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            // 创建 HTTP POST 请求
            HttpPost httpPost = new HttpPost(apiUrl);

            // 设置请求头
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

            // 设置请求体
            httpPost.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

            // 执行请求
            HttpResponse response = httpClient.execute(httpPost);

            // 获取响应实体
            HttpEntity entity = response.getEntity();

            // 提取并打印响应内容
             responseJson = EntityUtils.toString(entity);

            // 使用Jackson将JSON转换为Java对象
            ObjectMapper objectMapper = new ObjectMapper();
            OpenAiResponse openAiResponse = objectMapper.readValue(responseJson, OpenAiResponse.class);
            // 关闭连接
            httpClient.close();
           return openAiResponse.getChoices()[0].getMessage().getContent();
        } catch (Exception e) {
            throw new RuntimeException("远程请求chat异常");
        }

    }


}
