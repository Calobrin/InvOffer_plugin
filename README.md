5/26/2024

Oh boy what a session. This was technically a 2-day development session.
I ran into some troubles when running this, for example I had issues getting the SenderUUID for when accepting the offer.

Methods had been created to UpdatePendingOffers or to delete/resolvePendingOffers,
either of which would be called on the inventoryCloseListener depending on if the window was empty or not.
If the window wasn't empty, update the data with only the remaining items, and delete the ones the player took.
This would allow the player to run the command again to get what remained of the offer.

Then if the window was empty, it would simply delete the data entry entirely.

Again, the issue arose when trying to update, I had trouble sending the UUID saved in the AcceptOffer command to the closelistener.
The issue was I was using the method for the sendoffer command, which means I was setting the sender of the offer to be the same as the person who GOT the offer, so when it updated it would either write the data to have two of the same UUID's,
or it would not be able to find the offer of Player A and Player B UUID's.

I started back up on the 26'th after a few day break trying to implement a method within the AcceptOffer Command to save and retrieve the senderUUID (or target of accept offer command)
And then when the updateOffer method was called, it had what it needed to.
This also posed problems because I now had to pass AcceptOfferCommand to the eventlistener, which means I also had to include AcceptOfferCommand into the OfferCommand.
It became a giant mess, so I decided to create a separate event listener SPECIFICALLY for the accept window, I used to have it be an if check for the title name, but this made it easier.

I modified some things in the data manager to include more ways to retrieve the UUID, some of which may be removed later on who knows for now.
Eventually I got it working, but I ended up making changes to how the data is stored and updating.
The reason was, when the offer resolved, and I went to accept the offer again, instead of saying no active offer it would simply say the target was not found.
This is because of the if statement for if the targetUUID is null, and in this case it is because the data was deleted.
There may be a better way around this, but instead of deleting the offer entirely, we simply erase the inventory contents, while keeping the UUID pairs.
This way we change how the hasActiveOffers checks, which looks for an empty inventory, and it is able to update more cleanly.
This also allows us to inform the user that they already accepted the offer from that player and just makes the player experience a bit better,
because despite having it working. I wasn't happy with it.

I see some glaring issues in the future to be considered.

Because the offer and accept commands store the UUID of the target, it means each time the command is run it will reupdate that variable.
So what happens if someone has an active offer open, and while they are doing that a different player accepts a different offer.

If Player B accepts an offer from A, and has the window open,
and player C accepts an offer from Player D, when player B goes to close their window without taking everything, it will update the offer to be from Player D, despite it not being from that player

Something will need to be put in place that will change how it all works. Either make it so only one acceptOffer window can be running, in which case it would prevent all other players from accepting offers... But I imagine that to be used to exploit and block access to this offer from others..

So obviously I have work to do. But for now the basic functions of this plugin are working!

Next I need to set up commands for canceling offers and reclaiming their items, in the off chance the player they sent the offer to NEVER accepts it...

But one step at a time!!!