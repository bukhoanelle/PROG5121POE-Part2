# PROG5121 POE - Part 2 (QuickChat: Sending Messages)

This is my Part 2 submission for PROG5121. It builds on my Part 1 registration and login. The user has to log in first, and then they can send messages from a menu. It is a console app only, built in NetBeans with Maven.

## How it works

1. The user registers and logs in (from Part 1). They get three login attempts.
2. After logging in, the app shows "Welcome to QuickChat."
3. The user says how many messages they want to enter.
4. The menu keeps running until the user picks Quit:
   - 1) Send Messages
   - 2) Show recently sent messages (shows "Coming Soon.")
   - 3) Quit
5. For each message the user enters a recipient number and the message text, then chooses to send it, disregard it or store it for later.
6. When all the messages are done, the app shows the total number of messages sent.

## The Message class

- `checkMessageID()` checks that the message ID is not more than ten characters
- `checkRecipientCell()` checks the recipient number has an international code (it reuses the check from Part 1)
- `checkMessageLength()` checks the message is 250 characters or less
- `createMessageHash()` builds the hash, for example 00:0:HITONIGHT
- `sentMessage()` sends, disregards or stores a message
- `printMessages()` returns all the messages sent so far
- `returnTotalMessages()` returns how many messages were sent
- `storeMessage()` saves stored messages to a JSON file (messages.json)

## Tests

The unit tests are in MessageTest.java and use the test data from the brief. To run them, right-click the project in NetBeans and click Test. GitHub Actions also runs them every time I push.

## Notes

- The message ID is a random ten-digit number.
- The hash is the first two digits of the ID, the message number, and the first and last word of the message in capitals.
- I wrote the JSON file by hand instead of using a library, so the project has no extra dependencies.
