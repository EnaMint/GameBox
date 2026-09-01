package com.gamebox.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.record.assembler.RecordAssembler;
import com.gamebox.record.dto.RecordCreateDTO;
import com.gamebox.record.entity.GameRecord;
import com.gamebox.record.entity.RecordLike;
import com.gamebox.record.mapper.GameRecordMapper;
import com.gamebox.record.mapper.RecordLikeMapper;
import com.gamebox.record.service.RecordService;
import com.gamebox.record.vo.LikeResultVO;
import com.gamebox.record.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final GameRecordMapper gameRecordMapper;
    private final RecordLikeMapper recordLikeMapper;
    private final RecordAssembler recordAssembler;

    @Override
    public Page<RecordVO> feed(Integer page, Integer size) {
        LambdaQueryWrapper<GameRecord> wrapper = new LambdaQueryWrapper<GameRecord>()
                .eq(GameRecord::getStatus, 1)
                .orderByDesc(GameRecord::getCreatedAt);
        Page<GameRecord> result = gameRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return toRecordPage(result);
    }

    @Override
    public Page<RecordVO> my(Integer page, Integer size) {
        Long userId = requireLogin();
        LambdaQueryWrapper<GameRecord> wrapper = new LambdaQueryWrapper<GameRecord>()
                .eq(GameRecord::getUserId, userId)
                .eq(GameRecord::getStatus, 1)
                .orderByDesc(GameRecord::getCreatedAt);
        Page<GameRecord> result = gameRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return toRecordPage(result);
    }

    @Override
    public Page<RecordVO> userRecords(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<GameRecord> wrapper = new LambdaQueryWrapper<GameRecord>()
                .eq(GameRecord::getUserId, userId)
                .eq(GameRecord::getStatus, 1)
                .orderByDesc(GameRecord::getCreatedAt);
        Page<GameRecord> result = gameRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return toRecordPage(result);
    }

    @Override
    public Long create(RecordCreateDTO dto) {
        Long userId = requireLogin();
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        List<String> images = dto.getImages() == null ? List.of()
                : dto.getImages().stream()
                        .filter(img -> img != null && !img.isBlank())
                        .toList();
        if (images.size() > 9) {
            throw BizException.of("最多上传9张图片");
        }
        if (content.isEmpty() && images.isEmpty()) {
            throw BizException.of("动态内容与图片至少填写一项");
        }
        GameRecord record = new GameRecord();
        record.setUserId(userId);
        record.setGameId(dto.getGameId() == null ? 0L : dto.getGameId());
        record.setContent(content);
        record.setImages(String.join(",", images));
        record.setLikeCount(0);
        record.setStatus(1);
        gameRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void delete(Long id) {
        Long userId = requireLogin();
        GameRecord record = getActiveRecord(id);
        if (!record.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        gameRecordMapper.update(null, new LambdaUpdateWrapper<GameRecord>()
                .eq(GameRecord::getId, id)
                .set(GameRecord::getStatus, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResultVO toggleLike(Long id) {
        Long userId = requireLogin();
        GameRecord record = getActiveRecord(id);
        boolean nowLiked;
        RecordLike exist = recordLikeMapper.selectOne(new LambdaQueryWrapper<RecordLike>()
                .eq(RecordLike::getRecordId, id)
                .eq(RecordLike::getUserId, userId));
        if (exist != null) {
            recordLikeMapper.deleteById(exist.getId());
            nowLiked = false;
        } else {
            RecordLike like = new RecordLike();
            like.setRecordId(id);
            like.setUserId(userId);
            try {
                recordLikeMapper.insert(like);
                nowLiked = true;
            } catch (DuplicateKeyException e) {
                return new LikeResultVO(true, record.getLikeCount());
            }
        }
        gameRecordMapper.update(null, new LambdaUpdateWrapper<GameRecord>()
                .eq(GameRecord::getId, id)
                .setSql(nowLiked ? "like_count = like_count + 1" : "like_count = GREATEST(like_count - 1, 0)"));
        int likeCount = Math.max(0, record.getLikeCount() + (nowLiked ? 1 : -1));
        return new LikeResultVO(nowLiked, likeCount);
    }

    private Page<RecordVO> toRecordPage(Page<GameRecord> result) {
        Page<RecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toRecords(result.getRecords()));
        return voPage;
    }

    private List<RecordVO> toRecords(List<GameRecord> gameRecords) {
        if (gameRecords == null || gameRecords.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIds = gameRecords.stream().map(GameRecord::getUserId).collect(Collectors.toSet());
        Set<Long> gameIds = gameRecords.stream()
                .map(GameRecord::getGameId)
                .filter(gid -> gid != null && gid > 0)
                .collect(Collectors.toSet());
        Map<Long, UserBriefVO> userMap = recordAssembler.userMap(userIds);
        Map<Long, GameBriefVO> gameMap = recordAssembler.gameMap(gameIds);
        Set<Long> likedIds = likedRecordIds(gameRecords.stream().map(GameRecord::getId).toList());
        return gameRecords.stream().map(r -> {
            RecordVO vo = new RecordVO();
            vo.setId(r.getId());
            vo.setImages(splitImages(r.getImages()));
            vo.setContent(r.getContent());
            vo.setLikeCount(r.getLikeCount());
            vo.setCreatedAt(r.getCreatedAt());
            vo.setUser(userMap.getOrDefault(r.getUserId(),
                    UserBriefVO.builder().id(r.getUserId()).nickname("未知用户").build()));
            vo.setGame(r.getGameId() == null || r.getGameId() == 0 ? null : gameMap.get(r.getGameId()));
            vo.setLiked(likedIds.contains(r.getId()));
            return vo;
        }).toList();
    }

    private Set<Long> likedRecordIds(List<Long> recordIds) {
        Long userId = UserHolder.getUserId();
        if (userId == null || recordIds.isEmpty()) {
            return Set.of();
        }
        return recordLikeMapper.selectList(new LambdaQueryWrapper<RecordLike>()
                        .select(RecordLike::getRecordId)
                        .eq(RecordLike::getUserId, userId)
                        .in(RecordLike::getRecordId, recordIds))
                .stream()
                .map(RecordLike::getRecordId)
                .collect(Collectors.toSet());
    }

    private List<String> splitImages(String images) {
        if (images == null || images.isBlank()) {
            return List.of();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(img -> !img.isEmpty())
                .toList();
    }

    private GameRecord getActiveRecord(Long id) {
        GameRecord record = gameRecordMapper.selectById(id);
        if (record == null || record.getStatus() == null || record.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "动态不存在");
        }
        return record;
    }

    private Long requireLogin() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
