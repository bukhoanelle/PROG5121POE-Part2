package prog5121poe;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Message {

    // Messages the program returns to the user
    public static final String CELL_SUCCESS = "Cell phone number successfully captured.";
    public static final String CELL_FAILURE = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    public static final String LENGTH_SUCCESS = "Message ready to send.";
    public static final int MAX_MESSAGE_LENGTH = 250;

    // These are shared by every Message while the program is running
    private static final ArrayList<Message> sentMessages = new ArrayList<>();
    private static final ArrayList<Message> storedMessages = new ArrayList<>();
    private static final Random random = new Random();
    private static int totalMessagesSent = 0;
    private static String storageFile = "messages.json";

    // Each message has its own details
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;

    // Normal constructor: the message ID is generated automatically
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    // Second constructor: lets the unit tests choose the message ID
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
    }

    // Creates a random ten-digit message ID (leading zeros are allowed)
    private static String generateMessageID() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            id.append(random.nextInt(10));
        }
        return id.toString();
    }

    // The message ID must not be more than ten characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    public String getMessageIDStatus() {
        return "Message ID generated: " + messageID;
    }

    // Reuses the cell phone check from Part 1 (Login.checkCellPhoneNumber)
    public static String checkRecipientCell(String recipient) {
        if (recipient != null && Login.checkCellPhoneNumber(recipient)) {
            return CELL_SUCCESS;
        }
        return CELL_FAILURE;
    }

    // The message may not be longer than 250 characters
    public static String checkMessageLength(String text) {
        if (text.length() <= MAX_MESSAGE_LENGTH) {
            return LENGTH_SUCCESS;
        }
        int extraCharacters = text.length() - MAX_MESSAGE_LENGTH;
        return "Message exceeds 250 characters by " + extraCharacters + "; please reduce the size.";
    }

    // Hash = first two digits of the ID : message number : first word + last word (all caps)
    public String createMessageHash() {
        String firstTwoDigits = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;

        String[] words = messageText.trim().split("\\s+");
        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);

        return (firstTwoDigits + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // Removes punctuation, so "tonight?" becomes "tonight"
    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // choice 1 = send, 2 = disregard, 3 = store to send later
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                totalMessagesSent++;
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                if (storeMessage()) {
                    return "Message successfully stored.";
                }
                return "Message could not be stored.";
            default:
                return "Invalid option, please choose 1, 2 or 3.";
        }
    }

    // The details are shown in this order: ID, hash, recipient, message
    public String getFullDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + createMessageHash() + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    // Returns every message that was sent while the program has been running
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }

        StringBuilder allMessages = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            allMessages.append(sentMessages.get(i).getFullDetails());
            if (i < sentMessages.size() - 1) {
                allMessages.append("\n\n");
            }
        }
        return allMessages.toString();
    }

    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    // Saves every stored message to a JSON file. Returns true if the file was written.
    public boolean storeMessage() {
        storedMessages.add(this);

        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            json.append(storedMessages.get(i).toJson());
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");

        try (FileWriter writer = new FileWriter(storageFile)) {
            writer.write(json.toString());
            return true;
        } catch (IOException e) {
            System.out.println("Could not write to " + storageFile + ": " + e.getMessage());
            return false;
        }
    }

    // One message as a JSON object
    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escapeJson(messageID) + "\",\n"
                + "    \"messageNumber\": " + messageNumber + ",\n"
                + "    \"recipient\": \"" + escapeJson(recipient) + "\",\n"
                + "    \"message\": \"" + escapeJson(messageText) + "\",\n"
                + "    \"messageHash\": \"" + escapeJson(createMessageHash()) + "\"\n"
                + "  }";
    }

    // Makes sure quotes, slashes and line breaks do not break the JSON file
    private static String escapeJson(String text) {
        StringBuilder escaped = new StringBuilder();
        for (char character : text.toCharArray()) {
            switch (character) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.toString();
    }

    // Used by the unit tests so each test starts clean
    public static void resetMessages() {
        sentMessages.clear();
        storedMessages.clear();
        totalMessagesSent = 0;
    }

    // Used by the unit tests so they do not write into the project folder
    public static void setStorageFile(String path) {
        storageFile = path;
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }
}
