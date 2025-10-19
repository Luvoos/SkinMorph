package com.luvoos.skinmorph.command;

import com.luvoos.skinmorph.manager.LanguageManager;
import com.luvoos.skinmorph.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class HelpMessage
{
    private final Map<Integer, Map<String, Component>> pages = new LinkedHashMap<>();
    private final LanguageManager langManager;

    public HelpMessage(LanguageManager languageManager)
    {
        this.langManager = languageManager;

        Map<String, Component> page1 = new LinkedHashMap<>();
        page1.put("/skin", langManager.translate("messages./skin-description"));
        page1.put("/skin set <skinName>", langManager.translate("messages./skin-set-description"));
        page1.put("/skin set <skinName> <targetName>", langManager.translate("messages./skin-set-target-description"));
        page1.put("/skin default", langManager.translate("messages./skin-default-description"));
        page1.put("/skin default <targetName>", langManager.translate("messages./skin-default-target-description"));
        pages.put(1, page1);

        Map<String, Component> page2 = new LinkedHashMap<>();
        page2.put("/skin random", langManager.translate("messages./skin-random-description"));
        page2.put("/skin random <targetName>", langManager.translate("messages./skin-random-target-description"));
        page2.put("/skin favorites add <skinName>", langManager.translate("messages./skin-favorites-add-description"));
        page2.put("/skin favorites remove <skinName>", langManager.translate("messages./skin-favorites-remove-description"));
        page2.put("/skin settings", langManager.translate("messages./skin-settings-description"));
        pages.put(2, page2);

        Map<String, Component> page3 = new LinkedHashMap<>();
        page3.put("/skin cache clear", langManager.translate("messages./skin-cache-clear-description"));
        page3.put("/skin cache remove <skinName>", langManager.translate("messages./skin-cache-remove-description"));
        page3.put("/skin cache info", langManager.translate("messages./skin-cache-info-description"));
        pages.put(3, page3);
    }

    public void sendHelp(@NotNull final CommandSender sender, int page)
    {
        Map<String, Component> helpEntries = pages.get(page);
        if (helpEntries == null) {
            sender.sendMessage(langManager.translate("messages.invalid-help-page"));
            return;
        }

        if (page == 3) {
            sender.sendMessage(Component.text(" "));
            sender.sendMessage(Component.text(" "));
        }

        // Help Header
        sender.sendMessage(langManager.translate("messages.help-header"));

        if (sender instanceof final Player player) {
            sender.sendMessage(langManager.translate("messages.hover-commands"));
            sender.sendMessage(Component.text(" "));

            // Commands and their descriptions
            for (Map.Entry<String, Component> entry : helpEntries.entrySet()) {
                final String command = entry.getKey();
                final Component coloredCommand = ColorUtil.colorize("<gray>" + command);
                final String commandSuggestion = command.split("<")[0];
                final Component description = entry.getValue();

                Component line = coloredCommand
                        .clickEvent(ClickEvent.suggestCommand(commandSuggestion))
                        .hoverEvent(HoverEvent.showText(description));
                player.sendMessage(line);
            }

            // Navigation
            final int maxPage = pages.size();
            final int currentPage = page;

            TextComponent.Builder nav = Component.text().append(Component.text("\n"));

            if (currentPage > 1) {
                Component previous = ColorUtil.colorize("<gray><bold>←")
                        .clickEvent(ClickEvent.runCommand("skin help " + (currentPage - 1)))
                        .hoverEvent(HoverEvent.showText(langManager.translate("messages.go-to-page", Map.of(
                                "ph1", String.valueOf((currentPage - 1))
                        ))));
                nav.append(previous);
            }

            nav.append(Component.text(" "));
            nav.append(langManager.translate("messages.help-page", Map.of(
                    "ph1", String.valueOf(currentPage),
                    "ph2", String.valueOf(maxPage)
            )));
            nav.append(Component.text(" "));

            if (currentPage < maxPage) {
                Component next = ColorUtil.colorize("<gray><bold>→")
                        .clickEvent(ClickEvent.runCommand("skin help " + (currentPage + 1)))
                        .hoverEvent(HoverEvent.showText(langManager.translate("messages.go-to-page", Map.of(
                                "ph1", String.valueOf((currentPage + 1))
                        ))));
                nav.append(next);
            }

            player.sendMessage(nav.build());
        } else {
            sender.sendMessage(" ");
            helpEntries.forEach((cmd, desc) -> sender.sendMessage(cmd + " - " + PlainTextComponentSerializer.plainText().serialize(desc)));
        }
    }
}
