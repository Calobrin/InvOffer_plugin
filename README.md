9/30/2024
This is (or should be) the final development session for 0.1.8, where we are including the cancelOffer command and all the logic that follows that.
This session was devoted to setting up the changes to the Database, I wanted to have an additional column made that would check/track the statuses of offers in the database.
I believe this was mentioned in my 0.1.8.1 README, but I wanted three states: pending, accepting, canceling.

**Pending** is the default setting, once an offer is created and sent to a player it is in the **pending** status. A **pending** status offer can be _accepted_ or _canceled_.

**Accepting** is a conditional status, an offer is only set to **accepting** after the command is run and while the inventory window of the offer is still open. Once the window closes it returns to **pending**

**Canceling** is a final version of a offer status, if the user who sent the offer runs this command, their offer is set to **canceling** and will be so until it resolves and deletes. Once canceled the offer cannot be accepted by the player, even if the offer still exists

So basically this whole setup added the new column to the database, and we have a few new methods to get the status, set a status, 
as well as an additional method to reset ANY accepting offer statuses to pending again. 
reset is meant to be run at the start of the server/plugin initialization. 
I want to make sure any offers that didn't get to close (player crash/disconnect, or the server shut off), which would make the offer status not change back to pending, would be resolved and fixed on a server restart

For accepting an offer, during the onCommand part of the command is when it would set the offer to be **accepting**, 
and in the listener would be when it resets to **pending**. And canceling, 
same as accept when the command is run the offer status is set to **canceling**, but is NOT undone and reset to pending inside the listener.

Overall this dev session worked well, I feel like things are getting a lot easier now that I know a bit more of what I am doing. The foundation is set, not much else to do but continue refining the plugin.
But as it currently sits it works beautifully and I couldn't be happier.