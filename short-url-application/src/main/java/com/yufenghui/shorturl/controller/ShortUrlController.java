package com.yufenghui.shorturl.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yufenghui.shorturl.common.Response;
import com.yufenghui.shorturl.common.ShortUrlResponse;
import com.yufenghui.shorturl.mapper.UrlMapMapper;
import com.yufenghui.shorturl.model.UrlMap;
import com.yufenghui.shorturl.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * ShortUrlController
 *
 * @author yufenghui
 * @date 2024/10/23 13:17
 */
@Slf4j
@Controller
public class ShortUrlController {

    private static final String REDIS_CACHE_KEY_PREFIX = "su:";

    @Value("${app.base-url:}")
    private String baseUrl;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UrlMapMapper urlMapMapper;

    /**
     * 根据原始URL生成短链
     *
     * @param originUrl
     * @return
     */
    @ResponseBody
    @GetMapping("/gen")
    public Response<ShortUrlResponse> gen(String originUrl) {
        // 参数校验
        if (StrUtil.isBlank(originUrl)) {
            throw new IllegalArgumentException("originUrl不能为空");
        }

        // 判断是否已经生成短链，是否需要Bloom Filter？
        LambdaQueryWrapper<UrlMap> queryWrapper = Wrappers.lambdaQuery(UrlMap.class)
                .eq(UrlMap::getOriginUrl, originUrl);

        UrlMap existUrlMap = urlMapMapper.selectOne(queryWrapper);
        if (existUrlMap != null && StrUtil.isNotBlank(existUrlMap.getShortUrl())) {
            log.info("短链已存在，short-url={}, origin-url={}", existUrlMap.getShortUrl(), existUrlMap.getOriginUrl());
            return Response.succeed(ShortUrlResponse.builder().shortUrl(existUrlMap.getShortUrl()).build());
        }

        // 如果没有生成，生成短链
        String code = HashUtil.hashToBase62(originUrl + RandomUtil.randomString(10));
        // DB存储短链
        UrlMap urlMap = UrlMap.builder()
                .code(code)
                .shortUrl(baseUrl + code)
                .originUrl(originUrl)
                .createTime(LocalDateTime.now())
                .build();

        try {
            urlMapMapper.insert(urlMap);
        } catch (DuplicateKeyException e) {
            // 说明短链冲突，需要重新生成短链，然后再存储，此处直接抛出
            log.info("短链生成冲突，origin-url={}", originUrl);
            return Response.fail("短链生成冲突，请重试一下");
        }

        // 保存Redis缓存
        saveCache(code, originUrl);

        return Response.succeed(ShortUrlResponse.builder().shortUrl(baseUrl + code).build());
    }

    /**
     * 访问短链，跳转到原始地址
     *
     * @param code
     */
    @GetMapping("/{code:[0-9a-zA-Z]+$}")
    public void toOrigin(@PathVariable("code") String code, HttpServletResponse response) {
        if (StrUtil.isBlank(code)) {
            throw new IllegalArgumentException("短链不能为空");
        }
        // 根据长度判断是否为短链
        if ((code.length() > 10)) {
            throw new IllegalArgumentException("短链不合法");
        }

        // 从Redis缓存中获取短链
        String originUrl = getCache(code);

        if (originUrl == null || StrUtil.isBlank(originUrl)) {
            // 如果不存在，从数据库中获取
            LambdaQueryWrapper<UrlMap> queryWrapper = Wrappers.lambdaQuery(UrlMap.class)
                    .eq(UrlMap::getCode, code);

            UrlMap urlMap = urlMapMapper.selectOne(queryWrapper);
            if (urlMap != null && StrUtil.isNotBlank(urlMap.getOriginUrl())) {
                originUrl = urlMap.getOriginUrl();

                // 刷新Redis缓存
                saveCache(code, originUrl);
            }
            // 如果数据库中也不存在，是否需要生成短链

        }

        // 跳转
        if (StrUtil.isNotBlank(originUrl)) {
            sendRedirect(response, originUrl);
        } else {
            throw new IllegalArgumentException("短链不存在");
        }
    }

    private void sendRedirect(HttpServletResponse response, String page) {
        try {
            response.sendRedirect(page);
        } catch (Exception e) {
            log.error("Http重定向出现异常, =" + page, e);
        }
    }


    private String getCache(String code) {
        String originUrl = null;
        try {
            // 从Redis缓存中获取短链
            originUrl = redissonClient.<String>getBucket(REDIS_CACHE_KEY_PREFIX + code).get();
        } catch (Throwable t) {
            // 忽略，Redis异常不能影响使用
            log.error("Redis操作异常", t);
        }

        return originUrl;
    }

    private void saveCache(String code, String originUrl) {
        try {
            // 短链生成后存到Redis中，采用什么数据结构
            redissonClient.getBucket(REDIS_CACHE_KEY_PREFIX + code).set(originUrl, 20, TimeUnit.SECONDS);
        } catch (Throwable t) {
            // 忽略，Redis异常不能影响使用
            log.error("Redis操作异常", t);
        }
    }

}
