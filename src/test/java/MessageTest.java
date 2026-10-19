package prog5121poe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageTest {

    @TempDir
    Path tempDir;

    // Test data from the brief
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    @BeforeEach
    public void setUp() {
        // every test starts with no sent or stored messages
        // and saves its JSON file in a temporary folder
        Message.resetMessages();
        Message.setStorageFile(tempDir.resolve("messages.json").toString());
    }

    // Message length

    @Test
    public void testMessageLength_Success() {
        assertEquals("Message ready to send.", Message.checkMessageLength(TEXT_1));
    }

    @Test
    public void testMessageLength_ExactlyTwoHundredAndFifty() {
        String text = "a".repeat(250);
        assertEquals("Message ready to send.", Message.checkMessageLength(text));
    }

    @Test
    public void testMessageLength_Failure() {
        String text = "a".repeat(255);
        assertEquals("Message exceeds 250 characters by 5; please reduce the size.",
                Message.checkMessageLength(text));
    }

    // Recipient cell number

    @Test
    public void testRecipientCell_Success() {
        assertEquals("Cell phone number successfully captured.",
                Message.checkRecipientCell(RECIPIENT_1));
    }

    @Test
    public void testRecipientCell_Failure() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                Message.checkRecipientCell(RECIPIENT_2));
    }

    // Message hash

    @Test
    public void testMessageHash_TestCase1() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    public void testMessageHash_RemainingMessagesInLoop() {
        String[] ids = {"0012345678", "1298765432", "5500000001"};
        int[] numbers = {0, 1, 2};
        String[] texts = {TEXT_1, TEXT_2, "Thanks"};
        String[] expected = {"00:0:HITONIGHT", "12:1:HIPAYMENT", "55:2:THANKSTHANKS"};

        for (int i = 0; i < ids.length; i++) {
            Message message = new Message(ids[i], numbers[i], RECIPIENT_1, texts[i]);
            assertEquals(expected[i], message.createMessageHash());
        }
    }

    // Message ID

    @Test
    public void testMessageID_IsCreated() {
        Message message = new Message(0, RECIPIENT_1, TEXT_1);
        assertEquals("Message ID generated: " + message.getMessageID(), message.getMessageIDStatus());
        assertEquals(10, message.getMessageID().length());
        assertTrue(message.getMessageID().matches("\\d{10}"));
        assertTrue(message.checkMessageID());
    }

    @Test
    public void testMessageID_TooLong() {
        Message message = new Message("12345678901", 0, RECIPIENT_1, TEXT_1);
        assertFalse(message.checkMessageID());
    }

    // Send, disregard, store

    @Test
    public void testSentMessage_Send() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully sent.", message.sentMessage(1));
    }

    @Test
    public void testSentMessage_Disregard() {
        Message message = new Message("1298765432", 1, RECIPIENT_2, TEXT_2);
        assertEquals("Press 0 to delete the message.", message.sentMessage(2));
    }

    @Test
    public void testSentMessage_Store() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully stored.", message.sentMessage(3));
    }

    // Totals and printing

    @Test
    public void testReturnTotalMessages_OneSentOneDiscarded() {
        Message first = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        Message second = new Message("1298765432", 1, RECIPIENT_2, TEXT_2);

        first.sentMessage(1);
        second.sentMessage(2);

        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages_ShowsSentMessages() {
        Message first = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        first.sentMessage(1);

        String expected = "Message ID: 0012345678\n"
                + "Message Hash: 00:0:HITONIGHT\n"
                + "Recipient: +27718693002\n"
                + "Message: Hi Mike, can you join us for dinner tonight?";

        assertEquals(expected, Message.printMessages());
    }

    @Test
    public void testPrintMessages_NothingSentYet() {
        assertEquals("No messages have been sent yet.", Message.printMessages());
    }

    // JSON storage

    @Test
    public void testStoreMessage_WritesJsonFile() throws IOException {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);

        assertTrue(message.storeMessage());

        String fileContent = Files.readString(tempDir.resolve("messages.json"));
        assertTrue(fileContent.contains("\"messageID\": \"0012345678\""));
        assertTrue(fileContent.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(fileContent.contains("\"recipient\": \"+27718693002\""));
        assertTrue(fileContent.contains("Hi Mike, can you join us for dinner tonight?"));
    }

    @Test
    public void testStoreMessage_EscapesQuotes() throws IOException {
        Message message = new Message("0012345678", 0, RECIPIENT_1, "Say \"hello\" now");

        message.storeMessage();

        String fileContent = Files.readString(tempDir.resolve("messages.json"));
        assertTrue(fileContent.contains("Say \\\"hello\\\" now"));
    }
}
