package com.flowoa.service.warmflow;

import org.dromara.warm.flow.ui.vo.Dict;
import org.dromara.warm.flow.ui.service.HandlerDictService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 办理人字典服务实现
 */
@Service
public class HandlerDictServiceImpl implements HandlerDictService {

    @Override
    public List<Dict> getHandlerDict() {
        List<Dict> dictList = new ArrayList<>();

        dictList.add(new Dict("默认表达式", "${handler}"));
        dictList.add(new Dict("SpEL表达式", "#{@user.evalVar(#handler)}"));
        dictList.add(new Dict("自定义", ""));

        return dictList;
    }
}
