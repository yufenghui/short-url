package com.yufenghui.shorturl.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UrlMap
 *
 * @author yufenghui
 * @date 2024/10/23 14:09
 */
@Data
@Builder
@TableName("url_map")
public class UrlMap {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("short_url")
    private String shortUrl;

    @TableField("origin_url")
    private String originUrl;

    @TableField("create_time")
    private LocalDateTime createTime;

}
