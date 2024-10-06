10/05/2024

Quick spontaneous session - 0.1.9! 

I decided I wanted to go in and add some polish and pizazz. I thought it might be neat to allow the execution of commands by clicking on the text sent to players during offer transactions.

For example, you get an offer and the plugin tells you that you can accept the offer by typing in /acceptoffer playername, but now that "/acceptoffer playername" is hoverable to show you a simplified summary of item contents, and is also clickable to immediately run the command.

It's still a little jank thanks to Minecraft and character limits, 
there is some odd spacing right now so the message puts the "playername" part of the clickable text as a new line and with a space behind it. 
I could circumvent this by adding this entire portion of the message AFTER the previous part and make it a new line but eh. 
For now I was content with just this basic premise working.

Oh I nearly forgot to mention. Bit of a change, I renamed DataManager to be OfferManager, as well as created a new class in the data folder for InventoryManager, 
which currently houses the methods to serialize/deserialize inventory. I plan to eventually migrate some methods out of the previously called DataManager to make things more organized

This was meant to be a smaller little dev session, and I am happy with the results. 
so most of 0.1.9 development will be focusing on polishing up the messages sent to players and improving the overall feel of the plugin.

(I also forgot to change my commit message. god damnit all)