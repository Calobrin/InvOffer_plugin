9/7/2024

As mentioned in the commit message (I REALLY ought to put that more into the Readme. Also this readme is still auto-defaulting to an earlier branch and I need to figure out how to turn that off)
This is my first session back working with the plugin since May.
I took a hiatus from this as I had a trip planned, and I was procrastinating getting back mostly out of fear that I have no idea what I am doing and getting started is the hardest part for me, Anyways...

I feel I must have mentioned this at some point but MUCH of this development is with the aid of ChatGPT as a tool for examples, documentation, and bouncing ideas around. As none of my friends are into coding I can't really ask them for ideas so I am on my own.
I had concerns for a bit on what happens if multiple users are using the invoffer commands at the same time. For example the UUID was being set via a method in acceptOfferCommand, so if Player A sends to B, and B accepts the offer. While that window is open player C could send to D, and therefore they set the UUID of player A for B's offer into player C. It could be a nightmare so I had to consider ways to work around it.

ChatGPT mentioned multiple concurrent access to the file for writing data could pose problems of data going where it shouldn't, and so I decided to try to use a database like SQLite (I checked my servers plugin folders to see how some of my favorite plugins handle data, and saw our Minepacks plugin using an SQLite db file)
So I spent like 4+ hours GUTTING the majority of the DataManager class methods and replacing all of the YML file stuff to using SQLite DB stuff, such as initializing, opening closing and all the CRUD operations and inserting them into our previously made methods for handling offers.

It looks good so far, after some testing I do have the .db file being created and placed now where it NEEDS to go. But there is a massive problem.

Serialization of Inventory Data...

Working with the YML it was fairly simple IIRC, but moving onto SQLite there is a whole bunch of issues and complexities, but I genuinely believe it is worth it.
As I was wrapping up my session because honestly I am exhausted after working on this for so long (Especially for my first day back working on this) the AI mentioned alternatives to the method we are going about for serialization and then mentioned a libray called ItemSerialization for Minecraft and I am just like "But why didn't you say so sooner!!"

I intend to test out this newly discovered library next time I have a go at developing. While I am displeased to not have this development branch in a WORKING state, the groundwork set here to setting up the DB is MASSIVE in my eyes.
Yes I wanted to move onto creating the cancelOfferCommand, but this I think is important for considering the future of the plugin, having a DB like this makes the plugin more scaleable and honestly the YML file I wasn't utterly pleased with.

For now I rest, hoping to get really back into developing this plugin and deploying it onto my server.
