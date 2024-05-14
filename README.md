##05/12/2024

###**(This Readme was edited days after)**
I worked on implementing an event listener for the OfferCommand, so when the inventory window is closed the offer would be sent and saved.
I had some problems at the end of this development session with the targetUUID being null. 
It also appeared to be making multiple calls to the eventlistener where it would send my debug message to check for the targetUUID multiple times.
