package eu.octanne.mcboyard.modules.morse;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        BookBuilder builder = new BookBuilder();
        int numberOfDash = 0;
        int numberOfDot = 0;

        builder.append(word).endOfLine().endOfLine();

        for (char c : word.toCharArray()) {
            if (c == ' ') {
                builder.endOfLine();
                continue;
            }

            String morse = getMorseChar(c);
            builder.append(c).append(" : ").append(morse).endOfLine();
            numberOfDash += morse.chars().filter(ch -> ch == '-').count();
            numberOfDot += morse.chars().filter(ch -> ch == '.').count();
        }

        builder.endOfLine();

        builder.append("Nombre de points : ").append(String.valueOf(numberOfDot)).endOfLine();
        builder.append("Nombre de tirets : ").append(String.valueOf(numberOfDash)).endOfLine();

        return builder.toBook("Résultat d'analyse", "McBoyard");
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
