package net.gliby.voicechat.common.commands;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class CommandChatMode extends CommandBase {

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] par2ArrayOfStr, BlockPos pos) {
        return par2ArrayOfStr.length == 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, "distance", "global", "world") : (par2ArrayOfStr.length == 2 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, this.getListOfPlayerUsernames(server)) : null);
    }

    public String getChatMode(int chatMode) {
        return chatMode == 0 ? "distance" : (chatMode == 2 ? "global" : (chatMode == 1 ? "world" : "distance"));
    }

    protected int getChatModeFromCommand(ICommandSender par1ICommandSender, String par2Str) {
        return !par2Str.equalsIgnoreCase("distance") && !par2Str.startsWith("d") && !par2Str.equalsIgnoreCase("0") ? (!par2Str.equalsIgnoreCase("world") && !par2Str.startsWith("w") && !par2Str.equalsIgnoreCase("1") ? (!par2Str.equalsIgnoreCase("global") && !par2Str.startsWith("g") && !par2Str.equalsIgnoreCase("2") ? 0 : 2) : 1) : 0;
    }

    @Override
    public String getName() {
        return "vchatmode";
    }

    @Override
    public String getUsage(ICommandSender par1ICommandSender) {
        return "/vchatmode <mode> or /vchatmode <mode> [player]";
    }

    protected String[] getListOfPlayerUsernames(MinecraftServer server) {
        return server.getOnlinePlayerNames();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2) {
        return par2 == 1;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender par1ICommandSender, String[] par2ArrayOfStr) throws CommandException {
        if (par2ArrayOfStr.length > 0) {
            int chatMode = this.getChatModeFromCommand(par1ICommandSender, par2ArrayOfStr[0]);
            EntityPlayerMP player = null;

            try {
                player = par2ArrayOfStr.length >= 2 ? getPlayer(server, par1ICommandSender, par2ArrayOfStr[1]) : getCommandSenderAsPlayer(par1ICommandSender);
            } catch (PlayerNotFoundException var7) {
                var7.printStackTrace();
            }

            if (player != null) {
                ServerStreamManager dataManager = VoiceChat.getServerInstance().getServerNetwork().getDataManager();
                dataManager.chatModeMap.put(player.getPersistentID(), chatMode);
                ServerStream stream = dataManager.getStream(player.getEntityId());
                if (stream != null) {
                    stream.dirty = true;
                }

                if (player != par1ICommandSender) {
                    notifyCommandListener(par1ICommandSender, this, player.getName() + " set chat mode to " + this.getChatMode(chatMode).toUpperCase() + " (" + chatMode + ")", par2ArrayOfStr[0]);
                } else {
                    player.sendMessage(new TextComponentString("Set own chat mode to " + this.getChatMode(chatMode).toUpperCase() + " (" + chatMode + ")"));
                    switch (chatMode) {
                        case 0:
                            player.sendMessage(new TextComponentString("Only players near you can hear you."));
                            break;
                        case 1:
                            player.sendMessage(new TextComponentString("Every player in this world can hear you"));
                            break;
                        case 2:
                            player.sendMessage(new TextComponentString("Every player can hear you."));
                    }
                }
            }
        }

    }
}
