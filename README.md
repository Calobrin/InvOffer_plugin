9/14/2024

I started up todays session by making sure that slight tweak I did post 0.1.6.5 was correctly working.
Last time I set up the acceptOfferCommand to use a HashMap of the UUID's so when multiple players run the command,
it won't overwrite who sent the offer when updating. I forgot to add the same approach to SENDING offers so while beta testing my
plugin I noticed the offer got redirected to a different player than what I used in the command.

After a quick test (it worked), I went straight on to further development.
I know I wanted to make a CancelOffer command, but I felt it may be better to quickly create a command to show a list of all pending offers a player may have.
The idea is, you need the name of the player to accept (and cancel, in the future) offers. But if you happened to have logged off, or had a long lasting offer you may never know.
Well now, you can run the /listoffers command, and it will tell you if you have any sent or received pending offers.
On TOP of this, it will tell you if you DON'T have any pending sent/received offers.
So you can have a pending sent, but tells you no pending received, or vice versa.
It was simple enough to do, just two more methods in the DataManager and handling how it works through strings.

Overall happy with this- it all worked first try too! I may be getting better at this c: