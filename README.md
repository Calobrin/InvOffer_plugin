This is my repository for a personal Spigot MC plugin I am developing for my own personal use / my community server.
I am exceptionally new to Spigot/Plugin development so this is a learning curve process. I have no expected release date of this plugin.

I am creating a plugin that would allow players to remotely give each other items, The idea came from to how an admin could use EssentialsX /Invsee command to move, add, or remove items from a players inventory. 
The idea being that this could be used to remotely give players items without having them be next to each other in game,
by sending an inventory screen of items the target player could open to receive.


I intend the commmand to work like this:
Player uses /invoffer <target> (alias as /offer) - target will be checked to not be yourself, offline, more than one target, or null.
This opens an inventory GUI that the player can send up to 9 slots in the menu.
Items will be saved, and an offer will be sent to a player, notifying the target that a player sent them an offer, and which player did. 
They could then run something like the following:  (this idea is far down the path, not fully fleshed out)
target player uses /offerAccept <target>  where the target is the player who sent them the offer. (This may require not allowing more than one offer from a specific player at a time...)
The inventory window opens showing the items added by the original sender, where the player can then take the items from that screen, emptying it and closing it which then deletes the offer.
Additionally, if not all items are collected, they could either run the command again (so long as the offer still exists, will likely include a way for the offer sender to cancel the offer) to claim the remaining items.
