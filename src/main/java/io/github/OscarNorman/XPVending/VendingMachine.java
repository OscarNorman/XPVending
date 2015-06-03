package io.github.OscarNorman.XPVending;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VendingMachine {
	private class Item{
		public String dispname;
		public int amount;
		public int price;
		public String gameName;
	}

	private ArrayList<Item> Items = new ArrayList<Item>();
	private int currentItem;
	private String type;

	public VendingMachine(ConfigurationSection configSect, String t){
		type = t;
		
		Set<String> k = configSect.getKeys(false);
		for(String str:k){
			Item a = new Item();
			a.price = configSect.getInt(str + ".price");	
			a.dispname = configSect.getString(str + ".displayname");
			a.amount = configSect.getInt(str + ".amount");
			a.gameName = str;
			Items.add(a);
		}

	}
	
	public String getType(){
		return type;
	}

	public String[] getCurrentNames(){
		String[] result = new String[3];
		int lastItem = currentItem-1;
		int nextItem = currentItem+1;
		if(lastItem<0){
			lastItem=Items.size()-1;
		}
		if(nextItem>=Items.size()){
			nextItem=0;
		}

		result[0] = Items.get(lastItem).dispname;
		result[1] = Items.get(currentItem).dispname;
		result[2] = Items.get(nextItem).dispname;
		return result;
	}
	public String getCurrentName(){
		return Items.get(currentItem).dispname;
	}

	public boolean buyItem(Player p){
		int playersXP = p.getLevel();
		int cost = Items.get(currentItem).price;
		Inventory pinv = p.getInventory();
		if(playersXP<cost){
			return false;
		}else{
			String[] i = Items.get(currentItem).gameName.split(":");

			String type = i[0];
			int count = 1;
			short meta = 0;

			switch (i.length) { 
			case 1: 
				break; 
			case 2: 
				count = Integer.parseInt(i[1]); 
				break; 
			case 3: 
				count = Integer.parseInt(i[2]);  
				meta = Short.parseShort(i[1]);
				break; 
			} 
			Material m = Material.matchMaterial(type);
			if (m != null) {
				ItemStack is;
				if(meta!=0){
					is = new ItemStack(m,count,meta);
					pinv.addItem(is);
				}else{
					is = new ItemStack(m,count);
					pinv.addItem(is);
				}
				playersXP = playersXP - cost;
				p.setLevel(playersXP);

			} else {
				p.sendMessage("Item "+type+" could not be found");
			}
		}

		return true;
	}

	public void nextItem(){
		currentItem=currentItem+1;
		if(currentItem>=Items.size()){
			currentItem=0;
		}
	}

	public String getCurrentQuantity() {		
		return new Integer(Items.get(currentItem).amount).toString();
	}

	public String getCurrentCost() {
		return new Integer(Items.get(currentItem).price).toString();
	}
}
