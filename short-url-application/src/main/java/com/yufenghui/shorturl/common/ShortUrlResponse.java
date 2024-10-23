package com.yufenghui.shorturl.common;

import lombok.Builder;
import lombok.Data;

/**
 * ShortUrlResponse
 *
 * @author yufenghui
 * @date 2024/10/23 13:49
 */
@Data
@Builder
public class ShortUrlResponse {

    private final String shortUrl;

}
