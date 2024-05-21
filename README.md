##05/20/2024 

Another big session of development here.
I went back to where I was previously, I needed to set it up to prevent opening the GUI if sending an offer, if you already had an offer sent.
After that, it was on to creating the command class and logic for the AcceptOfferCommand, which went fairly well.
It is set up in a similar way to the OfferCommand, where it makes sure the sender is a player, but it is a lot more concise than that class (I may go back to mirror that idea later)
This was also a lot more confusing because it was no longer "player and target" but "player and sender".

I also had to create a new method in the DataManager class to accept the offer, which handles the logic for retrieving the info stored for when an offer is sent, and opens the GUI with the items.
This comes with the additional requirements of creating a NEW event listener, for clicking, which is primarily to prevent players from adding items to the inventories via any means. This took a LOT of work to get going and I was able to get through via spam clicking. By setting up an additional event listener for onInventoryDRAG is what really seemed to get it to work. Still not 100% sure how everything works but hey, it is working as intended.
One other change I had to do was change the titles for the inventory GUI's opening. I just had "InvOffer GUI" but now I have a ": send" or ": Accept" to determine the difference, because the onInventoryClose listener would trigger even for the accept offer, so instead of create logic to determine which command was being used it was better to make the titles unique, on top of the purposes of easy identification.

Next steps is to have the item data deleted once the offer is accepted and all items are removed, so when the window is empty it will delete the contents from file.
