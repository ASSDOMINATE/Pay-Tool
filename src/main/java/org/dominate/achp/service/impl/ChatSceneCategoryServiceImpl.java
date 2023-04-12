package org.dominate.achp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dominate.achp.entity.ChatSceneCategory;
import org.dominate.achp.entity.dto.SceneCategoryDTO;
import org.dominate.achp.entity.wrapper.SceneWrapper;
import org.dominate.achp.mapper.ChatSceneCategoryMapper;
import org.dominate.achp.service.IChatSceneCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 对话场景分类 服务实现类
 * </p>
 *
 * @author dominate
 * @since 2023-04-06
 */
@Service
public class ChatSceneCategoryServiceImpl extends ServiceImpl<ChatSceneCategoryMapper, ChatSceneCategory> implements IChatSceneCategoryService {

    @Override
    public List<SceneCategoryDTO> enabledList() {
        QueryWrapper<ChatSceneCategory> query = new QueryWrapper<>();
        query.lambda().eq(ChatSceneCategory::getDel, false);
        List<ChatSceneCategory> categoryList = list(query);
        return SceneWrapper.build().entityCategoryDTO(categoryList);
    }
}
