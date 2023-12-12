package cn.hoxinte.pay.common.helper;

import cn.hoxinte.pay.common.enums.ChatRoleType;
import cn.hoxinte.pay.common.enums.ExceptionType;
import cn.hoxinte.pay.sys.exception.BusinessException;
import cn.hoxinte.tool.utils.StringUtil;
import com.openai.theokanning.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.reactivex.Flowable;
import org.junit.Test;

import java.time.Duration;
import java.util.List;

public class ChatGptHelperTest {

    @Test
    public void testWallet(){
        String s = ChatGptHelper.requestWallet(30,"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");
        System.out.println(s);
        s = ChatGptHelper.requestWallet(30,"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");
        System.out.println(s);
    }

    @Test
    public void testGPT() {
        String result = ChatGptHelper.send("简单解释下超弦定理").getReply();
        System.out.println(result);
    }

    @Test
    public void testGPTStream(){
        OpenAiService service = new OpenAiService("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", Duration.ofSeconds(55));
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .temperature(0.8)
                .maxTokens(100)
                .messages(List.of(ChatGptHelper.createMessage("简单解释下超弦定理",true)))
                .n(1)
                .stream(true)
                .build();
        Flowable<ChatCompletionChunk> serviceFlow = service.streamChatCompletion(request);
        serviceFlow.blockingForEach((result) -> {
            ChatMessage message = result.getChoices().get(0).getMessage();
            if (StringUtil.isEmpty(message.getContent())) {
                return;
            }
            if (StringUtil.isEmpty(message.getRole())) {
                message.setRole(ChatRoleType.AI.getRole());
            }
            try {
                System.out.println(message);
            } catch (Exception e) {
                service.shutdownExecutor();
                throw BusinessException.create(ExceptionType.EMPTY_ERROR);
            }
        });
        service.shutdownExecutor();
    }

}
