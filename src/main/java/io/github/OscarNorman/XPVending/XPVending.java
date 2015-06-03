package io.github.OscarNorman.XPVending;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class XPVending extends JavaPlugin implements Listener{

	HashMap<Block,VendingMachine> vendingMachines = new HashMap<Block,VendingMachine>();


	@EventHandler
	public void playerInteract(PlayerInteractEvent e){
		if(vendingMachines.containsKey(e.getClickedBlock())){
			Player p = e.getPlayer();
			VendingMachine v = vendingMachines.get(e.getClickedBlock());
			if(e.getAction()==Action.RIGHT_CLICK_BLOCK){
				if(p.isSneaking()){
					p.sendMessage(ChatColor.DARK_GREEN+"The Cost Of "+ChatColor.translateAlternateColorCodes('$',v.getCurrentName())+ChatColor.DARK_GREEN+" Is "+ChatColor.RED+v.getCurrentCost()+ChatColor.DARK_GREEN+" XP Levels For "+ChatColor.RED+v.getCurrentQuantity());
					
					
				}else{
					v.nextItem();
					updateSign(e.getClickedBlock(), v);
				}
			}
			if(e.getAction()==Action.LEFT_CLICK_BLOCK){
				if(v.buyItem(p)){
					
				}else{
					p.sendMessage(ChatColor.DARK_GREEN+"You Don't Have Enough Levels!");
				}
				e.setCancelled(true);
			}
		}

	}
	@EventHandler
	public void onSignChange(SignChangeEvent e){
		this.getLogger().info("Sign Changed");
		String lineText = e.getLine(0);
		Player p = e.getPlayer();
		if(p.hasPermission("xpvending.create")){
			if(lineText.startsWith("[XPVending]")){
				if(addVendingMachine(lineText.substring(12),e.getBlock())) {
					p.sendMessage("You Made A Vending Machine Of Type: "+lineText.substring(12)+" !");
					saveVendingMachines();
					e.setCancelled(true);
				}else{
					p.sendMessage("This Vending Machine Type Doesn't Exist!");
				}
			}
		}else{
			p.sendMessage("You do not have permission!");
		}

	}

	@Override
	public void onEnable() {
		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(this, this);
		this.saveDefaultConfig();
		
		//load existing signs
		int i=0;
		while(this.getConfig().contains("vendingmachines."+i)){
			String type = this.getConfig().getString("vendingmachines."+i+".type");
			Location location = (Location)(this.getConfig().get("vendingmachines."+i+".location"));
			Block b = location.getBlock();
			addVendingMachine(type,b);
			i+=1;
		}
			
	}

	@Override
	public void onDisable() {
		try {
			this.getConfig().save("plugins/XPVending/config.yml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveVendingMachines(){
		int i=0;
		getLogger().info("Saving Vending Machines");
		for (Map.Entry<Block, VendingMachine> entry : vendingMachines.entrySet()) {
		    Block b = entry.getKey();
		    VendingMachine v = entry.getValue();
		    
		    ConfigurationSection cs = this.getConfig().createSection("vendingmachines."+i);
		    cs.set("type", v.getType());
		    cs.set("location", b.getLocation());
		    i++;
		}
		
		
	}
	
	private boolean addVendingMachine(String type,Block block){
		String key = "types."+type+".items";
		if(this.getConfig().contains(key)){
			ConfigurationSection configSection = this.getConfig().getConfigurationSection(key);
			if(configSection!=null){
				VendingMachine v = new VendingMachine(configSection, type);
				vendingMachines.put(block, v);
				updateSign(block, v);
				return true;
			}
		}
		return false;
	}
	private void updateSign(Block b, VendingMachine v){
		String[] name = v.getCurrentNames();
		Sign sign = (Sign) b.getState();
		sign.setLine(0, "[XPVending]");
		sign.setLine(1, ChatColor.translateAlternateColorCodes('$', name[0]));
		sign.setLine(2, ">"+ChatColor.translateAlternateColorCodes('$', name[1])+ChatColor.BLACK+"<");
		sign.setLine(3, ChatColor.translateAlternateColorCodes('$', name[2]));
		sign.update();
	}



}

