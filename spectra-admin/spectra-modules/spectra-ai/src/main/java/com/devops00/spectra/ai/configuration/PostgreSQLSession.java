package com.devops00.spectra.ai.configuration;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.ai.javabean.entity.AiSession;
import com.devops00.spectra.ai.service.AiSessionService;
import io.agentscope.core.session.ListHashUtil;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PostgreSQL数据库 session持久化实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/5 17:16
 */
@RequiredArgsConstructor
public class PostgreSQLSession implements Session {

    private final AiSessionService aiSessionService;

    /**
     * Suffix for hash storage keys.
     */
    private static final String HASH_KEY_SUFFIX = ":_hash";

    /**
     * item_index value for single state values.
     */
    private static final int SINGLE_STATE_INDEX = 0;

    @Override
    public void save(SessionKey sessionKey, String key, State value) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        String json = JsonUtils.getJsonCodec().toJson(value);

        var session = new AiSession();
        session.setSessionId(sessionId);
        session.setStateKey(key);
        session.setItemIndex(SINGLE_STATE_INDEX);
        session.setStateData(json);

        // 1. 根据业务字段构建查询条件，查找数据库中是否已存在该记录
        LambdaQueryWrapper<AiSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiSession::getSessionId, sessionId)
                .eq(AiSession::getStateKey, key)
                .eq(AiSession::getItemIndex, SINGLE_STATE_INDEX);

        // 2. 查询现有记录
        AiSession existingSession = aiSessionService.getOne(queryWrapper);

        // 3. 如果记录存在，将数据库中的独立 id 赋值给当前对象
        if (existingSession != null) {
            session.setId(existingSession.getId());
        }

        // 4. 此时再调用 saveOrUpdate，MP 会根据 id 是否为空正确判断是更新还是新增
        aiSessionService.saveOrUpdate(session);
    }


    @Override
    public void save(SessionKey sessionKey, String key, List<? extends State> values) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        if (values.isEmpty()) {
            return;
        }

        String hashKey = key + HASH_KEY_SUFFIX;

        // Compute current hash
        String currentHash = ListHashUtil.computeHash(values);

        // Get stored hash
        String storedHash = getStoredHash(sessionId, hashKey);

        // Get existing count
        int existingCount = getListCount(sessionId, key);

        // Determine if full rewrite is needed
        boolean needsFullRewrite =
                ListHashUtil.needsFullRewrite(values, storedHash, existingCount);

        if (needsFullRewrite) {
            deleteListItems(sessionId, key);
            insertAllItems(sessionId, key, values);
            saveHash(sessionId, hashKey, currentHash);
        } else if (values.size() > existingCount) {
            List<? extends State> newItems =
                    values.subList(existingCount, values.size());
            insertItems(sessionId, key, newItems, existingCount);
            saveHash(sessionId, hashKey, currentHash);
        }
        // else: no change, skip
    }

    /**
     * Get stored hash value for a list.
     *
     * @param sessionId session identifier
     * @param hashKey   the hash key (e.g., "memory_messages:_hash")
     * @return the stored hash, or null if not found
     */
    private String getStoredHash(String sessionId, String hashKey) {
        AiSession session = aiSessionService.getOne(
                new LambdaQueryWrapper<AiSession>()
                        .select(AiSession::getStateData)
                        .eq(AiSession::getSessionId, sessionId)
                        .eq(AiSession::getStateKey, hashKey)
                        .eq(AiSession::getItemIndex, SINGLE_STATE_INDEX)
        );
        return session != null ? session.getStateData() : null;
    }

    /**
     * Save hash value for a list.
     *
     * @param sessionId session identifier
     * @param hashKey   the hash key
     * @param hash      the hash value to save
     */
    private void saveHash(String sessionId, String hashKey, String hash) {
        // 1. 构建查询条件，精准定位联合唯一键
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getSessionId, sessionId)
                .eq(AiSession::getStateKey, hashKey)
                .eq(AiSession::getItemIndex, SINGLE_STATE_INDEX);

        // 2. 查询是否已存在
        AiSession existingSession = aiSessionService.getOne(wrapper);

        if (existingSession != null) {
            // 3. 存在则更新 (等价于 ON DUPLICATE KEY UPDATE)
            existingSession.setStateData(hash);
            aiSessionService.updateById(existingSession);
        } else {
            // 4. 不存在则插入
            AiSession newSession = new AiSession();
            newSession.setSessionId(sessionId);
            newSession.setStateKey(hashKey);
            newSession.setItemIndex(SINGLE_STATE_INDEX);
            newSession.setStateData(hash);
            aiSessionService.save(newSession);
        }
    }

    /**
     * Delete all items for a list state.
     *
     * @param sessionId session identifier
     * @param key       the state key
     */
    private void deleteListItems(String sessionId, String key) {
        aiSessionService.remove(
                new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getSessionId, sessionId)
                        .eq(AiSession::getStateKey, key)
        );
    }

    /**
     * Insert all items for a list state.
     *
     * @param sessionId session identifier
     * @param key       the state key
     * @param values    the values to insert
     */
    private void insertAllItems(String sessionId, String key, List<? extends State> values) {
        insertItems(sessionId, key, values, 0);
    }

    /**
     * Insert items for a list state starting at a given index.
     *
     * @param sessionId  session identifier
     * @param key        the state key
     * @param items      the items to insert
     * @param startIndex the starting index for item_index
     */
    private void insertItems(String sessionId, String key, List<? extends State> items, int startIndex) {
        // 1. 将 State 列表转换为 AiSession 实体列表
        List<AiSession> entities = new ArrayList<>(items.size());
        int index = startIndex;
        for (State item : items) {
            AiSession entity = new AiSession();
            entity.setSessionId(sessionId);
            entity.setStateKey(key);
            entity.setItemIndex(index);
            entity.setStateData(JsonUtils.getJsonCodec().toJson(item));
            entities.add(entity);
            index++;
        }

        // 2. 使用 MP 的批量保存方法（默认每 1000 条提交一次，避免内存溢出）
        aiSessionService.saveBatch(entities);
    }

    /**
     * Get the count of items in a list state (max index + 1).
     */
    private int getListCount(String sessionId, String key) {
        // 1. 构建查询条件（使用 Lambda 避免硬编码字段名）
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSession::getSessionId, sessionId)
                .eq(AiSession::getStateKey, key)
                .select(AiSession::getItemIndex); // 只查询 item_index 字段，减少数据传输

        // 2. 执行查询并获取最大索引
        List<AiSession> list = aiSessionService.list(wrapper);

        // 3. 处理结果：如果列表为空，返回 0；否则取最大值 + 1
        if (list == null || list.isEmpty()) {
            return 0;
        }

        int maxIndex = list.stream()
                .mapToInt(AiSession::getItemIndex)
                .max()
                .orElse(0);

        return maxIndex + 1;
    }

    @Override
    public <T extends State> Optional<T> get(SessionKey sessionKey, String key, Class<T> type) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

