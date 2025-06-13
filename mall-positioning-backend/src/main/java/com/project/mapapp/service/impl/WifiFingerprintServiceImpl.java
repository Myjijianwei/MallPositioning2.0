package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.model.entity.WifiFingerprint;
import com.project.mapapp.service.WifiFingerprintService;
import com.project.mapapp.mapper.WifiFingerprintMapper;
import org.springframework.stereotype.Service;

/**
* @author jjw
* @description 针对表【wifi_fingerprint(WIFI指纹表)】的数据库操作Service实现
* @createDate 2025-03-03 14:31:28
*/
@Service
public class WifiFingerprintServiceImpl extends ServiceImpl<WifiFingerprintMapper, WifiFingerprint>
    implements WifiFingerprintService{

}




