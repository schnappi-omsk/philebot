# PhilBot

A Telegram bot to post Xbox achievements and stats in your group. It works with XAPI, which is a proprietary third-party API for Xbox profiles.
In order to use an app you'll need a XAPI token and a Telegram Bot API token.

## Features

1. The bot is built using the TelegramLongPollingBot framework.
2. It sends scheduled periodic messages and retrieves achievements at fixed delays.
3. It has built-in command response functionalities.\
4. Bot uses PostgreSQL

## Environment Variables

This project relies on the following environment variables:

**API_TOKEN** - token got from XAPI (paid subscription)  
**TGAPI_TOKEN** - your Telegram bot token  
**SERVICE_HOST** - public host, where this app is deployed  
**PG_HOST**, **PG_PORT**, **PG_DB**, **PG_USER**, **PG_PASS** - PosgreSQL parameters  

