package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 聊天输入监听器
 * 用于处理GUI界面的聊天输入功能
 */
@SuppressWarnings("deprecation") // 使用已弃用的AsyncPlayerChatEvent以确保Bukkit兼容性
public class ChatInputListener extends AbstractListener {

    /**
     * 聊天输入类型枚举
     */
    public enum InputType {
        NUMBER,     // 数值输入
        TEXT,       // 文本输入
        DECIMAL     // 小数输入
    }

    /**
     * 聊天输入数据类
     */
    public static class ChatInputData {
        private final InputType type;
        private final Consumer<String> callback;
        private final String promptMessage;
        private final String cancelMessage;

        public ChatInputData(InputType type, Consumer<String> callback, String promptMessage, String cancelMessage) {
            this.type = type;
            this.callback = callback;
            this.promptMessage = promptMessage;
            this.cancelMessage = cancelMessage;
        }

        public InputType getType() { return type; }
        public Consumer<String> getCallback() { return callback; }
        public String getPromptMessage() { return promptMessage; }
        public String getCancelMessage() { return cancelMessage; }
    }

    // 等待聊天输入的玩家映射
    private final Map<UUID, ChatInputData> waitingPlayers = new ConcurrentHashMap<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public ChatInputListener(SagaDungeons plugin) {
        super(plugin);
    }

    /**
     * 处理玩家聊天事件
     * @param event 聊天事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // 检查玩家是否在等待聊天输入
        if (!waitingPlayers.containsKey(playerId)) {
            return;
        }

        // 取消聊天事件，防止消息广播
        event.setCancelled(true);

        // 获取输入数据
        ChatInputData inputData = waitingPlayers.get(playerId);
        String message = event.getMessage().trim();

        // 检查是否为取消命令
        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("取消")) {
            // 移除等待状态
            waitingPlayers.remove(playerId);
            
            // 发送取消消息
            MessageUtil.sendMessage(player, inputData.getCancelMessage());
            return;
        }

        // 验证输入
        String validatedInput = validateInput(message, inputData.getType());
        if (validatedInput == null) {
            // 输入无效，发送错误消息
            sendValidationError(player, inputData.getType());
            return;
        }

        // 移除等待状态
        waitingPlayers.remove(playerId);

        // 执行回调
        try {
            inputData.getCallback().accept(validatedInput);
        } catch (Exception e) {
            plugin.getLogger().warning("聊天输入回调执行失败: " + e.getMessage());
            MessageUtil.sendMessage(player, "command.admin.edit.input-error");
        }
    }

    /**
     * 请求玩家输入数值
     * @param player 玩家
     * @param promptMessage 提示消息路径
     * @param callback 回调函数
     */
    public void requestNumberInput(Player player, String promptMessage, Consumer<String> callback) {
        requestInput(player, InputType.NUMBER, promptMessage, callback);
    }

    /**
     * 请求玩家输入文本
     * @param player 玩家
     * @param promptMessage 提示消息路径
     * @param callback 回调函数
     */
    public void requestTextInput(Player player, String promptMessage, Consumer<String> callback) {
        requestInput(player, InputType.TEXT, promptMessage, callback);
    }

    /**
     * 请求玩家输入小数
     * @param player 玩家
     * @param promptMessage 提示消息路径
     * @param callback 回调函数
     */
    public void requestDecimalInput(Player player, String promptMessage, Consumer<String> callback) {
        requestInput(player, InputType.DECIMAL, promptMessage, callback);
    }

    /**
     * 请求玩家输入
     * @param player 玩家
     * @param type 输入类型
     * @param promptMessage 提示消息路径
     * @param callback 回调函数
     */
    private void requestInput(Player player, InputType type, String promptMessage, Consumer<String> callback) {
        UUID playerId = player.getUniqueId();

        // 如果玩家已经在等待输入，先移除旧的等待状态
        if (waitingPlayers.containsKey(playerId)) {
            waitingPlayers.remove(playerId);
        }

        // 创建输入数据
        ChatInputData inputData = new ChatInputData(
                type,
                callback,
                promptMessage,
                "command.admin.edit.input-cancelled"
        );

        // 添加到等待映射
        waitingPlayers.put(playerId, inputData);

        // 发送提示消息
        MessageUtil.sendMessage(player, promptMessage);
        MessageUtil.sendMessage(player, "command.admin.edit.input-cancel-hint");
    }

    /**
     * 验证输入
     * @param input 输入内容
     * @param type 输入类型
     * @return 验证后的输入，如果无效则返回null
     */
    private String validateInput(String input, InputType type) {
        switch (type) {
            case NUMBER:
                try {
                    int number = Integer.parseInt(input);
                    if (number < 0) {
                        return null; // 不允许负数
                    }
                    return String.valueOf(number);
                } catch (NumberFormatException e) {
                    return null;
                }

            case DECIMAL:
                try {
                    double decimal = Double.parseDouble(input);
                    if (decimal < 0) {
                        return null; // 不允许负数
                    }
                    return String.valueOf(decimal);
                } catch (NumberFormatException e) {
                    return null;
                }

            case TEXT:
                // 文本输入验证
                if (input.isEmpty()) {
                    return null;
                }
                // 移除颜色代码中的特殊字符，但保留颜色代码
                if (input.length() > 50) {
                    return null; // 限制文本长度
                }
                return input;

            default:
                return null;
        }
    }

    /**
     * 发送验证错误消息
     * @param player 玩家
     * @param type 输入类型
     */
    private void sendValidationError(Player player, InputType type) {
        switch (type) {
            case NUMBER:
                MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                break;
            case DECIMAL:
                MessageUtil.sendMessage(player, "command.admin.edit.invalid-decimal");
                break;
            case TEXT:
                MessageUtil.sendMessage(player, "command.admin.edit.invalid-text");
                break;
        }
    }

    /**
     * 取消玩家的聊天输入等待状态
     * @param player 玩家
     */
    public void cancelInput(Player player) {
        UUID playerId = player.getUniqueId();
        if (waitingPlayers.containsKey(playerId)) {
            ChatInputData inputData = waitingPlayers.remove(playerId);
            MessageUtil.sendMessage(player, inputData.getCancelMessage());
        }
    }

    /**
     * 检查玩家是否在等待聊天输入
     * @param player 玩家
     * @return 是否在等待输入
     */
    public boolean isWaitingForInput(Player player) {
        return waitingPlayers.containsKey(player.getUniqueId());
    }

    /**
     * 清理所有等待状态
     */
    public void clearAllWaiting() {
        waitingPlayers.clear();
    }

    /**
     * 获取等待输入的玩家数量
     * @return 等待输入的玩家数量
     */
    public int getWaitingPlayersCount() {
        return waitingPlayers.size();
    }
}
