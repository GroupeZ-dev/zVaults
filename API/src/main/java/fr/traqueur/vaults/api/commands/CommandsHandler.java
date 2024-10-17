package fr.traqueur.vaults.api.commands;

import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.vaults.api.messages.Message;

public class CommandsHandler implements MessageHandler {
    @Override
    public String getNoPermissionMessage() {
        return Message.NO_PERMISSION_MESSAGE.translate(true);
    }

    @Override
    public String getOnlyInGameMessage() {
        return Message.ONLY_IN_GAME_MESSAGE.translate(true);
    }

    @Override
    public String getMissingArgsMessage() {
        return Message.MISSING_ARGS_MESSAGE.translate(true);
    }

    @Override
    public String getArgNotRecognized() {
        return Message.ARG_NOT_RECOGNIZED_MESSAGE.translate(true);
    }

    @Override
    public String getRequirementMessage() {
        return Message.NO_REQUIREMENT_MESSAGE.translate(true);
    }
}