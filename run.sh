#!/bin/bash

list=(
"Rosa Darkmoon",
"Joe Dog Stoker",
"Salty Simon Bloodrayne",
"Gold Thomas Digger",
"Bloody Edward Blacksmith",
"Doubloon Thomas Digger",
"Captain Rosa Silverblade",
"Salty Johnny Bloodrayne",
"Morbid James Darkblade",
"Captain Jack Harker",
"Joe Black Harker",
"Scarlet George Bloodrayne",
"Captain John Bloodrayne",
"Swag Scarlet Bloodrayne",
"Nathan Blimey Ravenblack",
"Anne Canon Hawkins",
"Bloody Thomas Redbeard",
"Stormy James Blackman",
"Salty Johnny Gully",
"Salty Katja Davis",
"Deadlights Simon Stoker",
"Francois Scully",
"John Ravenbeard",
"Gruesome Johnny Bloodrayne",
"George Dangerous Sangre",
"Captain Marcia Hawkins",
"Henri Bloody Redbeard",
"Captain Peter Grimbeard",
"Jacob Brutal Shelley",
"Devil Bill Dreadbeard",
"Bloody Simon Bloodrayne",
"Silver Steve Gully",
"Stormy Billy Grimbeard",
"Walker Silver Blackman",
"Captain John Ravenblack",
"Steve Blacksmith",
"Johnny Gold Ravenblack",
"Vicious Jacob Redbeard",
"George Blackstroker",
"Tessa Crawhawk",
"Killer Brutus Crawhawk",
"Bill Dangerous Black",
"Captain James Fargloom",
"Evil John Blackman",
"Nathan Barbaric Hawkins",
"Hungry Steve Blackstroker",
"Roger Gray Fargloom",
"Shanty Nathan Deadwood"
)

i=$(( $RANDOM % 48 ))

java -cp "bin/" mm19.runner.TestClientRunner  "${list[$i]}"
#java -jar sam.jar  "${list[$i]}"

