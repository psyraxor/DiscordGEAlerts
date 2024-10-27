package com.SpoonPleae;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DiscordGEAlert
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DiscordGEAlertPlugin.class);
		RuneLite.main(args);
	}
}