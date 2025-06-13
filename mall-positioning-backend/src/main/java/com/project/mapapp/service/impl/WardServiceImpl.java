package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.model.entity.Ward;
import com.project.mapapp.service.WardService;
import com.project.mapapp.mapper.WardMapper;
import org.springframework.stereotype.Service;

/**
* @author jjw
* @description 针对表【ward(被监护人信息表)】的数据库操作Service实现
* @createDate 2025-03-22 12:57:23
*/
@Service
public class WardServiceImpl extends ServiceImpl<WardMapper, Ward>
    implements WardService{

}




