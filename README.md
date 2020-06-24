# discordBot (very basic instructions)
Running the bot on linux
1. install maven
2. run `mvn clean install` to compile the bot
3. configure the bot using the config files, see database.properties.example and servers.json.example for examples
4. run `java -jar target/discordBot-1.0.jar` to run the bot

Running the discord bot on heroku
1. create a dyno(free tier)
2. add the heroku postgres add-on to the dyno (free tier)
3. connect to this github repo as a depolyment method
4. manually deploy the bot
5. configure the environment variables by setting the config vars in the settings
6. add values for BOT_TOKEN and SERVERS_JSON, the DATABASE_URL should already be set by the postgres add on
7. enable the dyno

## Notes
Bot must have a role above every member that it might want to change the role of (this can be done with a dummy role that adds no other permissions if wanted)
