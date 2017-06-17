# findBusTime

## One time configuration
1. Use [this wizard](https://console.developers.google.com/start/api?id=calendar) to create or select a project in the Google Developers Console and automatically turn on the API. Click Continue, then Go to credentials.
2. On the Add credentials to your project page, click the Cancel button.
3. At the top of the page, select the OAuth consent screen tab. Select an Email address, enter a Product name if not already set, and click the Save button.
4. Select the Credentials tab, click the Create credentials button and select OAuth client ID.
5. Select the application type Other, enter the name "Finding Bus time", and click the Create button.
6. Click OK to dismiss the resulting dialog.
7. Click the file_download (Download JSON) button to the right of the client ID.
8. Move this file to resources and replace the existing file.

## Changes to be done to read from correct team calendar
In [Quickstart.java](src/main/java/QuickStart.java) search for "calendarListEntry.getSummary()" and give the name of the team calendar from which events has to be read.

## Configuring cron job in your local
In your local give the following command 0 10 * * 1-5 <replace with the path to current folder>/script.sh
The above configuration will run the job every workday (Monday to Friday) at 10 AM


## Configuring slackbot for incoming webhooks
currently the slackbot will post the message to the channel configured in the incoming webhooks to change the channel do the following
1. Go to [slack integrations page](https://twu2017trainers.slack.com/apps/manage/custom-integrations)
2. Select Incoming Webhooks
3. Edit Webhook named harsha's life-save :D
4. In Integration Settings -> Post to Channel select the channel to which the bot should post to.

**Note: When running for the first time the program will prompt to give calendar permissions you will need to hit the url in the console in your browser and give calendar permissions.**