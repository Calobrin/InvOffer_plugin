##05/14/2024 

Today was a big day for development.
I spent about 5 hours developing the plugin.
Last time I was experiencing the null ptr exception for the targetUUID, this was my main focus for today, but also I thought it must simply be for when the savePendingOffers() method was called.

I knew the methods were inadequate, while my activeOffers (what checks to see via a key : value pair of player : target UUIDs) was saving to a file, 
the inventory data for these offers (labeled simply as pendingOffers which I may change someday to a better naming convention) was being saved to memory.

To make these into persistent storage I recreated all three methods from scratch, to save to a new separate file called pending_offers.yml
I of course overlooked something and instead had the inventory data from pending being saved alongside the active offers. So I had to go back and ensure there were two separate file objects to work with.

More work was done to ensure the data was being saved correctly, by using the serialization of the inventory contents and saving it to a file.
All the ground work is set up, the command runs and saves the data as well as the current active offer. I imagine I could combine these two files, or rather seek the UUID's saved in the inventory data, but for now I am keeping it separate.
Now I just need to tidy up with some things to prevent duplicate offers being sent, and then I just need to set up the acceptOffer, cancelOffer, and maybe even a offerList command to work with the files and data being saved.

I feel like I am over the hump here and it will all be downhill and smooth sailing, but who knows what difficulties will crop up as I move forward.
