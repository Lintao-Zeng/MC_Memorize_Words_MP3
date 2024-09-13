package com.example.examplemod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("examplemod")  // 替换为你的模组ID
public class ExampleMod {

    public ExampleMod() {
        // 确保事件总线注册
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GiveItemCommand()); // 注册命令和事件
    }

    // 订阅 RegisterCommandsEvent 事件，注册命令
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        GiveItemCommand.register(event.getDispatcher());
    }

}
