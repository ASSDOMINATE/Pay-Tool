package org.dominate.achp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwja.tool.utils.SqlUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.dominate.achp.common.enums.CardRecordState;
import org.dominate.achp.common.enums.CardType;
import org.dominate.achp.common.enums.ExceptionType;
import org.dominate.achp.common.utils.UniqueCodeUtil;
import org.dominate.achp.entity.BaseCard;
import org.dominate.achp.entity.BaseCardRecord;
import org.dominate.achp.entity.dto.CardRecordDTO;
import org.dominate.achp.entity.req.PageReq;
import org.dominate.achp.entity.wrapper.CardWrapper;
import org.dominate.achp.mapper.BaseCardRecordMapper;
import org.dominate.achp.service.IBaseCardRecordService;
import org.dominate.achp.sys.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 付费卡密记录 服务实现类
 * </p>
 *
 * @author dominate
 * @since 2023-04-04
 */
@Service
public class BaseCardRecordServiceImpl extends ServiceImpl<BaseCardRecordMapper, BaseCardRecord> implements IBaseCardRecordService {

    @Override
    public List<CardRecordDTO> userRecordList(int accountId) {
        QueryWrapper<BaseCardRecord> query = new QueryWrapper<>();
        query.lambda().eq(BaseCardRecord::getAccountId, accountId)
                .orderByDesc(BaseCardRecord::getUpdateTime);
        List<BaseCardRecord> recordList = list(query);
        return CardWrapper.build().entityCardRecordDTO(recordList);
    }

    @Override
    public List<CardRecordDTO> cardRecordList(int cardId, PageReq page) {
        QueryWrapper<BaseCardRecord> query = new QueryWrapper<>();
        query.lambda().eq(BaseCardRecord::getCardId, cardId)
                .last(SqlUtil.pageLimit(page.getSize(), page.getPage()))
                .orderByDesc(BaseCardRecord::getId);
        List<BaseCardRecord> recordList = list(query);
        return CardWrapper.build().entityCardRecordDTO(recordList);
    }

    @Override
    public CardRecordDTO checkUserRecord(int accountId) throws BusinessException {
        QueryWrapper<BaseCardRecord> query = new QueryWrapper<>();
        query.lambda().eq(BaseCardRecord::getAccountId, accountId)
                .eq(BaseCardRecord::getState, CardRecordState.USING.getCode())
                .last(SqlUtil.limitOne());
        BaseCardRecord record = getOne(query);
        if (null == record) {
            throw BusinessException.create(ExceptionType.NOT_CARD_USING);
        }
        return CardWrapper.build().entityDTO(record);
    }

    @Override
    public boolean hasBinding(int accountId) {
        QueryWrapper<BaseCardRecord> query = new QueryWrapper<>();
        query.lambda().eq(BaseCardRecord::getAccountId, accountId)
                .eq(BaseCardRecord::getState, CardRecordState.USING.getCode());
        return count(query) > 0;
    }

    @Override
    public boolean bindRecord(int accountId, int id, BaseCard card) {
        BaseCardRecord update = new BaseCardRecord();
        update.setId(id);
        update.setAccountId(accountId);
        CardType cardType = CardType.getValueByCode(card.getType());
        switch (cardType) {
            case DAY:
                Date start = new Date();
                Date expire = DateUtils.addDays(start, card.getDayLimit());
                update.setStartTime(start);
                update.setExpireTime(expire);
                break;
            case COUNT:
                update.setRemainCount(card.getCountLimit());
                break;
            default:
                return false;
        }
        update.setState(CardRecordState.USING.getCode());
        return updateById(update);
    }

    @Override
    public boolean bindRecord(int accountId, BaseCard card) {
        int recordId = createRecord(card);
        return bindRecord(accountId, recordId, card);
    }

    @Override
    public int createRecord(BaseCard card) {
        BaseCardRecord record = new BaseCardRecord();
        record.setCardId(card.getId());
        record.setCardName(card.getName());
        record.setExchangeKey(UniqueCodeUtil.createExchangeKey());
        record.setCardType(card.getType());
        record.setRemainCount(card.getCountLimit());
        record.setRequestCount(0);
        Date emptyTime = new Date(0);
        record.setStartTime(emptyTime);
        record.setExpireTime(emptyTime);
        record.setState(CardRecordState.NOT_USE.getCode());
        save(record);
        return record.getId();
    }

    @Override
    public BaseCardRecord findActiveRecord(String code) {
        QueryWrapper<BaseCardRecord> query = new QueryWrapper<>();
        query.lambda().eq(BaseCardRecord::getExchangeKey, code)
                .eq(BaseCardRecord::getState, CardRecordState.NOT_USE.getCode())
                .last(SqlUtil.limitOne());
        return getOne(query);
    }
}
