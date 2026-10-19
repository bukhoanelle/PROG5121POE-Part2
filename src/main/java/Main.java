package prog5121poe;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        Registration registration = new Registration();
        Login login = new Login(registration);

        // Registration section (Part 1)
        System.out.println(" Registration ");

        System.out.print("Enter your first name: ");
        String firstName = input.nextLine().trim();

        System.out.print("Enter your last name: ");
        String lastName = input.nextLine().trim();

        System.out.print("Enter a username: ");
        String username = input.nextLine().trim();

        System.out.print("Enter a password: ");
        String password = input.nextLine().trim();

        System.out.print("Enter your cell phone number (with country code, e.g. +27...): ");
        String cellPhone = input.nextLine().trim();

        String registrationMessage = registration.registerUser(firstName, lastName, username, password, cellPhone);
        System.out.println(registrationMessage);

        boolean registeredSuccessfully = registrationMessage.contains("Username successfully captured.")
                && registrationMessage.contains("Password successfully captured.")
                && registrationMessage.contains("Cell number successfully captured.");

        if (!registeredSuccessfully) {
            System.out.println("\nRegistration was not successful, so login cannot proceed.");
            input.close();
            return;
        }

        // Login section (Part 1) - the user gets three attempts
        boolean loggedIn = false;
        int attempts = 0;

        System.out.println("\n Login ");
        while (!loggedIn && attempts < 3) {
            System.out.print("Enter your username: ");
            String loginUsername = input.nextLine().trim();

            System.out.print("Enter your password: ");
            String loginPassword = input.nextLine().trim();

            loggedIn = login.loginUser(loginUsername, loginPassword);
            System.out.println(login.returnLoginStatus(loggedIn));
            attempts++;
        }

        // Part 2: messages can only be sent after a successful login
        if (!loggedIn) {
            System.out.println("\nToo many failed attempts. You must log in to use QuickChat.");
            input.close();
            return;
        }

        runQuickChat(input);
        input.close();
    }

    // The QuickChat menu keeps running until the user chooses Quit
    private static void runQuickChat(Scanner input) {
        System.out.println("\nWelcome to QuickChat.");

        int maxMessages = askForNumberOfMessages(input);
        int messagesEntered = 0;
        boolean running = true;

        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.print("Choose an option: ");

            int choice = readNumber(input);

            switch (choice) {
                case 1:
                    if (messagesEntered >= maxMessages) {
                        System.out.println("You have already entered the " + maxMessages
                                + " message(s) you chose at the start.");
                    } else {
                        // keep going until the set number of messages has been entered
                        while (messagesEntered < maxMessages) {
                            System.out.println("\nMessage " + (messagesEntered + 1) + " of " + maxMessages);
                            enterMessage(input, messagesEntered);
                            messagesEntered++;
                        }
                        System.out.println("\nTotal number of messages sent: " + Message.returnTotalMessages());
                    }
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    System.out.println("Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter 1, 2 or 3.");
            }
        }
    }

    // The user decides at the start how many messages they want to enter
    private static int askForNumberOfMessages(Scanner input) {
        int number = 0;
        while (number < 1) {
            System.out.print("How many messages do you want to enter? ");
            number = readNumber(input);
            if (number < 1) {
                System.out.println("Please enter a number greater than 0.");
            }
        }
        return number;
    }

    // Collects one message, then lets the user send, disregard or store it
    private static void enterMessage(Scanner input, int messageNumber) {
        // recipient: keep asking until the number is valid
        String recipient = "";
        boolean recipientValid = false;
        while (!recipientValid) {
            System.out.print("Enter the recipient's cell phone number (e.g. +27718693002): ");
            recipient = input.nextLine().trim();

            String recipientStatus = Message.checkRecipientCell(recipient);
            System.out.println(recipientStatus);
            recipientValid = recipientStatus.equals(Message.CELL_SUCCESS);
        }

        // message text: keep asking until it is not empty and not too long
        String text = "";
        boolean messageValid = false;
        while (!messageValid) {
            System.out.print("Enter your message (maximum 250 characters): ");
            text = input.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("The message cannot be empty.");
                continue;
            }

            String lengthStatus = Message.checkMessageLength(text);
            if (lengthStatus.equals(Message.LENGTH_SUCCESS)) {
                System.out.println(lengthStatus);
                messageValid = true;
            } else {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(lengthStatus);
            }
        }

        Message message = new Message(messageNumber, recipient, text);
        System.out.println(message.getMessageIDStatus());

        // send, disregard or store
        boolean finished = false;
        while (!finished) {
            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");

            int option = readNumber(input);

            if (option == 1) {
                System.out.println(message.sentMessage(1));
                System.out.println("\n" + message.getFullDetails());
                finished = true;
            } else if (option == 2) {
                System.out.println(message.sentMessage(2));
                int confirm = readNumber(input);
                if (confirm == 0) {
                    System.out.println("Message deleted.");
                    finished = true;
                } else {
                    System.out.println("Message was not deleted.");
                }
            } else if (option == 3) {
                System.out.println(message.sentMessage(3));
                finished = true;
            } else {
                System.out.println("Invalid option, please choose 1, 2 or 3.");
            }
        }
    }

    // Reads a whole number from the console; returns -1 if the user typed something else
    private static int readNumber(Scanner input) {
        String line = input.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
