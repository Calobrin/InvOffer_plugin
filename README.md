9/28/2024

This development session was split in two, and I did not happen to write any readme documentation for the 0.1.8 version
I ran into trouble trying to set up my cancelOfferCommand, to put it plainly I had decided to go on a different route for how this command would work.

Instead of opening the inventory GUI for the player canceling to just take the items out, I just wanted to place the items directly into the inventory.
It didn't really work out though and while I said before I rely on ChatGPT as a resource tool, it was of no use and was looping into methods of how to show the inventory window.

I eventually gave up because I wasn't sure how to do it, I COULD have just created the Cancel Offer to run almost exactly like the Accept Offer, but I wanted to try something different.

Well, I changed course. Now the cancel offer command is almost a mirror of how the accept works. I also refactored the inventoryCloseEventListener for accepting offers to be AcceptAndCancel, and there is a conditional check for the title/size of the inventory, and then two methods to handle the canceling and accepting. Basically all of the logic contained in the old acceptListener was slapped into a method and then called if the title matches in the event listener.

I didn't run into too many problems here, seeing how like I said it mirrors the cancel offer command the groundwork is laid out already, so next time I have some big plans!

I do NOT want the ability to duplicate items. What happens if a player is accepting the offer and taking the items out, and then the player who sent it cancels? Well of course, all the items are in the DB until that person accepting it closes the window. 
So if you cancel it during an accept, you have two windows containing duplicate items.
I intend now to change how my DB is created, and make a new column for the offer status: Pending, Accepted, and Canceling.

Note how I said "canceling", which means it may not be fully canceled yet, this is to cover the instance of a player not being able to take all items from the offer to cancel it. Regardless, it is in the process of canceling and once it begins it can no longer be accepted.
I may also change it to be "Accepting" because this is also true, I only want the accepting status to be in place while the window is actively open. Otherwise it returns to a pending status.
This also means that while it is being actively accepted you cannot cancel it. If the player happens to not get all the items, but the sender wants it back well they can get the items still remaining in the offer, but NOT while that window is open.

Again, all future plans. For now the cancel offer command works, but I need to do a bit more.
So next update will probably be a 0.1.8.2, that way we consider the "8" to be the inclusion of canceling offers.