package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static com.mojang.text2speech.Narrator.LOGGER;

public class GiveItemCommand {
    private static final WordMeaningLoader wordLoader = new WordMeaningLoader();
    private static Map.Entry<String, String> currentWord = null;
    static String itemName;
    static int index = 1;
    static int quantity = 1;

    // 初始化加载单词文件
    static {
        wordLoader.loadWordsFromFile("config/words.txt"); // 假设文件位于 config 文件夹中
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("giveitem")
                .then(Commands.argument("item", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            // 获取所有物品，去掉前缀
                            for (Item item : ForgeRegistries.ITEMS) {
                                ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                                if (key != null) {
                                    builder.suggest(key.toString().replace("minecraft:", "")); //去除前缀
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("quantity", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer player = source.getPlayerOrException();
                                    itemName = StringArgumentType.getString(context, "item");
                                    quantity = IntegerArgumentType.getInteger(context, "quantity");

                                    // 获取下一个单词
                                    currentWord = wordLoader.getNextWord();
                                    if (currentWord != null) {
                                        player.sendSystemMessage(Component.literal("请回答这个单词的释义: " + currentWord.getKey()));

                                    // 播放单词声音
                                    try (FileInputStream fileInputStream = new FileInputStream("D:\\sounds\\" + index + ".mp3")) {
                                        Player mp3player = new Player(fileInputStream);
                                        mp3player.play();
                                    } catch (JavaLayerException | IOException e) {
                                        LOGGER.error("音频播放出错：", e);
                                    }
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    // 添加处理聊天输入的事件
    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();

        if (currentWord != null) {
            // 验证释义
            if (message.equals(currentWord.getValue())) {

                ItemStack itemStack = getItemStack(itemName, quantity); // 获取物品，数量为玩家指定的数量
                if (itemStack != null) {
                    player.addItem(itemStack); // 给玩家物品
                    player.sendSystemMessage(Component.literal("回答正确！已给你 " + quantity + " 个: " + itemName));
                    index++;
                    if(index > 25)//这里的25根据单词数量调整
                        index = 1;
                } else {
                    player.sendSystemMessage(Component.literal("物品不存在: " + itemName));
                }
            } else {
                // player.sendSystemMessage(Component.literal("释义错误，请重新输入！单词：" + currentWord.getValue() + "，释义：" + message));
                player.sendSystemMessage(Component.literal("回答错误，请重新输入释义：" + currentWord.getKey()));

                // 播放单词声音
                try (FileInputStream fileInputStream = new FileInputStream("D:\\sounds\\" + index + ".mp3")) {
                    Player mp3player = new Player(fileInputStream);
                    mp3player.play();
                } catch (JavaLayerException | IOException e) {
                    LOGGER.error("音频播放出错：", e);
                }
            }
            // 取消事件以防止重复消息
            event.setCanceled(true);
        }
    }

    // 动态获取物品，支持 Minecraft 中的所有物品和数量
    private static ItemStack getItemStack(String itemName, int quantity) {
        if (!itemName.contains(":")) {
            itemName = "minecraft:" + itemName;
        }

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));

        if (item != null) {
            return new ItemStack(item, quantity);
        } else {
            return null;
        }
    }
}
