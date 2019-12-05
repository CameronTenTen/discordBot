#!/bin/bash
if [ $# -eq 0 ]
	then 
		echo "file containing token must be supplied as an argument"
		exit 1
fi
java -cp "./lib/*:src" core.DiscordBot $(cat $1)