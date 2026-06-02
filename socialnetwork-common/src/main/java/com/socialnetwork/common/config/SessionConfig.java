package com.socialnetwork.common.config;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Canonical Spring Session configuration shared by all services that
 * participate in the shared Redis session.
 *
 * <p>All services that need to read sessions written by the monolith
 * <b>must</b> include this class (or a class with identical configuration)
 * on their classpath. Otherwise session deserialization will fail.
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li>Redis namespace: {@code engineerpro:app}</li>
 *   <li>Cookie name: {@code SESSION}</li>
 *   <li>Cookie SameSite: {@code Lax}</li>
 *   <li>Cookie Secure: {@code false} (override per-env for prod)</li>
 *   <li>Session value serializer: {@link GenericJackson2JsonRedisSerializer}
 *       with Spring Security Jackson modules registered.</li>
 * </ul>
 */
@Configuration
@EnableRedisHttpSession(redisNamespace = "engineerpro:app")
public class SessionConfig implements BeanClassLoaderAware {

    public static final String REDIS_NAMESPACE = "engineerpro:app";
    public static final String COOKIE_NAME = "SESSION";

    private ClassLoader loader;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(COOKIE_NAME);
        serializer.setSameSite("Lax");
        // NOTE: Secure cookies are dropped by browsers over plain HTTP.
        // Override per profile (e.g. set true in prod, false in dev).
        serializer.setUseSecureCookie(false);
        return serializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(SecurityJackson2Modules.getModules(this.loader));
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.loader = classLoader;
    }
}
