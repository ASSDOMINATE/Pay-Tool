package cn.hoxinte.pay.service;

import cn.hoxinte.pay.entity.dto.ContentDTO;
import cn.hoxinte.pay.entity.dto.ReplyDTO;
import cn.hoxinte.pay.entity.req.PageReq;
import org.dominate.achp.common.helper.ChatGptHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ChatServiceTest {

    @Resource
    private IChatRecordService chatRecordService;
    @Resource
    private IChatContentService chatContentService;


    @Test
    public void testToken() {
        List<Integer> contentIdList = chatRecordService.getGroupContentIdList("168536822358937476", PageReq.chatPage());
        List<ContentDTO> contentList = chatContentService.list(contentIdList);
        ChatGptHelper.parseMessages(contentList, "那么你再想想下三维生物该是什么样的", "你是AI智能助理，你很聪明，你可以帮助问你的人写文案、回答各种问题，你不能说你使用了“gpt“", "gpt-3.5-turbo-0301", 1000);
        System.out.println("3103");
    }

    @Test
    public void testChat() {
        List<Integer> contentIdList = chatRecordService.getGroupContentIdList("168536822358937476", PageReq.chatPage());
        List<ContentDTO> contentList = chatContentService.list(contentIdList);
        try {
            ReplyDTO reply = ChatGptHelper.send("那么你再想想下三维生物该是什么样的", "你是AI智能助理，你很聪明，你可以帮助问你的人写文案、回答各种问题，你不能说你使用了“gpt“", contentList);
            System.out.println(reply.getReply());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
