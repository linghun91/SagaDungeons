package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 物品构建器
 * 用于构建GUI界面中的物品
 */
public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    /**
     * 构造函数
     * @param material 材质
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * 构造函数
     * @param material 材质
     * @param amount 数量
     */
    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * 构造函数
     * @param itemStack 物品堆
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * 设置名称
     * @param name 名称
     * @return 物品构建器
     */
    public ItemBuilder setName(String name) {
        itemMeta.setDisplayName(MessageUtil.colorize(name));
        return this;
    }

    /**
     * 设置描述
     * @param lore 描述
     * @return 物品构建器
     */
    public ItemBuilder setLore(String... lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(MessageUtil.colorize(line));
        }
        itemMeta.setLore(coloredLore);
        return this;
    }

    /**
     * 设置描述
     * @param lore 描述
     * @return 物品构建器
     */
    public ItemBuilder setLore(List<String> lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(MessageUtil.colorize(line));
        }
        itemMeta.setLore(coloredLore);
        return this;
    }

    /**
     * 添加描述
     * @param line 描述行
     * @return 物品构建器
     */
    public ItemBuilder addLoreLine(String line) {
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(MessageUtil.colorize(line));
        itemMeta.setLore(lore);
        return this;
    }

    /**
     * 添加附魔
     * @param enchantment 附魔
     * @param level 等级
     * @return 物品构建器
     */
    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * 添加物品标志
     * @param flag 物品标志
     * @return 物品构建器
     */
    public ItemBuilder addItemFlag(ItemFlag flag) {
        itemMeta.addItemFlags(flag);
        return this;
    }

    /**
     * 设置不可破坏
     * @param unbreakable 是否不可破坏
     * @return 物品构建器
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * 设置自定义模型数据
     * @param customModelData 自定义模型数据
     * @return 物品构建器
     */
    public ItemBuilder setCustomModelData(int customModelData) {
        itemMeta.setCustomModelData(customModelData);
        return this;
    }

    /**
     * 隐藏所有属性
     * @return 物品构建器
     */
    public ItemBuilder hideAllAttributes() {
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        itemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        return this;
    }

    /**
     * 构建物品
     * @return 物品堆
     */
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
