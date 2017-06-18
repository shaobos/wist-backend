Welcome to Rest.li
==================

This is a rest.li application using gradle as the build system.

#Setup
##Build
To build for the first time, use gradle 1.8 or greater and run:

```
gradle build :wist-server:JettyRunWar
```


Once running, you can send a GET request to the server with:

`curl http://localhost:8080/wist-server/word/voracious`

Command to generate gradle wrapper:
`gradle wrapper --gradle-version 3.2.1`

##Cronjob
```
*/1 * * * * cd /home/ssb/wist-backend && git pull && python /home/ssb/wist-backend/data_injest.py
```

###Redis
Check out Redis and run `./redis-server` so that it hosts a server at port `6379`
