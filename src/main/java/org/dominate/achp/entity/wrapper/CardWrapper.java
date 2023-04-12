package org.dominate.achp.entity.wrapper;

import org.dominate.achp.common.enums.CardRecordState;
import org.dominate.achp.common.enums.CardType;
import org.dominate.achp.entity.BaseCard;
import org.dominate.achp.entity.BaseCardRecord;
import org.dominate.achp.entity.dto.CardDTO;
import org.dominate.achp.entity.dto.CardRecordDTO;

import java.util.ArrayList;
import java.util.List;

public class CardWrapper {

    public static CardWrapper build() {
        return new CardWrapper();
    }

    public CardDTO entityDTO(BaseCard entity) {
        CardDTO dto = new CardDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDesr(entity.getDesr());
        dto.setBalance(entity.getBalance());
        dto.setStock(entity.getStock());
        dto.setCountLimit(entity.getCountLimit());
        dto.setDayLimit(entity.getDayLimit());
        dto.setProductCode(entity.getProductCode());
        CardType type = CardType.getValueByCode(entity.getType());
        dto.setCardTypeCode(type.getCode());
        dto.setCardTypeName(type.getName());
        return dto;
    }

    public List<CardDTO> entityCardDTO(List<BaseCard> entityList) {
        List<CardDTO> dtoList = new ArrayList<>(entityList.size());
        for (BaseCard entity : entityList) {
            dtoList.add(entityDTO(entity));
        }
        return dtoList;
    }

    public CardRecordDTO entityDTO(BaseCardRecord entity) {
        CardRecordDTO dto = new CardRecordDTO();
        dto.setId(entity.getId());
        dto.setCardId(entity.getCardId());
        dto.setCardName(entity.getCardName());
        dto.setExchangeKey(entity.getExchangeKey());
        dto.setExpireTime(entity.getExpireTime().getTime());
        dto.setStartTime(entity.getStartTime().getTime());
        dto.setRemainCount(entity.getRemainCount());
        dto.setRequestCount(entity.getRequestCount());
        CardType cardType = CardType.getValueByCode(entity.getCardType());
        dto.setCardTypeCode(cardType.getCode());
        dto.setCardTypeName(cardType.getName());
        CardRecordState recordState = CardRecordState.getValueByCode(entity.getState());
        dto.setStateCode(recordState.getCode());
        dto.setStateName(recordState.getName());
        return dto;
    }

    public List<CardRecordDTO> entityCardRecordDTO(List<BaseCardRecord> entityList) {
        List<CardRecordDTO> dtoList = new ArrayList<>(entityList.size());
        for (BaseCardRecord entity : entityList) {
            dtoList.add(entityDTO(entity));
        }
        return dtoList;
    }
}
