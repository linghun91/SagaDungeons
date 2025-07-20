package cn.i7mc.sagadungeons.model;

import java.util.UUID;

/**
 * 副本邀请数据模型
 * 存储副本邀请的信息
 */
public class DungeonInvitation {

    private final String dungeonId;
    private final UUID inviterUUID;
    private final UUID inviteeUUID;
    private final long expirationTime;

    /**
     * 构造函数
     * @param dungeonId 副本ID
     * @param inviterUUID 邀请者UUID
     * @param inviteeUUID 被邀请者UUID
     * @param expirationTime 过期时间
     */
    public DungeonInvitation(String dungeonId, UUID inviterUUID, UUID inviteeUUID, long expirationTime) {
        this.dungeonId = dungeonId;
        this.inviterUUID = inviterUUID;
        this.inviteeUUID = inviteeUUID;
        this.expirationTime = expirationTime;
    }

    /**
     * 获取副本ID
     * @return 副本ID
     */
    public String getDungeonId() {
        return dungeonId;
    }

    /**
     * 获取邀请者UUID
     * @return 邀请者UUID
     */
    public UUID getInviterUUID() {
        return inviterUUID;
    }

    /**
     * 获取被邀请者UUID
     * @return 被邀请者UUID
     */
    public UUID getInviteeUUID() {
        return inviteeUUID;
    }

    /**
     * 获取过期时间
     * @return 过期时间
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * 检查邀请是否已过期
     * @return 是否已过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}
