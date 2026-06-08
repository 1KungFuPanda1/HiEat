package com.sky.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模板对象...");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 创建Redis序列化器
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        
        // 设置Redis的key序列化器
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        
        // 设置Redis的value序列化器
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    
    /**
     * 自定义FastJson的Redis序列化器
     */
    public static class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
        private final Class<T> clazz;
        
        static {
            // 设置全局的autoType
            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        }
        
        public FastJsonRedisSerializer(Class<T> clazz) {
            super();
            this.clazz = clazz;
        }
        
        @Override
        public byte[] serialize(T t) {
            if (t == null) {
                return new byte[0];
            }
            return com.alibaba.fastjson.JSON.toJSONBytes(t, SerializerFeature.WriteClassName);
        }
        
        @Override
        public T deserialize(byte[] bytes) {
            if (bytes == null || bytes.length <= 0) {
                return null;
            }
            return com.alibaba.fastjson.JSON.parseObject(bytes, clazz);
        }
    }
}
