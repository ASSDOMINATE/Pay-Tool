package org.dominate.achp.entity.dto;

import com.alibaba.fastjson.JSON;
import com.hwja.tool.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.dominate.achp.common.enums.SceneItemType;


/**
 * 场景项单选/多选类型
 *
 * @author dominate
 * @since 2023-04-14
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class SceneItemSelectDTO extends SceneItemBaseDTO {

    /**
     * 选项词数组
     */
    private String[] selectWords;

    /**
     * 最大选择数量
     */
    private Integer maxSelected;

    @Override
    public SceneItemBaseDTO parseJson(String json, SceneItemType itemType, String title, Integer itemId) {
        SceneItemBaseDTO item = JSON.parseObject(json, SceneItemSelectDTO.class);
        item.setId(itemId);
        item.setType(itemType);
        if(StringUtil.isNotEmpty(title)){
            item.setTypeName(title);
        }
        return item;
    }
}
