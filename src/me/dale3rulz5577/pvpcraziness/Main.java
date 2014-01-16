package me.dale3rulz5577.pvpcraziness;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
//The Database code in this doesn't actually work, I will have a working solution soon
public class Main extends JavaPlugin implements Listener {
	String starter = ChatColor.GOLD + "[" + ChatColor.RED + "PVP"
			+ ChatColor.GOLD + "] ";
	ItemStack bow = new ItemStack(Material.BOW, 1);
	ItemStack sword = new ItemStack(Material.STONE_SWORD, 1);
	ItemStack arrow = new ItemStack(Material.ARROW, 6);
	ItemStack cap = new ItemStack(Material.LEATHER_HELMET, 1);
	ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
	ItemStack pants = new ItemStack(Material.LEATHER_LEGGINGS, 1);
	ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
	PotionEffect healing = new PotionEffect(PotionEffectType.REGENERATION, 240, 10);
	public int online = Bukkit.getServer().getOnlinePlayers().length;
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;
	private Database db;
	PluginDescriptionFile pdfFile = getDescription();

	@SuppressWarnings("deprecation")
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info("[" + pdfFile.getName() + "] Starting...");
		this.logger.info("[" + pdfFile.getName() + "] Starting database...");
		db = new SQLite(this.logger, "[PVPCrazinessDB] ", this.getDataFolder()
				.getAbsolutePath(), "PVPCraziness", ".sqlite");
		db.open();
			try {
				db.query("CREATE TABLE IF NOT EXISTS players (id INTEGER PRIMARY KEY, name VARCHAR UNIQUE ON CONFLICT IGNORE, kills INTEGER, deaths INTEGER, rep INTEGER);");
				this.logger.info("[" + pdfFile.getName() + "] Started!");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getScheduler()
				.scheduleAsyncRepeatingTask(this, new Runnable() {
					public void run() {
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							p.setFoodLevel(20);
						}
					}
				}, 60L, 5L);

	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info("[" + pdfFile.getName() + "] Closing Database...");
		db.close();
		this.logger.info("[" + pdfFile.getName()
				+ "] Database Closed! Continuing to disable...");
	}

	public void setScoreBoard(Player player) {

	}

	@SuppressWarnings("deprecation")
	public void startTimer(final Player player) {
		this.getServer().getScheduler()
				.scheduleAsyncRepeatingTask(this, new Runnable() {
					public void run() {
						int num = 12;
						if (num != 0) {
							num--;
						} else {
							player.sendMessage(ChatColor.GOLD + "["
									+ ChatColor.RED + "PVP" + ChatColor.GOLD
									+ "] " + ChatColor.AQUA
									+ "You are no longer spawn protected!");
						}
					}
				}, 0L, 20L);
	}

	public void updateKills(Player killer) throws IOException {
		try {
			ResultSet repget = db.query("SELECT rep FROM players WHERE name = '"
					+ killer.getName() + "';");
			ResultSet killsget = db.query("SELECT kills FROM players WHERE name = '"
					+ killer.getName() + "';");
			int kills = ((Number) killsget.getObject(1)).intValue();
			int rep = ((Number) repget.getObject(1)).intValue();
			int newkills = kills + 1;
			int newrep = rep + 1;
			if(!db.isOpen()){
				db.open();
				db.query("UPDATE players SET kills = " + newkills + ", rep = "
						+ newrep + " WHERE name = '" + killer.getName() + "';");
				killer.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "PVP"
						+ ChatColor.GOLD + "] " + ChatColor.AQUA + "You now have "
						+ ChatColor.GREEN + newkills + ChatColor.AQUA + " kills and "
						+ ChatColor.GREEN + newrep + ChatColor.AQUA + " rep.");
			}else{
			db.query("UPDATE players SET kills = " + newkills + ", rep = "
					+ newrep + " WHERE name = '" + killer.getName() + "';");
			killer.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "PVP"
					+ ChatColor.GOLD + "] " + ChatColor.AQUA + "You now have "
					+ ChatColor.GREEN + newkills + ChatColor.AQUA + " kills and "
					+ ChatColor.GREEN + newrep + ChatColor.AQUA + " rep.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateDeaths(Player dead)
			throws IOException {
		try {
			ResultSet deathsget = db.query("SELECT rep FROM players WHERE name = '"
					+ dead.getName() + "';");
			int deaths = ((Number) deathsget.getObject(1)).intValue();
			int newdeaths = deaths + 1;
			db.query("UPDATE players SET deaths = " + newdeaths  + " WHERE name = '" + dead.getName() + "';");
			db.query(".backup PVPCraziness.sqlite;");
			dead.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "PVP"
					+ ChatColor.GOLD + "] " + ChatColor.AQUA + "You now have "
					+ ChatColor.GREEN + newdeaths + ChatColor.AQUA + " deaths.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent pjoin) {
		online = Bukkit.getServer().getOnlinePlayers().length;
		Player player = pjoin.getPlayer();
		player.setFoodLevel(20);
		player.setMaxHealth(20);
		startTimer(player);
		try {
			db.query("INSERT INTO players(name, kills, deaths, rep) values('"
					+ player.getName() + "', 0, 0, 0);");
			db.query(".backup PVPCraziness.sqlite;");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		player.getInventory().clear();
		pjoin.setJoinMessage(ChatColor.GOLD + "[" + ChatColor.RED + "PVP"
				+ ChatColor.GOLD + "] " + ChatColor.AQUA + player.getName()
				+ ChatColor.GREEN + " has joined the PVP game! Users online: "
				+ online);
		player.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "PVP"
				+ ChatColor.GOLD + "] " + ChatColor.BLUE
				+ "Kill as many players as possible!");
		healing.apply(player);
		player.getInventory().setHelmet(cap);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(pants);
		player.getInventory().setBoots(boots);
		player.getInventory().addItem(sword);
		player.getInventory().addItem(bow);
		player.getInventory().addItem(arrow);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent pleave) {
		Player player = pleave.getPlayer();
		pleave.setQuitMessage(ChatColor.GOLD + "[" + ChatColor.RED + "PVP"
				+ ChatColor.GOLD + "] " + ChatColor.AQUA + player.getName()
				+ ChatColor.DARK_RED + " left the game!");
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent death) {
		death.getDrops().clear();
		LivingEntity living = death.getEntity();
		Player player = (Player) living;
		String pname = player.getName();
		DamageCause cause = death.getEntity().getLastDamageCause().getCause();
		if (cause == DamageCause.DROWNING) {
			((PlayerDeathEvent) death).setDeathMessage(starter
					+ ChatColor.GREEN + pname + ChatColor.AQUA + " drowned");
			try {
				updateDeaths(player);
				saveConfig();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (cause == DamageCause.ENTITY_ATTACK) {
			String kname = player.getKiller().getName();
			Player killer = player.getKiller();
			((PlayerDeathEvent) death).setDeathMessage(ChatColor.GOLD + "["
					+ ChatColor.RED + "PVP" + ChatColor.GOLD + "] "
					+ ChatColor.GREEN + pname + ChatColor.AQUA
					+ " was killed by " + ChatColor.RED + kname
					+ ChatColor.AQUA + "!");
			try {
				updateKills(killer);
				updateDeaths(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (cause == DamageCause.FALL) {
			((PlayerDeathEvent) death).setDeathMessage(starter
					+ ChatColor.GREEN + pname + ChatColor.AQUA + " fell!");
			try {
				updateDeaths(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (cause == DamageCause.PROJECTILE) {
			String kname = player.getKiller().getName();
			((PlayerDeathEvent) death).setDeathMessage(starter
					+ ChatColor.GREEN + pname + ChatColor.AQUA
					+ " was shot by " + ChatColor.RED + kname + ChatColor.AQUA
					+ "!");
			try{
				updateDeaths(player);
				updateKills(player.getKiller());
			}catch(Exception e){
				e.printStackTrace();
			}
		} else {
			((PlayerDeathEvent) death).setDeathMessage(starter
					+ ChatColor.GREEN + pname + ChatColor.AQUA + " died!");
			try {
				updateDeaths(player);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent respawn) {
		Player player = respawn.getPlayer();
		healing.apply(player);
		player.getInventory().clear();
		player.getInventory().setHelmet(cap);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(pants);
		player.getInventory().setBoots(boots);
		player.getInventory().addItem(sword);
		player.getInventory().addItem(bow);
		player.getInventory().addItem(arrow);
	}

	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent cse) {
		if (cse.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
			cse.setCancelled(true);
		}
	}
	@EventHandler
	public void onServerPing(ServerListPingEvent ping) {
		int playersOnline = Bukkit.getServer().getOnlinePlayers().length;
		Random number = new Random();
		int randomNumber = number.nextInt(100);
		if (randomNumber < 50) {
			ping.setMotd(ChatColor.AQUA + "There are " + ChatColor.GOLD
					+ playersOnline + ChatColor.AQUA
					+ " players online! Join Now!");
		} else {
			ping.setMotd(ChatColor.GOLD + "There are " + ChatColor.AQUA
					+ playersOnline + ChatColor.GOLD
					+ " players online! Join Now!");
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent bbe) {
		bbe.setCancelled(true);
	}

	@EventHandler
	public void onBlockOnFire(BlockIgniteEvent bie) {
		bie.setCancelled(true);
	}
}
