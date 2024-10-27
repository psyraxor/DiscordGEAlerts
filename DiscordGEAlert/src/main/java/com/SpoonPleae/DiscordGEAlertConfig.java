package com.SpoonPleae;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gewebhook")
public interface DiscordGEAlertConfig extends Config
{
	@ConfigItem(
			keyName = "webhookUrl",
			name = "Discord Webhook URL",
			description = "URL for Discord webhook to receive GE notifications"
	)
	default String webhookUrl()
	{
		return "";
	}
}