// 1. 构建查询条件
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSession::getSessionId, sessionId)
                .eq(AiSession::getStateKey, key)
                .eq(AiSession::getItemIndex, SINGLE_STATE_INDEX)
                .select(AiSession::getStateData); // 仅查询 state_data 字段，避免全表字段加载

// 2. 执行查询（使用 selectOne 获取单条记录）
        AiSession session = aiSessionService.getOne(wrapper);

// 3. 处理结果并反序列化
        if (session != null && session.getStateData() != null) {
            return Optional.of(JsonUtils.getJsonCodec().fromJson(session.getStateData(), type));
        }

        return Optional.empty();
    }

    @Override
    public <T extends State> List<T> getList(SessionKey sessionKey, String key, Class<T> itemType) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

// 1. 构建查询条件
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSession::getSessionId, sessionId)
                .eq(AiSession::getStateKey, key)
                .orderByAsc(AiSession::getItemIndex) // 按 item_index 升序排序
                .select(AiSession::getStateData);    // 仅查询 state_data 字段，减少数据传输

// 2. 执行查询获取列表
        List<AiSession> sessions = aiSessionService.list(wrapper);

// 3. 将结果转换为业务对象列表
        List<T> result = new ArrayList<>();
        if (sessions != null) {
            for (AiSession session : sessions) {
                String json = session.getStateData();
                if (json != null) {
                    result.add(JsonUtils.getJsonCodec().fromJson(json, itemType));
                }
            }
        }

        return result;
    }

    @Override
    public boolean exists(SessionKey sessionKey) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);

        // 1. 构建查询条件
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSession::getSessionId, sessionId);

        // 2. 使用 exists 方法判断记录是否存在
        return aiSessionService.exists(wrapper);
    }

    @Override
    public void delete(SessionKey sessionKey) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);

// 1. 构建删除条件
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSession::getSessionId, sessionId);

// 2. 执行删除操作
        aiSessionService.remove(wrapper);
    }

    @Override
    public Set<SessionKey> listSessionKeys() {
// 1. 构建查询条件
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(AiSession::getSessionId)      // 仅查询 session_id 字段
                .orderByAsc(AiSession::getSessionId); // 按 session_id 升序排序

// 2. 执行查询获取列表
        List<AiSession> sessions = aiSessionService.list(wrapper);

// 3. 提取 session_id 并转换为 Set 集合（天然去重）
        return sessions.stream()
                .map(session -> SimpleSessionKey.of(session.getSessionId()))
                .collect(Collectors.toSet());
    }


    /**
     * Validate a session ID format.
     *
     * @param sessionId Session ID to validate
     * @throws IllegalArgumentException if session ID is invalid
     */
    protected void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (sessionId.contains("/") || sessionId.contains("\\")) {
            throw new IllegalArgumentException("Session ID cannot contain path separators");
        }
        if (sessionId.length() > 255) {
            throw new IllegalArgumentException("Session ID cannot exceed 255 characters");
        }
    }

    /**
     * Validate a state key format.
     *
     * @param key State key to validate
     * @throws IllegalArgumentException if state key is invalid
     */
    private void validateStateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("State key cannot be null or empty");
        }
        if (key.length() > 255) {
            throw new IllegalArgumentException("State key cannot exceed 255 characters");
        }
    }

}
