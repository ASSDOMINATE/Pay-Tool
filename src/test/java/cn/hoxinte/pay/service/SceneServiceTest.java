package cn.hoxinte.pay.service;


import cn.hoxinte.pay.entity.ChatRecord;
import cn.hoxinte.pay.entity.ChatScene;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SceneServiceTest {

    @Resource
    private IChatSceneService chatSceneService;
    @Resource
    private IChatRecordService chatRecordService;

    @Test
    public void debugStatistic() {
        QueryWrapper<ChatRecord> recordQuery = new QueryWrapper<>();
        recordQuery.lambda().gt(ChatRecord::getSceneId, 0);
        List<ChatRecord> recordList = chatRecordService.list(recordQuery);
        Map<Integer, ChatScene> sceneMap = new HashMap<>();
        Map<Integer, Set<String>> sceneGroupMap = new HashMap<>();
        for (ChatRecord record : recordList) {
            if (!sceneMap.containsKey(record.getSceneId())) {
                ChatScene scene = new ChatScene();
                scene.setId(record.getSceneId());
                scene.setSendCount(0);
                scene.setChatCount(0);
                sceneMap.put(record.getSceneId(), scene);
            }
            ChatScene scene = sceneMap.get(record.getSceneId());
            scene.setSendCount(scene.getSendCount() + 1);
            if (!sceneGroupMap.containsKey(record.getSceneId())) {
                sceneGroupMap.put(record.getSceneId(), new HashSet<>());
            }
            sceneGroupMap.get(record.getSceneId()).add(record.getGroupId());
        }
        for (Map.Entry<Integer, Set<String>> integerSetEntry : sceneGroupMap.entrySet()) {
            ChatScene scene = sceneMap.get(integerSetEntry.getKey());
            scene.setChatCount(integerSetEntry.getValue().size());
        }
        for (ChatScene value : sceneMap.values()) {
            System.out.println(value);
        }
        chatSceneService.updateBatchById(sceneMap.values());
    }


}
