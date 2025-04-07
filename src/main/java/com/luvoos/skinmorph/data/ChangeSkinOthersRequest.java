package com.luvoos.skinmorph.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChangeSkinOthersRequest {

    final Player caller;
    final Player target;
    final String skinName;
    final boolean sendResponse;
    private boolean complete;

    public ChangeSkinOthersRequest(@NotNull Player caller, @NotNull Player target, @NotNull String skinName, boolean sendResponse, boolean complete)
    {
        this.caller = caller;
        this.target = target;
        this.skinName = skinName;
        this.sendResponse = sendResponse;
        this.complete = complete;
    }

    public boolean isComplete()
    {
        return (caller != null && target != null && skinName != null && !skinName.isEmpty());
    }

    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }
}
