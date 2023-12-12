package cn.hoxinte.pay.service;

import cn.hoxinte.pay.common.enums.CardRecordState;
import cn.hoxinte.pay.common.enums.CardType;
import cn.hoxinte.pay.common.enums.ExceptionType;
import cn.hoxinte.pay.entity.BaseCard;
import cn.hoxinte.pay.entity.BaseCardRecord;
import cn.hoxinte.pay.entity.dto.CardRecordDTO;
import cn.hoxinte.pay.entity.wrapper.CardWrapper;
import cn.hoxinte.pay.sys.exception.BusinessException;
import cn.hoxinte.tool.utils.SqlUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CardServiceTest {

    @Resource
    private CardService cardService;
    @Resource
    private IBaseCardService baseCardService;
    @Resource
    private IBaseCardRecordService baseCardRecordService;

    private static CardRecordDTO cardRecord = null;

    @Test
    public void cardInfo(){
        CardRecordDTO record = cardService.checkUserRecord(132476);
        String waitRecordInfo = baseCardRecordService.getRecordWaitInfo(132476);
        System.out.println(record.getInfo() + " " + waitRecordInfo);
    }

    @Test
    public void cardCheckTest() {
        int accountId = 132492;
        // 1.缓存中有记录，检查是否过期
        if (null != cardRecord) {
            if (CardType.checkRecordUsed(cardRecord.getCardTypeCode(), cardRecord.getExpireTime(), cardRecord.getRemainCount())) {
                System.out.println("设置为过期");
                System.out.println(accountId + " "+ cardRecord.getId() + " " + cardRecord.getRequestCount() +" "+cardRecord.getRemainCount());
                baseCardRecordService.saveRecordUsed(accountId, cardRecord.getId(), cardRecord.getRequestCount(), cardRecord.getRemainCount());
                throw BusinessException.create(ExceptionType.SEND_CARD_LIMIT);
            }
            return;
        }
        // 2.查询数据库中用户是否有记录
        QueryWrapper<BaseCardRecord> query = new QueryWrapper<>();
        query.lambda().eq(BaseCardRecord::getAccountId, accountId);
        // 从来没有开通过
        if (0 == baseCardRecordService.count(query)) {
            throw BusinessException.create(ExceptionType.NOT_BUY_USING);
        }
        // 3.查询可用的记录
        query.lambda().eq(BaseCardRecord::getState, CardRecordState.USING.getCode())
                .last(SqlUtil.limitOne());
        BaseCardRecord record = baseCardRecordService.getOne(query);
        if (null != record) {
            System.out.println("查询到使用中的卡");
            cardRecord = CardWrapper.build().entityDTO(record);
            // 重新执行 checkUserRecord->1 进行状态检查
            cardCheckTest();
            return;
        }
        // 4.查找待使用的卡
        QueryWrapper<BaseCardRecord> waitQuery = new QueryWrapper<>();
        waitQuery.lambda().eq(BaseCardRecord::getAccountId, accountId)
                .eq(BaseCardRecord::getState, CardRecordState.WAIT.getCode())
                .last(SqlUtil.limitOne());
        record = baseCardRecordService.getOne(waitQuery);
        if (null != record) {
            System.out.println("设置待使用的卡");
            // 设置为启用后清理缓存
            // TODO 这个方法应该调整到上一层Service
            BaseCard card = baseCardService.getById(record.getCardId());
            if (baseCardRecordService.saveRecordUsing(accountId, record.getId(), card)) {
                cardRecord = CardWrapper.build().entityDTO(record);
                // 才设置为启用的卡不需要检查，直接返回即可
                return;
            }
        }
        throw BusinessException.create(ExceptionType.NOT_CARD_USING);
    }
}
