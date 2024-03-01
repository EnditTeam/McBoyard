package eu.octanne.mcboyard.modules.morse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class MorseTranslator {
    private MorseTranslator() {
    }

    /**
     * Return the display name or the type of the item
     */
    public static String getItemName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "AIR";
        }
        ItemMeta itemmeta = item.getItemMeta();
        if (itemmeta.hasDisplayName()) {
            TextComponent text = (TextComponent) itemmeta.displayName().asComponent();
            return text.content();
        } else {
            return item.getType().name();
        }
    }

    /**
     * Create a book with the morse translation of the word
     */
    public static ItemStack translateWithBook(String word) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Résultat d'analyse");
        meta.setAuthor("McBoyard");
        meta.pages(translate(word, 14));
        book.setItemMeta(meta);

        return book;
    }

    /**
     * Translate a word into morse code
     */
    public static TextComponent[] translate(String word, int linesPerPage) {
        List<Component> pages = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        int numberOfLinesInCurrentPage = 0;
        int numberOfDash = 0;
        int numberOfDot = 0;

        builder.append(word).append("\n\n");
        numberOfLinesInCurrentPage += 2;

        for (char c : word.toCharArray()) {
            if (numberOfLinesInCurrentPage >= linesPerPage) {
                pages.add(Component.text(builder.toString()));
                builder = new StringBuilder();
                numberOfLinesInCurrentPage = 0;
            }

            if (c == ' ') {
                builder.append("\n");
                continue;
            }

            String morse = getMorseChar(c);
            builder.append(c).append(" : ").append(morse).append("\n");
            numberOfDash += morse.chars().filter(ch -> ch == '-').count();
            numberOfDot += morse.chars().filter(ch -> ch == '.').count();
            numberOfLinesInCurrentPage++;
        }

        if (numberOfLinesInCurrentPage >= linesPerPage - 1) {
            pages.add(Component.text(builder.toString()));
            builder = new StringBuilder();
        }
        builder.append("\n");

        // Add 0 before if needed (for a 2 char str)
        String numberOfDotStr = padLeft(String.valueOf(numberOfDot), 2, '0');
        String numberOfDashStr = padLeft(String.valueOf(numberOfDash), 2, '0');
        String numberOfDotDashStr = padLeft(String.valueOf(numberOfDot + numberOfDash), 2, '0');

        builder.append("Code : ").append(numberOfDotStr).append(numberOfDashStr).append(numberOfDotDashStr).append("\n");
        pages.add(Component.text(builder.toString()));

        return pages.toArray(new TextComponent[0]);
    }

    /**
     * Add characters to the left of a string until it reaches the desired length
     */
    private static String padLeft(String s, int n, char c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n - s.length(); i++) {
            sb.append(c);
        }
        sb.append(s);
        return sb.toString();
    }

    private static String[] morseTable = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.",
        "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};
    private static String[] morseTableNumbers = {"-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."};

    /**
     * Return the morse code of a character (non accentuated)
     */
    public static String getMorseChar(char c) {
        // Remove common accents
        switch (c) {
            case 'á':
            case 'à':
            case 'â':
            case 'ä':
                c = 'a';
                break;
            case 'é':
            case 'è':
            case 'ê':
            case 'ë':
                c = 'e';
                break;
            case 'í':
            case 'ì':
            case 'î':
            case 'ï':
                c = 'i';
                break;
            case 'ó':
            case 'ò':
            case 'ô':
            case 'ö':
                c = 'o';
                break;
            case 'ú':
            case 'ù':
            case 'û':
            case 'ü':
                c = 'u';
                break;
            case 'ý':
            case 'ỳ':
            case 'ŷ':
            case 'ÿ':
                c = 'y';
                break;
            case 'ñ':
                c = 'n';
                break;
            case 'ç':
                c = 'c';
                break;
        }

        if (c >= 'A' && c <= 'Z') {
            return morseTable[c - 'A'];
        } else if (c >= 'a' && c <= 'z') {
            return morseTable[c - 'a'];
        } else if (c >= '0' && c <= '9') {
            return morseTableNumbers[c - '0'];
        } else {
            return "?";
        }
    }
}
