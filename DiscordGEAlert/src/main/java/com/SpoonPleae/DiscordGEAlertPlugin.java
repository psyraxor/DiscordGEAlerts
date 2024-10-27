package com.SpoonPleae;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.*;

import java.io.IOException;

@Slf4j
@PluginDescriptor(
		name = "Discord GE Alerts",
		description = "Sends a notification to Discord when a GE offer is ready to collect",
		tags = {"GE", "Grand Exchange", "Discord", "webhook"}
)
public class DiscordGEAlertPlugin extends Plugin
{
	private static final OkHttpClient httpClient = new OkHttpClient();

	@Inject
	private Client client;

	@Inject
	private DiscordGEAlertConfig config;

	@Inject
	private ItemManager itemManager;

	@Provides
	DiscordGEAlertConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DiscordGEAlertConfig.class);
	}

	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event)
	{
		GrandExchangeOffer offer = event.getOffer();
		GrandExchangeOfferState state = offer.getState();

		if (state == GrandExchangeOfferState.BOUGHT || state == GrandExchangeOfferState.SOLD)
		{
			int itemId = offer.getItemId();
			ItemComposition itemComposition = itemManager.getItemComposition(itemId);
			String itemName = itemComposition.getName();

			// Get offer details
			String playerName = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "Unknown Player";
			String offerType = (state == GrandExchangeOfferState.BOUGHT) ? "Buy" : "Sell";
			int quantity = offer.getTotalQuantity();
			int price = offer.getPrice();

			// Create the notification message
			String message = String.format(
					"%s, your GE %s offer for %s is ready to collect!\nQuantity: %d\nPrice: %d GP each",
					playerName, offerType, itemName, quantity, price
			);

			sendDiscordNotification(message);
		}
	}

	private void sendDiscordNotification(String message)
	{
		String webhookUrl = config.webhookUrl();
		if (webhookUrl == null || webhookUrl.isEmpty())
		{
			log.warn("Discord webhook URL not set");
			return;
		}

		RequestBody body = new FormBody.Builder()
				.add("content", message)
				.build();

		Request request = new Request.Builder()
				.url(webhookUrl)
				.post(body)
				.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.error("Failed to send Discord webhook notification", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				if (!response.isSuccessful())
				{
					log.error("Unexpected response from Discord webhook: " + response);
				}
				response.close();
			}
		});
	}
}