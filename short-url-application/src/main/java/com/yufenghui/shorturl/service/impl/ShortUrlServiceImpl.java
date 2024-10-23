package com.yufenghui.shorturl.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.yufenghui.shorturl.mapper.UrlMapMapper;
import com.yufenghui.shorturl.model.UrlMap;
import com.yufenghui.shorturl.service.ShortUrlService;
import com.yufenghui.shorturl.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * ShortUrlServiceImpl
 *
 * @author yufenghui
 * @date 2024/10/23 18:06
 */
@Slf4j
@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    @Value("${app.base-url:}")
    private String baseUrl;

    @Autowired
    private UrlMapMapper urlMapMapper;

    /**
     * 生成短链
     *
     * @param originUrl
     * @return
     */
    @Transactional
    @Override
    public String create(String originUrl) {

        // 判断是否已经生成短链，是否需要Bloom Filter？
        // 数据库校验，必须用 select for update 否则会重复
//        LambdaQueryWrapper<UrlMap> queryWrapper = Wrappers.lambdaQuery(UrlMap.class)
//                .eq(UrlMap::getOriginUrl, originUrl);
//        UrlMap existUrlMap = urlMapMapper.selectOne(queryWrapper);

        UrlMap existUrlMap = urlMapMapper.selectForUpdate(originUrl);
        if (existUrlMap != null && StrUtil.isNotBlank(existUrlMap.getShortUrl())) {
            log.info("短链已存在，short-url={}, origin-url={}", existUrlMap.getShortUrl(), existUrlMap.getOriginUrl());
            return existUrlMap.getCode();
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

        urlMapMapper.insert(urlMap);

        return code;
    }

}
