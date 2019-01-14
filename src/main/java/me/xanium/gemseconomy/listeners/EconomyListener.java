/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.listeners;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.economy.Account;
import me.xanium.gemseconomy.economy.AccountManager;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EconomyListener implements Listener {

    private final GemsEconomy plugin = GemsEconomy.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        Account account = AccountManager.getAccount(event.getUniqueId());

        if (account == null) {
            account = new Account(event.getUniqueId(), event.getName());

            if(!GemsEconomy.getDataStore().getName().equalsIgnoreCase("yaml")){
                GemsEconomy.getDataStore().createAccount(account);
            }else {
                GemsEconomy.getDataStore().saveAccount(account);
            }

            UtilServer.consoleLog("New Account created for: " + account.getDisplayName());

        } else if (!account.getNickname().equals(event.getName()) || account.getNickname() == null) {
            account.setNickname(event.getName());
            GemsEconomy.getDataStore().saveAccount(account);
            UtilServer.consoleLog("Name change found! Updating account " + account.getDisplayName() + "...");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Account account = AccountManager.getAccount(player);
        AccountManager.getAccounts().remove(account);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Account account = AccountManager.getAccount(player);
            if (account != null && !AccountManager.getAccounts().contains(account)) {
                AccountManager.getAccounts().add(account);
            }
        });

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (AccountManager.getDefaultCurrency() == null && (player.isOp() || player.hasPermission("gemseconomy.command.currency"))) {
                player.sendMessage(F.getPrefix() + "§cYou have not made a currency yet. Please do so by \"§e/currency§c\".");
            }
        }, 60);
    }

}

