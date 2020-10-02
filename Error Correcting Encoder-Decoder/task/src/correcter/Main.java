package correcter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Main {

    final static Scanner scanner = new Scanner(System.in);
    final static Random random = new Random();

    public static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    //Content print functions
    public static void printTextView(String text) {
        System.out.println("text view: " + text);
    }

    public static void printHexView(char[] bytes){
        System.out.print("hex view: ");
        for (char aByte : bytes) {
            System.out.printf("%02X ", (int) aByte);
        }
        System.out.println();
    }

    public static void printBinaryView(char[] bytes) {
        System.out.print("binary view: ");
        for (char aByte : bytes) {
            String binary = Integer.toBinaryString(aByte);
            System.out.print("0".repeat(8 - binary.length()) + binary + " ");
        }
        System.out.println();
    }

    public static void printContentsSend(String text) {
        char[] bytes = text.toCharArray();
        System.out.println("send.txt");
        printTextView(text);
        printHexView(bytes);
        printBinaryView(bytes);
        System.out.println();
    }

    public static void printContentsEncoded(String text, String binaryForm) {
        char[] bytes = text.toCharArray();
        System.out.println("encoded.txt: ");

        System.out.print("expand: ");
        for (String binary : expand(binaryForm)) {
            System.out.print(binary + " ");
        }
        System.out.println();

        System.out.print("parity: ");
        for (char aByte : bytes) {
            String binary = Integer.toBinaryString(aByte);
            System.out.print("0".repeat(8 - binary.length()) + binary + " ");
        }
        System.out.println();

        printHexView(bytes);
        System.out.println();
    }


    //Encoding functions Mode: ENCODING
    public static String convertToByte(String text) {
        StringBuilder joined = new StringBuilder();
        for (char aByte : text.toCharArray()) {
            String converted = Integer.toBinaryString(aByte);
            joined.append("0".repeat(8 - converted.length())).append(converted);
        }
        return joined.toString();
    }


    public static String[] expand(String binaryForm) {
        StringBuilder bytes = new StringBuilder();
        String[] packFourBits = binaryForm.split("(?<=\\G.{4})");
        for (String packFourBit : packFourBits) {
            bytes.append("..").append(packFourBit.charAt(0)).append(".");
            for (int i = 1; i < 4; i++) {
                bytes.append(packFourBit.charAt(i));
            }
            bytes.append(".");
        }
        return bytes.toString().split("(?<=\\G.{8})");
    }

    public static String[] hammingCode(String[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            StringBuilder aByte = new StringBuilder(bytes[i]);
            char[] bits = bytes[i].toCharArray();
            char first = (char) (bits[2] ^ bits[4] ^ bits[6]);
            char second = (char) (bits[2] ^ bits[5] ^ bits[6]);
            char third = (char) (bits[4] ^ bits[5] ^ bits[6]);
            aByte.setCharAt(0, first);
            aByte.setCharAt(1, second);
            aByte.setCharAt(3, third);
            aByte.setCharAt(7, '0');
            bytes[i] = aByte.toString();
        }
        return bytes;
    }

    public static char[] getBytes(String[] arrBite) {
        char[] ch = new char[arrBite.length];
        for (int i = 0; i < arrBite.length; i++) {
            ch[i] = (char) Integer.parseInt(arrBite[i], 2);
        }
        return ch;
    }

    public static String encodeText(String text) {
        printContentsSend(text);
        String appendedBinaries = convertToByte(text);
        String[] encodedBinaries = hammingCode(expand(appendedBinaries));
        char[] ch = getBytes(encodedBinaries);
        String encoded = new String(ch);
        printContentsEncoded(encoded, appendedBinaries);
        return encoded;
    }


    //Send functions Mode: SEND
    public static void printContentsReceived(String text) {
        char[] bytes = text.toCharArray();
        System.out.println("received.txt:");
        printBinaryView(bytes);
        printHexView(bytes);
        System.out.println();
    }

    public static char[] distortText(String text) {
        char[] bytes = text.toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= 1 << random.nextInt(7);
        }
        return bytes;
    }

    public static String sendText(String text) {
        char[] distortedTextBits = distortText(text);
        String distortedText = new String(distortedTextBits);
        printContentsReceived(distortedText);
        return distortedText;
    }


    //Decode functions Mode: DECODE
    public static void printContentsDecode(String text) {
        char[] bytes = text.toCharArray();
        System.out.println("decoded.txt:");
        printBinaryView(bytes);
        printHexView(bytes);
        printTextView(text);
    }

    public static String[] hammingEncode(String[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            StringBuilder aByte = new StringBuilder(bytes[i]);
            char[] bits = bytes[i].toCharArray();
            int first = bits[0] == (bits[2] ^ bits[4] ^ bits[6]) ? 0 : 1;
            int second = bits[1] == (bits[2] ^ bits[5] ^ bits[6]) ? 0 : 2;
            int third = bits[3] == (bits[4] ^ bits[5] ^ bits[6]) ? 0 : 4;
            int index = first + second + third;
            if (index != 0) {
                char c = bits[index - 1];
                aByte.setCharAt(index - 1, c == '1' ? '0' : '1');
            }
            bytes[i] = aByte.toString();
        }
        return bytes;
    }

    public static String[] extract(String[] bytes) {
        StringBuilder bits = new StringBuilder();
        for (String aByte : bytes) {
            bits.append(aByte.charAt(2));
            for (int j = 4; j < 7; j++) {
                bits.append(aByte.charAt(j));
            }
        }
        return bits.toString().split("(?<=\\G.{8})");
    }

    public static String decodeText(String text) {
        String[] bytes = convertToByte(text).split("(?<=\\G.{8})");
        String[] corrected = extract(hammingEncode(bytes));
        char[] ch = getBytes(corrected);
        String decoded = new String(ch);
        printContentsDecode(decoded);
        return decoded;
    }

    // Option functions
    public static void encode() {
        try (OutputStream outputStream = new FileOutputStream("encoded.txt", false)) {
            String text = readFileAsString("./send.txt");
            if ("".equals(text)) {
                outputStream.write("".getBytes());
                return;
            }
            String encodedText = encodeText(text);
            for (char c : encodedText.toCharArray()) {
                outputStream.write(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send() {
        try (OutputStream outputStream = new FileOutputStream("received.txt", false)) {
            FileInputStream inputStream = new FileInputStream("encoded.txt");
            StringBuilder text = new StringBuilder();
            int charAsNumber = inputStream.read();
            while (charAsNumber != -1) {
                text.append((char) charAsNumber);
                charAsNumber = inputStream.read();
            }
            inputStream.close();
            String sent = sendText(text.toString());
            for (char c : sent.toCharArray()) {
                outputStream.write(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decode() {
        try (FileWriter writer = new FileWriter("decoded.txt", false)) {
            FileInputStream inputStream = new FileInputStream("received.txt");
            StringBuilder text = new StringBuilder();
            int charAsNumber = inputStream.read();
            while (charAsNumber != -1) {
                text.append((char) charAsNumber);
                charAsNumber = inputStream.read();
            }
            inputStream.close();
            String decoded = decodeText(text.toString());
            writer.write(decoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.print("Write a mode: ");
        String mode = scanner.nextLine();
        switch (mode) {
            case "encode": {
                encode();
                break;
            }
            case "send": {
                send();
                break;
            }
            case "decode": {
                decode();
                break;
            }
            default: {
                System.out.println("There is not such option: " + mode);
                break;
            }
        }
    }
}

