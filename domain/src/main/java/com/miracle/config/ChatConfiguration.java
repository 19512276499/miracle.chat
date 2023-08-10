package com.miracle.config;

import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class ChatConfiguration {

    @Value("${chatgpt.apiKey}")
    private List<String> apiKey;
    @Value("${chatgpt.apiHost}")
    private String apiHost;

    @Bean
    public OpenAiStreamClient openAiStreamClient() {

        //本地开发需要配置代理地址,本地无需开加速器即可访问
       // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        //!!!!!!测试或者发布到服务器千万不要配置Level == BODY!!!!
        //!!!!!!测试或者发布到服务器千万不要配置Level == BODY!!!!
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                //.proxy(proxy)
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .build();
        return OpenAiStreamClient
                .builder()
                .apiHost(apiHost)
                .apiKey(apiKey)
                //自定义key使用策略 默认随机策略
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(okHttpClient)
                .build();
    }


}
