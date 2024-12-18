package com.yufenghui.shorturl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufenghui.shorturl.model.UrlMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * UrlMapMapper
 *
 * @author yufenghui
 * @date 2024/10/23 14:15
 */
@Mapper
public interface UrlMapMapper extends BaseMapper<UrlMap> {

    UrlMap selectForUpdate(@Param("originUrl") String originUrl);

}
