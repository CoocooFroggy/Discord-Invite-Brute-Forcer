# Discord-Invite-Brute-Forcer

Unfortunately, I cannot host the bot for everyone without it being immediately rate limited by Discord. You'll have to set it up yourself if you want to brute force an invite, or DM me CoocooFroggy#7742 if you want me to personally host.

## Code Setup

There are two placeholders in Main.java that you'll need to replace:
- Search for `YOUR ID HERE` and replace with a Guild (Server) ID where you want the bot
- Search for `YOUR MOD ROLE ID HERE` and replace with the ID of a moderator role, so only mods can start brute invites

## Environment Variables

Set your token in an environment variable to BRUTE_TOKEN
In a shell script to launch the bot, you can do
`export BRUTE_TOKEN=[token]`

## Building

Do `./gradlew shadowjar` in the project directory to make a jar file. Run it with `java -jar build/libs/Invite Brute Forcer-1.0-all.jar`.

## Usage:
Do `/brute` and fill in the arguments. 3 characters is probably the optimal amount without it taking months. 4 character insensitive is the limit before it takes over a year.
