9/12/2024

This was a shorter development session. Focusing on fixing the issues with Inventory Serialization from the previous. Which is why this is labeled as 1.6.5, it is merely a continuation from the previous.
Worked around a few different methods of serialization, using Gson which ran into the issue of "optional" being a problem for Gson, then using bukkit/spigot API and then finalizing on the method in this branches code. Which to be honest I am not fully grasping at this time, from what I understand, it is converting the inventory data into a Baase64-encoded string which is stored into the database.
Then it uses the "oos.writeObject(inventory.getContents())" to serialize the inventory contents.

And then essentially the reverse occurs where we deserialize the inventory contents and put them into an inventory window with the matching title of what is expected in the inventoryclose listener for the acceptOffer command. I also had a problem with that where I overlooked the title being generated, so despite GETTING the inventory contents when accepting, the offer wasn't resolving or informing the player who sent it of its progress. Was a simple enough fix.
Also one MAJOR thing I changed here is how the senderUUID was being saved.
This was always one of my major concerns and why I even went the approach of using SQLite, where each time the player ran the acceptOffer command, it would set a variable to be the UUID of the person who sent the offer. But since when closing the offer window when accepting it would check that UUID to save the data, or update what remained in that case, it could cause unexpected behavior.
Now, instead of the method in AcceptOffer being a variable, we now use another HashMap that holds the key/value of the UUID for the player who sent and received the offer. This also isn't stored persistently like the inventory offer data or anything, but that doesn't matter as this should only really be a thing during the servers runtime.

With this in place I feel confident enough to beta test this plugin on my server, and perhaps see more ways to expand (or fix) this plugin as I move forward.
